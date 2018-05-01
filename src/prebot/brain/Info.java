package prebot.brain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Player;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import prebot.information.UnitData;
import prebot.main.manager.InformationManager;

public class Info {
	public static Info of() {
		return InformationManager.Instance().getInfo();
	}
	
	public Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();
	public Map<Player, Boolean> mainBaseLocationChanged = new HashMap<Player, Boolean>();
	public Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();
	public Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();
	public Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	public Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	public Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();
	public Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();
	public Map<BaseLocation, Vector<Position>> baseRegionVerticesMap = new HashMap<>();
}
