package prebot.strategy.analyse.protoss;

import bwapi.UnitType;
import prebot.strategy.analyse.UnitAnalyser;

public class ZealotAnalyser extends UnitAnalyser {

	public ZealotAnalyser() {
		super(UnitType.Protoss_Zealot);
	}

	@Override
	public void analyse() {
//		List<UnitInfo> found = found();
//		if (!found.isEmpty() && found.size() >= 3) {
//			int threeZealotFrame = EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.time(UnitType.Protoss_Gateway, 20) + UnitType.Protoss_Zealot.buildTime() * 2;
//			int threeZealotInMyRegionFrame = threeZealotFrame + baseToBaseFrame(UnitType.Protoss_Zealot);
//			if (threeZealotFrame > ) {
//				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "3 zealots in enemy base");
//			} else if (euiCountBeforeWhere(zealotFoundInfo.euiList, threeZealotInMyRegionFrame, RegionType.ENEMY_FIRST_EXPANSION, RegionType.ETC, RegionType.MY_BASE) >= 3) {
//				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "3 zealots");
//			}
//		}
	}
}
