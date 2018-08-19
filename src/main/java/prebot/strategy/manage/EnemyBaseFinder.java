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
import prebot.common.util.TilePositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class EnemyBaseFinder {

	private boolean finished = false;
	
	private static EnemyBaseFinder instance = new EnemyBaseFinder();

	public static EnemyBaseFinder Instance() {
		return instance;
	}

	public void update() {
		if (finished) {
			return;
		}
		
		if (InfoUtils.enemyBase() != null) {
			StrategyIdea.enemyBaseExpected = null;
			finished = true;
			return;
		}
		if (StrategyIdea.enemyBaseExpected != null && Prebot.Broodwar.isExplored(StrategyIdea.enemyBaseExpected.getTilePosition())) {
			StrategyIdea.enemyBaseExpected = null;
		}
		if (StrategyIdea.enemyBaseExpected != null) {
			return;
		}
		
		BaseLocation expectedByBuilding = expectedByBuilding();
		if (expectedByBuilding != null) {
			StrategyIdea.enemyBaseExpected = expectedByBuilding;
			return;
		}
		
		BaseLocation expectedByScout = expectedByUnit();
		if (expectedByScout != null) {
			StrategyIdea.enemyBaseExpected = expectedByScout;
		}
	}

	private BaseLocation expectedByBuilding() {
		BaseLocation baseExpected = null;
		List<UnitInfo> enemyUnits = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL);
		
		Position centerPosition = TilePositionUtils.getCenterTilePosition().toPosition();
		for (UnitInfo eui : enemyUnits) {
			if (!eui.getType().isBuilding()) {
				continue;
			}
			
			int minimumDistance = 999999;
			for (BaseLocation startLocation : BWTA.getStartLocations()) {
				if (startLocation.getTilePosition().equals(InfoUtils.myBase().getTilePosition())) {
					continue;
				}
				if (Prebot.Broodwar.isExplored(startLocation.getTilePosition())) {
					continue;
				}
				if (eui.getLastPosition().getDistance(centerPosition) < 500) {
					continue;
				}

				int dist = PositionUtils.getGroundDistance(eui.getLastPosition(), startLocation.getPosition());
				if (dist < minimumDistance) {
					baseExpected = startLocation;
					minimumDistance = dist;
				}
			}
		}
		return baseExpected;
	}

	private BaseLocation expectedByUnit() {
		int scoutLimitFrames;
		if (InfoUtils.enemyRace() == Race.Protoss) {
			scoutLimitFrames = TimeUtils.timeToFrames(1, 25); // 9파일런 서치 한번에 왔을때 약 1분 20초
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			scoutLimitFrames = TimeUtils.timeToFrames(2, 35); // 9드론 서치 한번에 왔을때 약 2분 30초
		} else {
			scoutLimitFrames = TimeUtils.timeToFrames(2, 0);
		}
		
		if (TimeUtils.after(scoutLimitFrames)) {
			return null;
		}
		
		List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.ALL
				, UnitType.Protoss_Probe, UnitType.Zerg_Drone, UnitType.Terran_SCV, UnitType.Zerg_Overlord
				, UnitType.Zerg_Zergling, UnitType.Protoss_Zealot, UnitType.Terran_Marine);
		
		if (euiList.isEmpty()) {
			return null;
		}

		Position scoutPosition = euiList.get(0).getLastPosition();
		CommonCode.RegionType regionType = PositionUtils.positionToRegionType(scoutPosition);

		
		if (regionType != RegionType.MY_BASE && regionType != RegionType.MY_FIRST_EXPANSION) {
//			Prebot.Broodwar.sendText("hi");
			return null;
		}

		Position fromPosition = InfoUtils.myBase().getPosition();
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

}
