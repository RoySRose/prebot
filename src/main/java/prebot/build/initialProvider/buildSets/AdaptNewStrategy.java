package prebot.build.initialProvider.buildSets;


import javax.sound.midi.MidiDevice.Info;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation.GameMap;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/// 봇 프로그램 설정
public class AdaptNewStrategy extends BaseBuild{


    public AdaptNewStrategy(TilePosition firstSupplyPos, TilePosition barrackPos, TilePosition secondSupplyPos, TilePosition factoryPos, TilePosition bunkerPos, ExpansionOption strategy) {
  	
    	if(strategy == ExpansionOption.TWO_STARPORT) {
    		
    		if(InformationManager.Instance().enemyRace == Race.Zerg) {
    		
	        	if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.UNKNOWN) {
	        		queueBuild(true, UnitType.Terran_Supply_Depot, secondSupplyPos);
	        		queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	    	    	queueBuild(true, UnitType.Terran_Vulture);
	    	    	queueBuild(true, UnitType.Terran_Starport);
	    	    	queueBuild(true, UnitType.Terran_Starport);
	    	    	
	        	}else {
	        		
	        		queueBuild(true, UnitType.Terran_Supply_Depot);
	        		queueBuild(true, UnitType.Terran_SCV, UnitType.Terran_SCV, UnitType.Terran_SCV);
	    	    	queueBuild(true, UnitType.Terran_Vulture);
	    	    	queueBuild(true, UnitType.Terran_Starport);
	    	    	queueBuild(true, UnitType.Terran_Starport);
	
	        	}
    		}
    	}else if(strategy == ExpansionOption.TWO_FACTORY) {
    		if(InformationManager.Instance().enemyRace == Race.Zerg) {
	    		if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.UNKNOWN) {
	    	
	    			queueBuild(true,UnitType.Terran_Factory, factoryPos);
	    	    	
	        	}else {
	        		
	        		queueBuild(true,UnitType.Terran_Factory);
	
	        	}
    		}
    	}
    }
}
