package prebot.brain.knowledge.impl.inerrableaction;

import java.util.List;

import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.brain.Idea;
import prebot.brain.Info;
import prebot.brain.information.UnitInfo;
import prebot.brain.knowledge.InerrableActionKnowledge;
import prebot.common.code.Code.SquadName;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.UnitUtils;

public class ActivateScoutDefense extends InerrableActionKnowledge {

	@Override
	protected boolean doSomething() {
		if (UnitUtils.hasUnit(UnitType.Terran_Marine, UnitFindRange.COMPLETE)
				|| UnitUtils.hasUnit(UnitType.Terran_Vulture, UnitFindRange.COMPLETE)) {
			Idea.of().squadNameToDeactivate.add(SquadName.SCOUT_DEFENSE);
			return false;
		}
		
		BaseLocation myBase = Info.of().myBase;
		Region myRegion = BWTA.getRegion(myBase.getPosition());
		List<UnitInfo> euiList = Info.of().euiListInMyRegion.get(myRegion);
		if (euiList.isEmpty()) {
			Idea.of().squadNameToDeactivate.add(SquadName.SCOUT_DEFENSE);
		} else {
			Idea.of().squadNameToActivate.add(SquadName.SCOUT_DEFENSE);
		}
		return true;
	}
}
