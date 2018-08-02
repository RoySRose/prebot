package prebot.micro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.micro.predictor.WraithFightPredictor;
import prebot.micro.targeting.TargetScoreCalculator;
import prebot.micro.targeting.WraithTargetCalculator;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;
import prebot.strategy.constant.StrategyConfig;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceManager.StrikeLevel;
import prebot.strategy.manage.AirForceTeam;

public class DecisionMaker {
	
	private static final int BACKOFF_DIST = 64;
	private static final int TOO_TOO_FAR_DISTANCE = 450;
	
	private TargetScoreCalculator targetScoreCalculator;

	public DecisionMaker(TargetScoreCalculator targetScoreCalculator) {
		this.targetScoreCalculator = targetScoreCalculator;
	}

	public Decision makeDecisionForSiegeMode(Unit myUnit, Collection<UnitInfo> euiList) {
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
				decision = Decision.stop(myUnit);
			} else {
				decision = Decision.attackUnit(myUnit, bestTargetUnitInfo);
			}
		} else {
			if (closeUndetectedEnemy != null || tooCloseTarget != null) {
				decision = Decision.change(myUnit);
			} else if (tooFarTarget != null) {
				if (closestTooFarTargetDistance > TOO_TOO_FAR_DISTANCE) {
					decision = Decision.change(myUnit);
				} else {
					decision = Decision.hold(myUnit);
				}
			} else {
				decision = Decision.attackPosition(myUnit);
			}
		}
		return decision;
	}

	public Decision makeDecisionForAirForce(AirForceTeam airForceTeam, Collection<UnitInfo> euiList, int strikeLevel) {
		int wraithMemorySeconds = 3;
		if (strikeLevel < StrikeLevel.SORE_SPOT) {
			wraithMemorySeconds = StrategyConfig.IGNORE_ENEMY_UNITINFO_SECONDS;
		}
		
		List<UnitInfo> euiListAirDefenseBuilding = new ArrayList<>();
		List<UnitInfo> euiListAirWeapon = new ArrayList<>();
		List<UnitInfo> euiListFeed = new ArrayList<>();
		List<UnitInfo> euiListDetector = new ArrayList<>();
		for (UnitInfo eui : euiList) {
			if (UnitUtils.ignorableEnemyUnitInfo(eui, wraithMemorySeconds)) { // 레이쓰는 기억력이 안좋다.
				continue;
			}

			boolean isAirDefenseBuilding = false;
			for (UnitType airDefenseUnitType : UnitUtils.enemyAirDefenseUnitType()) {
				if (eui.getType() == airDefenseUnitType) {
					isAirDefenseBuilding = true;
					break;
				}
			}
			
			if (eui.isCompleted() || eui.getLastHealth() >= eui.getType().maxHitPoints() * 0.8) {
				if (isAirDefenseBuilding) {
					euiListAirDefenseBuilding.add(eui);
				} else if (MicroUtils.airEnemyType(eui.getType())) {
					euiListAirWeapon.add(eui);
				} else {
					euiListFeed.add(eui);
				}
			} else {
				euiListFeed.add(eui);
			}
			
			if (eui.getType().isDetector()) {
				if (eui.getLastPosition().getDistance(airForceTeam.leaderUnit.getPosition()) > 500) {
					continue;
				}
				euiListDetector.add(eui);
			}
		}

		// air driving 오차로 상대 건물 공격범위안으로 들어왔을 경우
		for (UnitInfo eui : euiListAirDefenseBuilding) {
			Unit enemyUnit = UnitUtils.unitInSight(eui);
			if (enemyUnit != null) {
				// 벙커 별도 처리
				if (enemyUnit.getType() == UnitType.Terran_Bunker) {
					int range = Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 64;// + AirForceManager.AIR_FORCE_SAFE_DISTANCE;
					if (enemyUnit.getDistance(airForceTeam.leaderUnit) < range) {
						return Decision.fleeFromUnit(airForceTeam.leaderUnit, eui);
					}
				} else {
					if (enemyUnit.isInWeaponRange(airForceTeam.leaderUnit)) {
						return Decision.fleeFromUnit(airForceTeam.leaderUnit, eui);
					}
				}
			}
		}
		
		if (airForceTeam.repairCenter != null) {
			return Decision.attackPosition(airForceTeam.leaderUnit);
		}

		UnitInfo bestTargetInfo = null;

		if (!euiListAirWeapon.isEmpty()) {
			if (AirForceManager.Instance().isAirForceDefenseMode()) {
				if (airForceTeam.cloakable()) {
					return Decision.change(airForceTeam.leaderUnit);
				}
				bestTargetInfo = getBestTargetInfo(airForceTeam, euiListAirWeapon, euiListAirDefenseBuilding);
				
			} else {
				boolean detectUnitExist = !euiListDetector.isEmpty() || airForceTeam.damagedEffectiveFrame > 10; // 10 hitpoints reduced
				boolean cloakingBonus = airForceTeam.cloakingMode && !detectUnitExist;
				boolean mainSquadBonus = airForceTeam.leaderUnit.getDistance(StrategyIdea.mainSquadCenter) < 150;
//				System.out.println("cloakingBonus : " + cloakingBonus);
				
				SmallFightPredict fightPredict = WraithFightPredictor.airForcePredictByUnitInfo(airForceTeam.memberList, euiListAirWeapon, cloakingBonus, mainSquadBonus);
//				System.out.println("fightPredict = " + fightPredict);
				
				if (fightPredict == SmallFightPredict.ATTACK) {
					bestTargetInfo = getBestTargetInfo(airForceTeam, euiListAirWeapon, euiListAirDefenseBuilding);

				} else {
					if (euiListDetector.isEmpty() && !cloakingBonus && airForceTeam.cloakable()) {
						SmallFightPredict cloakingFightPredict = WraithFightPredictor.airForcePredictByUnitInfo(airForceTeam.memberList, euiListAirWeapon, true, mainSquadBonus);
						if (cloakingFightPredict == SmallFightPredict.ATTACK) {
							return Decision.change(airForceTeam.leaderUnit);
						}
					}
					
					for (UnitInfo eui : euiListAirWeapon) {
						Unit unitInSight = UnitUtils.unitInSight(eui);
						if (unitInSight != null) {
							if (unitInSight.isInWeaponRange(airForceTeam.leaderUnit)) {
								return Decision.fleeFromUnit(airForceTeam.leaderUnit, eui);
							} else {
								// isInWeaponRange는 제외해도 괜찮다.
								int enemyUnitDistance = airForceTeam.leaderUnit.getDistance(unitInSight);
								int weaponMaxRange = Prebot.Broodwar.enemy().weaponMaxRange(unitInSight.getType().airWeapon()) + 30;
								if (enemyUnitDistance < weaponMaxRange) {
									return Decision.fleeFromUnit(airForceTeam.leaderUnit, eui);
								}
							}
						}
					}
					bestTargetInfo = getBestTargetInfo(airForceTeam, euiListFeed, euiListAirDefenseBuilding, StrikeLevel.SORE_SPOT);
				}
			}
			
		} else {
			if (AirForceManager.Instance().isAirForceDefenseMode()) {
				if (airForceTeam.uncloakable()) {
					return Decision.change(airForceTeam.leaderUnit);
				}
			}
			bestTargetInfo = getBestTargetInfo(airForceTeam, euiListFeed, euiListAirDefenseBuilding);
		}

		if (bestTargetInfo != null) {
			boolean bestTargetProtectedByBuilding = protectedByBuilding(bestTargetInfo, euiListAirDefenseBuilding);
			if (bestTargetProtectedByBuilding) {
				return Decision.attackUnit(airForceTeam.leaderUnit, bestTargetInfo);
			} else {
				return Decision.kitingUnit(airForceTeam.leaderUnit, bestTargetInfo);
			}
		} else {
			return Decision.attackPosition(airForceTeam.leaderUnit);
		}
	}
	
	private UnitInfo getBestTargetInfo(AirForceTeam airForceTeam, List<UnitInfo> euiListTarget, List<UnitInfo> euiListAirDefenseBuilding) {
		return getBestTargetInfo(airForceTeam, euiListTarget, euiListAirDefenseBuilding, AirForceManager.Instance().getStrikeLevel());
	}
	
	private UnitInfo getBestTargetInfo(AirForceTeam airForceTeam, List<UnitInfo> euiListTarget, List<UnitInfo> euiListAirDefenseBuilding, int strikeLevel) {
		UnitInfo bestTargetInfo = null;
		int highestFeedScore = 0;

		boolean inWeaponRange = false;
		boolean protectedByBuilding = false;
		for (UnitInfo eui : euiListTarget) {
			Unit enemyInSight = UnitUtils.unitInSight(eui);
			if (enemyInSight != null) {
				if (!enemyInSight.isDetected()) {
					continue;
				}
				inWeaponRange = airForceTeam.isInAirForceWeaponRange(enemyInSight);
			}
			
			protectedByBuilding = protectedByBuilding(eui, euiListAirDefenseBuilding);
			if (!inWeaponRange && protectedByBuilding) { // 건물에 의해 보호받는 빌딩은 제외. 공격범위내에 있으면 예외
				continue;
			}

			WraithTargetCalculator wraithTargetCalculator = (WraithTargetCalculator) targetScoreCalculator;
			wraithTargetCalculator.setStrikeLevel(strikeLevel);
			int score = wraithTargetCalculator.calculate(airForceTeam.leaderUnit, eui);
			if (score > highestFeedScore) {
				bestTargetInfo = eui;
				highestFeedScore = score;
			}
		}
		return bestTargetInfo;
	}

	private boolean protectedByBuilding(UnitInfo eui, List<UnitInfo> euiListAirDefenseBuilding) {
		for (UnitInfo euiBuilding : euiListAirDefenseBuilding) {
			int buildingWeaponRange = euiBuilding.getType().airWeapon().maxRange();// + AirForceManager.AIR_FORCE_SAFE_DISTANCE;
			double distanceWithBuilding = eui.getLastPosition().getDistance(euiBuilding.getLastPosition());
			if (distanceWithBuilding < buildingWeaponRange) {
				return true;
			}
		}
		return false;
	}
	
	public Decision makeDecisionForAirForceMovingDetail(AirForceTeam airForceTeam, Collection<UnitInfo> euiList, boolean movingAttack) {
		boolean allUnitCoolTimeReady = true;
		for (Unit wraith : airForceTeam.memberList) {
			if (wraith.getGroundWeaponCooldown() > 0) {
				allUnitCoolTimeReady = false;
				break;
			}
		}
		
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

					// 공격범위 내에 있어야 한다.
					if (!airForceTeam.isInAirForceWeaponRange(enemyUnit)) {
						continue;
					}
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
				return Decision.attackUnit(airForceTeam.leaderUnit, targetInfo); // 유닛 공격
			}
		}
		
		// 공격할 적이 없을 경우 이동한다.
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
			return Decision.unite(airForceTeam.leaderUnit); // 뭉치기
		} else {
			if (allUnitCoolTimeReady) {
				return Decision.attackPosition(airForceTeam.leaderUnit); // 이동
			} else {
				return Decision.kitingUnit(airForceTeam.leaderUnit, null); // 카이팅 eui 정보는 decision에 저장된게 있다.
			}
		}
	}

	public Decision makeDecision(Unit myUnit, Collection<UnitInfo> euiList) {
		return makeDecision(myUnit, euiList, false);
	}
	
	public Decision makeDecision(Unit myUnit, Collection<UnitInfo> euiList, boolean overwhelm) {
		UnitInfo bestTargetUnitInfo = null;
		int highestScore = 0;
		for (UnitInfo eui : euiList) {
			if (!MicroUtils.canAttack(myUnit, eui)) {
				continue;
			}
			if (!overwhelm && isCloseDangerousTarget(myUnit, eui)) {
				return Decision.fleeFromUnit(myUnit, eui);
			}
			int score = targetScoreCalculator.calculate(myUnit, eui);
			if (score > highestScore) {
				bestTargetUnitInfo = eui;
				highestScore = score;
			}
		}

		Decision decision;
		if (bestTargetUnitInfo != null) {
			decision = Decision.kitingUnit(myUnit, bestTargetUnitInfo);
		} else {
			decision = Decision.attackPosition(myUnit);
		}
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
			enemyWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 96;
		} else {
			if (myUnit.isFlying()) {
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
