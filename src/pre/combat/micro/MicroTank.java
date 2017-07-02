package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import pre.MapGrid;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

public class MicroTank extends MicroManager {

	private static final int SIEGE_MODE_MIN_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange();
	private static final int SIEGE_MODE_MAX_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
	private static final int TANK_MODE_RANGE = UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange();

	private static final int SIEGE_MODE_INNER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().innerSplashRadius();
	private static final int SIEGE_MODE_MEDIAN_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().medianSplashRadius();
	private static final int SIEGE_MODE_OUTER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().outerSplashRadius();
	
	private static final int SIEGE_LINK_DISTANCE = 150;

	@Override
	protected void executeMicro(List<Unit> targets) {
		List<Unit> tanks = getUnits();

		// figure out targets
		List<Unit> tankTargets = new ArrayList<>();
		for (Unit target : targets) {
			if (target.isVisible() && !target.isFlying() & !target.isStasised()) {
				tankTargets.add(target);
			}
		}

		boolean seigeResearched = InformationManager.Instance().selfPlayer.hasResearched(TechType.Tank_Siege_Mode);
	    
		for (Unit tank : tanks) {
//	        boolean tankNearChokepoint = false; 
//	        for (Chokepoint choke : BWTA.getChokepoints()) {
//	            if (choke.getCenter().getDistance(tank.getPosition()) < 64) {
//	                tankNearChokepoint = true;
//	                break;
//	            }
//	        }
			
			if (!tankTargets.isEmpty()) { // 적이 있을 경우 전투
				if (tank.isSieged()) { // 시즈모드인 경우
					Unit target = getTarget(tank, tankTargets, true);
					
					if (target == null) {
						// 1. 스플래시로 인한 아군피해가 예상될 경우, target이 null일 수 있다.
						
					} else if (tank.getDistance(target) < SIEGE_MODE_MIN_RANGE) {
						// 2. 범위내에 공격할 적이 없고, 질럿따위가 붙어있는 경우 시즈를 푼다.
						tank.unsiege();
						
					} else if (tank.getDistance(target) > SIEGE_MODE_MAX_RANGE) {
						// 3. 범위 밖의 적에 대해서 시즈를 푸는 것은 신중해야 한다.
						// 연결되어 포격할 수 있는 탱크가 포격할 수 없다면 시즈를 푼다.
						
						boolean targetIsFree = true; // target이 시즈포격에서 자유로운가
						Unit closestTank = tank;
						int closestTargetDistance = tank.getDistance(target); // 해당 시즈의 범위는 닿지 않는다. 그래서 체크하는거다.
						while (closestTank != null && targetIsFree) { // 연결된 시즈 중 타겟에 닿는 시즈가 있는지 체크한다.
							List<Unit> linkedTanks = MapGrid.Instance().getUnitsNear(closestTank.getPosition(), SIEGE_LINK_DISTANCE, true, false);
							closestTank = null;
							for (Unit linkedTank : linkedTanks) {
								if (linkedTank.getType() != UnitType.Terran_Siege_Tank_Siege_Mode) {
									continue;
								}
								
								int targetDistanceWithLinked = linkedTank.getDistance(target);
								if (targetDistanceWithLinked <= SIEGE_MODE_MAX_RANGE) { // 연결된 탱크가 포격할 수 있다면 target은 얻어맞고 시즈를 풀 필요없다.
									targetIsFree = false;
									break;
								} else if (targetDistanceWithLinked < closestTargetDistance) { // 연결된 탱크가 포격할 수 없지만 더 가깝다면 다시 또 연결된 탱크를 찾는다.
									closestTank = linkedTank;
									closestTargetDistance = targetDistanceWithLinked;
									break;
								}
							}
						}
						
						if (targetIsFree) {
							tank.unsiege();
						}
						
					} else {
						// 4. 공격할 타깃이 있는 경우 포격
						CommandUtil.attackUnit(tank, target);
					}
					
				} else { // 탱크모드인 경우
					List<Unit> targetsInTankRange = MapGrid.Instance().getUnitsNear(tank.getPosition(), TANK_MODE_RANGE, false, true);
					if (!targetsInTankRange.isEmpty() || !seigeResearched) { // 탱크모드로 때릴 적이 있다면 때림
						Unit target = getTarget(tank, tankTargets, false);
						MicroUtils.preciseKiting(tank, target, true, true, order.getPosition());
					} else { // 가까운 곳에 탱크모드로 때릴 적이 없으면 시즈모드로 때릴 적을 찾는다.
						List<Unit> targetsInSiegeRange = MapGrid.Instance().getUnitsNear(tank.getPosition(), SIEGE_MODE_MAX_RANGE, false, true);
						if (!targetsInSiegeRange.isEmpty()) { // 시즈모드로 때릴 적을 찾았다.
							Unit target = getTarget(tank, targetsInSiegeRange, true);
							// 포격할 수 있는 거리이면 시즈모드로 변경.
							// 밀리유닛이면 걍 퉁퉁포로 무빙샷(시즈모드로 타깃을 정하여 splash때문에 target이 null이 될 수 있다.)
							if (target != null && target.getType().groundWeapon().maxRange() > SIEGE_MODE_MIN_RANGE) {
								tank.siege();
							} else {
								target = getTarget(tank, targetsInSiegeRange, false);
								MicroUtils.preciseKiting(tank, target, true, true, order.getPosition());
							}
						} else { // 시즈모드로 때릴 적도 없다.
							boolean shouldSiege = false;
							List<Unit> linkedTanks = MapGrid.Instance().getUnitsNear(tank.getPosition(), SIEGE_LINK_DISTANCE, true, false);
							for (Unit linkedTank : linkedTanks) {
								if (linkedTank.getType() != UnitType.Terran_Siege_Tank_Siege_Mode) {
									continue;
								}
								
								List<Unit> targetsInLinkedSiegeRange = MapGrid.Instance().getUnitsNear(linkedTank.getPosition(), SIEGE_MODE_MAX_RANGE, false, true);
								if (!targetsInLinkedSiegeRange.isEmpty()) {
									shouldSiege = true;
									break;
								}
							}
							
							if (shouldSiege) {
								tank.siege();
							} else {
								Unit target = getTarget(tank, tankTargets, false);
								MicroUtils.preciseKiting(tank, target, true, true, order.getPosition());
							}
						}
					}
				}
				
			} else { // 적이 없을 경우 전진한다.
				// if we're not near the order position
				if (tank.getDistance(order.getPosition()) > 500) {
                    if (tank.canUnsiege()) {
                        tank.unsiege();
                    } else {
                    	CommandUtil.attackMove(tank, order.getPosition());
                    }
				} else {
					if (tank.canSiege()) {
						tank.siege();
					}
				}
			}
		}
	}
	
	// 시즈모드(isSieged)인 경우 공격범위 안에 적이 있으면 범위 내에서 적을 찾는다. splash로 인한 손해가 더 많을 경우 null이 리턴될 수 있다. 
	//                         공격범위 안에 적이 없으면 전체 적유닛(targets)에서 적을 찾는다.
	// 시즈모드가 아닌 경우 공격, 전체 전체 적유닛(targets)에서 적을 찾는다.
	private Unit getTarget(Unit tank, List<Unit> targets, boolean isSieged) {
		
		Unit bestTarget = null;
	    int bestTargetScore = -99999;
		
		List<Unit> newTargets = targets;
		if (isSieged) {
			List<Unit> targetsInSiegeRange = new ArrayList<>();
		    for (Unit target : targets) {
		    	int targetDistance = target.getDistance(tank);
		        if (targetDistance >= SIEGE_MODE_MIN_RANGE && targetDistance <= SIEGE_MODE_MAX_RANGE) {
		            targetsInSiegeRange.add(target);
		        }
		    }

		    if (!targetsInSiegeRange.isEmpty()) {
		    	newTargets = targetsInSiegeRange;
		    }
		}
		    
	    for (Unit target : newTargets) {
	        
	        int priorityScore = getAttackPriorityScore(tank, target); // 유닛별 우선순위
			int splashScore = 0; // 스플래시 점수
			int distanceScore = 0; // 거리 점수
			int hitPointScore = 0; // HP 점수

	        // 스플래시
			if (isSieged) {
				List<Unit> unitsInSplash = target.getUnitsInRadius(SIEGE_MODE_OUTER_SPLASH_RAD);
		        for (Unit unitInSplash : unitsInSplash) {
	        		int splashUnitDistance = target.getDistance(unitInSplash);
		        	int priorityInSpash = getAttackPriorityScore(tank, unitInSplash);
			        if (splashUnitDistance <= SIEGE_MODE_INNER_SPLASH_RAD) {
			        	priorityInSpash = (int) (priorityInSpash * 0.8);
			        } else if (splashUnitDistance <= SIEGE_MODE_MEDIAN_SPLASH_RAD) {
			        	priorityInSpash = (int) (priorityInSpash * 0.4);
			        } else if (splashUnitDistance <= SIEGE_MODE_OUTER_SPLASH_RAD) {
			        	priorityInSpash = (int) (priorityInSpash * 0.2);
			        } else {
			        	MyBotModule.Broodwar.sendText("tank splash enemy error");
			        }
			        
			        // 아군일 경우 우선순위를 뺀다. priority값이 마이너스(-)가 나올 수도 있다. 이때는 타겟으로 지정하지 않는다.
		        	if (unitInSplash.getPlayer() == InformationManager.Instance().enemyPlayer) {
		        		splashScore += priorityInSpash;
		        	} else if (unitInSplash.getPlayer() == InformationManager.Instance().selfPlayer) {
		        		splashScore -= priorityInSpash;
		        	}
		        }
		        
		        if (priorityScore + splashScore < 0) { // splash로 인한 아군피해가 심한경우
		        	continue;
		        }
		        
			} else {
		        int distance = tank.getDistance(target) - TANK_MODE_RANGE;
		        if (distance > 0) {
		        	distanceScore -= distance / 50;
		        }
			}
			
			// 한방에 죽는다면 HP 높을 수록 우선순위가 높다.
			// 한방에 안죽는다면 HP가 낮을 수록 우선순위가 높다.
			if (MicroUtils.killedByNShot(tank, target, 1)) {
				hitPointScore += target.getHitPoints() / 10;
	        } else {
	        	hitPointScore -= target.getHitPoints() / 10;
	        }
	        
			if (priorityScore + splashScore + distanceScore + hitPointScore > bestTargetScore) {
				bestTarget = target;
				bestTargetScore = priorityScore + splashScore + distanceScore + hitPointScore;
			}
	    }
	    
	    return bestTarget;
	}
	
	//참고표 : http://kin.naver.com/qna/detail.nhn?d1id=2&dirId=2020401&docId=40566552
	private int getAttackPriorityScore(Unit tank, Unit target) {
		UnitType targetType = target.getType();
		
		// 0순위 : 움직이는 스파이더 마인
		if (targetType == UnitType.Terran_Vulture_Spider_Mine) { // && target.isMoving()
			if (!target.isBurrowed()) {
				MyBotModule.Broodwar.sendText("Spider_Mine is unBurrowed");
			}
			
			if (!target.isDetected()) {
				MyBotModule.Broodwar.sendText("Spider_Mine is dectected");
			}
			return 15;
		}
		
		// 1순위 : 시즈탱크(시즈모드), 자폭병, 리버
		if (targetType == UnitType.Terran_Siege_Tank_Siege_Mode
				|| targetType == UnitType.Zerg_Infested_Terran
				|| targetType == UnitType.Protoss_Reaver) {
			return 14;
		}
		
		// 2순위 : 시즈탱크(탱크모드), 디파일러(중형), 하템(소형이지만 위험함)
		if (targetType == UnitType.Terran_Siege_Tank_Tank_Mode
				|| targetType == UnitType.Zerg_Defiler
				|| targetType == UnitType.Protoss_High_Templar) {
			return 13;
		}
		
		// 3순위 : 골리앗(대형), 울트라리스크, 닥템(소형이지만 위험함)
		if (targetType == UnitType.Terran_Goliath
				|| targetType == UnitType.Zerg_Ultralisk
				|| targetType == UnitType.Protoss_Dark_Templar) {
			return 12;
		}
		
		// 4순위 : 벌처, 럴커(중형), 드라군
		if (targetType == UnitType.Terran_Vulture
				|| targetType == UnitType.Zerg_Lurker
				|| targetType == UnitType.Protoss_Dragoon) {
			return 11;
		}
		
		// 5순위 : 고스트, 히드라, 아칸
		if (targetType == UnitType.Terran_Ghost
				|| targetType == UnitType.Zerg_Hydralisk
				|| targetType == UnitType.Protoss_Archon) {
			return 10;
		}

		// 6순위 : 마린, 메딕, 파이어뱃, 저글링, 질럿(소형)
		if (targetType == UnitType.Terran_Marine || targetType == UnitType.Terran_Medic || targetType == UnitType.Terran_Firebat
				|| targetType == UnitType.Zerg_Zergling
				|| targetType == UnitType.Protoss_Zealot) {
			return 9;
		}
		
		if (targetType == UnitType.Zerg_Lurker_Egg) {
			if (tank.isSieged()) { // 시즈모드로 알깨기
				return 8;
			} else {
				return 1;
			}
		}
		
		if (targetType == UnitType.Zerg_Larva || targetType == UnitType.Zerg_Egg) {
			if (tank.isSieged()) { // 시즈모드로 알깨기
				return 8;
			} else {
				return 1;
			}
		}
		
		// 나머지 쩌리들 : 일꾼, 다크아칸, 브루들링
		if (!targetType.isBuilding()) {
			if ((target.isConstructing() || target.isRepairing())) { // 수리중이거나 건설중인 SCV
	    		return 9;
	    	}
	  		return 7;
		}
		
		// 1순위 : 벙커, 나이더스커널, 포톤
		if (targetType == UnitType.Terran_Bunker
				|| targetType == UnitType.Zerg_Nydus_Canal
				|| targetType == UnitType.Protoss_Photon_Cannon) {
			return 7;
		}
		
		// 2순위 : 터렛, 성큰, 파일런
		if (targetType == UnitType.Terran_Missile_Turret
				|| targetType == UnitType.Zerg_Sunken_Colony
				|| targetType == UnitType.Protoss_Pylon) {
			return 6;
		}
		
		// 3순위 : 팩토리, 크립콜로니, 게이트
		if (targetType == UnitType.Terran_Factory
				|| targetType == UnitType.Zerg_Creep_Colony
				|| targetType == UnitType.Protoss_Gateway) {
			return 5;
		}
		
		// 4순위 : 서플, 스포어콜로니, 템플러아카이브
		if (targetType == UnitType.Terran_Factory
				|| targetType == UnitType.Zerg_Spore_Colony
				|| targetType == UnitType.Protoss_Templar_Archives) {
			return 4;
		}
		
		// 5순위 : 스타포트, 스파이어, 스타게이트
		if (targetType == UnitType.Terran_Factory
				|| targetType == UnitType.Zerg_Spire
				|| targetType == UnitType.Protoss_Stargate) {
			return 3;
		}
		
		// 6순위 : 아머리, 해처리, 넥서스
		if (targetType == UnitType.Terran_Factory
				|| targetType == UnitType.Zerg_Spawning_Pool
				|| targetType == UnitType.Protoss_Nexus) {
			return 2;
		}
		
		return 1;
	}

}
