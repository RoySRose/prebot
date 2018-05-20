package prebot.common.util;

import bwapi.TechType;
import bwapi.UpgradeType;
import prebot.common.main.Prebot;

public class UpgradeUtils {

	public static boolean isUpgraded(UpgradeType upgradeType) {
		return Prebot.Game.self().getUpgradeLevel(upgradeType) > 0;
	}
	
	public static boolean hasResearched(TechType techType) {
		return Prebot.Game.self().hasResearched(techType);
	}

	public static boolean isEnemyUpgraded(UpgradeType upgradeType) {
		return Prebot.Game.enemy().getUpgradeLevel(upgradeType) > 0;
	}

	public static boolean hasEnemyResearched(TechType techType) {
		return Prebot.Game.enemy().hasResearched(techType);
	}

}
