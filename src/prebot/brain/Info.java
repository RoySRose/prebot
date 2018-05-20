package prebot.brain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Position;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import prebot.brain.information.UnitData;
import prebot.brain.manager.InformationManager;
import prebot.common.code.Code.GameMap;

public class Info {
	public static Info of() {
		return InformationManager.Instance().getInfo();
	}
	
	public GameMap gameMap = GameMap.UNKNOWN;
	public BaseLocation myBase;
	public BaseLocation enemyBase;
	public List<BaseLocation> myOccupiedBases;
	public List<BaseLocation> enemyOccupiedBases;
	public Set<Region> myOccupiedRegions;
	public Set<Region> enemyOccupiedRegions;
	public Chokepoint myFirstChoke;
	public Chokepoint enemyFirstChoke;
	public Chokepoint mySecondChoke;
	public Chokepoint enemySecondChoke;
	public BaseLocation myFirstExpansionBase;
	public BaseLocation enemyFirstExpansionBase;
	public UnitData myUnitData;
	public UnitData enemyUnitData;
	
	public Map<Position, Vector<Position>> baseRegionVerticesMap = new HashMap<>();
}
