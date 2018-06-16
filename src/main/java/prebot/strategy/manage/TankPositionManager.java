package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.PositionReserveInfo;
import prebot.micro.constant.MicroConfig.FleeAngle;

public class TankPositionManager {
	
	private static final int NARROW_WIDTH = 250;
	private static final int NEAR_BUILDING_DISTANCE = 100;
	private static final int NEAR_CHOKE_DISTANCE = 200;
	
	private static final int POSITION_EXPIRE_FRAME = 24 * 4;

	private static final int SIEGE_MODE_MAX_COUNT_IN_RADIUS = 5;
	private static final int SIEGE_ARRANGE_RADIUS = 80;
	private static final int SIEGE_ARRANGE_DISTANCE_FROM_CENTER = 150;
	private static final int SIEGE_ARRANGE_DISTANCE_FROM_CENTER_ADJUST = 50;
	
	private static final int SIEGE_ARRANGE_DISTANCE_FROM_CENTER_TERRAN = 30;
	private static final int SIEGE_ARRANGE_DISTANCE_FROM_CENTER_TERRAN_ADJUST = 20;
	
	private static TankPositionManager instance = new TankPositionManager();

	public static TankPositionManager Instance() {
		return instance;
	}

	private Map<Integer, PositionReserveInfo> siegeModeReservedMap = new HashMap<>(); // key : tank id

	public void update() {
		List<Integer> expiredList = new ArrayList<>();
		for (Integer tankId : siegeModeReservedMap.keySet()) {
			PositionReserveInfo mineReserved = siegeModeReservedMap.get(tankId);
			if (TimeUtils.elapsedFrames(mineReserved.reservedFrame) > POSITION_EXPIRE_FRAME) {
				expiredList.add(tankId);
			}
		}

		for (Integer tankId : expiredList) {
			siegeModeReservedMap.remove(tankId);
		}
	}
	
	public Position getSiegeModePosition(int unitId) {
		PositionReserveInfo positionReserveInfo = siegeModeReservedMap.get(unitId);
		if (positionReserveInfo != null) {
			return positionReserveInfo.position;
		} else {
			return null;
		}
	}

	public Position findPositionToSiegeAndReserve(Position centerPosition, Unit tank, int maxSpreadRadius) {
		if (!tank.canSiege()) {
			return null;
		}
		
		int seigeNumLimit = 1;
		
		Integer[] randomSortedAngles = new Integer[FleeAngle.EIGHT_360_ANGLE.length];
		System.arraycopy(FleeAngle.EIGHT_360_ANGLE, 0, randomSortedAngles, 0, FleeAngle.EIGHT_360_ANGLE.length);
		Arrays.sort(randomSortedAngles, new Comparator<Integer>() {
			Random random = new Random();
			@Override public int compare(Integer int1, Integer int2) {
				return random.nextInt(3) - 1;
			}
		});
		
		while (seigeNumLimit < SIEGE_MODE_MAX_COUNT_IN_RADIUS) {
			int distanceFromCenter = SIEGE_ARRANGE_DISTANCE_FROM_CENTER;
			while (distanceFromCenter != CommonCode.NONE) {
				for (Integer angle : randomSortedAngles) {
					double radianAdjust = MicroUtils.rotate(0.0, angle);
					int xVector = (int) (distanceFromCenter * Math.cos(radianAdjust));
					int yVector = (int) (distanceFromCenter * Math.sin(radianAdjust));
				    Position movePosition = new Position(centerPosition.getX() + xVector, centerPosition.getY() + yVector);
					if (PositionUtils.isValidGroundPosition(movePosition) && PositionUtils.isValidGroundPath(tank.getPosition(), movePosition)) {
				    	if (!isProperPositionToSiege(movePosition)) {
				    		continue;
				    	}
				    	
				    	List<Unit> siegeModeTanks = UnitUtils.getUnitsInRadius(PlayerRange.SELF, movePosition, SIEGE_ARRANGE_RADIUS, UnitType.Terran_Siege_Tank_Siege_Mode);
				    	int reservedCount = getPositionReserveCountInRadius(movePosition, SIEGE_ARRANGE_RADIUS);
				    	
						if (siegeModeTanks.size() + reservedCount < seigeNumLimit) {
							PositionReserveInfo reserveInfo = new PositionReserveInfo(tank.getID(), movePosition, TimeUtils.elapsedFrames());
							siegeModeReservedMap.put(tank.getID(), reserveInfo);
							return movePosition;
						}
				    }
				}
				distanceFromCenter = adjustDistance(distanceFromCenter, maxSpreadRadius);
			}
			seigeNumLimit++;
		}
		
		return null;
	}

	private int adjustDistance(int distanceFromCenter, int maxDistance) {
		int adjustedDistance = distanceFromCenter;
		if (distanceFromCenter <= SIEGE_ARRANGE_DISTANCE_FROM_CENTER) {
			adjustedDistance -= SIEGE_ARRANGE_DISTANCE_FROM_CENTER_ADJUST;
			if (adjustedDistance <= 0) {
				adjustedDistance = SIEGE_ARRANGE_DISTANCE_FROM_CENTER + SIEGE_ARRANGE_DISTANCE_FROM_CENTER_ADJUST;
			}
		} else {
			adjustedDistance += SIEGE_ARRANGE_DISTANCE_FROM_CENTER_ADJUST;
			if (adjustedDistance > maxDistance) {
				return CommonCode.NONE;
			}
		}
		return adjustedDistance;
	}
	
	private int getPositionReserveCountInRadius(Position position, int radius) {
		int count = 0;
		for (PositionReserveInfo positionReserveInfo : siegeModeReservedMap.values()) {
			Unit tank = Prebot.Broodwar.getUnit(positionReserveInfo.unitId);
			if (tank.isSieged()) {
				continue;
			}
			if (position.getDistance(positionReserveInfo.position) <= radius) {
				count++;
			}
		}
		return count;
	}

	public boolean isProperPositionToSiege(Position position) {
		Chokepoint nearestChoke = BWTA.getNearestChokepoint(position);
		if (nearestChoke.getWidth() < NARROW_WIDTH) {
			if (nearestChoke.getCenter().getDistance(position) < NEAR_CHOKE_DISTANCE) {
				return false;
			}
		}
		
		List<Unit> unitList = Prebot.Broodwar.getUnitsOnTile(position.toTilePosition());
    	for (Unit unit : unitList) {
    		if (unit.getType().isBuilding()) {
    			return false;
    		}
    	}
    	
		BaseLocation expansionBase = InfoUtils.myFirstExpansion();
		if (position.getDistance(expansionBase.getPosition()) < NEAR_BUILDING_DISTANCE) {
			return false;
		}
		
		Position leftPosition = new Position(position.getX() - 30, position.getY());
		List<Unit> nearUnitList = UnitUtils.getUnitsInRadius(PlayerRange.SELF, leftPosition, NEAR_BUILDING_DISTANCE);
    	for (Unit nearUnit : nearUnitList) {
    		if (nearUnit.getType().isBuilding() && nearUnit.getType().canBuildAddon()) {
				return false;
    		}
    	}
		
		return true;
	}

}
