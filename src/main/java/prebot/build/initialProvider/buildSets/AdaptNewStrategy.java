package prebot.build.initialProvider.buildSets;

import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.UnitUtils;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/// 봇 프로그램 설정
public class AdaptNewStrategy extends BaseBuild {

	public void adapt(ExpansionOption strategy, TilePosition factoryPos, TilePosition starport1, TilePosition starport2) {
		if (strategy == ExpansionOption.TWO_STARPORT) {
			int starportCount = UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Starport);
			if (starportCount == 0) {
				queueBuild(true, UnitType.Terran_Starport, starport1);
				queueBuild(true, UnitType.Terran_Starport, starport2);
			} else if (starportCount == 1) {
				queueBuild(true, UnitType.Terran_Starport, starport2);
			}
			
		} else if (strategy == ExpansionOption.TWO_FACTORY) {
			int factoryCount = UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Factory);
			if (factoryCount <= 1) {
				queueBuild(true, UnitType.Terran_Factory, factoryPos);
			}
			
		} else if (strategy == ExpansionOption.ONE_STARPORT) {
			int starportCount = UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Starport);
			if (starportCount == 0) {
				queueBuild(true, UnitType.Terran_Starport, starport1);
			}
		}
	}
}
