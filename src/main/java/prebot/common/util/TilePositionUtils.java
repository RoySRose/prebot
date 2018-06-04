package prebot.common.util;

import bwapi.TilePosition;
import prebot.common.main.Prebot;

public class TilePositionUtils {

	/// 건설 가능한 tilePosition이면 true 리턴
	public static boolean isBuildable(TilePosition tilePosition, boolean includeBuilding) {
		return tilePosition.isValid() && Prebot.Broodwar.isBuildable(tilePosition, includeBuilding);
	}


	// 팩토리, 스타포트 등 건물의 애드온이 건설가능하면 true 리턴 (TODO 검증된 로직인가?)
	public static boolean addOnBuildable(TilePosition tilePosition) {
		return TilePositionUtils.isBuildable(new TilePosition(tilePosition.getX() + 4, tilePosition.getY() + 1), true)
				&& TilePositionUtils.isBuildable(new TilePosition(tilePosition.getX() + 5, tilePosition.getY() + 1), true)
				&& TilePositionUtils.isBuildable(new TilePosition(tilePosition.getX() + 4, tilePosition.getY() + 2), true)
				&& TilePositionUtils.isBuildable(new TilePosition(tilePosition.getX() + 5, tilePosition.getY() + 2), true);
	}
	
	public static TilePosition getCenterTilePosition() {
		return new TilePosition(64, 64);
	}
}