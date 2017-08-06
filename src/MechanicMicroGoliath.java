

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class MechanicMicroGoliath extends MechanicMicroAbstract {

	private SquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	
	private int saveUnitLevel = 1;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.saveUnitLevel = saveUnitLevel;
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
			Position kitingGoalPosition = order.getPosition();
			kOpt.setGoalPosition(kitingGoalPosition);
			MicroUtils.preciseKiting(goliath, decision.getTargetInfo(), kOpt);
			break;
			
		case 2: // attack move
			Position movePosition = order.getPosition();
			
			// 이동지역까지 attackMove로 간다.
			if (goliath.getDistance(movePosition) > order.getRadius()) {
				CommandUtil.attackMove(goliath, movePosition);
				
			} else { // 목적지 도착
				if (goliath.isIdle() || goliath.isBraking()) {
					Position randomPosition = MicroUtils.randomPosition(goliath.getPosition(), 100);
					CommandUtil.attackMove(goliath, randomPosition);
				}
			}
			break;
		}
	}

}
