package prebot.common.util;

import bwapi.Player;
import bwapi.Position;
import bwta.BaseLocation;
import prebot.brain.manager.InformationManager;

/**
 * @author insaneojw
 *
 */
public class PlayerUtils {

	/// player가 게임을 진행할 수 없으면 true를 반환
	public static boolean isDisabled(Player player) {
		return player == null || player.isDefeated() || player.leftGame();
	}

	/// first expansion을 점령했으면 expansion. 그렇지 않으면 base의 position을 리턴 
	public static Position getTargetPosition(Player player) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(player);
		if (firstExpansion != null && InformationManager.Instance().isOccupiedBaseLocation(firstExpansion, player)) {
			return firstExpansion.getPosition();
		}
		return getBasePosition(player);
	}

	/// base position 리턴
	public static Position getBasePosition(Player player) {
		BaseLocation mainBase = InformationManager.Instance().getMainBaseLocation(player);
		if (mainBase != null) {
			return mainBase.getPosition();
		}
		return null;
	}

	/// first expansion position 리턴
	public static Position getFirstExpansionPosition(Player player) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(player);
		if (firstExpansion != null) {
			return firstExpansion.getPosition();
		}
		return null;
	}
}
