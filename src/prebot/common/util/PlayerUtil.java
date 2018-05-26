package prebot.common.util;

import bwapi.Player;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;

/**
 * @author insaneojw
 *
 */
public class PlayerUtil {

	/// player가 게임을 진행할 수 없으면 true를 반환
	public static boolean isDisabled(Player player) {
		return player == null || player.isDefeated() || player.leftGame();
	}
	
	public static Player getPlayerByRange(PlayerRange playerRange) {
		if (playerRange == PlayerRange.SELF) {
			return Prebot.Broodwar.self();
		} else if (playerRange == PlayerRange.ENEMY) {
			return Prebot.Broodwar.enemy();
		} else if (playerRange == PlayerRange.NEUTRAL) {
			return Prebot.Broodwar.neutral();
		} else {
			return null;
		}
	}
	
}
