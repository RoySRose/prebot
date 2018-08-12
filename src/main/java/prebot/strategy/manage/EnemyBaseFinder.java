package prebot.strategy.manage;

import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
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
		if (StrategyIdea.enemyBaseExpected != null && Prebot.Broodwar.isExplored(StrategyIdea.enemyBaseExpected.getTilePosition())) {
			StrategyIdea.enemyBaseExpected = null;
		}
		if (StrategyIdea.enemyBaseExpected != null) {
			return;
		}
		
		BaseLocation baseExpected = expectedByBuilding();

		if (baseExpected != null) {
			StrategyIdea.enemyBaseExpected = baseExpected;
		} else {
			int scoutLimitFrames;
			if (InfoUtils.enemyRace() == Race.Protoss) {
				scoutLimitFrames = TimeUtils.timeToFrames(1, 40);
			} else {
				scoutLimitFrames = TimeUtils.timeToFrames(2, 0);
			}
			
			if (TimeUtils.before(scoutLimitFrames)) {
				BaseLocation expectedByScout = expectedByUnit();
				if (expectedByScout != null) {
					StrategyIdea.enemyBaseExpected = expectedByScout;
				}
			}
			
		}
	}

	private BaseLocation expectedByUnit() {
		List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL
				, UnitType.Protoss_Probe, UnitType.Zerg_Drone, UnitType.Terran_SCV, UnitType.Zerg_Overlord
				, UnitType.Zerg_Zergling, UnitType.Protoss_Zealot, UnitType.Terran_Marine);
		
		if (euiList.isEmpty()) {
			return null;
		}

		Position scoutPosition = euiList.get(0).getLastPosition();
		RegionType regionType = PositionUtils.positionToRegionType(scoutPosition);

		Position fromPosition;
		if (regionType == RegionType.MY_BASE || regionType == RegionType.MY_FIRST_EXPANSION) {
			fromPosition = InfoUtils.myBase().getPosition();
		} else {
			fromPosition = euiList.get(0).getLastPosition();
//			Prebot.Broodwar.sendText("hi");
		}

		BaseLocation closestBase = null;
		int minimumDistance = CommonCode.INT_MAX;
		BaseLocation myBase = InfoUtils.myBase();
		for (BaseLocation startLocation : BWTA.getStartLocations()) {
			if (startLocation.getTilePosition().equals(myBase.getTilePosition())) {
				continue;
			}
			if (Prebot.Broodwar.isExplored(startLocation.getTilePosition())) {
				continue;
			}

			int groundDistance = PositionUtils.getGroundDistance(startLocation.getPosition(), fromPosition);
			if (groundDistance < minimumDistance) {
				closestBase = startLocation;
				minimumDistance = groundDistance;
			}
		}
		return closestBase;
	}

	private BaseLocation expectedByBuilding() {
		BaseLocation baseExpected = null;
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
						baseExpected = startLocation;
						minimumDistance = dist;
					}
				}
			}
		}
		return baseExpected;
	}

}
