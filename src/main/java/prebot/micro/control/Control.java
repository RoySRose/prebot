package prebot.micro.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;
import prebot.common.LagObserver;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TilePositionUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder;

public abstract class Control {
	
	private Map<Integer, Integer> unitOutOfRegionMap = new HashMap<>();
	
	// TODO 추후 모든 컨트롤 적용 필요
	public void controlIfUnitExist(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (!unitList.isEmpty()) {
			control(unitList, euiList);
		}
	}

	public void controlIfUnitMoreThanTwo(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (unitList.size() > 1) {
			control(unitList, euiList);
		}
	}
		
	public abstract void control(Collection<Unit> unitList, Collection<UnitInfo> euiList);
	
	protected boolean skipControl(Unit unit) {
		return !TimeUtils.executeUnitRotation(unit, LagObserver.groupsize());
	}
	
	public boolean findRat(Collection<Unit> unitList) {
		Position centerPosition = TilePositionUtils.getCenterTilePosition().toPosition();
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}

			if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode && unit.canUnsiege()) {
				CommandUtils.unsiege(unit);
			} else {
				if (unit.isIdle()) {
					Position randomPosition = PositionUtils.randomPosition(centerPosition, 5000);
					if (unit.isFlying() || PositionUtils.isValidGroundPosition(randomPosition)) {
						CommandUtils.attackMove(unit, randomPosition);
					}
				}
			}
		}
		return false;
	}
	
	/// 수비시 campType에 따라 나가지 말아야 할 경계를 넘으면 되될아온다.
	protected boolean dangerousOutOfMyRegion(Unit unit) {
		if (LagObserver.groupsize() > 20) {
			return false;
		}
		
		// 베이스 지역 OK
		Region unitRegion = BWTA.getRegion(unit.getPosition());
		Region baseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		if (unitRegion == baseRegion) {
			return false;
		}
		PositionFinder.CampType campType = StrategyIdea.campType;
		if (campType == PositionFinder.CampType.INSIDE || campType == PositionFinder.CampType.FIRST_CHOKE) {
			return outOfRegionLongTime(unit);
		}
		
		// 앞마당 지역, 또는 앞마당 반경이내 OK
		Position expansionPosition = InfoUtils.myFirstExpansion().getPosition();
		Region expansionRegion = BWTA.getRegion(expansionPosition);
		if (unitRegion == expansionRegion) {
			return false;
		} else if (unit.getDistance(expansionPosition) < 150) {
			return false;
		}
		if (campType == PositionFinder.CampType.EXPANSION) {
			return outOfRegionLongTime(unit);
		}
		// 세번째 지역까지 OK
		if (unitRegion == InfoUtils.myThirdRegion()) {
			return false;
		}
		if (campType == PositionFinder.CampType.SECOND_CHOKE) {
			return outOfRegionLongTime(unit);
		}
		// 세번째 지역 반경 OK
		if (unit.getDistance(InfoUtils.myThirdRegion()) < 600) {
			return false;
		}
		if (unit.getDistance(InfoUtils.myReadyToPosition()) < 300) {
			return false;
		}

		return outOfRegionLongTime(unit);
	}

	private boolean outOfRegionLongTime(Unit unit) {
		Integer outFrame = unitOutOfRegionMap.get(unit.getID());
		if (outFrame == null || TimeUtils.elapsedFrames(outFrame) > 10 * TimeUtils.SECOND) { // 이력이없거나 오래된 정보는 새로만든다.
			unitOutOfRegionMap.put(unit.getID(), TimeUtils.elapsedFrames());
			return false;
		} else {
			return TimeUtils.elapsedFrames(outFrame) > 2 * TimeUtils.SECOND;
		}
	}
}
