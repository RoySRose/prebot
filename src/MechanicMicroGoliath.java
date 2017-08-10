

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwta.BaseLocation;

public class MechanicMicroGoliath extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	
	private boolean attackWithTank = false;
	
	private int saveUnitLevel = 1;
	private int stickToTankRadius = 0;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.saveUnitLevel = saveUnitLevel;
		
		this.attackWithTank = tankList.size() * 6 >= goliathList.size();
		if (this.attackWithTank) {
			this.stickToTankRadius = 150 + (int) (Math.log(goliathList.size()) * 15);
//			System.out.println("stickToTankRadius: " + stickToTankRadius);
		}
	}
	
	public void executeMechanicMicro(Unit goliath) {
		if (!CommonUtils.executeUnitRotation(goliath, LagObserver.groupsize())) {
			return;
		}

		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(goliath, enemiesInfo, saveUnitLevel); // 0: flee, 1: kiting, 2: attack
		KitingOption kOpt = KitingOption.defaultKitingOption();
		switch (decision.getDecision()) {
		case 0: // flee
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseFlee(goliath, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			boolean haveToFight = true;
			Unit closeTank = null;
			if (saveUnitLevel >= 1 && attackWithTank) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit tank : tankList) {
					int dist = goliath.getDistance(tank.getPosition());
					if (dist < closeDist) {
						closeTank = tank;
						closeDist = dist;
						// 가까운 곳에 탱크가 있으면 싸운다.
						if (closeDist < stickToTankRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			}
			if (haveToFight) {
				kOpt.setGoalPosition(order.getPosition());
				MicroUtils.preciseKiting(goliath, decision.getTargetInfo(), kOpt);
			} else {
				// 가까운 곳에 없으면 탱크로 이동
//				kOpt.setGoalPosition(closeTank.getPosition());
//				kOpt.setCooltimeAlwaysAttack(false);
				CommandUtil.move(goliath, closeTank.getPosition());
			}
			break;
			
		case 2: // attack move
			Position movePosition = order.getPosition();
			
			// 이동지역까지 attackMove로 간다.
			if (goliath.getDistance(movePosition) > order.getRadius()) {
//				CommandUtil.attackMove(goliath, movePosition);
				CommandUtil.move(goliath, movePosition);
				
			} else { // 목적지 도착
				if (goliath.isIdle() || goliath.isBraking()) {
					if (!goliath.isBeingHealed()) {
						Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), 100);
						CommandUtil.attackMove(goliath, randomPosition);
					}
				}
			}
			break;
		}
	}

}
