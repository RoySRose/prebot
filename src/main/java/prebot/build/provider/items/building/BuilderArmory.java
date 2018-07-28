package prebot.build.provider.items.building;

import bwapi.UnitType;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;

public class BuilderArmory extends DefaultBuildableItem {

	public BuilderArmory(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Armory)) {
			return false;
		}

		// 전략적 판단에 대한 부분은 리더에게 오더 받는다. 변경예정.
		// 긴급할 경우 자원 체크로직이 필요한가? 어차피 맨위 true로 올릴텐데?
		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Scout, UnitType.Protoss_Shuttle, UnitType.Terran_Wraith)) {
			setBlocking(true);
			setHighPriority(true);
			setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			return true;
		}

		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Arbiter, UnitType.Protoss_Arbiter_Tribunal)) {
			setBlocking(true);
			setHighPriority(true);
			setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			return true;
		}

		if (TimeUtils.afterTime(9, 0)) {
			return true;
		}

		// 활성화된 커맨드가 2개 이상일 경우
		return UnitUtils.activatedCommandCenterCount() >= 2;
	}

}
