package prebot.strategy.constant;

import bwapi.TechType;
import bwapi.UnitType;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class TimeMapForTerran {
	
	// PHASE1
	public static BuildTimeMap TERRAN_MECHANIC() {
		return new BuildTimeMap().setFeature(Feature.MECHANIC)
		.put(UnitType.Terran_Supply_Depot, 0, 55)
		.put(UnitType.Terran_Barracks, 1, 40)
		.put(UnitType.Terran_Refinery, 1, 45)
		.put(UnitType.Terran_Factory, 2, 40);
	}
	
	public static BuildTimeMap TERRAN_BBS() {
		return new BuildTimeMap().setFeature(Feature.BIONIC)
		.put(UnitType.Terran_Barracks, 1, 00)
		.put(UnitType.Terran_Barracks, 1, 25)
		.put(UnitType.Terran_Supply_Depot, 1, 45);
	}
	
	public static BuildTimeMap TERRAN_2BARRACKS() {
		return new BuildTimeMap().setFeature(Feature.BIONIC)
		.put(UnitType.Terran_Supply_Depot, 0, 55)
		.put(UnitType.Terran_Barracks, 1, 30)
		.put(UnitType.Terran_Barracks, 2, 0)
		.put(UnitType.Terran_Refinery, 2, 45);
	}
	
	public static BuildTimeMap TERRAN_1BARRACKS_DOUBLE() {
		return new BuildTimeMap().setFeature(Feature.DOUBLE, Feature.MECHANIC)
		.put(UnitType.Terran_Supply_Depot, 0, 55)
		.put(UnitType.Terran_Barracks, 1, 30)
		.put(UnitType.Terran_Command_Center, 2, 30)
		.put(UnitType.Terran_Refinery, 2, 50)
		.put(UnitType.Terran_Factory, 3, 40);
	}
	
	public static BuildTimeMap NO_BARRACKS_DOUBLE() {
		return new BuildTimeMap().setFeature(Feature.DOUBLE, Feature.MECHANIC)
		.put(UnitType.Terran_Supply_Depot, 0, 55)
		.put(UnitType.Terran_Command_Center, 2, 10)
		.put(UnitType.Terran_Barracks, 2, 20)
		.put(UnitType.Terran_Refinery, 2, 35)
		.put(UnitType.Terran_Factory, 3, 30);
	}
	
	// PHASE2
	public static BuildTimeMap TERRAN_1FAC_DOUBLE() {
		return TERRAN_MECHANIC()
		.put(UnitType.Terran_Command_Center, 3, 40);
	}
	
	public static BuildTimeMap TERRAN_1FAC_DOUBLE_1STAR() {
		return TERRAN_MECHANIC().setFeature(Feature.DETECT_IMPORTANT)
		.put(UnitType.Terran_Command_Center, 3, 40)
		.put(TechType.Tank_Siege_Mode, 4, 10)
		.put(UnitType.Terran_Starport, 4, 30);
	}
	
	public static BuildTimeMap TERRAN_1FAC_DOUBLE_ARMORY() {
		return TERRAN_MECHANIC()
		.put(UnitType.Terran_Command_Center, 3, 40)
		.put(UnitType.Terran_Armory, 4, 30)
		.put(UnitType.Terran_Academy, 5, 30);
	}
	
	public static BuildTimeMap TERRAN_2FAC() {
		return TERRAN_MECHANIC()
		.put(UnitType.Terran_Factory, 4, 0)
		.put(UnitType.Terran_Vulture_Spider_Mine, 4, 10);
	}
	
	public static BuildTimeMap TERRAN_1FAC_1STAR() {
		return TERRAN_MECHANIC().setFeature(Feature.DETECT_IMPORTANT)
		.put(UnitType.Terran_Starport, 3, 30);
	}
	
	public static BuildTimeMap TERRAN_2STAR() {
		return TERRAN_MECHANIC().setFeature(Feature.DETECT_IMPORTANT)
		.put(UnitType.Terran_Starport, 3, 30)
		.put(UnitType.Terran_Starport, 3, 30);
	}
	
	public static BuildTimeMap TERRAN_BIONIC() {
		return TERRAN_2BARRACKS().setFeature(Feature.BIONIC)
		  .put(UnitType.Terran_Academy, 3, 0)
		  .put(TechType.Stim_Packs, 3, 50);
	}
	
	public static BuildTimeMap TERRAN_2BARRACKS_1FAC() {
		return TERRAN_2BARRACKS().setFeature(Feature.BIONIC)
		  .put(UnitType.Terran_Factory, 3, 35);
	}
	
}
