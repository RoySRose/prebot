

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.UnitType;

public abstract class UnitAnalyser {
	
	private List<UnitInfo> foundAll = new ArrayList<>();
	private List<UnitInfo> foundFoundInEnemyBase = new ArrayList<>();
	private List<UnitInfo> foundInEnemyExpansion = new ArrayList<>();
	private List<UnitInfo> foundInMyBase = new ArrayList<>();
	private List<UnitInfo> foundInMyExpansion = new ArrayList<>();
	private List<UnitInfo> foundEtc = new ArrayList<>();
	private List<UnitInfo> foundUnknown = new ArrayList<>();
	
	private Map<Integer, Integer> buildStartFrameMap = new HashMap<>();
	
	private UnitType unitType;
	
	public UnitAnalyser(UnitType unitType) {
		this.unitType = unitType;
	}
	
	public abstract void analyse();
	
	public void upateFoundInfo() {
		if (!foundUnknown.isEmpty() && InfoUtils.enemyBase() != null) {
			for (UnitInfo found : foundUnknown) {
				CommonCode.RegionType regionType = PositionUtils.positionToRegionType(found.getLastPosition());
				if (regionType == CommonCode.RegionType.ENEMY_BASE) {
					foundFoundInEnemyBase.add(found);
				} else if (regionType == CommonCode.RegionType.ENEMY_FIRST_EXPANSION || regionType == CommonCode.RegionType.ENEMY_THIRD_REGION) {
					foundInEnemyExpansion.add(found);
				} else if (regionType == CommonCode.RegionType.MY_BASE) {
					foundInMyBase.add(found);
				} else if (regionType == CommonCode.RegionType.MY_FIRST_EXPANSION) {
					foundInMyExpansion.add(found);
				} else if (regionType == CommonCode.RegionType.ETC) {
					foundEtc.add(found);
				}
			}
			foundUnknown.clear();
		}
		
		
		List<UnitInfo> unitInfos = UnitUtils.getEnemyUnitInfoList(CommonCode.EnemyUnitFindRange.VISIBLE, unitType);
		if (unitInfos.isEmpty()) {
			return;
		}
		
		for (UnitInfo unitInfo : unitInfos) {
			boolean containUnit = false;
			for (UnitInfo found : foundAll) {
				if (unitInfo.getUnitID() == found.getUnitID()) {
					containUnit = true;
					break;
				}
			}
			if (!containUnit) {
				CommonCode.RegionType regionType = PositionUtils.positionToRegionType(unitInfo.getLastPosition());
				if (regionType == CommonCode.RegionType.ENEMY_BASE) {
					foundFoundInEnemyBase.add(unitInfo);
				} else if (regionType == CommonCode.RegionType.ENEMY_FIRST_EXPANSION) {
					foundInEnemyExpansion.add(unitInfo);
				} else if (regionType == CommonCode.RegionType.MY_BASE) {
					foundInMyBase.add(unitInfo);
				} else if (regionType == CommonCode.RegionType.MY_FIRST_EXPANSION) {
					foundInMyExpansion.add(unitInfo);
				} else if (regionType == CommonCode.RegionType.ETC) {
					foundEtc.add(unitInfo);
				} else {
					foundUnknown.add(unitInfo);
				}
				foundAll.add(unitInfo);
				
				 // 빌드시작한 시잔 (초). 이미 완성되어 알 수 없다면 UNKNOWN
				buildStartFrameMap.put(unitInfo.getUnitID(), TimeUtils.buildStartFrames(unitInfo.getUnit()));
			}
		}
	}

	protected List<UnitInfo> found(CommonCode.RegionType... regionTypes) {
		if (regionTypes == null || regionTypes.length == 0) {
			return foundAll;
		}
		List<UnitInfo> foundList = new ArrayList<>();
		for (CommonCode.RegionType regionType : regionTypes) {
			if (regionType == CommonCode.RegionType.ENEMY_BASE) {
				foundList.addAll(foundFoundInEnemyBase);
			} else if (regionType == CommonCode.RegionType.ENEMY_FIRST_EXPANSION) {
				foundList.addAll(foundInEnemyExpansion);
			} else if (regionType == CommonCode.RegionType.MY_BASE) {
				foundList.addAll(foundInMyBase);
			} else if (regionType == CommonCode.RegionType.MY_FIRST_EXPANSION) {
				foundList.addAll(foundInMyExpansion);
			} else if (regionType == CommonCode.RegionType.ETC) {
				foundList.addAll(foundEtc);
			} else {
				foundList.addAll(foundUnknown);
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
		return InformationManager.Instance().baseToBaseFrame(unitType);
	}
	
	protected int lastUnitFoundFrame(List<UnitInfo> euiList, int lastCount) {
		if (euiList.size() < lastCount) {
			lastCount = euiList.size();
		}
		int maxFrame = 0;
		for (int i = 0; i < lastCount; i++) {
			UnitInfo unitInfo = euiList.get(i);
			if (unitInfo.getUpdateFrame() > maxFrame) {
				maxFrame = unitInfo.getUpdateFrame();
			}
		}
		return maxFrame;
	}

}
