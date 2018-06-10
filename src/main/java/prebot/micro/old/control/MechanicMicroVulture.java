package prebot.micro.old.control;


import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.common.LagObserver;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.micro.constant.MicroCode.CombatStrategyDetail;
import prebot.micro.constant.MicroCode.SquadOrderType;
import prebot.micro.constant.MicroConfig;
import prebot.micro.old.OldCombatManager;
import prebot.micro.old.OldKitingOption;
import prebot.micro.old.OldMicroUtils;
import prebot.micro.old.OldSquadOrder;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.SpiderMineManger;
import prebot.strategy.manage.VultureTravelManager;
import prebot.strategy.manage.SpiderMineManger.MinePositionLevel;

public class MechanicMicroVulture extends MechanicMicroAbstract {

	private OldSquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> notVultureUnitList = new ArrayList<>();
	private int saveUnitLevel = 1;
	
	private boolean attackWithMechanics = false;
	private int stickToMechanicRadius = 0;
	
	public void prepareMechanic(OldSquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = OldMicroUtils.filterTargetInfos(enemiesInfo, false);
	}
	
	public void prepareMechanicAdditional(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel, boolean attackWithMechanics) {
		this.notVultureUnitList.clear();
		this.notVultureUnitList.addAll(tankList);
		this.notVultureUnitList.addAll(goliathList);
		this.saveUnitLevel = saveUnitLevel;
		this.attackWithMechanics = attackWithMechanics && notVultureUnitList.size() > 0;
		if (this.attackWithMechanics) {
			this.stickToMechanicRadius = 120 + (int) (Math.log(vultureList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToMechanicRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit vulture) {
		if (!TimeUtils.executeUnitRotation(vulture, LagObserver.groupsize())) {
			return;
		}
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(vulture, enemiesInfo, null, saveUnitLevel); // 0: flee, 1: kiting, 2: attack

		OldKitingOption kOpt = OldKitingOption.vultureKitingOption();
		Position retreatPosition = order.getPosition();
		switch (decision.getDecision()) {
		case 0: // flee 아예 싸울 생각이 없는 도망 : 성큰, 캐논, 시즈 등 접근금지, 또는 regroup 시
			if (order.getType() == SquadOrderType.WATCH || order.getType() == SquadOrderType.CHECK) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			}
			kOpt.setGoalPosition(retreatPosition);
			OldMicroUtils.preciseFlee(vulture, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			if (spiderMineOrderIssue(vulture)) {
				break;
			}
			if (order.getType() == SquadOrderType.WATCH) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			} else if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getCheckerTravelSite(vulture.getID());
				if (travelBase != null) {
					retreatPosition = travelBase.getPosition();
				}
			}

			boolean haveToFight = true;
			Unit closeMechanic = null;
			if (attackWithMechanics) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit mechanicUnit : notVultureUnitList) {
					int dist = vulture.getDistance(mechanicUnit.getPosition());
					if (dist < closeDist) {
						closeMechanic = mechanicUnit;
						closeDist = dist;
						// 가까운 곳에 메카닉유닛이 있으면 싸운다.
						if (closeDist < stickToMechanicRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			} else {
				for (Unit notVultureUnit : notVultureUnitList) {
					if (vulture.getDistance(notVultureUnit) < MicroConfig.Common.MAIN_SQUAD_COVERAGE) {
						kOpt = OldKitingOption.defaultKitingOption();
						retreatPosition = notVultureUnit.getPosition();
						break;
					}
				}
			}

			if (haveToFight) {
				Unit enemy = OldMicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && vulture.isInWeaponRange(enemy)) {
					vulture.holdPosition();
				} else {
					kOpt.setGoalPosition(retreatPosition);
					OldMicroUtils.preciseKiting(vulture, decision.getTargetInfo(), kOpt);
				}
			} else {
				CommandUtils.move(vulture, closeMechanic.getPosition());
			}
			break;
			
		case 2: // attack move
			if (spiderMineOrderIssue(vulture)) {
				break;
			}
			
			// checker : 각각의 목표지역(travelBase)으로 이동. (order position은 null이다.)
			// watcher : 목표지역(적base)으로 이동. 앞에 보이지 않는 적이 있으면 본진base로 후퇴.
			Position movePosition = order.getPosition();
			if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getCheckerTravelSite(vulture.getID());
				if (travelBase != null) {
					movePosition = travelBase.getPosition();
				}
			}
			
			if (MicroConfig.Common.versusMechanicSet()) {
				// 테란전용 go
				int distToOrder = vulture.getDistance(movePosition);
				if (distToOrder <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition의 둘러싼 대형을 만든다.
					if (vulture.isIdle() || vulture.isBraking()) {
						if (!vulture.isBeingHealed()) {
							Position randomPosition = PositionUtils.randomPosition(vulture.getPosition(), 100);
							CommandUtils.attackMove(vulture, randomPosition);
						}
					}
				} else {
					CommandUtils.attackMove(vulture, movePosition);
				}
				
			} else {
				// 이동지역까지 attackMove로 간다.
				if (vulture.getDistance(movePosition) > order.getRadius()) {
//					if (saveUnit) {
						CommandUtils.move(vulture, movePosition);
//					} else {
//						CommandUtil.attackMove(vulture, movePosition);
//					}
					
				} else { // 목적지 도착
					if (vulture.isIdle() || vulture.isBraking()) {
						Position randomPosition = PositionUtils.randomPosition(vulture.getPosition(), 100);
						CommandUtils.attackMove(vulture, randomPosition);
					}
				}
			}
			break;
		}
	}
	
	private boolean spiderMineOrderIssue(Unit vulture) {
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine == null) {
			positionToMine = SpiderMineManger.Instance().reserveSpiderMine(vulture, MinePositionLevel.NOT_MY_OCCUPIED);
		}
		if (positionToMine != null) {
			CommandUtils.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
			return true;
		}
		
		Unit spiderMineToRemove = SpiderMineManger.Instance().mineToRemove(vulture);
		if (spiderMineToRemove != null) {
			CommandUtils.attackUnit(vulture, spiderMineToRemove);
			return true;
		}
		
		return false;
	}
	

}
