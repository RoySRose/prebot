package prebot.brain.knowledge.impl.squad;

import prebot.brain.knowledge.InerrableActionKnowledge;

public class ActivateScoutDefense extends InerrableActionKnowledge {

	@Override
	protected boolean doSomething() {
//		if (UnitUtils.hasUnit(UnitType.Terran_Marine, UnitFindRange.COMPLETE)) {
//			Idea.of().squadNameToDeactivate.add(SquadName.SCOUT_DEFENSE);
//			return false;
//		}
//		
//		BaseLocation myBase = Info.of().myBase;
//		Region myRegion = BWTA.getRegion(myBase.getPosition());
//		List<UnitInfo> euiList = Info.of().euiListInMyRegion.get(myRegion);
//		if (euiList.isEmpty()) {
//			Idea.of().squadNameToDeactivate.add(SquadName.SCOUT_DEFENSE);
//		} else {
//			Idea.of().squadNameToActivate.add(SquadName.SCOUT_DEFENSE);
//		}
//		return true;
		
		return false;
	}
}
