package prebot.common.util;

import bwapi.UpgradeType;
import prebot.main.Prebot;

public class UpgradeUtils {

	public static boolean isUpgraded(UpgradeType upgradeType) {
		return Prebot.Game.self().getUpgradeLevel(upgradeType) > 0;
	}

	public static boolean isEnemyUpgraded(UpgradeType upgradeType) {
		return Prebot.Game.enemy().getUpgradeLevel(upgradeType) > 0;
	}

}
