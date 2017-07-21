package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import pre.MapGrid;
import pre.combat.SpiderMineManger;
import pre.combat.SquadOrder.SquadOrderType;
import pre.combat.VultureTravelManager;
import pre.manager.InformationManager;
import pre.util.CommandUtil;
import pre.util.KitingOption;
import pre.util.MicroSet;
import pre.util.MicroSet.FleeAngle;
import pre.util.MicroUtils;
import pre.util.TargetPriority;

public class MicroVulture extends MicroManager {
	
	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> vultures = getUnits();
		List<Unit> vultureTargets = MicroUtils.filterTargets(targets, false);
		boolean saveCondition = MicroUtils.unitSaveCondition(vultures, vultureTargets); // TODO
		
		KitingOption kitingOption = new KitingOption();
		kitingOption.setCooltimeAlwaysAttack(false);
		kitingOption.setUnitedKiting(false);
		kitingOption.setFleeAngle(FleeAngle.WIDE_ANGLE);
		kitingOption.setGoalPosition(order.getPosition());
		kitingOption.setSaveThisUnit(saveCondition ? true : false); // 싸워야할때는 싸운다.
		kitingOption.setRetreatToBase(order.getType() == SquadOrderType.WATCH ? true : false); // 회피가 아주 좋은 설정(병력감시 - watcher : 회피를 자신의 base로 한다)

		for (Unit vulture : vultures) {
			
			// 마인매설이 예약된 벌처라면 매설실행
			Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
			if (positionToMine != null) {
				CommandUtil.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
				continue;
			}
			
			// checker 벌처들은 각각의 orderPosition을 가진다.
			if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					kitingOption.setGoalPosition(travelBase.getPosition());
				}
			}
			
			Unit target = getTarget(vulture, vultureTargets);
			if (target != null) {
				List<Unit> nearTanks = new ArrayList<>();
				List<Unit> nearGoliaths = new ArrayList<>();
				List<Unit> nearVultures = new ArrayList<>();
				List<Unit> units = MapGrid.Instance().getUnitsNear(vulture.getPosition(), MicroSet.Common.TANK_COVERAGE, true, false, null);
				for (Unit unit : units) {
					if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						nearTanks.add(unit);
					} else if (unit.getType() == UnitType.Terran_Goliath) {
						nearGoliaths.add(unit);
					} else if (unit.getType() == UnitType.Terran_Vulture) {
						nearVultures.add(unit);
					}
				}
				Position centerPosition = null;
				if (!nearTanks.isEmpty()) {
					centerPosition = MicroUtils.centerOfUnits(nearTanks);
				} else if (!nearGoliaths.isEmpty()) {
					centerPosition = MicroUtils.centerOfUnits(nearGoliaths);
				}
				
				// 탱크, 골리앗이 근처에 있으면 쿨타임이 돌아올때 무조건 때린다.
				// TODO 예외케이스 확인 필요 (초반 질럿 다수 등)
				if (centerPosition != null) {
					kitingOption.setCooltimeAlwaysAttack(true);
					kitingOption.setFleeAngle(FleeAngle.NARROW_ANGLE);
					kitingOption.setGoalPosition(centerPosition);
					kitingOption.setSaveThisUnit(false);
					kitingOption.setRetreatToBase(false);
				} else {
					// 자신이 차지한 지역에서는 적극적으로 싸워야 한다.
					Region region = BWTA.getRegion(vulture.getPosition());
					for (Region occupied : InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer)) {
						if (region == occupied) {
							kitingOption.setSaveThisUnit(false);
							kitingOption.setRetreatToBase(false);
							break;
						}
					}
				}
				MicroUtils.preciseKiting(vulture, target, kitingOption);
				
			} else {
				// 마인매설 위치 체크
				Position minePosition = SpiderMineManger.Instance().goodPositionToMine(vulture);
				if (order.getType() == SquadOrderType.WATCH) {
					if (minePosition == null) {
						BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
						if (BWTA.getRegion(base.getPosition()) != BWTA.getRegion(vulture.getPosition())) {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroSet.Vulture.spiderMineNumPerPosition);
						}
					}
				}
				if (minePosition == null) {
					if (vulture.getDistance(kitingOption.getGoalPosition()) > squadRange) {
						CommandUtil.attackMove(vulture, kitingOption.getGoalPosition());
						
					} else { // 목적지 도착
						if (vulture.isIdle() || vulture.isBraking()) {
							Position randomPosition = MicroUtils.randomPosition(vulture.getPosition(), squadRange / 2);
							CommandUtil.attackMove(vulture, randomPosition);
						}
					}
				}
			}
		}
	}
	
	private Unit getTarget(Unit rangedUnit, List<Unit> targets) {
		Unit bestTarget = null;
		int bestTargetScore = -999999;

		for (Unit target : targets) {
			if (!target.isDetected()) continue;
			
			int priorityScore = TargetPriority.getPriority(rangedUnit, target); // 우선순위 점수
			
			int distanceScore = 0; // 거리 점수
			int hitPointScore = 0; // HP 점수
			int dangerousScore = 0; // 위험한 새끼 점수
			
			if (rangedUnit.isInWeaponRange(target)) {
				distanceScore += 100;
			}
			
			int siegeMinRange = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().minRange();
			if (target.getType().groundWeapon().maxRange() <= siegeMinRange) {
				List<Unit> nearUnits = MapGrid.Instance().getUnitsNear(target.getPosition(), siegeMinRange, true, false, null);
				for (Unit unit : nearUnits) {
					if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						dangerousScore += 100; // 시즈에 붙어있는 적은 죽여야 한다.
					} else {
						dangerousScore += 10;
					}
				}
			}
			
			distanceScore -= rangedUnit.getDistance(target) / 5;
	        hitPointScore -= target.getHitPoints() / 10;
			
	        int totalScore = 0;
	        if (order.getType() == SquadOrderType.WATCH) {
	        	totalScore = distanceScore;
	        } else {
	        	totalScore = priorityScore + distanceScore + hitPointScore + dangerousScore;
	        }
	        
	        if (totalScore > bestTargetScore) {
				bestTargetScore = totalScore;
				bestTarget = target;
			}
		}
		
		return bestTarget;
	}
}
