

import bwapi.UnitType;

public class BuilderAcademy extends DefaultBuildableItem {

	public BuilderAcademy(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Academy)) {
			return false;
		}
		
		setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextSupplePoint);

		if (TimeUtils.after(StrategyIdea.academyFrame)) {
			setBlocking(true);
			setHighPriority(true);
			return true;
		}

		if (TimeUtils.afterTime(9, 0)) {
			return true;
		}
		
		if (StrategyIdea.currentStrategy == EnemyStrategy.ZERG_FAST_1HAT_LURKER) {
			return true;
		}

		// 활성화된 커맨드가 2개 이상일 경우
		return UnitUtils.activatedCommandCenterCount() >= 2;
	}

}
