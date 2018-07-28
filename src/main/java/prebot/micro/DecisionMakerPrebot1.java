package prebot.micro;

import java.util.Collection;
import java.util.List;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.Common;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.micro.targeting.TargetPriority;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class DecisionMakerPrebot1 {
	public static Decision makeDecisionPrebot1(Unit mechanicUnit, Collection<UnitInfo> enemiesInfo, Collection<UnitInfo> flyingEnemiesInfo, int saveUnitLevel) {

		UnitInfo bestTargetInfo = null;
		int bestTargetScore = -999999;
		// int dangerousSiegeTankCount = 0;

		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = UnitUtils.unitInSight(enemyInfo);

			// 접근하면 안되는 적이 있는지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
			double distanceToNearEnemy = mechanicUnit.getDistance(enemyInfo.getLastPosition());
			boolean enemyIsComplete = enemyInfo.isCompleted();
			Position enemyPosition = enemyInfo.getLastPosition();
			UnitType enemyUnitType = enemyInfo.getType();
			if (enemy != null) {
				if (!UnitUtils.isValidUnit(enemy)) {
					continue;
				}
				distanceToNearEnemy = mechanicUnit.getDistance(enemy);
				enemyIsComplete = enemy.isCompleted();
				enemyPosition = enemy.getPosition();
				enemyUnitType = enemy.getType();
			}

			if (enemyIsComplete && saveUnitLevel >= 1) {
				if (enemyUnitType == UnitType.Zerg_Sunken_Colony || enemyUnitType == UnitType.Protoss_Photon_Cannon || enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode
						|| enemyUnitType == UnitType.Terran_Bunker || (enemyUnitType == UnitType.Zerg_Lurker && enemy != null && enemy.isBurrowed() && !enemy.isDetected())
						|| (saveUnitLevel >= 2 && allRangeUnitType(Prebot.Broodwar.enemy(), enemyUnitType))) {

					int enemyGroundWeaponRange = enemyUnitType.groundWeapon().maxRange();
					if (enemyUnitType == UnitType.Terran_Bunker) {
						enemyGroundWeaponRange = Prebot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 64; // 32->64(엄청 뚜두려맞아서 올림)
					}
					double safeDistance = enemyGroundWeaponRange;
					if (enemyUnitType == UnitType.Terran_Siege_Tank_Tank_Mode || enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode) {
						if (saveUnitLevel <= 1 && mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode && mechanicUnit.canSiege()) {
							if (MicroUtils.exposedByEnemy(mechanicUnit, flyingEnemiesInfo)) { // 적에게 노출되는 포지션이면 안전거리를 잰다.
								safeDistance += (Common.BACKOFF_DIST_SIEGE_TANK * 2 / 3);
							} else { // 공격 사정거리를 재어 들어간다.
								safeDistance = Tank.SIEGE_MODE_SIGHT + 30; // 320 + 30
							}

						} else {
							safeDistance += Common.BACKOFF_DIST_SIEGE_TANK;
						}

					} else if (enemyUnitType == UnitType.Zerg_Sunken_Colony || enemyUnitType == UnitType.Protoss_Photon_Cannon || enemyUnitType == UnitType.Terran_Bunker) {
						// 탱크는 시즈각을 재야하기 때문에 후퇴하면 안됨
						if (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
							safeDistance += (Common.BACKOFF_DIST_DEF_TOWER * 2 / 3);
						} else {
							safeDistance += Common.BACKOFF_DIST_DEF_TOWER;
						}
					} else {
						if (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
							safeDistance += (Common.BACKOFF_DIST_RANGE_ENEMY * 2 / 3);
						} else {
							safeDistance += Common.BACKOFF_DIST_RANGE_ENEMY;
						}
					}

					if (distanceToNearEnemy < safeDistance) {
						return Decision.fleeFromUnit(mechanicUnit, enemyInfo);
					}
					// else if ((enemyUnitType == UnitType.Terran_Siege_Tank_Tank_Mode || enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode)
					// && (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode || mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode)
					// && distanceToNearEnemy <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 50) {
					// if (++dangerousSiegeTankCount >= 2) { // 시즈거리재기에서 자신을 때릴 수 있는 시즈가 2기 이상이 있으면 선빵을 치더라도 손해가 있을 수 있다.
					// return MechanicMicroDecision.makeDecisionToFlee(enemyPosition);
					// }
					// }
				}
			}

			// 우선순위 점수 : 유닛 우선순위 맵
			int priorityScore = TargetPriority.getPriority(mechanicUnit.getType(), enemyUnitType);
			int distanceScore = 0; // 거리 점수 : 최고점수 100점. 멀수록 점수 높음.
			int hitPointScore = 0; // HP 점수 : 최고점수 50점. HP가 많을 수록 점수 낮음.
			int specialScore = 0; // 특별 점수 : 탱크앞에 붙어있는 밀리유닛 +100점

			if (enemy != null) {
				if (mechanicUnit.isInWeaponRange(enemy)) {
					distanceScore = 100;
				}
				distanceScore -= mechanicUnit.getDistance(enemy.getPosition()) / 5;
				hitPointScore = 50 - enemy.getHitPoints() / 10;

				// 시즈에 붙어있는 밀리유닛 : +200점
				if (enemyUnitType.groundWeapon().maxRange() <= Tank.SIEGE_MODE_MIN_RANGE) {
					List<Unit> nearSiege = UnitUtils.getUnitsInRadius(PlayerRange.SELF, enemyPosition, Tank.SIEGE_MODE_MIN_RANGE, UnitType.Terran_Siege_Tank_Siege_Mode);
					if (!nearSiege.isEmpty()) {
						specialScore += 100;
					}
				}
				// 클로킹 유닛 : -1000점
				if (!enemy.isDetected() && enemyUnitType == UnitType.Protoss_Dark_Templar) {
					specialScore -= 1000;
				}
				if (enemyUnitType == UnitType.Protoss_Interceptor) {
					specialScore -= 1000;
				}

			} else {
				distanceScore -= mechanicUnit.getDistance(enemyInfo.getLastPosition()) / 5;
				hitPointScore = 50 - enemyInfo.getLastHealth() / 10;
				specialScore = -100;
				if (enemyInfo.getType().isCloakable()) {
					continue;
				}
			}
			int totalScore = priorityScore + distanceScore + hitPointScore + specialScore;
			if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTargetInfo = enemyInfo;
			}
		}

		if (bestTargetInfo == null) {
			return Decision.attackPosition(mechanicUnit);
		} else {
			return Decision.kitingUnit(mechanicUnit, bestTargetInfo);
		}
	}
	
	public static Decision makeDecisionForSiegeMode(Unit mechanicUnit, Collection<UnitInfo> enemiesInfo, Collection<Unit> tanks, int saveUnitLevel) {

		UnitInfo bestTargetInfo = null;
		int bestTargetScore = -999999;
		
		// siegeTank 특수옵션
		boolean existSplashLossTarget = false;
		boolean existTooCloseTarget = false;
		boolean existTooFarTarget = false;
		boolean existCloakTarget = false;
		Unit cloakTargetUnit = null;
		boolean targetOutOfSight = false;
		
		Unit closestTooFarTarget = null;
		int closestTooFarTargetDistance = 0;
		
		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = UnitUtils.unitInSight(enemyInfo);
			if (enemy == null) {

//				int noUnitFrame = MicroSet.Common.NO_UNIT_FRAME;
//				if (enemyInfo.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || enemyInfo.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
//					noUnitFrame = MicroSet.Common.NO_SIEGE_FRAME;
//				}
				
				if (bestTargetInfo == null && !Prebot.Broodwar.isVisible(enemyInfo.getLastPosition().toTilePosition())) {
					
					int distanceToTarget = mechanicUnit.getDistance(enemyInfo.getLastPosition());
					if (saveUnitLevel == 0 && distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE + 5) {
						bestTargetInfo = enemyInfo;
						targetOutOfSight = true;
					} else if (saveUnitLevel >= 1 && distanceToTarget <= (Tank.SIEGE_MODE_MAX_RANGE + (int) Common.BACKOFF_DIST_SIEGE_TANK)) {
						bestTargetInfo = enemyInfo;
						targetOutOfSight = true;
					} else {
//						System.out.println(distanceToTarget + ", " + (MicroSet.Tank.SIEGE_MODE_MAX_RANGE + (int) MicroSet.Common.BACKOFF_DIST_SIEGE_TANK));
					}
				}
				
				continue;
			}

			UnitType enemyUnitType = enemy.getType();
			
			// 우선순위 점수 : 유닛 우선순위 맵
			int priorityScore = TargetPriority.getPriority(mechanicUnit.getType(), enemyUnitType);
			int splashScore = 0; // 스플래시 점수 : 시즈모드 탱크만 해당 
			int distanceScore = 0; // 거리 점수 : 최고점수 100점. 멀수록 점수 높음.
			int hitPointScore = 0; // HP 점수 : 최고점수 50점. HP가 많을 수록 점수 낮음.
			int specialScore = 0; // 특별 점수 : 탱크앞에 붙어있는 밀리유닛 +100점
			
			List<Unit> unitsInSplash = enemy.getUnitsInRadius(Tank.SIEGE_MODE_OUTER_SPLASH_RAD);
	        for (Unit unitInSplash : unitsInSplash) {
        		int splashUnitDistance = enemy.getDistance(unitInSplash.getPosition());
	        	int priorityInSpash = TargetPriority.getPriority(mechanicUnit, unitInSplash);
		        if (splashUnitDistance <= Tank.SIEGE_MODE_INNER_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.8);
		        } else if (splashUnitDistance <= Tank.SIEGE_MODE_MEDIAN_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.4);
		        } else if (splashUnitDistance <= Tank.SIEGE_MODE_OUTER_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.2);
		        }
		        
		        // 아군일 경우 우선순위를 뺀다. priority값이 마이너스(-)가 나올 수도 있다. 이때는 타겟으로 지정하지 않는다.
	        	if (unitInSplash.getPlayer() == InformationManager.Instance().enemyPlayer) {
	        		splashScore += priorityInSpash;
	        	} else if (unitInSplash.getPlayer() == InformationManager.Instance().selfPlayer) {
	        		splashScore -= priorityInSpash;
	        	}
	        }
	        if (priorityScore + splashScore < 0) { // splash로 인해 아군피해가 더 심한 경우 skip
	        	existSplashLossTarget = true;
	        	continue;
	        }

	    	int distanceToTarget = mechanicUnit.getDistance(enemy.getPosition());
			if (!mechanicUnit.isInWeaponRange(enemy)) { // 시즈 범위안에 타겟이 없을 경우 skip
				if (distanceToTarget < Tank.SIEGE_MODE_MIN_RANGE) {
					existTooCloseTarget = true;
		        } else if (distanceToTarget > Tank.SIEGE_MODE_MAX_RANGE) {
		        	existTooFarTarget = true;
		        	if (closestTooFarTarget == null || distanceToTarget < closestTooFarTargetDistance) {
		        		closestTooFarTarget = enemy;
		        		closestTooFarTargetDistance = distanceToTarget;
		        	}
		        }
	        	continue;
			}
			
			distanceScore = 100 - distanceToTarget / 5;

			// 시즈모드 : 한방에 죽는다면 HP 높을 수록 우선순위가 높다.
			if (MicroUtils.killedByNShot(mechanicUnit, enemy, 1)) {
				hitPointScore = 50 + enemy.getHitPoints() / 10;
			} else {
				hitPointScore = 50 - enemy.getHitPoints() / 10;
			}
			
			// 클로킹 유닛 : -1000점
			if (!enemy.isDetected()) {
				existCloakTarget = true;
				cloakTargetUnit = enemy;
				continue;
			}
			
			int totalScore = priorityScore + splashScore + distanceScore + hitPointScore + specialScore;
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTargetInfo = enemyInfo;
				targetOutOfSight = false;
			}
		}
		
		if (bestTargetInfo == null) {
			if (existSplashLossTarget) {
				return Decision.stop(mechanicUnit);
			} else if (existTooCloseTarget) {
				return Decision.change(mechanicUnit);
			} else if (existTooFarTarget) {
				if (mechanicUnit.getDistance(StrategyIdea.mainPosition) < Tank.SIEGE_MODE_MAX_RANGE + 50) {
					return Decision.doNothing(mechanicUnit);
				}
				for (Unit tank : tanks) { // target이 가까운 동료 시즈포격에서 자유롭지 못하다면 상태유지
					int distanceToTarget = tank.getDistance(closestTooFarTarget);
					if (saveUnitLevel <= 1 && tank.isInWeaponRange(closestTooFarTarget) && tank.getDistance(mechanicUnit.getPosition()) < Tank.SIEGE_LINK_DISTANCE) {
						return Decision.doNothing(mechanicUnit);
					} else if (saveUnitLevel == 2 && distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE + Common.BACKOFF_DIST_SIEGE_TANK) {
						return Decision.doNothing(mechanicUnit);
					}
				}
				if (saveUnitLevel == 0 || InformationManager.Instance().enemyRace != Race.Terran) {
					return Decision.change(mechanicUnit);
				}
				return Decision.doNothing(mechanicUnit);
			} else if (existCloakTarget) {
				if (cloakTargetUnit.getType() == UnitType.Protoss_Dark_Templar) {
					return Decision.change(mechanicUnit);
				} else if (cloakTargetUnit.getType() == UnitType.Zerg_Lurker) {
					return Decision.doNothing(mechanicUnit);
				}
			}
			return Decision.attackPosition(mechanicUnit);
		} else {
			if (targetOutOfSight) { // 보이지는 않지만 일정거리 안에 적이 있다면 시즈모드 유지(saveUnitLevel에 따라 변동)
//				CommonUtils.consoleOut(1, mechanicUnit.getID(), "11111");
				return Decision.doNothing(mechanicUnit);
			} else {
				return Decision.attackUnit(mechanicUnit, bestTargetInfo);
			}
		}
		
		
	}

	public static boolean allRangeUnitType(Player player, UnitType unitType) { // 아칸 이상부터는 레인지 어택이라고 할까
		return player.weaponMaxRange(unitType.groundWeapon()) > UnitType.Protoss_Archon.groundWeapon().maxRange();
	}
}