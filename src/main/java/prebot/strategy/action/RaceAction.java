package prebot.strategy.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.debug.UxColor;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;

public abstract class RaceAction extends Action {
	
	protected enum LastCheckType {
		BASE, FIRST_EXPANSION, GAS
	}
	
	protected class FoundInfo {
		public UnitType unitType;
		public List<UnitInfo> euiList;
//		public int buildStartFrame;

		public FoundInfo(UnitType unitType, List<UnitInfo> euiList, int buildStartFrame) {
			this.unitType = unitType;
			this.euiList = euiList;
//			this.buildStartFrame = buildStartFrame;
		}
	}
	protected Map<Integer, Integer> buildStartFrameMap = new HashMap<>(); // TODO display 유닛ID별 빌드시작 시작(건물)
	protected int lastCheckFrameBase = 0;
	protected int lastCheckFrameGas = 0;
	protected int lastCheckFrameFirstExpansion = 0;

	private EnemyStrategy enemyStrategyExpect = EnemyStrategy.UNKNOWN; // TODO diplay
	private EnemyStrategy enemyStrategy = EnemyStrategy.UNKNOWN; // TODO diplay
	private List<UnitType> enemyUnitTypes = new ArrayList<>(); // TODO diplay
	private Map<UnitType, List<UnitInfo>> foundEuiMap = new HashMap<>(); // TODO display 유닛타입별 발견시점의 UnitInfo
	
	private Race race;
	private int phasEndSec;
	
	public Race getRace() {
		return race;
	}

	public RaceAction(Race race, int phasEndSec) {
		super();
		this.race = race;
		this.phasEndSec = phasEndSec;
	}
	
	public void setUnitTypes(UnitType... types) {
		for (UnitType unitType : types) {
			enemyUnitTypes.add(unitType);
		}
	}
	
	protected FoundInfo getFoundInfo(UnitType unitType, RegionType... positionRegion) {
		List<UnitInfo> euiList = foundEuiMap.get(unitType);
		if (euiList == null || euiList.isEmpty()) {
			return new FoundInfo(unitType, Collections.<UnitInfo>emptyList(), CommonCode.UNKNOWN);
		} else {
			int buildStartFrame = CommonCode.UNKNOWN;
			if (unitType.isBuilding()) {
				buildStartFrame = buildStartFrameMap.get(euiList.get(0).getUnitID());
			}
			if (positionRegion == null) {
				return new FoundInfo(unitType, euiList, buildStartFrame);
			} else {
				List<UnitInfo> filtered = new ArrayList<>();
				for (UnitInfo eui : euiList) {
					RegionType enemyRegionType = PositionUtils.positionToRegionType(eui.getLastPosition());
					for (RegionType regionType : positionRegion) {
						if (regionType == enemyRegionType) {
							filtered.add(eui); 
						}
					}
				}
				return new FoundInfo(unitType, filtered, buildStartFrame);
			}
		}
	}
	
	protected FoundInfo getFoundInfo(UnitType unitType) {
		List<UnitInfo> euiList = foundEuiMap.get(unitType);
		if (euiList == null || euiList.isEmpty()) {
			return new FoundInfo(unitType, Collections.<UnitInfo>emptyList(), CommonCode.UNKNOWN);
		} else {
			int buildStartFrame = CommonCode.UNKNOWN;
			if (unitType.isBuilding()) {
				buildStartFrame = buildStartFrameMap.get(euiList.get(0).getUnitID());
			}
			return new FoundInfo(unitType, euiList, buildStartFrame);
		}
	}
	
	// 발견 당시에 막 건설완료됐다고 가정 (최대한 늦게 건설되었다고 가정)
	protected int defaultBuildStartCompelteJustBefore(FoundInfo foundInfo) {
		if (foundInfo.euiList.isEmpty()) {
			return CommonCode.UNKNOWN;
		} else {
			return defaultBuildStartCompelteJustBefore(foundInfo.euiList.get(0));
		}
	}
	
	protected int defaultBuildStartCompelteJustBefore(UnitInfo eui) {
		Integer buildStartFrame = buildStartFrameMap.get(eui.getUnitID());
		if (buildStartFrame != null && buildStartFrame != CommonCode.UNKNOWN) {
			return buildStartFrame;
		} else {
			return eui.getUpdateFrame() - eui.getType().buildTime();
		}
	}
	
	// 마지막 정찰시간에 건설시작됐다고 가정 (최대한 빠르게 건설되었다고 가정)
	protected int defaultBuildStartLastCheck(FoundInfo foundInfo, LastCheckType lastCheckType) {
		if (foundInfo.euiList.isEmpty()) {
			return CommonCode.UNKNOWN;
		} else {
			return defaultBuildStartLastCheck(foundInfo.euiList.get(0), lastCheckType);
		}
	}
	
	protected int defaultBuildStartLastCheck(UnitInfo eui, LastCheckType lastCheckType) {
		Integer buildStartFrame = buildStartFrameMap.get(eui.getUnitID());
		if (buildStartFrame != null && buildStartFrame != CommonCode.UNKNOWN) {
			return buildStartFrame;
		} else {
			if (lastCheckType == LastCheckType.BASE) {
				return lastCheckFrameBase - eui.getType().buildTime();
			} else if (lastCheckType == LastCheckType.FIRST_EXPANSION) {
				return lastCheckFrameFirstExpansion - eui.getType().buildTime();
			} else if (lastCheckType == LastCheckType.GAS) {
				return lastCheckFrameGas - eui.getType().buildTime();
			} else {
				return CommonCode.UNKNOWN;
			}
		}
	}
	
	protected int euiCountBefore(List<UnitInfo> euiList, int beforeFrame) {
		return euiCountBeforeWhere(euiList, beforeFrame);
	}
	
	protected int euiCountBeforeWhere(List<UnitInfo> euiList, int beforeFrame, RegionType... regionTypes) {
		int count = 0;
		for (UnitInfo eui : euiList) {
			if (eui.getUpdateFrame() > beforeFrame) {
				continue;
			}
			
			if (regionTypes == null) {
				count++;
			} else {
				RegionType euiRegionType = PositionUtils.positionToRegionType(eui.getLastPosition());
				for (RegionType regionType : regionTypes) {
					if (euiRegionType == regionType) {
						count++;
						break;
					}
				}
			}
		}
		return count;
	}
	
	protected int baseToBaseFrame(UnitType unitType) {
		if (InfoUtils.enemyFirstExpansion() == null) {
			return 0;
		}
		
		// 대략적인 firstExpansion <-> myExpansion 사이에 unitType이 이동하는데 걸리는 시간 리턴 (단위 frame)
		
		
		return 0;
	}
	
	protected void setEnemyStrategyExpect(EnemyStrategy enemyStrategyExpect, String... messages) {
		if (this.enemyStrategyExpect != enemyStrategyExpect) {
			displayEnemyStrategy(enemyStrategy, UxColor.CHAR_YELLOW, messages);
			this.enemyStrategyExpect = enemyStrategyExpect;
		}
	}
	
	protected void setEnemyStrategy(EnemyStrategy enemyStrategy, String... messages) {
		displayEnemyStrategy(enemyStrategy, UxColor.CHAR_BROWN, messages);
		this.enemyStrategy = this.enemyStrategyExpect = enemyStrategy;
	}

	private void displayEnemyStrategy(EnemyStrategy enemyStrategy, char color, String... messages) {
		Prebot.Broodwar.printf("적 전략 : " + enemyStrategy.name());
		for (String message : messages) {
			Prebot.Broodwar.printf(" - " + message);	
		}
	}
	
	protected boolean unknownEnemyStrategy() {
		return enemyStrategy == EnemyStrategy.UNKNOWN;
	}

	@Override
	public boolean exitCondition() {
		// 전략이 파악되었거나, 일정시간이 지날 때까지 전략을 알지 못하였다.
		if (enemyStrategy != null || TimeUtils.after(phasEndSec)) {
			if (enemyStrategy != EnemyStrategy.UNKNOWN) {
				StrategyIdea.phase01 = enemyStrategy;
			} else {
				StrategyIdea.phase01 = enemyStrategyExpect;
			}
			return true;
		}
		return false;
	}

	@Override
	public void action() {
		updateMap();
		updateVisitFrame();
		
		analyse();
	}

	protected abstract void analyse();
	
	private void updateMap() {
		for (UnitType unitType : enemyUnitTypes) {
			List<UnitInfo> euiListVisible = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, unitType);
			if (euiListVisible.isEmpty()) {
				continue;
			}

			List<UnitInfo> euiListSaved = foundEuiMap.get(unitType);
			if (euiListSaved == null || euiListSaved.isEmpty()) {
				euiListSaved = new ArrayList<>();
			}

			for (UnitInfo euiVisible : euiListVisible) {
				boolean containUnit = false;
				for (UnitInfo euiSaved : euiListSaved) {
					if (euiVisible.getUnitID() == euiSaved.getUnitID()) {
						containUnit = true;
						break;
					}
				}
				if (!containUnit) {
					euiListSaved.add(euiVisible);
					if (euiVisible.getType().isBuilding()) {
						buildStartFrameMap.put(euiVisible.getUnitID(), TimeUtils.buildStartSeconds(euiVisible.getUnit())); // 빌드시작한 시잔 (초). 이미 완성되어 알 수 없다면 UNKNOWN

//						if (enemyGasFoundSec == CommonCode.UNKNOWN && unitType.isRefinery()) {
//							enemyGasFoundSec = TimeUtils.elapsedSeconds();
//							enemyGasBuildSec = TimeUtils.buildStartSeconds(euiVisible.getUnit());
//						}
					}
//					if (enemyGasFoundSec == CommonCode.UNKNOWN) {
//						BaseLocation enemyBase = InfoUtils.enemyBase();
//						if (enemyBase != null) {
//							List<Unit> geysers = enemyBase.getGeysers();
//							if (!geysers.isEmpty()) {
//								Unit enemyGas = geysers.get(0);
//								if (Prebot.Broodwar.isVisible(enemyGas.getTilePosition())) {
//									enemyGasNotFoundSec = TimeUtils.elapsedSeconds();
//								}
//							}
//						}
//					}
				}
			}
			foundEuiMap.put(unitType, euiListSaved);
		}
	}

	/// 유닛 발견 맵을 업데이트한다.
	private void updateVisitFrame() {
		TilePosition enemyBaseTile = null;
		TilePosition enemyGasTile = null;
		TilePosition enemyFirstExpansionTile = null;
		
		if (Prebot.Broodwar.isVisible(enemyBaseTile)) {
			lastCheckFrameBase = TimeUtils.elapsedFrames();
		}
		if (Prebot.Broodwar.isVisible(enemyGasTile)) {
			lastCheckFrameGas = TimeUtils.elapsedFrames();
		}
		if (Prebot.Broodwar.isVisible(enemyFirstExpansionTile)) {
			lastCheckFrameFirstExpansion = TimeUtils.elapsedFrames();
		}
	}
}
