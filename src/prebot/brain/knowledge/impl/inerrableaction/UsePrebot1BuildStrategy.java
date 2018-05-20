package prebot.brain.knowledge.impl.inerrableaction;

import java.util.HashMap;
import java.util.Map;

import bwapi.Race;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.build.manager.BuildManager;
import prebot.main.Prebot;

/** 
 * build큐가 empty가 되었을때 general strategy로 변경
 */
public class UsePrebot1BuildStrategy extends InerrableActionKnowledge {
	@Override
	protected boolean doSomething() {
		if (!BuildManager.Instance().buildQueue.isEmpty()) {
			return true;
		}

		Map<UnitType, Integer> factoryUnitRatio = new HashMap<>();
		if (Prebot.Game.enemy().getRace() == Race.Protoss) {
			factoryUnitRatio.put(UnitType.Terran_Vulture, 1);
			factoryUnitRatio.put(UnitType.Terran_Siege_Tank_Tank_Mode, 1);
			factoryUnitRatio.put(UnitType.Terran_Goliath, 0);
			
		} else if (Prebot.Game.enemy().getRace() == Race.Zerg) {
			factoryUnitRatio.put(UnitType.Terran_Vulture, 0);
			factoryUnitRatio.put(UnitType.Terran_Siege_Tank_Tank_Mode, 1);
			factoryUnitRatio.put(UnitType.Terran_Goliath, 2);
			
		} else if (Prebot.Game.enemy().getRace() == Race.Terran) {
			factoryUnitRatio.put(UnitType.Terran_Vulture, 0);
			factoryUnitRatio.put(UnitType.Terran_Siege_Tank_Tank_Mode, 2);
			factoryUnitRatio.put(UnitType.Terran_Goliath, 1);
			
		} else {
			factoryUnitRatio.put(UnitType.Terran_Vulture, 0);
			factoryUnitRatio.put(UnitType.Terran_Siege_Tank_Tank_Mode, 1);
			factoryUnitRatio.put(UnitType.Terran_Goliath, 1);
		}
		
		Idea.of().factoryUnitRatio = factoryUnitRatio;
		return false;
	}
}