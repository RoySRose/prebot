package prebot.common.util;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import bwapi.Position;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import prebot.common.main.Prebot;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation;
import prebot.strategy.UnitInfo;

public class InfoUtils {
	
	public static BaseLocation myBase() {
		return InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self());
	}
	
	public static BaseLocation enemyBase() {
		return InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.enemy());
	}
	
	public static Chokepoint myFirstChoke() {
		return InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self());
	}

	public static Chokepoint enemyFirstChoke() {
		return InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.enemy());
	}

	public static Chokepoint mySecondChoke() {
		return InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.self());
	}

	public static Chokepoint enemySecondChoke() {
		return InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.enemy());
	}

	public static BaseLocation myFirstExpansion() {
		return InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.self());
	}
	
	public static BaseLocation enemyFirstExpansion() {
		return InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.enemy());
	}
	
	public static List<BaseLocation> myOtherExpansionsSorted() {
		return InformationManager.Instance().getOtherExpansionLocations(Prebot.Broodwar.self());
	}
	
	public static List<BaseLocation> enemyOtherExpansionsSorted() {
		return InformationManager.Instance().getOtherExpansionLocations(Prebot.Broodwar.enemy());
	}
	
	public static List<BaseLocation> myOccupiedBases() {
		return InformationManager.Instance().getOccupiedBaseLocations(Prebot.Broodwar.self());
	}
	
	public static List<BaseLocation> enemyOccupiedBases() {
		return InformationManager.Instance().getOccupiedBaseLocations(Prebot.Broodwar.enemy());
	}
	
	public static Map<Integer, UnitInfo> enemyUnitInfoMap() {
		return InformationManager.Instance().getUnitAndUnitInfoMap(Prebot.Broodwar.enemy());
	}
	
	public static MapSpecificInformation mapInformation() {
		return InformationManager.Instance().getMapSpecificInformation();
	}
	
	public static Vector<Position> getRegionVertices(BaseLocation base) {
		return InformationManager.Instance().getBaseRegionVerticesMap(base);
	}
	
	public static List<UnitInfo> euiListInMyRegion(Region myRegion) {
		return InformationManager.Instance().getEuiListInMyRegion(myRegion);
	}
}
