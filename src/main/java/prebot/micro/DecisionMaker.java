package prebot.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.common.constant.CommonCode;
import prebot.common.debug.UXManager;
import prebot.common.main.Prebot;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.micro.predictor.WraithFightPredictor;
import prebot.micro.targeting.TargetScoreCalculator;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceTeam;

public class DecisionMaker {
	
	private static final int BACKOFF_DIST = 64;
	private static final int TOO_TOO_FAR_DISTANCE = 450;
	
	private TargetScoreCalculator targetScoreCalculator;

	public DecisionMaker(TargetScoreCalculator targetScoreCalculator) {
		this.targetScoreCalculator = targetScoreCalculator;
	}

	public Decision makeDecisionForSiegeMode(Unit myUnit, List<UnitInfo> euiList) {
		UnitInfo bestTargetUnitInfo = null;
		int highestScore = 0;
		
		Unit closeUndetectedEnemy = null;
		Unit tooCloseTarget = null;
		Unit tooFarTarget = null;
		int closestTooFarTargetDistance = CommonCode.INT_MAX;
		boolean targetInRangeButOutOfSight = false;
		
		for (UnitInfo eui : euiList) {
			if (!MicroUtils.canAttack(myUnit, eui)) {
				continue;
			}
			Unit enemyUnit = UnitUtils.unitInSight(eui);
			if (enemyUnit == null) {
				if (bestTargetUnitInfo == null) {
					int distanceToTarget = myUnit.getDistance(eui.getLastPosition());
					if (distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE) {
						bestTargetUnitInfo = eui;
						targetInRangeButOutOfSight = true;
					}
				}
				continue;
			}
			if (isCloseUndetectedGroundUnit(myUnit, eui)) {
				closeUndetectedEnemy = enemyUnit;
				continue;
			}
			
			int distanceToTarget = myUnit.getDistance(enemyUnit.getPosition());
			if (!myUnit.isInWeaponRange(enemyUnit)) { // 시즈 범위안에 타겟이 없을 경우 skip
				if (distanceToTarget < Tank.SIEGE_MODE_MIN_RANGE) {
					tooCloseTarget = enemyUnit;
		        } else if (distanceToTarget > Tank.SIEGE_MODE_MAX_RANGE) {
		        	if (tooFarTarget == null || distanceToTarget < closestTooFarTargetDistance) {
			        	tooFarTarget = enemyUnit;
		        		closestTooFarTargetDistance = distanceToTarget;
		        	}
		        }
	        	continue;
			}
			
			int score = targetScoreCalculator.calculate(myUnit, eui);
			if (score > highestScore) {
				bestTargetUnitInfo = eui;
				highestScore = score;
				targetInRangeButOutOfSight = false;
			}
		}

		Decision decision;
		if (bestTargetUnitInfo != null) {
			if (targetInRangeButOutOfSight || highestScore <= 0) {
				decision = Decision.stop();
			} else {
				decision = Decision.attackUnit(bestTargetUnitInfo);
			}
		} else {
			if (closeUndetectedEnemy != null || tooCloseTarget != null) {
				decision = Decision.change();
			} else if (tooFarTarget != null) {
				if (closestTooFarTargetDistance > TOO_TOO_FAR_DISTANCE) {
					decision = Decision.change();
				} else {
					decision = Decision.hold();
				}
			} else {
				decision = Decision.attackPosition();
			}
		}
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}

	public Decision makeDecisionForAirForce(AirForceTeam airForceTeam, List<UnitInfo> euiList) {
		List<UnitInfo> euiListAirDefenseBuilding = new ArrayList<>();
		List<UnitInfo> euiListAirWeapon = new ArrayList<>();
		List<UnitInfo> euiListFeed = new ArrayList<>();
		for (UnitInfo eui : euiList) {
			if (UnitUtils.ignorableEnemyUnitInfo(eui, 3)) { // 레이쓰는 기억력이 안좋다.
				continue;
			}

			boolean isAirDefenseBuilding = false;
			for (UnitType airDefenseUnitType : UnitUtils.enemyAirDefenseUnitType()) {
				if (eui.getType() == airDefenseUnitType) {
					isAirDefenseBuilding = true;
					break;
				}
			}
			if (isAirDefenseBuilding) {
				euiListAirDefenseBuilding.add(eui);
			} else if (eui.getType().airWeapon() != WeaponType.None) {
				euiListAirWeapon.add(eui);
			} else {
				euiListFeed.add(eui);
			}
		}

		// air driving 오차로 상대 건물 공격범위안으로 들어왔을 경우
		for (UnitInfo eui : euiListAirDefenseBuilding) {
			Unit enemyUnit = UnitUtils.unitInSight(eui);
			if (enemyUnit != null) {
				if (enemyUnit.isInWeaponRange(airForceTeam.leaderUnit)) {
					return Decision.attackPosition();
				}

				// TODO 벙커 별도 처리가 필요한지 테스트
				// if (enemyUnit.getType() == UnitType.Terran_Bunker) {
				// int range = Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32 + AirForceManager.AIR_FORCE_SAFE_DISTANCE;
				// if (enemyUnit.getDistance(airForceTeam.leaderUnit) < range) {
				// System.out.println("##### avoid bunker");
				// return Decision.attackPosition();
				// }
				// }
			}
		}

		UnitInfo bestTargetInfo = null;

		if (!euiListAirWeapon.isEmpty()) {
			SmallFightPredict fightPredict = WraithFightPredictor.airForcePredictByUnitInfo(airForceTeam.memberList, euiListAirWeapon);
			if (fightPredict == SmallFightPredict.ATTACK) {
				bestTargetInfo = getBestTargetInfo(airForceTeam, euiListAirWeapon, euiListAirDefenseBuilding);

			} else if (fightPredict == SmallFightPredict.BACK) {
				for (UnitInfo eui : euiListAirWeapon) {
					Unit unitInSight = UnitUtils.unitInSight(eui);
					if (unitInSight != null) {
						if (unitInSight.isInWeaponRange(airForceTeam.leaderUnit)) {
							return Decision.fleeFromPosition();
						} else {
							// isInWeaponRange는 제외해도 괜찮다.
							int enemyUnitDistance = airForceTeam.leaderUnit.getDistance(unitInSight);
							int weaponMaxRange = Prebot.Broodwar.enemy().weaponMaxRange(unitInSight.getType().airWeapon()) + 30;
							if (enemyUnitDistance < weaponMaxRange) {
								return Decision.fleeFromPosition();
							}
						}
					}
				}
				bestTargetInfo = getBestTargetInfo(airForceTeam, euiListFeed, euiListAirDefenseBuilding);
			}

		} else {
			bestTargetInfo = getBestTargetInfo(airForceTeam, euiListFeed, euiListAirDefenseBuilding);
		}

		if (bestTargetInfo != null) {
			boolean bestTargetProtectedByBuilding = protectedByBuilding(bestTargetInfo, euiListAirDefenseBuilding);
			if (bestTargetProtectedByBuilding) {
				return Decision.attackUnit(bestTargetInfo);
			} else {
				return Decision.kitingUnit(bestTargetInfo);
			}
		} else {
			return Decision.attackPosition();
		}
	}
	
	private UnitInfo getBestTargetInfo(AirForceTeam airForceTeam, List<UnitInfo> euiListTarget, List<UnitInfo> euiListAirDefenseBuilding) {
		UnitInfo bestTargetInfo = null;
		int highestFeedScore = 0;

		boolean inWeaponRange = false;
		boolean protectedByBuilding = false;
		for (UnitInfo eui : euiListTarget) {
			inWeaponRange = UnitUtils.unitInSight(eui) != null && airForceTeam.isInAirForceWeaponRange(eui.getUnit());
			protectedByBuilding = protectedByBuilding(eui, euiListAirDefenseBuilding);
			if (!inWeaponRange && protectedByBuilding) { // 건물에 의해 보호받는 빌딩은 제외. 공격범위내에 있으면 예외
				continue;
			}

			int score = targetScoreCalculator.calculate(airForceTeam.leaderUnit, eui);
			if (score > highestFeedScore) {
				bestTargetInfo = eui;
				highestFeedScore = score;
			}
		}
		return bestTargetInfo;
	}

	private boolean protectedByBuilding(UnitInfo eui, List<UnitInfo> euiListAirDefenseBuilding) {
		for (UnitInfo euiBuilding : euiListAirDefenseBuilding) {
			int buildingWeaponRange = euiBuilding.getType().airWeapon().maxRange() + AirForceManager.AIR_FORCE_SAFE_DISTANCE;
			double distanceWithBuilding = eui.getLastPosition().getDistance(euiBuilding.getLastPosition());
			if (distanceWithBuilding < buildingWeaponRange) {
				return true;
			}
		}
		return false;
	}
	
	public Decision makeDecisionForAirForceMovingDetail(AirForceTeam airForceTeam, List<UnitInfo> euiList, boolean movingAttack) {
		boolean allUnitCoolTimeReady = true;
		for (Unit wraith : airForceTeam.memberList) {
			if (wraith.getGroundWeaponCooldown() > 0) {
				allUnitCoolTimeReady = false;
				break;
			}
		}
		
		Decision decision = null;
		if (allUnitCoolTimeReady) {
			UnitInfo targetInfo = null;
			int minimumLinearDistance = CommonCode.INT_MAX;
			for (UnitInfo eui : euiList) {
				if (eui.getType() == UnitType.Zerg_Larva || eui.getType() == UnitType.Zerg_Egg || eui.getType() == UnitType.Zerg_Lurker_Egg) {
					continue;
				}
				
				Unit enemyUnit = UnitUtils.unitInSight(eui);
				if (enemyUnit == null) {
					continue;
				}
				
//				// 대공능력이 있는 적이 있다면 이동시 공격을 하지 않는다.
//				// (만약 대공능력이 있는 적을 공격을 해야 한다면 이전 단계 판단에서 지정된다.)
//				if (eui.getType().airWeapon() != WeaponType.None) {
//					targetInfo = null;
//					break;
//				}
				
				if (movingAttack) { // 이동시 진행각도에 있는 적만 공격하고 싶으세요?
					// 약 45도 이상 벌어진 적은 이동하며 공격하지 않음 (3.14 = 90도, 0.78 = 45도)
					double leaderTargetRadian = MicroUtils.targetDirectionRadian(airForceTeam.leaderUnit.getPosition(), airForceTeam.getTargetPosition());
					double leaderEnemyRadian = MicroUtils.targetDirectionRadian(airForceTeam.leaderUnit.getPosition(), enemyUnit.getPosition());
					if (Math.abs(leaderTargetRadian - leaderEnemyRadian) > 0.78) {
//						System.out.println("wraith - targetposition : " + leaderTargetRadian);
//						System.out.println("wraith - " + eui.getType() + " : " + leaderEnemyRadian);
						continue;
					}
				}

				// 공격범위 내에 있어야 한다.
				if (!airForceTeam.isInAirForceWeaponRange(enemyUnit)) {
					continue;
				}
				// 최소 진행각도의 적을 타게팅하기 위함
				int distanceEnemyToLeader = enemyUnit.getDistance(airForceTeam.leaderUnit);
				int distanceEnemyToTargetPosition = enemyUnit.getDistance(airForceTeam.getTargetPosition());
				if (distanceEnemyToLeader + distanceEnemyToTargetPosition < minimumLinearDistance) {
					targetInfo = eui;
					minimumLinearDistance = distanceEnemyToLeader + distanceEnemyToTargetPosition;
				}
			}
			if (targetInfo != null) {
				decision  = Decision.attackUnit(targetInfo);
			}
		}
		
		// 공격할 적이 없을 경우 이동한다.
		if (decision == null) {
			// air force team이 너무 분산되어 있는 경우 모으도록 한다.
			int averageDistance = 0;
			int memberSize = airForceTeam.memberList.size();
			if (memberSize > 1) {
				int sumOfDistance = 0;
				for (Unit member : airForceTeam.memberList) {
					if (member.getID() != airForceTeam.leaderUnit.getID()) {
						sumOfDistance += member.getDistance(airForceTeam.leaderUnit);
					}
				}
				averageDistance = sumOfDistance / (memberSize - 1);
			}
			if (averageDistance > 5) {
				decision = Decision.unite();
			} else {
				if (allUnitCoolTimeReady) {
					decision = Decision.attackPosition();
				} else {
					decision = Decision.fleeFromPosition();
				}
			}
		}
		UXManager.Instance().addDecisionListForUx(airForceTeam.leaderUnit, decision);
		return decision;
	}

	public Decision makeDecision(Unit myUnit, List<UnitInfo> euiList) {
		UnitInfo bestTargetUnitInfo = null;
		int highestScore = 0;
		for (UnitInfo eui : euiList) {
			if (!MicroUtils.canAttack(myUnit, eui)) {
				continue;
			}
			if (isCloseDangerousTarget(myUnit, eui)) {
				return Decision.fleeFromUnit(eui);
			}
			int score = targetScoreCalculator.calculate(myUnit, eui);
			if (score > highestScore) {
				bestTargetUnitInfo = eui;
				highestScore = score;
			}
		}

		Decision decision;
		if (bestTargetUnitInfo != null) {
			decision = Decision.kitingUnit(bestTargetUnitInfo);
		} else {
			decision = Decision.attackPosition();
		}
		UXManager.Instance().addDecisionListForUx(myUnit, decision);
		return decision;
	}

	private boolean isCloseDangerousTarget(Unit myUnit, UnitInfo eui) {
		boolean enemyIsComplete = eui.isCompleted();
		Position enemyPosition = eui.getLastPosition();
		UnitType enemyUnitType = eui.getType();
		
		Unit enemyUnit = UnitUtils.unitInSight(eui);
		if (UnitUtils.isValidUnit(enemyUnit)) {
			enemyIsComplete = enemyUnit.isCompleted();
			enemyPosition = enemyUnit.getPosition();
			enemyUnitType = enemyUnit.getType();
		}

		// 접근하면 안되는 유닛타입인지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
		if (!enemyIsComplete || !isDangerousType(myUnit, enemyUnitType, enemyUnit)) {
			return false;
		}

		// 접근하면 안되는 거리인지 있는지 판단
		int distanceToNearEnemy = myUnit.getDistance(enemyPosition);
		int enemyWeaponRange = 0;

		if (enemyUnitType == UnitType.Terran_Bunker) {
			enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
		} else {
			if (!myUnit.isFlying()) {
				enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(enemyUnitType.airWeapon());
			} else {
				enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(enemyUnitType.groundWeapon());
			}
		}
		return distanceToNearEnemy <= enemyWeaponRange + BACKOFF_DIST;
	}

	// 접근하면 안되는 적이 있는지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
	private boolean isDangerousType(Unit myUnit, UnitType enemyUnitType, Unit enemyUnit) {
		if (myUnit.isFlying()) {
			UnitType[] enemyAirDefenseUnitType = UnitUtils.enemyAirDefenseUnitType();
			for (UnitType airDefenseUnitType : enemyAirDefenseUnitType) {
				if (enemyUnitType == airDefenseUnitType) {
					return true;
				}
			}
			return false;
			// || (marine, goliath, dragoon, archon, hydra..) {

		} else {
			return enemyUnitType == UnitType.Zerg_Sunken_Colony
					|| enemyUnitType == UnitType.Protoss_Photon_Cannon
					|| enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode
					|| enemyUnitType == UnitType.Terran_Bunker
					|| (enemyUnitType == UnitType.Zerg_Lurker && enemyUnit != null && enemyUnit.isBurrowed() && !enemyUnit.isDetected());
			// || (saveUnitLevel >= 2 && allRangeUnitType(PreBot.Broodwar.enemy(), enemyUnitType))) {
		}
	}
	
	private boolean isCloseUndetectedGroundUnit(Unit myUnit, UnitInfo eui) {
		if (myUnit.isFlying()) {
			return false;
		}

		Unit enemyUnit = UnitUtils.unitInSight(eui);
		if (enemyUnit == null || enemyUnit.isDetected()) {
			return false;
		}
		
		if (eui.getType() == UnitType.Protoss_Dark_Templar) {
			return myUnit.getDistance(enemyUnit) < 150;
		} else if (eui.getType() == UnitType.Zerg_Lurker) {
			return enemyUnit.isInWeaponRange(myUnit);
		} else {
			return false;
		}
	}
}
