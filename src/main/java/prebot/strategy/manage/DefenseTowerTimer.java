package prebot.strategy.manage;

import bwapi.Race;
import prebot.strategy.InformationManager;

public class DefenseTowerTimer {
	
	private static DefenseTowerTimer instance = new DefenseTowerTimer();

	public static DefenseTowerTimer Instance() {
		return instance;
	}
	
	public void update() {
		if (InformationManager.Instance().enemyRace == Race.Protoss) {
			
		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {
			
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
			
		} else {
			
		}
	}
	
}
