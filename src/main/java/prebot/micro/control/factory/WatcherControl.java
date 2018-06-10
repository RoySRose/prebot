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
import prebot.micro.VultureCombatPredictor;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.VultureCombatResult;
import prebot.strategy.manage.SpiderMineManger;
import prebot.strategy.manage.SpiderMineManger.MinePositionLevel;

/// MainSquad <-> 적 기지 or 주력병력 주둔지 이동하여 마인 매설 
public class WatcherControl extends Control {
	
	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		VultureCombatResult result = VultureCombatResult.ATTACK;
		if (!StrategyIdea.initiated) {
			result = VultureCombatPredictor.watcherPredictByUnitInfo(unitList, euiList);
		}
		
		if (result == VultureCombatResult.ATTACK) {
			fight(unitList, euiList);

		} else if (result == VultureCombatResult.BACK) {
			regroup(unitList);
		}
	}

	private void fight(List<Unit> unitList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);
		FleeOption fOption = new FleeOption(StrategyIdea.campPosition, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, false);
		
		for (Unit unit : unitList) {
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (!spiderMineOrderIssue(unit)) {
					MicroUtils.kiting(unit, decision.eui, kOption);
				}
				
			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				if (!spiderMineOrderIssue(unit)) {
					CommandUtils.attackMove(unit, StrategyIdea.attackPosition);
				}
			}
		}
	}

	private void regroup(List<Unit> unitList) {
		// 전방에 있는 벌처는 후퇴, 후속 벌처는 전진하여 squad유닛을 정비한다.
		Unit leader = UnitUtils.getClosestUnitToPosition(unitList, StrategyIdea.attackPosition);
		for (Unit unit : unitList) {
			if (unit.getID() == leader.getID() || unit.getDistance(leader) <= 300) {
				CommandUtils.move(unit, StrategyIdea.campPosition);
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
