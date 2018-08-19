package prebot.common.util;

import bwapi.Player;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;

/**
 * @author insaneojw
 *
 */
public class PlayerUtils {

	/// player가 게임을 진행할 수 없으면 true를 반환
	public static boolean isDisabled(Player player) {
		return player == null || player.isDefeated() || player.leftGame();
	}
	
	public static Player getPlayerByRange(CommonCode.PlayerRange playerRange) {
		if (playerRange == CommonCode.PlayerRange.SELF) {
			return Prebot.Broodwar.self();
		} else if (playerRange == CommonCode.PlayerRange.ENEMY) {
			return Prebot.Broodwar.enemy();
		} else if (playerRange == CommonCode.PlayerRange.NEUTRAL) {
			return Prebot.Broodwar.neutral();
		} else {
			return null;
		}
	}
	
	public static boolean enoughResource(UnitType unitType) {
		return enoughResource(unitType.mineralPrice(), unitType.gasPrice());
	}
	
	public static boolean enoughResource(int mineralNeed, int gasNeed) {
		return mineralNeed <= Prebot.Broodwar.self().minerals() &&  gasNeed <= Prebot.Broodwar.self().gas();
	}
	
}
