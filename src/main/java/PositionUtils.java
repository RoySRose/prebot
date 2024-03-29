

import java.util.List;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

/**
 * @author insaneojw
 *
 */
public class PositionUtils {

	/// 유효한 position이면 true
	public static boolean isValidPosition(Position position) {
		return position != Position.None && position != Position.Invalid && position != Position.Unknown && position.isValid();
	}
	
	public static boolean isAllValidPosition(Position position, Position...positions) {
		if (!isValidPosition(position)) {
			return false;
		}
		for (Position p : positions) {
			if (!isValidPosition(p)) {
				return false;
			}
		}
		return true;
	}

    /// 유효한 tileposition이면 true
    public static boolean isValidTilePosition(TilePosition tilePosition) {
        return tilePosition != TilePosition.None && tilePosition != TilePosition.Invalid && tilePosition != TilePosition.Unknown && tilePosition.isValid();
    }

	/// 유효한 지상 position이면 true
	public static boolean isValidGroundPosition(Position position) {
		if (!isValidPosition(position)) {
			return false;
		}
		return BWTA.getRegion(position) != null && MyBotModule.Broodwar.isWalkable(position.getX() / 8, position.getY() / 8);
	}

	/// unit이 이동하기에 유효한 position이면 true
	public static boolean isValidPositionToMove(Position position, Unit unit) {
		return unit.isFlying() ? PositionUtils.isValidPosition(position)
				: (PositionUtils.isValidGroundPosition(position) && PositionUtils.isValidGroundPath(unit.getPosition(), position));
	}

	/// 두 position 사이를 지상으로 이동할 수 있으면 true
	public static boolean isValidGroundPath(Position from, Position to) {
		return MyBotModule.Broodwar.hasPath(from, to) && isConnected(from, to);
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

	/// 중앙
	public static Position center(Position position1, Position position2) {
		return new Position((position1.getX() + position2.getX())/2, (position1.getY() + position2.getY())/2);
	}
		
	/// 두 position이 같은 region이면 true
	public static boolean isSameRegion(Position position1, Position position2) {
		return BWTA.getRegion(position1) == BWTA.getRegion(position2);
	}

	/// 두 position 사이의 거리를 리턴
	public static int getGroundDistance(Position origin, Position destination) {
		return MapTools.Instance().getGroundDistance(origin, destination);
	}

	public static CommonCode.RegionType positionToRegionType(Position position) {
		Region positionRegion = BWTA.getRegion(position);
		Region myBaseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		if (positionRegion == myBaseRegion) {
			return CommonCode.RegionType.MY_BASE;
		}
		Region myExpansionRegion = BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
		if (positionRegion == myExpansionRegion) {
			return CommonCode.RegionType.MY_FIRST_EXPANSION;
		}
		Region myThirdRegion = InfoUtils.myThirdRegion();
		if (positionRegion == myThirdRegion) {
			return CommonCode.RegionType.MY_THIRD_REGION;
		}
		if (InfoUtils.enemyBase() != null) {
			Region enemyBaseRegion = BWTA.getRegion(InfoUtils.enemyBase().getPosition());
			if (positionRegion == enemyBaseRegion) {
				return CommonCode.RegionType.ENEMY_BASE;
			}
			Region enemyExpansionRegion = BWTA.getRegion(InfoUtils.enemyFirstExpansion().getPosition());
			if (positionRegion == enemyExpansionRegion) {
				return CommonCode.RegionType.ENEMY_FIRST_EXPANSION;
			}
			Region enemyThirdRegion = InfoUtils.enemyThirdRegion();
			if (positionRegion == enemyThirdRegion) {
				return CommonCode.RegionType.ENEMY_THIRD_REGION;
			}
			return CommonCode.RegionType.ETC;
		}
		return CommonCode.RegionType.UNKNOWN;
	}

	public static Region regionTypeToRegion(CommonCode.RegionType regionType) {
		if (regionType == CommonCode.RegionType.MY_BASE) {
			return BWTA.getRegion(InfoUtils.myBase().getPosition());
		}
		if (regionType == CommonCode.RegionType.MY_FIRST_EXPANSION) {
			return BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
		}
		if (regionType == CommonCode.RegionType.MY_THIRD_REGION) {
			return InfoUtils.myThirdRegion();
		}
		
		if (InfoUtils.enemyBase() != null) {
			if (regionType == CommonCode.RegionType.ENEMY_BASE) {
				return BWTA.getRegion(InfoUtils.enemyBase().getPosition());
			}
			if (regionType == CommonCode.RegionType.ENEMY_FIRST_EXPANSION) {
				return BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
			}
			if (regionType == CommonCode.RegionType.ENEMY_THIRD_REGION) {
				return InfoUtils.enemyThirdRegion();
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
		return getClosestPositionToPosition(positionList, position, CommonCode.DOUBLE_MAX);
	}
	
	public static Position getClosestPositionToPosition(List<Position> positionList, Position position, double limitDistance) {
		if (positionList.size() == 0) {
			return null;
		}
		if (!PositionUtils.isValidPosition(position)) {
			return positionList.get(0);
		}

		Position closestPosition = null;
		double closestDist = limitDistance;

		for (Position p : positionList) {
			double dist = p.getDistance(position);
			if (closestPosition == null && dist < closestDist) {
				closestPosition = p;
				closestDist = dist;
			}
		}
		return closestPosition;
	}
	
	public static Position randomPosition(Position sourcePosition, int dist) {
		int x = sourcePosition.getX() + (int) (Math.random() * dist) - dist / 2;
		int y = sourcePosition.getY() + (int) (Math.random() * dist) - dist / 2;
		return new Position(x, y).makeValid();
	}

	public static Position positionAdjsuted(Position position, int x, int y) {
		if (!PositionUtils.isValidPosition(position)) {
			return Position.None;
		}
		return new Position(position.getX() + x, position.getY() + y).makeValid();
	}

	public static boolean equals(Position position1, Position position2) {
		if (position1 == null || position2 == null) {
			return false;
		}
		return position1.getX() == position2.getX() && position1.getY() == position2.getY();
	}
}
