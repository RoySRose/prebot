package prebot.micro.control.airforce;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.control.Control;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;

public class AirForceControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList, Position targetPosition) {

//		int[] fleeRadius = AirForceManager.Instance().fleeRadius;
		
		for (Unit wraith : unitList) {
//			Decision decision = decision(euiList);
//			
//			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
//				// 감당못할 마린, 골리앗 등이 나타남.
////				FleeOption fOption = new FleeOption(targetPosition, false, fleeRadius);
//				MicroUtils.airFlee(wraith, targetPosition);
//				
//			} else if (decision.type == DecisionType.ATTACK_UNIT) {
//				// 
//				
//			} else if (decision.type == DecisionType.RIGHT_CLICK) {
//				MicroUtils.airDriving(wraith, targetPosition);
//			}
			

			MicroUtils.airDriving(wraith, targetPosition);
			if (wraith.getDistance(targetPosition) < 100) {
				AirForceManager.Instance().changeTargetIndex();
			}
		}
	}

	private Decision decision(List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
		return null;
	}
}
