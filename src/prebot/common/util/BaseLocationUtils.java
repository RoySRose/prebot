package prebot.common.util;

import java.util.List;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.common.code.Code.CommonCode;
import prebot.common.util.internal.IConditions.BaseCondition;
import prebot.main.Prebot;

public class BaseLocationUtils {

	public static BaseLocation getClosestBaseToPosition(List<BaseLocation> baseList, Position position) {
		return getClosestBaseToPosition(baseList, position, new BaseCondition() {
			@Override
			public boolean correspond(BaseLocation base) {
				return true;
			}
		});
	}
	
	/** baseList 중 position에 가장 가까운 base 리턴 */
	public static BaseLocation getClosestBaseToPosition(List<BaseLocation> baseList, Position position, BaseCondition baseCondition) {
		if (baseList.size() == 0) {
			return null;
		}
		if (!PositionUtils.isValidPosition(position)) {
			return baseList.get(0);
		}

		BaseLocation closestBase = null;
		double closestDist = CommonCode.DOUBLE_MAX;

		for (BaseLocation base : baseList) {
			if (!baseCondition.correspond(base)) {
				continue;
			}
			double dist = base.getPosition().getDistance(position);
			if (closestBase == null || dist < closestDist) {
				closestBase = base;
				closestDist = dist;
			}
		}
		return closestBase;
	}
	
	public static BaseLocation getGroundClosestBaseToPositionNotExplored(List<BaseLocation> baseList, BaseLocation fromBase) {
		return getGroundClosestBaseToPosition(baseList, fromBase, new BaseCondition() {
			@Override
			public boolean correspond(BaseLocation base) {
				return !Prebot.Game.isExplored(base.getTilePosition());
			}
		});
	}
	
	/** baseList 중 position에 가장 가까운 base 리턴 */
	public static BaseLocation getGroundClosestBaseToPosition(List<BaseLocation> baseList, BaseLocation fromBase, BaseCondition baseCondition) {
		if (baseList.size() == 0) {
			return null;
		}

		BaseLocation closestBase = null;
		double closestDist = CommonCode.DOUBLE_MAX;

		for (BaseLocation base : baseList) {
			if (!baseCondition.correspond(base)) {
				continue;
			}
			if (base.equals(fromBase)) {
				closestBase = base;
				break;
			}
			double dist = (double) (base.getGroundDistance(fromBase)  + 0.5);
			if (closestBase == null || dist < closestDist) {
				closestBase = base;
				closestDist = dist;
			}
		}
		return closestBase;
	}
}
