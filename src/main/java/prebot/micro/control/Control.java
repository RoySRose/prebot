package prebot.micro.control;

import java.util.Collection;

import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.Region;
import prebot.common.LagObserver;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder.CampType;

public abstract class Control {
	
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
	
	/// 수비시 campType에 따라 나가지 말아야 할 경계를 넘으면 되될아온다.
	protected boolean dangerousOutOfMyRegion(Unit unit) {
		if (LagObserver.groupsize() > 20) {
			return false;
		}
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			return false;
		}
		
		// 베이스 지역 OK
		Region unitRegion = BWTA.getRegion(unit.getPosition());
		Region baseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		if (unitRegion == baseRegion) {
			return false;
		}
		CampType campType = StrategyIdea.campType;
		if (campType == CampType.INSIDE || campType == CampType.FIRST_CHOKE) {
			return true;
		}
		
		// 앞마당 지역, 또는 앞마당 반경이내 OK
		Position expansionPosition = InfoUtils.myFirstExpansion().getPosition();
		Region expansionRegion = BWTA.getRegion(expansionPosition);
		if (unitRegion == expansionRegion) {
			return false;
		} else if (unit.getDistance(expansionPosition) < 100) {
			return false;
		}
		if (campType == CampType.EXPANSION) {
			return true;
		}
		
		// 세번째 지역까지 OK
		if (unitRegion == InfoUtils.myThirdRegion()) {
			return false;
		}
		if (campType == CampType.SECOND_CHOKE) {
			return true;
		}
		
		// 세번째 지역 반경 OK
		if (unit.getDistance(InfoUtils.myThirdRegion()) < 500) {
			return false;
		}

		return true;
	}
}
