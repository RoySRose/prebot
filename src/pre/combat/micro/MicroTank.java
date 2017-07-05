package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Chokepoint;
import pre.MapGrid;
import pre.combat.SquadOrder.SqaudOrderType;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.CommandUtil;
import pre.util.MicroSet;
import pre.util.MicroSet.FleeAngle;
import pre.util.MicroUtils;
import pre.util.TargetPriority;

public class MicroTank extends MicroManager {

	private static final int SIEGE_MODE_MIN_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange();
	private static final int SIEGE_MODE_MAX_RANGE = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange();
	private static final int TANK_MODE_RANGE = UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange();

	private static final int SIEGE_MODE_INNER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().innerSplashRadius();
	private static final int SIEGE_MODE_MEDIAN_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().medianSplashRadius();
	private static final int SIEGE_MODE_OUTER_SPLASH_RAD = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().outerSplashRadius();
	
	private static final int SIEGE_LINK_DISTANCE = 300;
	private static final int INITIATE_SIZE = 1;
	
	private int initiatedFrame = 0;

	@Override
	protected void executeMicro(List<Unit> targets) {
		List<Unit> tanks = getUnits();
		List<Unit> tankTargets = MicroUtils.filterTargets(targets, false);
		
		// 탱크는 전투의 중심이다. 명령의 상태를 BATTLE로 바꾸어 이니시에이팅한다.
		if (tankTargets.isEmpty()) {
			if (order.getType() == SqaudOrderType.BATTLE) {
				order.setType(SqaudOrderType.ATTACK);
				MyBotModule.Broodwar.sendText("battle finished!");
			}
			initiatedFrame = 0;
			for (Unit tank : tanks) { moveIt(tank); } // 적이 있을 없을 경우, orderPosition으로 이동
			return;
		}
		
		if (order.getType() == SqaudOrderType.ATTACK && initiatedFrame == 0 && tanks.size() >= INITIATE_SIZE) {
			MyBotModule.Broodwar.sendText("initiate!");
			order.setType(SqaudOrderType.BATTLE);
			order.setPosition(MicroUtils.centerOfUnits(tanks));
			initiatedFrame = MyBotModule.Broodwar.getFrameCount();
		}
		
		for (Unit tank : tanks) {
//	        boolean tankNearChokepoint = false; 
//	        for (Chokepoint choke : BWTA.getChokepoints()) {
//	            if (choke.getCenter().getDistance(tank.getPosition()) < 64) {
//	                tankNearChokepoint = true;
//	                break;
//	            }
//	        }
			
			
			// [시즈/언시즈 룰]
			// 시즈모드로 되어 있을 경우
			//   1. 유효범위내에 적이 있지만 스플래시 데미지가 더 크다고 판단 -> 언시즈 TODO 조금 기다려보자
			//   2. 유효범위내에 적이 없고, 질럿, 저글링 따위가 붙어있다 -> 언시즈
			//   3. 유효범위내에 적이 없고, 적이 최대범위보다 멀리 있다
			//      1) 목표지점에 자리를 잡았다 -> 상태유지
			//      1) 포격할 수 있는 연결된 시즈가 있다 -> 상태유지
			//      2) 포격할 수 있는 연결된 시즈가 없다 -> 공격당할 우리편이 없다 -> 상태유지
			//      3) 포격할 수 있는 연결된 시즈가 없다 -> 공격당할 우리편이 없다 -> 언시즈
			//   4. 유효범위내에 적이 있다 -> 포격
			// 탱크모드로 되어 있는 경우
			//   1. 이니시에이트 -> 시즈모드
			//   2. 탱크모드로 때릴 적이 있다 -> 카이팅 TODO 그래도 시즈모드를 해야될 때가 있다.
			//   3. 탱크모드로는 때릴 적이 없고, 시즈범위내에  적이 있다
			//      1) 밀리유닛이다 -> 카이팅
			//      2) 레인지유닛이다 -> 시즈모드
			//   4. 시즈모드 범위에도 때릴 적이 적이 없다.
			//      1) 포격할 수 있는 연결된 시즈가 있다 -> 시즈모드
			//      2) 포격할 수 있는 연결된 시즈가 없다 -> 카이팅 
			
			// ***** 시즈모드 *****
			if (tank.isSieged()) {
				Unit target = getTarget(tank, tankTargets, true);
				
				if (target == null) {
					// 1. 유효범위내에 적이 있지만 스플래시 데미지가 더 크다고 판단
					tank.stop();
					
				} else if (tank.getDistance(target) < SIEGE_MODE_MIN_RANGE) {
					// 2. 유효범위내에 적이 없고, 질럿, 저글링 따위가 붙어있다
					tank.unsiege();
					
				} else if (tank.getDistance(target) > SIEGE_MODE_MAX_RANGE) {
					// 3. 유효범위내에 적이 없고, 적이 최대범위보다 멀리 있다
					// (범위 밖의 적에 대해서 시즈를 푸는 것은 신중해야 한다.)
					
					// 자리를 잡았을 때, 시즈를 푸는 것은 위험하다. 
//					if (tank.getDistance(squadCenter) <= squadRange) {
//						return;
//					}
					
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
						List<Unit> ourUnits = MapGrid.Instance().getUnitsNear(target.getPosition(), target.getType().groundWeapon().maxRange(), true, false);
						if (!ourUnits.isEmpty()) {
							tank.unsiege();
						}
					}
					
				} else {
					// 4. 유효범위내에 적이 있다
					CommandUtil.attackUnit(tank, target);
				}
				
			} else { // ***** 탱크모드 *****
				List<Unit> targetsInTankRange = MapGrid.Instance().getUnitsNear(tank.getPosition(), TANK_MODE_RANGE, false, true);
				if (!MicroSet.Upgrade.hasResearched(TechType.Tank_Siege_Mode)) {
					// 0. 시즈 개발이 안됐으면 퉁퉁
					Unit target = null;
					if (targetsInTankRange.isEmpty()) {
						target = getTarget(tank, tankTargets, false);
					} else {
						target = getTarget(tank, targetsInTankRange, false);
					}
					MicroUtils.preciseKiting(tank, target, true, true, order.getPosition(), FleeAngle.NARROW_ANGLE);
					
				} else if (initiatedFrame == MyBotModule.Broodwar.getFrameCount() && tank.getDistance(squadCenter) <= squadRange) {
					// 1. 이니시에이트
					tank.siege();
					
				} else if (!targetsInTankRange.isEmpty()) {
					// 2. 탱크모드로 때릴 적이 있다
					Unit target = getTarget(tank, targetsInTankRange, false);
					MicroUtils.preciseKiting(tank, target, true, true, order.getPosition(), FleeAngle.NARROW_ANGLE);
					
				} else {
					List<Unit> targetsInSiegeRange = MapGrid.Instance().getUnitsNear(tank.getPosition(), SIEGE_MODE_MAX_RANGE, false, true);
					
					if (!targetsInSiegeRange.isEmpty()) {
						// 3. 탱크모드로는 때릴 적이 없고, 시즈범위내에  적이 있다
						
						Unit target = getTarget(tank, targetsInSiegeRange, true);
						// 포격할 수 있는 거리이면 시즈모드로 변경.
						// 밀리유닛이면 걍 퉁퉁포로 무빙샷(시즈모드로 타깃을 정하여 splash때문에 target이 null이 될 수 있다.)
						if (target == null || target.getType().groundWeapon().maxRange() <= SIEGE_MODE_MIN_RANGE) {
							target = getTarget(tank, targetsInSiegeRange, false);
							MicroUtils.preciseKiting(tank, target, true, true, order.getPosition(), FleeAngle.NARROW_ANGLE);
						} else {
							tank.siege();
						}
						
					} else {
						// 4. 시즈모드 범위에도 때릴 적이 적이 없다.
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
							MicroUtils.preciseKiting(tank, target, true, true, order.getPosition(), FleeAngle.NARROW_ANGLE);
						}
					}
				}
			}
		}
	}
	
	
	private void moveIt(Unit tank) {
		if (tank.getDistance(order.getPosition()) <= squadRange) {
			// 도착했다면 시즈모드
			if (tank.canSiege()) {
				Position positionToSiege = findPositionToSiege();
				if (positionToSiege != null) {
					if (tank.getDistance(positionToSiege) <= 50) {
						tank.siege();
					} else {
						CommandUtil.attackMove(tank, positionToSiege);
					}
				}
			} else {
				// TODO 시즈 위치 조절
			}
		} else {
			// 아직 도착하지 못했을때
            if (tank.canUnsiege()) {
                tank.unsiege();
            } else {
            	CommandUtil.attackMove(tank, order.getPosition());
            }
		}
	}
	
	private Position findPositionToSiege() {
		Chokepoint choke = BWTA.getNearestChokepoint(order.getPosition());
		double radian = 0.0;
		
		int limit = 1;
		int distance = 100;
		
		while (limit < 10) {
			while (distance < squadRange) {
				for (Integer angle : MicroSet.FleeAngle.EIGHT_360_ANGLE) {
					double radianAdjust = MicroUtils.rotate(radian, angle);
				    Position fleeVector = new Position((int)(distance * Math.cos(radianAdjust)), (int)(distance * Math.sin(radianAdjust)));
				    int x = order.getPosition().getX() + fleeVector.getX();
				    int y = order.getPosition().getY() + fleeVector.getY();
				    
				    Position movePosition = new Position(x, y);
				    if (movePosition.isValid() && BWTA.getRegion(movePosition) != null
							&& MyBotModule.Broodwar.isWalkable(movePosition.getX() / 8, movePosition.getY() / 8)) {
				    	
				    	if (choke.getCenter().getDistance(movePosition) >= 128) {
				    		int siegeCount = 0;
					    	List<Unit> units = MapGrid.Instance().getUnitsNear(movePosition, 100, true, false);
							for (Unit unit : units) {
					    		if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					    			siegeCount++;
					    		}
					    	}
							if (siegeCount <= limit) {
								return movePosition;
							} 
				    	}
				    }
				}
				distance += 100;
			}
			limit++;
		}
		
		MyBotModule.Broodwar.sendText("findPositionToSiege is null");
		return null;
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
	        
	        int priorityScore = TargetPriority.getPriority(tank, target); // 유닛별 우선순위
			int splashScore = 0; // 스플래시 점수
			int distanceScore = 0; // 거리 점수
			int hitPointScore = 0; // HP 점수

	        // 스플래시
			if (isSieged) {
				List<Unit> unitsInSplash = target.getUnitsInRadius(SIEGE_MODE_OUTER_SPLASH_RAD);
		        for (Unit unitInSplash : unitsInSplash) {
	        		int splashUnitDistance = target.getDistance(unitInSplash);
		        	int priorityInSpash = TargetPriority.getPriority(tank, unitInSplash);
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
				if (tank.isInWeaponRange(target)) {
					distanceScore += 100;
				}
				
				distanceScore -= tank.getDistance(target) / 5;
			}
			
			// 한방에 죽는다면 HP 높을 수록 우선순위가 높다.
			// 한방에 안죽는다면 HP가 낮을 수록 우선순위가 높다.
			if (MicroUtils.killedByNShot(tank, target, 1)) {
				hitPointScore += target.getHitPoints() / 10;
	        } else {
	        	hitPointScore -= target.getHitPoints() / 10;
	        }
	        
			int totalScore = priorityScore + splashScore + distanceScore + hitPointScore;
			if (totalScore > bestTargetScore) {
				bestTarget = target;
				bestTargetScore = totalScore;
			}
	    }
	    
	    return bestTarget;
	}

}
