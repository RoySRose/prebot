package prebot.strategy.analyse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;

public abstract class UnitAnalyser {
	
	private List<UnitInfo> foundAll = new ArrayList<>();
	private List<UnitInfo> foundFoundInEnemyBase = new ArrayList<>();
	private List<UnitInfo> foundInEnemyExpansion = new ArrayList<>();
	private List<UnitInfo> foundInMyBase = new ArrayList<>();
	private List<UnitInfo> foundInMyExpansion = new ArrayList<>();
	private List<UnitInfo> foundEtc = new ArrayList<>();
	
	private Map<Integer, Integer> buildStartFrameMap = new HashMap<>();
	
	private UnitType unitType;
	
	public UnitAnalyser(UnitType unitType) {
		this.unitType = unitType;
	}
	
	public abstract void analyse();
	
	public void upateFoundInfo() {
		List<UnitInfo> units = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, unitType);
		if (units.isEmpty()) {
			return;
		}
		
		for (UnitInfo unit : units) {
			boolean containUnit = false;
			for (UnitInfo found : foundAll) {
				if (unit.getUnitID() == found.getUnitID()) {
					containUnit = true;
					break;
				}
			}
			if (!containUnit) {
				RegionType regionType = PositionUtils.positionToRegionType(unit.getLastPosition());
				if (regionType == RegionType.UNKNOWN) {
					continue;
				} else if (regionType == RegionType.ENEMY_BASE) {
					foundFoundInEnemyBase.add(unit);
				} else if (regionType == RegionType.ENEMY_FIRST_EXPANSION) {
					foundInEnemyExpansion.add(unit);
				} else if (regionType == RegionType.MY_BASE) {
					foundInMyBase.add(unit);
				} else if (regionType == RegionType.MY_FIRST_EXPANSION) {
					foundInMyExpansion.add(unit);
				} else {
					foundEtc.add(unit);
				}
				foundAll.add(unit);
				
				 // 빌드시작한 시잔 (초). 이미 완성되어 알 수 없다면 UNKNOWN
				buildStartFrameMap.put(unit.getUnitID(), TimeUtils.buildStartFrames(unit.getUnit()));
			}
		}
	}
	
	protected List<UnitInfo> found(RegionType... regionTypes) {
		if (regionTypes == null || regionTypes.length == 0) {
			return foundAll;
		}
		List<UnitInfo> foundList = new ArrayList<>();
		for (RegionType regionType : regionTypes) {
			if (regionType == RegionType.ENEMY_BASE) {
				foundList.addAll(foundFoundInEnemyBase);
			} else if (regionType == RegionType.ENEMY_FIRST_EXPANSION) {
				foundList.addAll(foundInEnemyExpansion);
			} else if (regionType == RegionType.MY_BASE) {
				foundList.addAll(foundInMyBase);
			} else if (regionType == RegionType.MY_FIRST_EXPANSION) {
				foundList.addAll(foundInMyExpansion);
			} else {
				foundList.addAll(foundEtc);
			}
		}
		return foundList;
	}
	
	protected int buildStartFrame(UnitInfo eui) {
		return buildStartFrameMap.get(eui.getUnitID());
	}
	
	protected int buildStartFrameDefaultJustBefore(UnitInfo eui) {
		int buildStartFrame = buildStartFrame(eui);
		if (buildStartFrame == CommonCode.UNKNOWN) {
			return eui.getUpdateFrame() - eui.getType().buildTime();
		} else {
			return buildStartFrame;
		}
	}
	
	protected int baseToBaseFrame(UnitType unitType) {
		return 0;
	}

}
