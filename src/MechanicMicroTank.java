

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class MechanicMicroTank extends MechanicMicroAbstract {
	
	private SquadOrder order;
	private List<UnitInfo> enemiesInfo;
	private List<UnitInfo> flyingEnemisInfo;
	
	private List<Unit> vultureList = new ArrayList<>();
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	
	private int initFrame = 0;
	private int saveUnitLevel = 1;
	
	private int siegeModeSpreadRadius = 200;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = MicroUtils.filterTargetInfos(enemiesInfo, false);
		this.flyingEnemisInfo = MicroUtils.filterFlyingTargetInfos(enemiesInfo);
	}
	
	public void prepareMechanicAdditional(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel, int initFrame) {
		this.vultureList = vultureList;
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.initFrame = initFrame;
		this.siegeModeSpreadRadius = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + (int) (Math.log(tankList.size()) * 10);
		this.saveUnitLevel = saveUnitLevel;
	}
	
	public void executeMechanicMicro(Unit tank) {
		if (!CommonUtils.executeUnitRotation(tank, LagObserver.groupsize())) {
			return;
		}
		
		if (tank.isSieged()) {
			executeSiegeMode(tank);
		} else {
			executeTankMode(tank);
		}
	}
	
	private void executeSiegeMode(Unit tank) {
		if (initFrame + 24 >= MyBotModule.Broodwar.getFrameCount()) {
			return;
		}

		// Decision -> 0: stop, 1: kiting(attack unit), 2: change
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecisionForSiegeMode(tank, enemiesInfo, tankList, order, saveUnitLevel);
		SpiderMineManger.Instance().addRemoveList(tank);
		switch (decision.getDecision()) {
		case -1: // do nothing
			break;
			
		case 0: // stop 유효범위내에 적이 있지만 스플래시 데미지가 더 크다고 판단
			tank.stop();
			break;
			
		case 1: // attack unit
			UnitInfo targetInfo = decision.getTargetInfo();
			CommandUtil.attackUnit(tank, targetInfo.getUnit());
			break;
			
		case 2: // go
			Position movePosition = order.getPosition();
			if (tank.canUnsiege()) {
				if(tank.getDistance(movePosition) > siegeModeSpreadRadius || MicroUtils.existTooNarrowChoke(tank.getPosition())) {
					tank.unsiege();
				}
			}
			break;
			
		case 3: // change
			if (tank.canUnsiege()) {
				tank.unsiege();
			}
			break;
		}
	}
	
	private void executeTankMode(Unit tank) {
		if (initFrame + 24 >= MyBotModule.Broodwar.getFrameCount()) {
			tank.siege();
			return;
		}
		
		KitingOption kOpt = KitingOption.defaultKitingOption();
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(tank, enemiesInfo, flyingEnemisInfo, saveUnitLevel); // 0: flee, 1: kiting, 2: go, 3: change
		switch (decision.getDecision()) {
		case 0: // flee
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseFlee(tank, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			UnitInfo targetInfo = decision.getTargetInfo();
			Unit target = MicroUtils.getUnitIfVisible(targetInfo);
			Position targetPosition = targetInfo.getLastPosition();
			UnitType targetType = targetInfo.getType();
			if (target != null) {
				targetPosition = target.getPosition();
				targetType = target.getType();
			}
			if (vultureList.size() + goliathList.size() <= MicroSet.Tank.OTHER_UNIT_COUNT_SIEGE_AGAINST_MELEE
					&& targetType.groundWeapon().maxRange() <= MicroSet.Tank.SIEGE_MODE_MIN_RANGE) { // melee 타깃 카이팅(백업 유닛이 있으면 밀리유닛 상대로도 시즈모드 변경) 
				Position kitingGoalPosition = order.getPosition();
				kOpt.setGoalPosition(kitingGoalPosition);
				MicroUtils.preciseKiting(tank, decision.getTargetInfo(), kOpt);
			} else {
				boolean shouldSiege = false;
				int distanceToTarget = tank.getDistance(targetPosition);

				if (targetType != UnitType.Terran_Siege_Tank_Tank_Mode && targetType != UnitType.Terran_Siege_Tank_Siege_Mode) {
					if (distanceToTarget <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 5) { // 시즈범위 안에 있다면 시즈
						shouldSiege = true;
					} else {
						for (Unit otherTank : tankList) { // target이 다른 가까운 시즈포격범위 내에 있다면 시즈모드로 변경
							if (otherTank.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
									&& otherTank.getDistance(targetPosition) <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 5
									&& otherTank.getDistance(tank.getPosition()) < MicroSet.Tank.SIEGE_LINK_DISTANCE) { 
								shouldSiege = true;
								break;
							}
						}
					}
				} else {
					if (saveUnitLevel <= 1 && distanceToTarget <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + 5) {
						shouldSiege = true;
					} else if (saveUnitLevel == 2 && distanceToTarget <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE + MicroSet.Common.BACKOFF_DIST_SIEGE_TANK + 10) {
						shouldSiege = true;
					}
				}
				
				if (shouldSiege && tank.canSiege()) {
					tank.siege();
				} else {
					Position kitingGoalPosition = order.getPosition();
					kOpt.setGoalPosition(kitingGoalPosition);
					MicroUtils.preciseKiting(tank, decision.getTargetInfo(), kOpt);
				}
			}
			break;
			
		case 2: // go
			if (tank.getDistance(order.getPosition()) <= siegeModeSpreadRadius && tank.canSiege() && !MicroUtils.existTooNarrowChoke(tank.getPosition())) {
				Position positionToSiege = findPositionToSiege(siegeModeSpreadRadius); // orderPosition의 중심으로 펼쳐진 시즈 대형을 만든다.
				if (positionToSiege != null) {
					if (tank.getDistance(positionToSiege) < 30) {
						tank.siege();
					} else {
						CommandUtil.attackMove(tank, positionToSiege);
					}
				}
			} else if (tank.getDistance(order.getPosition()) <= order.getRadius()) {
				if (tank.isIdle() || tank.isBraking()) {
					if (!tank.isBeingHealed()) {
						Position randomPosition = MicroUtils.randomPosition(tank.getPosition(), 100);
						CommandUtil.attackMove(tank, randomPosition);
					}
				}
			} else {
            	CommandUtil.attackMove(tank, order.getPosition());
			}
			break;
			
		case 3: // change
			tank.siege();
			break;
		}
	}
	
	private Position findPositionToSiege(int siegeAreaDist) {
		int seigeNumLimit = 1;
		int distanceFromOrderPosition = MicroSet.Tank.SIEGE_ARRANGE_DISTANCE;
		
		while (seigeNumLimit < 10) {
			while (distanceFromOrderPosition < siegeAreaDist) {
				for (Integer angle : MicroSet.FleeAngle.EIGHT_360_ANGLE) {
					double radianAdjust = MicroUtils.rotate(0.0, angle);
				    Position fleeVector = new Position((int)(distanceFromOrderPosition * Math.cos(radianAdjust)), (int)(distanceFromOrderPosition * Math.sin(radianAdjust)));
				    int x = order.getPosition().getX() + fleeVector.getX();
				    int y = order.getPosition().getY() + fleeVector.getY();
				    
				    Position movePosition = new Position(x, y);
				    if (MicroUtils.isValidGroundPosition(movePosition)) {
				    	if (MicroUtils.existTooNarrowChoke(movePosition)) {
				    		continue;
				    	}

				    	boolean somethingInThePosition = false;
				    	boolean addOnPosition = false;
				    	List<Unit> exactPositionUnits = MyBotModule.Broodwar.getUnitsInRadius(movePosition, 15);
				    	for (Unit unit : exactPositionUnits) {
				    		if (!unit.getType().canMove()) {
				    			somethingInThePosition = true;
				    			break;
				    		}
				    	}
				    	
				    	Position pos1 = new Position(movePosition.getX() - 30, movePosition.getY() - 30);
				    	List<Unit> nearUnits = MapGrid.Instance().getUnitsNear(pos1, 100, true, false, null);
				    	for (Unit nearUnit : nearUnits) {
				    		if (nearUnit.getType().canBuildAddon()) {
				    			addOnPosition = true;
				    			break;
				    		}
				    	}
				    	
				    	if (!somethingInThePosition && !addOnPosition) {
				    		int siegeCount = MapGrid.Instance().getUnitsNear(movePosition, 100, true, false, UnitType.Terran_Siege_Tank_Siege_Mode).size();
							if (siegeCount < seigeNumLimit) {
								return movePosition;
							}
				    	}
				    }
				}
				
				if (distanceFromOrderPosition <= 0) {
					distanceFromOrderPosition = MicroSet.Tank.SIEGE_ARRANGE_DISTANCE + 50;
				} else if (distanceFromOrderPosition <= MicroSet.Tank.SIEGE_ARRANGE_DISTANCE) {
					distanceFromOrderPosition -= 50;
				} else {
					distanceFromOrderPosition += 50;
				}
			}
			distanceFromOrderPosition = MicroSet.Tank.SIEGE_ARRANGE_DISTANCE;
			seigeNumLimit++;
		}
		
		//MyBotModule.Broodwar.sendText("findPositionToSiege is null");
		return null;
	}
}
