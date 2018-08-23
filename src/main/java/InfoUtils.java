

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

public class InfoUtils {
	
	public static Race myRace() {
		return InformationManager.Instance().selfRace;
	}
	
	public static Race enemyRace() {
		return InformationManager.Instance().enemyRace;
	}
	
	public static BaseLocation myBase() {
		return InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
	}
	
	public static BaseLocation enemyBase() {
		return InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
	}
	
	public static Chokepoint myFirstChoke() {
		return InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self());
	}

	public static Chokepoint enemyFirstChoke() {
		return InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.enemy());
	}

	public static Chokepoint mySecondChoke() {
		return InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
	}

	public static Chokepoint enemySecondChoke() {
		return InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.enemy());
	}

	public static BaseLocation myFirstExpansion() {
		return InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
	}
	
	public static BaseLocation enemyFirstExpansion() {
		return InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy());
	}

	public static Position myReadyToPosition() {
		return InformationManager.Instance().getReadyToAttackPosition(MyBotModule.Broodwar.self());
	}

	public static Position enemyReadyToPosition() {
		return InformationManager.Instance().getReadyToAttackPosition(MyBotModule.Broodwar.enemy());
	}
	
	public static List<BaseLocation> enemyOtherExpansions() {
		return InformationManager.Instance().getOtherExpansionLocations();
	}
	
	public static List<BaseLocation> myOccupiedBases() {
		return InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.self());
	}
	
	public static List<BaseLocation> enemyOccupiedBases() {
		return InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.enemy());
	}
	
	public static boolean enemyFirstExpansionOccupied() {
		Boolean enemyFirstExpansionOccupied = SpecificValueCache.get(SpecificValueCache.ValueType.ENEMY_FIRST_EXPANSION_OCCUPIED, Boolean.class);
		if (enemyFirstExpansionOccupied != null) {
			return enemyFirstExpansionOccupied;
		}
		enemyFirstExpansionOccupied = Boolean.FALSE;
		BaseLocation enemyFirstExpansion = enemyFirstExpansion();
		if (enemyFirstExpansion != null) {
			for (BaseLocation base : InfoUtils.enemyOccupiedBases()) {
				if (base.equals(enemyFirstExpansion)) {
					enemyFirstExpansionOccupied = Boolean.TRUE;
					break;
				}
			}
		}
		SpecificValueCache.put(SpecificValueCache.ValueType.ENEMY_FIRST_EXPANSION_OCCUPIED, enemyFirstExpansionOccupied);
		return enemyFirstExpansionOccupied;
	}
	
	public static Unit myBaseGas() {
		if (myBase() != null) {
			List<Unit> geysers = myBase().getGeysers();
			if (geysers != null && !geysers.isEmpty()) {
				return geysers.get(0);
			}
		}
		return null;
	}
	
	public static Unit enemyBaseGas() {
		if (enemyBase() != null) {
			List<Unit> geysers = enemyBase().getGeysers();
			if (geysers != null && !geysers.isEmpty()) {
				return geysers.get(0);
			}
		}
		return null;
	}

	public static int myNumUnits(UnitType... unitTypes) {
		int numUnits = 0;
		for (UnitType unitType : unitTypes) {
			numUnits += InformationManager.Instance().getNumUnits(unitType, MyBotModule.Broodwar.self());
		}
		return numUnits;
	}
	
	public static int enemyNumUnits(UnitType... unitTypes) {
		int numUnits = 0;
		for (UnitType unitType : unitTypes) {
			numUnits += InformationManager.Instance().getNumUnits(unitType, MyBotModule.Broodwar.enemy());
		}
		return numUnits;
	}
	
	public static int myDeadNumUnits(UnitType... unitTypes) {
		int numUnits = 0;
		for (UnitType unitType : unitTypes) {
			numUnits += MyBotModule.Broodwar.self().deadUnitCount(unitType);
		}
		return numUnits;
	}
	
	public static int enemyDeadNumUnits(UnitType... unitTypes) {
		int numUnits = 0;
		for (UnitType unitType : unitTypes) {
			numUnits += MyBotModule.Broodwar.enemy().deadUnitCount(unitType);
		}
		return numUnits;
	}
	
	public static Map<Integer, UnitInfo> enemyUnitInfoMap() {
		return InformationManager.Instance().getUnitAndUnitInfoMap(MyBotModule.Broodwar.enemy());
	}
	
	public static MapSpecificInformation mapInformation() {
		return InformationManager.Instance().getMapSpecificInformation();
	}
	
	public static Vector<Position> getRegionVertices(BaseLocation base) {
		return InformationManager.Instance().getBaseRegionVerticesMap(base);
	}
	
	public static List<UnitInfo> euiListInMyRegion(Region myRegion) {
		List<UnitInfo> euiListInMyRegion = InformationManager.Instance().getEuiListInMyRegion(myRegion);
		if (euiListInMyRegion == null) {
			euiListInMyRegion = new ArrayList<>();
		}
		return euiListInMyRegion;
	}
	
	public static Set<UnitInfo> euiListInBase() {
		return InformationManager.Instance().getEuisInBaseRegion();
	}

	public static Set<UnitInfo> euiListInExpansion() {
		return InformationManager.Instance().getEuisInExpansionRegion();
	}
	
	public static Set<UnitInfo> euiListInThirdRegion() {
		return InformationManager.Instance().getEuisInThirdRegion();
	}
	
	public static Region myThirdRegion() {
		return InformationManager.Instance().getThirdRegion(MyBotModule.Broodwar.self());
	}
	
	public static Region enemyThirdRegion() {
		return InformationManager.Instance().getThirdRegion(MyBotModule.Broodwar.enemy());
	}
	
	
	public static int squadUnitSize(MicroConfig.SquadInfo squadInfo) {
		Squad squad = CombatManager.Instance().squadData.getSquad(squadInfo.squadName);
		if (squad != null && squad.unitList != null) {
			return squad.unitList.size();
		} else {
			return 0;
		}
	}
}
