

import bwapi.Player;
import bwapi.UnitType;

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
			return MyBotModule.Broodwar.self();
		} else if (playerRange == CommonCode.PlayerRange.ENEMY) {
			return MyBotModule.Broodwar.enemy();
		} else if (playerRange == CommonCode.PlayerRange.NEUTRAL) {
			return MyBotModule.Broodwar.neutral();
		} else {
			return null;
		}
	}
	
	public static boolean enoughResource(UnitType unitType) {
		return enoughResource(unitType.mineralPrice(), unitType.gasPrice());
	}
	
	public static boolean enoughResource(int mineralNeed, int gasNeed) {
		return mineralNeed <= MyBotModule.Broodwar.self().minerals() &&  gasNeed <= MyBotModule.Broodwar.self().gas();
	}
	
}
