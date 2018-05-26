package prebot.strategy;

import java.util.ArrayList;
import java.util.List;

import bwta.BaseLocation;
import prebot.strategy.constant.StrategyCode.GameMap;

public class MapSpecificInformation {
	
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
	
	public boolean notUseReadyToAttackPosition() {
		return map == GameMap.THE_HUNTERS;
	}

}