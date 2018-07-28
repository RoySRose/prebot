package prebot.strategy;

import java.util.ArrayList;
import java.util.List;

import bwta.BaseLocation;

public class MapSpecificInformation {

	public static enum GameMap {
		UNKNOWN, CIRCUITBREAKER, OVERWATCH,
		FIGHTING_SPIRITS, LOST_TEMPLE, THE_HUNTERS
	}

	private GameMap map;
	private List<BaseLocation> startingBaseLocation = new ArrayList<>();

	public GameMap getMap() {
		return map;
	}

	public void setMap(GameMap map) {
		this.map = map;
	}

	public List<BaseLocation> getStartingBaseLocation() {
		return startingBaseLocation;
	}

	public void setStartingBaseLocation(List<BaseLocation> startingBaseLocation) {
		this.startingBaseLocation = startingBaseLocation;
	}
}