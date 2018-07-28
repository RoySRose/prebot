package prebot.build.provider.items.unit;

import bwapi.Race;
import bwapi.UnitType;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.StarportUnitSelector;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;

public class BuilderScienceVessel extends DefaultBuildableItem {

	StarportUnitSelector starportUnitSelector;

	public BuilderScienceVessel(MetaType metaType, StarportUnitSelector starportUnitSelector) {
		super(metaType);
		this.starportUnitSelector = starportUnitSelector;
	}

	public final boolean buildCondition() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) == 0) {
			return false;
		}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Science_Facility) == 0) {
			return false;
		}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Control_Tower) == 0) {
			return false;
		}

		if (starportUnitSelector.getSelected().equals(metaType.getUnitType())) {
			return true;
		}
		
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Science_Vessel)) {
			return false;
		}
		
		// 활성화된 커맨드가 2개 이상일 경우
		int activatedCommandCenterCount = UnitUtils.activatedCommandCenterCount();
		int maxVesselCount = getMaxVesselCount();
		int allVesselCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Science_Vessel);

		return allVesselCount <= maxVesselCount && activatedCommandCenterCount >= 2;
	}

	private int getMaxVesselCount() {
		if (InfoUtils.enemyRace() == Race.Protoss) {
			if (UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Arbiter, UnitType.Protoss_Arbiter_Tribunal)) {
				return 2;
			} else if (UnitUtils.enemyUnitDiscovered(UnitType.Protoss_Dark_Templar)) {
				return 1;
			}
			
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			// TODO 투스타 전략이면 베슬을 많이 쓰지 않으므로 숫자를 조절할 필요가 있다.
			if (UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Lurker, UnitType.Zerg_Egg)) {
				return 2;
			} else if (UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Mutalisk)) {
				if (UnitUtils.getEnemyUnitInfoCount(EnemyUnitFindRange.ALL, UnitType.Zerg_Mutalisk) >= 8) {
					return 2;
				}
				return 1;
				
			} else if (UnitUtils.enemyUnitDiscovered(UnitType.Zerg_Hive, UnitType.Zerg_Defiler, UnitType.Zerg_Defiler_Mound)) {
				return 3;
			}
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			if (UnitUtils.enemyUnitDiscovered(UnitType.Terran_Wraith, UnitType.Terran_Control_Tower)) {
				return 1;
			}
		}
		
		return 0;
	}
}
