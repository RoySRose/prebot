package prebot.micro.old.control;


import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.LagObserver;
import prebot.common.MapGrid;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.TimeUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.old.OldMicroUtils;
import prebot.micro.old.OldKitingOption;
import prebot.micro.old.OldSquadOrder;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.SpiderMineManger;

public class MechanicMicroTank extends MechanicMicroAbstract {
	
	private OldSquadOrder order;
	private List<UnitInfo> enemiesInfo;
	private List<UnitInfo> flyingEnemisInfo;
	
	private List<Unit> vultureList = new ArrayList<>();
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	
	private int initFrame = 0;
	private boolean useInitFrame = false;
	private int saveUnitLevel = 1;
	
	private int siegeModeSpreadRadius = 200;
	
	public void prepareMechanic(OldSquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = OldMicroUtils.filterTargetInfos(enemiesInfo, false);
		this.flyingEnemisInfo = OldMicroUtils.filterFlyingTargetInfos(enemiesInfo);
	}
	
	public void prepareMechanicAdditional(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel, int initFrame) {
		this.vultureList = vultureList;
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.initFrame = initFrame;
		this.siegeModeSpreadRadius = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + (int) (Math.log(tankList.size()) * 11);
		this.saveUnitLevel = saveUnitLevel;
		this.useInitFrame = false;
	}
	
	public void executeMechanicMicro(Unit tank) {
		if (!TimeUtils.executeUnitRotation(tank, LagObserver.groupsize())) {
			return;
		}
		
		if (tank.isSieged()) {
			executeSiegeMode(tank);
		} else {
			executeTankMode(tank);
		}
	}
	
	private void executeSiegeMode(Unit tank) {
		if (initFrame + 24 >= Prebot.Broodwar.getFrameCount()) {
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
			CommandUtils.attackUnit(tank, targetInfo.getUnit());
			break;
			
		case 2: // go
			if (MicroConfig.Common.versusMechanicSet()) {
				// 테란전용 go
				int distToOrder = tank.getDistance(order.getPosition());
				if (tank.canUnsiege()) {
					if(distToOrder > MicroConfig.Tank.SIEGE_MODE_MAX_RANGE
							|| OldMicroUtils.existTooNarrowChoke(tank.getPosition())
							|| OldMicroUtils.isExpansionPosition(tank.getPosition())) {
						tank.unsiege();
					}
				}
				
			} else {
				Position movePosition = order.getPosition();
				if (tank.canUnsiege()) {
					if(tank.getDistance(movePosition) > siegeModeSpreadRadius
							|| OldMicroUtils.existTooNarrowChoke(tank.getPosition())
							|| OldMicroUtils.isExpansionPosition(tank.getPosition())) {
						tank.unsiege();
					}
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
		if (Prebot.Broodwar.self().getRace() != Race.Terran) {
			if (!useInitFrame && initFrame + 24 >= Prebot.Broodwar.getFrameCount()) {
				useInitFrame = true;
				tank.siege();
				return;
			}
		}
		
		OldKitingOption kOpt = OldKitingOption.defaultKitingOption();
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(tank, enemiesInfo, flyingEnemisInfo, saveUnitLevel); // 0: flee, 1: kiting, 2: go, 3: change
		switch (decision.getDecision()) {
		case 0: // flee
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			OldMicroUtils.preciseFlee(tank, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			UnitInfo targetInfo = decision.getTargetInfo();
			Unit target = OldMicroUtils.getUnitIfVisible(targetInfo);
			Position targetPosition = targetInfo.getLastPosition();
			UnitType targetType = targetInfo.getType();
			if (target != null) {
				targetPosition = target.getPosition();
				targetType = target.getType();
			}
			if (target != null && targetType == UnitType.Terran_Vulture_Spider_Mine && tank.isInWeaponRange(target)) {
				tank.holdPosition();
				
			} if (vultureList.size() + goliathList.size() <= MicroConfig.Tank.OTHER_UNIT_COUNT_SIEGE_AGAINST_MELEE
					&& targetType.groundWeapon().maxRange() <= MicroConfig.Tank.SIEGE_MODE_MIN_RANGE) { // melee 타깃 카이팅(백업 유닛이 있으면 밀리유닛 상대로도 시즈모드 변경) 
				Position kitingGoalPosition = order.getPosition();
				kOpt.setGoalPosition(kitingGoalPosition);
				OldMicroUtils.preciseKiting(tank, decision.getTargetInfo(), kOpt);
			} else {
				boolean shouldSiege = false;
				int distanceToTarget = tank.getDistance(targetPosition);

				if (targetType != UnitType.Terran_Siege_Tank_Tank_Mode && targetType != UnitType.Terran_Siege_Tank_Siege_Mode) {
					if (distanceToTarget <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 5) { // 시즈범위 안에 있다면 시즈
						shouldSiege = true;
					} else {
						for (Unit otherTank : tankList) { // target이 다른 가까운 시즈포격범위 내에 있다면 시즈모드로 변경
							if (otherTank.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
									&& otherTank.getDistance(targetPosition) <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 5
									&& otherTank.getDistance(tank.getPosition()) < MicroConfig.Tank.SIEGE_LINK_DISTANCE) { 
								shouldSiege = true;
								break;
							}
						}
					}
				} else {
					if (saveUnitLevel == 0 && distanceToTarget <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 5) {
						shouldSiege = true;
					} else if (saveUnitLevel > 0 && distanceToTarget <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + MicroConfig.Common.BACKOFF_DIST_SIEGE_TANK + 10) {
						shouldSiege = true;
					}
				}
				
				if (shouldSiege && tank.canSiege()) {
					tank.siege();
				} else {
					Position kitingGoalPosition = order.getPosition();
					kOpt.setGoalPosition(kitingGoalPosition);
					OldMicroUtils.preciseKiting(tank, decision.getTargetInfo(), kOpt);
				}
			}
			break;
			
		case 2: // go
			if (MicroConfig.Common.versusMechanicSet()) {
				// 테란전용 go
				int distToOrder = tank.getDistance(order.getPosition());
				if (distToOrder <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE
						&& !OldMicroUtils.existTooNarrowChoke(tank.getPosition())
						&& !OldMicroUtils.isExpansionPosition(tank.getPosition())) { // orderPosition의 둘러싼 대형을 만든다.
					if (tank.canSiege()) {
						tank.siege();
					} else {
						if (tank.isIdle() || tank.isBraking()) {
							if (!tank.isBeingHealed()) {
								Position randomPosition = OldMicroUtils.randomPosition(tank.getPosition(), 100);
								CommandUtils.attackMove(tank, randomPosition);
							}
						}
					}
				} else {
					CommandUtils.attackMove(tank, order.getPosition());
				}
				
			} else {
				// 일반적인 go
				if (tank.getDistance(order.getPosition()) <= siegeModeSpreadRadius && tank.canSiege()) {
					Position positionToSiege = findPositionToSiege(tank, siegeModeSpreadRadius); // orderPosition의 중심으로 펼쳐진 시즈 대형을 만든다.
					if (positionToSiege != null) {
						if (tank.getDistance(positionToSiege) < 30) {
							tank.siege();
						} else {
							CommandUtils.attackMove(tank, positionToSiege);
						}
					}
					if (tank.isIdle() || tank.isBraking()) {
						if (!tank.isBeingHealed()) {
							Position randomPosition = OldMicroUtils.randomPosition(tank.getPosition(), 100);
							CommandUtils.attackMove(tank, randomPosition);
						}
					}
				} else if (tank.getDistance(order.getPosition()) <= order.getRadius()) {
					if (tank.isIdle() || tank.isBraking()) {
						if (!tank.isBeingHealed()) {
							Position randomPosition = OldMicroUtils.randomPosition(tank.getPosition(), 100);
							CommandUtils.attackMove(tank, randomPosition);
						}
					}
				} else {
	            	CommandUtils.attackMove(tank, order.getPosition());
				}
			}
			break;
			
		case 3: // change
			tank.siege();
			break;
		}
	}
	
	private Position findPositionToSiege(Unit tank, int siegeAreaDist) {
		int seigeNumLimit = 1;
		int distanceFromOrderPosition = MicroConfig.Tank.getSiegeArrangeDistance();
		
		while (seigeNumLimit < 8) {
			while (distanceFromOrderPosition < siegeAreaDist) {
				for (Integer angle : MicroConfig.FleeAngle.EIGHT_360_ANGLE) {
					double radianAdjust = OldMicroUtils.rotate(0.0, angle);
				    Position fleeVector = new Position((int)(distanceFromOrderPosition * Math.cos(radianAdjust)), (int)(distanceFromOrderPosition * Math.sin(radianAdjust)));
				    int x = order.getPosition().getX() + fleeVector.getX();
				    int y = order.getPosition().getY() + fleeVector.getY();
				    
				    Position movePosition = new Position(x, y);
				    if (OldMicroUtils.isValidGroundPosition(movePosition)
				    		&& Prebot.Broodwar.hasPath(tank.getPosition(), movePosition)
							&& OldMicroUtils.isConnectedPosition(tank.getPosition(), movePosition)) {
				    	if (OldMicroUtils.existTooNarrowChoke(movePosition) || OldMicroUtils.isExpansionPosition(movePosition)) {
				    		continue;
				    	}

				    	List<Unit> exactPositionUnits = Prebot.Broodwar.getUnitsInRadius(movePosition, 20);
				    	for (Unit unit : exactPositionUnits) {
				    		if (!unit.getType().canMove()) {
				    			continue;// somethingInThePosition
				    		}
				    	}
				    	
				    	Position shiftLeftUpPos = new Position(movePosition.getX() - 30, movePosition.getY() - 30);
				    	List<Unit> nearUnits = MapGrid.Instance().getUnitsNear(shiftLeftUpPos, 100, true, false, null);
				    	for (Unit nearUnit : nearUnits) {
				    		if (nearUnit.getType().canBuildAddon()) {
				    			continue; // addOnPosition
				    		}
				    	}
				    	
			    		int siegeCount = MapGrid.Instance().getUnitsNear(movePosition, 100, true, false, UnitType.Terran_Siege_Tank_Siege_Mode).size();
						if (siegeCount < seigeNumLimit) {
							return movePosition;
						}
				    }
				}
				
				if (distanceFromOrderPosition <= 0) {
					distanceFromOrderPosition = MicroConfig.Tank.getSiegeArrangeDistance() + MicroConfig.Tank.getSiegeArrangeDistanceAdjust();
				} else if (distanceFromOrderPosition <= MicroConfig.Tank.getSiegeArrangeDistance()) {
					distanceFromOrderPosition -= MicroConfig.Tank.getSiegeArrangeDistanceAdjust();
				} else {
					distanceFromOrderPosition += MicroConfig.Tank.getSiegeArrangeDistanceAdjust();
				}
			}
			distanceFromOrderPosition = MicroConfig.Tank.getSiegeArrangeDistance();
			seigeNumLimit++;
		}
		
		//MyBotModule.Broodwar.sendText("findPositionToSiege is null");
		return null;
	}
}
