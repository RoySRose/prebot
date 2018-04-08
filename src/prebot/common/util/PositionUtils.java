package prebot.common.util;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.Region;
import prebot.main.PreBot;

/**
 * @author insaneojw
 *
 */
public class PositionUtils {

	/// 유효한 position이면 true
	public static boolean isValidPosition(Position position) {
		return position != Position.None && position != Position.Invalid && position != Position.Unknown && position.isValid();
	}

	/// 유효한 지상 position이면 true
	public static boolean isValidGroundPosition(Position position) {
		if (!isValidPosition(position)) {
			return false;
		}
		return BWTA.getRegion(position) != null && PreBot.Broodwar.isWalkable(position.getX() / 8, position.getY() / 8);
	}

	/// unit이 이동하기에 유효한 position이면 true
	public static boolean isValidPositionToMove(Position position, Unit unit) {
		return unit.isFlying() ? PositionUtils.isValidPosition(position)
				: PositionUtils.isValidGroundPosition(position) && PositionUtils.isValidGroundPath(unit.getPosition(), position);
	}

	/// 두 position 사이를 지상으로 이동할 수 있으면 true
	public static boolean isValidGroundPath(Position from, Position to) {
		return PreBot.Broodwar.hasPath(from, to) && isConnected(from, to);
	}

	/// 두 position이 같은 region이거나, 가까운 choke에서 서로 연결된 region이면 true
	public static boolean isConnected(Position from, Position to) {
		Region regionSrc = BWTA.getRegion(from);
		Region regionDst = BWTA.getRegion(to);
		if (regionSrc == regionDst) {
			return true;
		} else {
			Pair<Region, Region> regions = BWTA.getNearestChokepoint(from).getRegions();
			return regions.first == regionSrc && regions.second == regionDst || regions.first == regionDst && regions.second == regionSrc;
		}
	}

	/// 두 position이 같은 region이면 true
	public static boolean isSameRegion(Position position1, Position position2) {
		return BWTA.getRegion(position1) == BWTA.getRegion(position2);
	}
}
