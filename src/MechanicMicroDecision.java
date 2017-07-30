

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class MechanicMicroDecision {


	public static MechanicMicroDecision makeDecisionToDoNothing() {
		return new MechanicMicroDecision(-1, null);
	}
	public static MechanicMicroDecision makeDecisionToStop() {
		return new MechanicMicroDecision(0, null);
	}
	public static MechanicMicroDecision makeDecisionToFlee(UnitInfo targetInfo) {
		return new MechanicMicroDecision(0, targetInfo);
	}
	public static MechanicMicroDecision makeDecisionToKiting(UnitInfo targetInfo) {
		return new MechanicMicroDecision(1, targetInfo);
	}
	public static MechanicMicroDecision makeDecisionToGo() {
		return new MechanicMicroDecision(2, null);
	}
	public static MechanicMicroDecision makeDecisionToChange() {
		return new MechanicMicroDecision(3, null);
	}
	
	public int getDecision() {
		return decision;
	}
	
	// 벌처, 골리앗 -> 0: flee, 1: kiting, 2: go
	// 탱크모드 -> 0: flee, 1: kiting, 2: go, 3: change
	// 시즈모드 -> 0: stop, 1: kiting(attack unit), 2: go, 3:change
	private int decision; 
	private UnitInfo targetInfo;
	
	private MechanicMicroDecision(int decision, UnitInfo targetInfo) {
		this.decision = decision;
		this.targetInfo = targetInfo;
	}
	
	public UnitInfo getTargetInfo() {
		return targetInfo;
	}
	
	public static MechanicMicroDecision makeDecisionForSiegeMode(Unit mechanicUnit, List<UnitInfo> enemiesInfo, List<Unit> tanks, SquadOrder order) {

		UnitInfo bestTargetInfo = null;
		int bestTargetScore = -999999;
		
		// siegeTank 특수옵션
		boolean existSplashLossTarget = false;
		boolean existTooCloseTarget = false;
		boolean existTooFarTarget = false;
		boolean existCloakTarget = false;
		
		Unit closestTooFarTarget = null;
		int closestTooFarTargetDistance = 0;
		
		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);
			if (enemy == null || !CommandUtil.IsValidUnit(enemy)) { // 시야에 없는 경우 skip
				continue;
			}

			UnitType enemyUnitType = enemy.getType();
			
			// 우선순위 점수 : 유닛 우선순위 맵
			int priorityScore = TargetPriority.getPriority(mechanicUnit.getType(), enemyUnitType);
			int splashScore = 0; // 스플래시 점수 : 시즈모드 탱크만 해당 
			int distanceScore = 0; // 거리 점수 : 최고점수 100점. 멀수록 점수 높음.
			int hitPointScore = 0; // HP 점수 : 최고점수 50점. HP가 많을 수록 점수 낮음.
			int specialScore = 0; // 특별 점수 : 탱크앞에 붙어있는 밀리유닛 +100점
			
			List<Unit> unitsInSplash = enemy.getUnitsInRadius(MicroSet.Tank.SIEGE_MODE_OUTER_SPLASH_RAD);
	        for (Unit unitInSplash : unitsInSplash) {
        		int splashUnitDistance = enemy.getDistance(unitInSplash.getPosition());
	        	int priorityInSpash = TargetPriority.getPriority(mechanicUnit, unitInSplash);
		        if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_INNER_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.8);
		        } else if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_MEDIAN_SPLASH_RAD) {
		        	priorityInSpash = (int) (priorityInSpash * 0.4);
		        } else if (splashUnitDistance <= MicroSet.Tank.SIEGE_MODE_OUTER_SPLASH_RAD) {
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
				if (distanceToTarget < MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
					existTooCloseTarget = true;
		        } else if (distanceToTarget > MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
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
				continue;
			}
			
			int totalScore = priorityScore + splashScore + distanceScore + hitPointScore + specialScore;
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTargetInfo = enemyInfo;
			}
		}
		
		if (bestTargetInfo == null) {
			if (existSplashLossTarget) {
				return MechanicMicroDecision.makeDecisionToStop();
			} else if (existTooCloseTarget) {
				return MechanicMicroDecision.makeDecisionToChange();
			} else if (existTooFarTarget) {
				if (mechanicUnit.getDistance(order.getPosition()) < order.getRadius()) {
					return MechanicMicroDecision.makeDecisionToDoNothing();
				}
				for (Unit tank : tanks) { // target이 시즈포격에서 자유로운가
					if (tank.isInWeaponRange(closestTooFarTarget) && tank.getDistance(mechanicUnit.getPosition()) < MicroSet.Tank.SIEGE_LINK_DISTANCE) {
						return MechanicMicroDecision.makeDecisionToDoNothing();
					}
				}
				return MechanicMicroDecision.makeDecisionToChange();
			} else if (existCloakTarget) {
				return MechanicMicroDecision.makeDecisionToChange();
			}
			return MechanicMicroDecision.makeDecisionToGo();
		} else {
			return MechanicMicroDecision.makeDecisionToKiting(bestTargetInfo);
		}
		
		
	}
	public static MechanicMicroDecision makeDecision(Unit mechanicUnit, List<UnitInfo> enemiesInfo) {
		
		UnitInfo bestTargetInfo = null;
		int bestTargetScore = -999999;
		
		for (UnitInfo enemyInfo : enemiesInfo) {
			Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);

			// 접근하면 안되는 적이 있는지 판단 (성큰, 포톤캐논, 시즈탱크, 벙커)
			boolean enemyIsComplete = enemyInfo.isCompleted();
			Position enemyPosition = enemyInfo.getLastPosition();
			UnitType enemyUnitType = enemyInfo.getType();
			if (enemy != null) {
				if (!CommandUtil.IsValidUnit(enemy)) {
					continue;
				}
				enemyIsComplete = enemy.isCompleted();
				enemyPosition = enemy.getPosition();
				enemyUnitType = enemy.getType();
			}
			
			if (enemyIsComplete) {
				int enemyGroundWeaponRange = 0;
				if (enemyUnitType == UnitType.Zerg_Sunken_Colony
						|| enemyUnitType == UnitType.Protoss_Photon_Cannon
						|| enemyUnitType == UnitType.Terran_Siege_Tank_Siege_Mode
						|| enemyUnitType == UnitType.Terran_Bunker
						|| (enemyUnitType == UnitType.Zerg_Lurker && enemy != null && enemy.isBurrowed())) {
					
					enemyGroundWeaponRange = enemyUnitType.groundWeapon().maxRange();
					if (enemyUnitType == UnitType.Terran_Bunker) {
						enemyGroundWeaponRange = MyBotModule.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
					}
					
					double distanceToNearEnemy = mechanicUnit.getDistance(enemyPosition);
					double safeDistance = enemyGroundWeaponRange + (mechanicUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode ?
							MicroSet.Common.DEF_TOWER_BACKOFF_DIST_TANK : MicroSet.Common.DEF_TOWER_BACKOFF_DIST);
					if (distanceToNearEnemy < safeDistance) {
						return MechanicMicroDecision.makeDecisionToFlee(enemyInfo);
					}
				}
			}

			if (enemy == null && !enemyUnitType.isBuilding()
					&& MyBotModule.Broodwar.getFrameCount() - enemyInfo.getUpdateFrame() > MicroSet.Common.NO_UNIT_FRAME) {
				continue;
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
				if (enemyUnitType.groundWeapon().maxRange() <= MicroSet.Tank.SIEGE_MODE_MIN_RANGE) {
					List<Unit> nearSiege = MapGrid.Instance().getUnitsNear(enemyPosition, MicroSet.Tank.SIEGE_MODE_MIN_RANGE, true, false, UnitType.Terran_Siege_Tank_Siege_Mode);
					if (!nearSiege.isEmpty()) {
						specialScore += 100;
					}
				}
				// 클로킹 유닛 : -1000점
				if (!enemy.isDetected()) {
					specialScore -= 1000;
				}
		        
			} else {
				distanceScore = -100;
				hitPointScore = -100;
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
			return MechanicMicroDecision.makeDecisionToGo();
		} else {
			return MechanicMicroDecision.makeDecisionToKiting(bestTargetInfo);
		}
	}
	@Override
	public String toString() {
		return "Decision [decision=" + decision + ", targetInfo=" + targetInfo + "]";
	}
}
