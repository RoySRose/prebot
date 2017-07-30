

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;

public class MechanicMicroVulture extends MechanicMicroAbstract {

	private SquadOrder order;
	private List<UnitInfo> enemiesInfo;
	private List<Unit> notVultureUnitList;
	
	public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList) {
		this.notVultureUnitList = new ArrayList<>();
		this.notVultureUnitList.addAll(tankList);
		this.notVultureUnitList.addAll(goliathList);
	}
	
	public void executeMechanicMicro(Unit vulture) {
		if (!CommonUtils.executeUnitRotation(vulture, 5)) {
			return;
		}
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(vulture, enemiesInfo); // 0: flee, 1: kiting, 2: attack

		KitingOption kOpt = KitingOption.vultureKitingOption();
		Position retreatPosition = order.getPosition();
		switch (decision.getDecision()) {
		case 0: // flee
			kOpt.setHaveToFlee(true);
			if (order.getType() == SquadOrderType.WATCH || order.getType() == SquadOrderType.CHECK) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			}
			kOpt.setGoalPosition(retreatPosition);
			MicroUtils.preciseKiting(vulture, decision.getTargetInfo(), kOpt);
			break;
			
		case 1: // kiting
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			if (order.getType() == SquadOrderType.WATCH) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			} else if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					retreatPosition = travelBase.getPosition();
				}
			}
			kOpt.setGoalPosition(retreatPosition);
			for (Unit notVultureUnit : notVultureUnitList) {
				if (vulture.getDistance(notVultureUnit) < MicroSet.Common.TANK_COVERAGE) {
					kOpt.setCooltimeAlwaysAttack(true);
					kOpt.setUnitedKiting(true);
					kOpt.setGoalPosition(notVultureUnit.getPosition());
					kOpt.setFleeAngle(MicroSet.FleeAngle.NARROW_ANGLE);
					kOpt.setHaveToFlee(false);
					break;
				}
			}
			MicroUtils.preciseKiting(vulture, decision.getTargetInfo(), kOpt);
			break;
			
		case 2: // attack move
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			
			// checker : 각각의 목표지역(travelBase)으로 이동. (order position은 null이다.)
			// watcher : 목표지역(적base)으로 이동. 앞에 보이지 않는 적이 있으면 본진base로 후퇴.
			Position movePosition = order.getPosition();
			if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					movePosition = travelBase.getPosition();
				}
			}
			
			// 이동지역까지 attackMove로 간다.
			if (vulture.getDistance(movePosition) > order.getRadius()) {
				CommandUtil.attackMove(vulture, movePosition);
				
			} else { // 목적지 도착
				if (vulture.isIdle() || vulture.isBraking()) {
					Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), 100);
					CommandUtil.attackMove(vulture, randomPosition);
				}
			}
			break;
		}
	}
	
	private boolean useReservedSpiderMine(Unit vulture) {
		// 마인매설이 예약된 벌처라면 매설실행
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine != null) {
			CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
			return true;
		}
		return false;
	}
	
	private boolean reserveSpiderMine(Unit vulture) {
		Position minePosition = SpiderMineManger.Instance().goodPositionToMine(vulture, MicroSet.Vulture.spiderMineNumPerPosition);
		if (minePosition == null && order.getType() == SquadOrderType.WATCH) {
			BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (BWTA.getRegion(base.getPosition()) != BWTA.getRegion(vulture.getPosition())) {
				minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerPosition);
			}
		}
		if (minePosition != null) { // 매설할 마인이 있다면 종료
			return true;
		}
		return false;
	}
	
	private boolean removeSpiderMine(Unit vulture) {
		return SpiderMineManger.Instance().removeMine(vulture);
	}

}
