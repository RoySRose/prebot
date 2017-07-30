package pre.combat.micro.mechanic;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwta.BaseLocation;
import pre.UnitInfo;
import pre.combat.SquadOrder;
import pre.manager.InformationManager;
import pre.util.CommandUtil;
import pre.util.CommonUtils;
import pre.util.KitingOption;
import pre.util.MicroUtils;

public class MechanicMicroGoliath extends MechanicMicroAbstract {

	private SquadOrder order;
	private List<UnitInfo> enemiesInfo;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void executeMechanicMicro(Unit goliath) {
		if (!CommonUtils.executeUnitRotation(goliath, 5)) {
			return;
		}
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(goliath, enemiesInfo); // 0: flee, 1: kiting, 2: attack

		KitingOption kOpt = KitingOption.defaultKitingOption();
		switch (decision.getDecision()) {
		case 0: // flee
			kOpt.setHaveToFlee(true);
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseKiting(goliath, decision.getTargetInfo(), kOpt);
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
