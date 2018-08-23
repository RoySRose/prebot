

import bwapi.TechType;
import bwapi.UpgradeType;

public class UpgradeUtils {

	public static boolean isUpgraded(UpgradeType upgradeType) {
		return MyBotModule.Broodwar.self().getUpgradeLevel(upgradeType) > 0;
	}
	
	public static boolean hasResearched(TechType techType) {
		return MyBotModule.Broodwar.self().hasResearched(techType);
	}

	public static boolean isEnemyUpgraded(UpgradeType upgradeType) {
		return MyBotModule.Broodwar.enemy().getUpgradeLevel(upgradeType) > 0;
	}

	public static boolean hasEnemyResearched(TechType techType) {
		return MyBotModule.Broodwar.enemy().hasResearched(techType);
	}

}
