package prebot.strategy.manage;

import java.util.HashSet;
import java.util.Set;

import bwapi.UnitType;
import prebot.strategy.analyse.Clue;

public class ClueManager {
	
	private static ClueManager instance = new ClueManager();
	
	public static ClueManager Instance() {
		return instance;
	}

	private Set<Clue.ClueInfo> clueInfoSet = new HashSet<>();
	
	public Set<Clue.ClueInfo> getClueInfoList() {
		return clueInfoSet;
	}
	
	public void addClueInfo(Clue.ClueInfo info) {
		if (containsClueInfo(info)) {
			return;
		}
		
		Clue.ClueInfo removeInfo = null;
		for (Clue.ClueInfo clueInfo : clueInfoSet) {
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
	
	public boolean containsClueType(Clue.ClueType type) {
		for (Clue.ClueInfo clueInfo : clueInfoSet) {
			if (clueInfo.type == type) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsClueInfo(Clue.ClueInfo info) {
		return clueInfoSet.contains(info);
	}
	
	public int baseToBaseFrame(UnitType unitType) {
		return 0;
	}
}
