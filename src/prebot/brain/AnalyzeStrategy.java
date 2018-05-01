package prebot.brain;

import bwapi.Race;
import prebot.main.Prebot;

public class AnalyzeStrategy {
	
	/// 종족별 전략을 분석한다.
	public static void analyze() {
		
		Race enemyRace = Prebot.Game.enemy().getRace();
		
		if (enemyRace == Race.Terran) {
			analyzeTerranStrategy();
			
		} else if (enemyRace == Race.Protoss) {
			analyzeProtossStrategy();
			
		} else if (enemyRace == Race.Zerg) {
			analyzeZergStrategy();
			
		} else {
			analyzeUnknownRaceStrategy();
		}
		
	}

	private static void analyzeTerranStrategy() {
		// TODO Auto-generated method stub
		
	}

	private static void analyzeProtossStrategy() {
		// TODO Auto-generated method stub
		
	}

	private static void analyzeZergStrategy() {
		// TODO Auto-generated method stub
		
	}

	private static void analyzeUnknownRaceStrategy() {
		// TODO Auto-generated method stub
		
	}

}
