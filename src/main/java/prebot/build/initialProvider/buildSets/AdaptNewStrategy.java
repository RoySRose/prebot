package prebot.build.initialProvider.buildSets;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.strategy.InformationManager;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/// 봇 프로그램 설정
public class AdaptNewStrategy extends BaseBuild {

	public void adapt(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos
			, TilePosition bunkerPos, TilePosition starport1, TilePosition starport2, ExpansionOption strategy) {
		if (strategy == ExpansionOption.TWO_STARPORT) {
			if (InformationManager.Instance().enemyRace == Race.Zerg) {
				queueBuild(true, UnitType.Terran_Starport, starport1);
				queueBuild(true, UnitType.Terran_Starport, starport2);

			} else if (InformationManager.Instance().enemyRace == Race.Terran) {
				queueBuild(true, UnitType.Terran_Starport, starport1);
				queueBuild(true, UnitType.Terran_Starport, starport2);

			} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
				queueBuild(true, UnitType.Terran_Starport, starport1);
				queueBuild(true, UnitType.Terran_Starport, starport2);
			}
		} else if (strategy == ExpansionOption.TWO_FACTORY) {
			if (InformationManager.Instance().enemyRace == Race.Zerg) {
				queueBuild(true, UnitType.Terran_Factory, factoryPos);

			} else if (InformationManager.Instance().enemyRace == Race.Terran) {
				queueBuild(true, UnitType.Terran_Factory, factoryPos);

			} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
				queueBuild(true, UnitType.Terran_Factory, factoryPos);
			}
		}
	}
}
