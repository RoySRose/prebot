package prebot.common.util;

import java.util.List;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.PositionRegion;
import prebot.common.main.Prebot;
import prebot.common.util.internal.MapTools;

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
		return BWTA.getRegion(position) != null && Prebot.Broodwar.isWalkable(position.getX() / 8, position.getY() / 8);
	}

	/// unit이 이동하기에 유효한 position이면 true
	public static boolean isValidPositionToMove(Position position, Unit unit) {
		return unit.isFlying() ? PositionUtils.isValidPosition(position)
				: PositionUtils.isValidGroundPosition(position) && PositionUtils.isValidGroundPath(unit.getPosition(), position);
	}

	/// 두 position 사이를 지상으로 이동할 수 있으면 true
	public static boolean isValidGroundPath(Position from, Position to) {
		return Prebot.Broodwar.hasPath(from, to) && isConnected(from, to);
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

	/// 두 position 사이의 거리를 리턴
	public static int getGroundDistance(Position origin, Position destination) {
		return MapTools.Instance().getGroundDistance(origin, destination);
	}

	public static PositionRegion positionToPositionRegion(Position position) {
		Region positionRegion = BWTA.getRegion(position);
		Region myBaseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		if (positionRegion == myBaseRegion) {
			return PositionRegion.MY_BASE;
		}
		Region myExpansionRegion = BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
		if (positionRegion == myExpansionRegion) {
			return PositionRegion.MY_FIRST_EXPANSION;
		}
		if (InfoUtils.enemyBase() != null) {
			Region enemyBaseRegion = BWTA.getRegion(InfoUtils.enemyBase().getPosition());
			if (positionRegion == enemyBaseRegion) {
				return PositionRegion.ENEMY_BASE;
			}
			Region enemyExpansionRegion = BWTA.getRegion(InfoUtils.enemyFirstExpansion().getPosition());
			if (positionRegion == enemyExpansionRegion) {
				return PositionRegion.ENEMY_FIRST_EXPANSION;
			}
			return PositionRegion.ETC;
		}
		return PositionRegion.UNKNOWN;
	}

	public static Region positionRegionToRegion(PositionRegion positionRegion) {
		if (positionRegion == PositionRegion.MY_BASE) {
			return BWTA.getRegion(InfoUtils.myBase().getPosition());
		}
		if (positionRegion == PositionRegion.MY_FIRST_EXPANSION) {
			return BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
		}
		
		if (InfoUtils.enemyBase() != null) {
			if (positionRegion == PositionRegion.ENEMY_BASE) {
				return BWTA.getRegion(InfoUtils.enemyBase().getPosition());
			}
			if (positionRegion == PositionRegion.ENEMY_FIRST_EXPANSION) {
				return BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
			}
		}
		return null;
	}
	
	public static boolean isStartingPosition(Position position) {
		for (BaseLocation startingBase : BWTA.getStartLocations()) {
			if (startingBase.getPosition().equals(position)) {
				return true;
			}
		}
		return false;
	}
	
	/** positionList 중 position에 가장 가까운 position 리턴 */
	public static Position getClosestPositionToPosition(List<Position> positionList, Position position) {
		if (positionList.size() == 0) {
			return null;
		}
		if (!PositionUtils.isValidPosition(position)) {
			return positionList.get(0);
		}

		Position closestPosition = null;
		double closestDist = CommonCode.DOUBLE_MAX;

		for (Position p : positionList) {
			double dist = p.getDistance(position);
			if (closestPosition == null || dist < closestDist) {
				closestPosition = p;
				closestDist = dist;
			}
		}
		return closestPosition;
	}
	
	public static Position randomPosition(Position sourcePosition, int dist) {
		int x = sourcePosition.getX() + (int) (Math.random() * dist) - dist / 2;
		int y = sourcePosition.getY() + (int) (Math.random() * dist) - dist / 2;
		return new Position(x, y);
	}
}
