package prebot.strategy.manage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;

public class ClueManager {
	
	// lair, hydraden, spire, core, adun, templar arch, robotics, robo support, factory, starport 
	public Map<UnitType, Integer> buildTimeExpectMap = new HashMap<>();
	
	private static ClueManager instance = new ClueManager();
	
	public static ClueManager Instance() {
		return instance;
	}

	private Set<ClueInfo> clueInfoSet = new HashSet<>();
	
	public Set<ClueInfo> getClueInfoList() {
		return clueInfoSet;
	}
	
	public void addClueInfo(ClueInfo info) {
		if (containsClueInfo(info)) {
			return;
		}
		
		ClueInfo removeInfo = null;
		for (ClueInfo clueInfo : clueInfoSet) {
			if (clueInfo.type == info.type) {
				removeInfo = clueInfo;
				break;
			}
		}
		if (removeInfo != null) {
			clueInfoSet.remove(removeInfo);
		}
		clueInfoSet.add(info);
	}
	
	public boolean containsClueType(ClueType type) {
		for (ClueInfo clueInfo : clueInfoSet) {
			if (clueInfo.type == type) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsClueInfo(ClueInfo info) {
		return clueInfoSet.contains(info);
	}
	
	public int getExpectBuildFrame(UnitType buildingType) {
		Integer expectFrame = buildTimeExpectMap.get(buildingType);
		return expectFrame == null ? CommonCode.UNKNOWN : expectFrame;
	}
	
	public void updateExpectBuildFrame(UnitType buildingType, int frame) {
		buildTimeExpectMap.put(buildingType, frame);
	}
	
	public void updateExpectBuildIfBigger(UnitType buildingType, int frame) {
		int expectFrame = getExpectBuildFrame(buildingType);
		if (expectFrame == CommonCode.UNKNOWN || frame > expectFrame) {
			updateExpectBuildFrame(buildingType, frame);
		}
	}
	
	public int baseToBaseFrame(UnitType unitType) {
		return 0;
	}
}
