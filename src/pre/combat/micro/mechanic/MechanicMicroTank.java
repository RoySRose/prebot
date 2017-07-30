package pre.combat.micro.mechanic;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import pre.MapGrid;
import pre.UnitInfo;
import pre.combat.SpiderMineManger;
import pre.combat.SquadOrder;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.CommandUtil;
import pre.util.CommonUtils;
import pre.util.KitingOption;
import pre.util.MicroSet;
import pre.util.MicroUtils;

public class MechanicMicroTank extends MechanicMicroAbstract {

	private SquadOrder order;
	private List<UnitInfo> enemiesInfo;
	
	private List<Unit> tankList;
	private List<Unit> goliathList;
	private int initFrame;
	private int siegeModeSpreadRadius;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, int initFrame) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.initFrame = initFrame;
		this.siegeModeSpreadRadius = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + (int) (Math.log(tankList.size()) * 10);
	}
	
	public void executeMechanicMicro(Unit tank) {
		if (!CommonUtils.executeUnitRotation(tank, 5)) {
			return;
		}
		
		if (tank.isSieged()) {
			executeSiegeMode(tank);
		} else {
			executeTankMode(tank);
		}
	}
	
	private void executeSiegeMode(Unit tank) {
		if (initFrame + 10 >= MyBotModule.Broodwar.getFrameCount()) {
			return;
		}
		
		// Decision -> 0: stop, 1: kiting(attack unit), 2: change
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecisionForSiegeMode(tank, enemiesInfo, tankList, order);
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
			if (tank.canUnsiege() && tank.getDistance(movePosition) > order.getRadius()) {
				tank.unsiege();
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
		if (initFrame + 10 >= MyBotModule.Broodwar.getFrameCount()) {
			tank.siege();
			return;
		}
		
		KitingOption kOpt = KitingOption.defaultKitingOption();
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(tank, enemiesInfo); // 0: flee, 1: kiting, 2: go, 3: change
		switch (decision.getDecision()) {
		case 0: // flee
			kOpt.setHaveToFlee(true);
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseKiting(tank, decision.getTargetInfo(), kOpt);
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
			if (targetType.groundWeapon().maxRange() <= MicroSet.Tank.SIEGE_MODE_MIN_RANGE) { // melee 타깃
				Position kitingGoalPosition = order.getPosition();
				kOpt.setGoalPosition(kitingGoalPosition);
				MicroUtils.preciseKiting(tank, decision.getTargetInfo(), kOpt);
			} else {
				boolean shouldSiege = false;
				int distanceToTarget = tank.getDistance(targetPosition);
				if (distanceToTarget <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE) {
					shouldSiege = true;
				} else {
					for (Unit otherTank : tankList) { // target이 시즈포격에서 자유로운가
						if (otherTank.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
								&& otherTank.getDistance(targetPosition) <= MicroSet.Tank.SIEGE_MODE_MAX_RANGE
								&& otherTank.getDistance(tank.getPosition()) < MicroSet.Tank.SIEGE_LINK_DISTANCE) {
							shouldSiege = true;
							break;
						}
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
			
			if (tank.getDistance(order.getPosition()) <= siegeModeSpreadRadius && tank.canSiege()) {
				Position positionToSiege = findPositionToSiege(siegeModeSpreadRadius);
				if (positionToSiege != null) {
					if (tank.getDistance(positionToSiege) <= 50) {
						tank.siege();
					} else {
						CommandUtil.attackMove(tank, positionToSiege);
					}
				}
			} else if (tank.getDistance(order.getPosition()) <= order.getRadius()) {
				if (tank.isIdle() || tank.isBraking()) {
					Position randomPosition = MicroUtils.randomPosition(tank.getPosition(), 100);
					CommandUtil.attackMove(tank, randomPosition);
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
		Chokepoint choke = BWTA.getNearestChokepoint(order.getPosition());
		
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
				    if (movePosition.isValid() && BWTA.getRegion(movePosition) != null
							&& MyBotModule.Broodwar.isWalkable(movePosition.getX() / 8, movePosition.getY() / 8)) {
				    	
				    	if (choke.getCenter().getDistance(movePosition) >= 128) {
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
		
		MyBotModule.Broodwar.sendText("findPositionToSiege is null");
		return null;
	}
}
