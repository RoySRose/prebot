package prebot.common.util;

import bwapi.UpgradeType;
import prebot.main.PreBot;

public class UpgradeUtils {

	public static boolean isUpgraded(UpgradeType upgradeType) {
		return PreBot.Broodwar.self().getUpgradeLevel(upgradeType) > 0;
	}

	public static boolean isEnemyUpgraded(UpgradeType upgradeType) {
		return PreBot.Broodwar.enemy().getUpgradeLevel(upgradeType) > 0;
	}

}
