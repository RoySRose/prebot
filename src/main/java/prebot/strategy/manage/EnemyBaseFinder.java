package prebot.strategy.manage;

import java.util.List;

import bwta.BWTA;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class EnemyBaseFinder {

	private static EnemyBaseFinder instance = new EnemyBaseFinder();

	public static EnemyBaseFinder Instance() {
		return instance;
	}
	
	public void update() {
		// 끝까지 상대 location 못 찾았을때
		if (InfoUtils.enemyBase() != null) {
			StrategyIdea.enemyBaseExpected = null;
			return;
		}

		BaseLocation closestBase = null;
		List<UnitInfo> enemyUnits = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL);
		for (UnitInfo eui : enemyUnits) {

			if (eui.getType().isBuilding()) {
				int minimumDistance = 999999;
				for (BaseLocation startLocation : BWTA.getStartLocations()) {
					if (startLocation.getTilePosition().equals(InfoUtils.myBase().getTilePosition())) {
						continue;
					}
					if (Prebot.Broodwar.isExplored(startLocation.getTilePosition())) {
						continue;
					}

					int dist = PositionUtils.getGroundDistance(eui.getLastPosition(), startLocation.getPosition());
					if (dist < minimumDistance) {
						closestBase = startLocation;
						minimumDistance = dist;
					}
				}
			}

//			if (closestBase == null) {
//				int minimumDistance = 999999;
//				for (BaseLocation startLocation : BWTA.getStartLocations()) {
//					if (startLocation.getTilePosition().equals(InfoUtils.myBase().getTilePosition()))
//						continue;
//					if (Prebot.Broodwar.isExplored(startLocation.getTilePosition())) {
//						continue;
//					}
//
//					int dist = PositionUtils.getGroundDistance(eui.getLastPosition(), startLocation.getPosition());
//					if (dist < minimumDistance) {
//						closestBase = startLocation;
//						minimumDistance = dist;
//					}
//				}
//			}
		}

		StrategyIdea.enemyBaseExpected = closestBase;
	}

}
