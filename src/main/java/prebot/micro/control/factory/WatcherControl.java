package prebot.micro.control.factory;

import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;
import prebot.strategy.manage.SpiderMineManger;
import prebot.strategy.manage.SpiderMineManger.MinePositionLevel;

/// MainSquad <-> 적 기지 or 주력병력 주둔지 이동하여 마인 매설 
public class WatcherControl extends Control {

	private static final int REGROUP_UNIT_RADIUS = 300;
	private SmallFightPredict smallFightPredict;

	public void setSmallFightPredict(SmallFightPredict smallFightPredict) {
		this.smallFightPredict = smallFightPredict;
	}
	
	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList, Position targetPosition) {
		if (smallFightPredict == SmallFightPredict.ATTACK) {
			fight(unitList, euiList, targetPosition);

		} else if (smallFightPredict == SmallFightPredict.BACK) {
			regroup(unitList, targetPosition);
		}
	}

	private void fight(List<Unit> unitList, List<UnitInfo> euiList, Position targetPosition) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);
		FleeOption fOption = new FleeOption(StrategyIdea.campPosition, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, false);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (!spiderMineOrderIssue(unit)) {
					MicroUtils.kiting(unit, decision.eui, kOption);
				}
				
			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				if (!spiderMineOrderIssue(unit)) {
					CommandUtils.attackMove(unit, targetPosition);
				}
			}
		}
	}

	private void regroup(List<Unit> unitList, Position targetPosition) {
		// 전방에 있는 벌처는 후퇴, 후속 벌처는 전진하여 squad유닛을 정비한다.
		Unit leader = UnitUtils.getClosestUnitToPosition(unitList, targetPosition);
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			if (unit.getID() == leader.getID() || unit.getDistance(leader) <= REGROUP_UNIT_RADIUS) {
				CommandUtils.move(unit, StrategyIdea.mainSquadPosition);
			} else {
				CommandUtils.move(unit, leader.getPosition());
			}
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
