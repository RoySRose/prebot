package pre.util;

import java.util.ArrayList;
import java.util.List;

import bwta.BaseLocation;

public class MapSpecificInformation {
	
	public enum MAP { LostTemple, FightingSpririts, TheHunters, Unknown };
	
	private MAP map;
	private List<BaseLocation> startingBaseLocation = new ArrayList<>();

	public MAP getMap() {
		return map;
	}
	public void setMap(MAP map) {
		this.map = map;
	}
	public List<BaseLocation> getStartingBaseLocation() {
		return startingBaseLocation;
	}
	public void setStartingBaseLocation(List<BaseLocation> startingBaseLocation) {
		this.startingBaseLocation = startingBaseLocation;
	}
	
	public boolean notUseReadyToAttackPosition() {
		return map == MAP.TheHunters;
	}

}
