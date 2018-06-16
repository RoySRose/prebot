package prebot.micro.control.factory;

import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.constant.MicroConfig.Common;
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
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		if (smallFightPredict == SmallFightPredict.ATTACK) {
			fight(unitList, euiList);

		} else if (smallFightPredict == SmallFightPredict.BACK) {
			regroup(unitList);
		}
	}

	private void fight(List<Unit> unitList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, false);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (spiderMineOrderIssue(unit)) {
					continue;
				}
				List<Unit> otherMechanics = UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(), Common.MAIN_SQUAD_COVERAGE,
						UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath);
				Unit enemyUnit = UnitUtils.unitInSight(decision.eui);
				if (!otherMechanics.isEmpty() && enemyUnit != null) {
					if (enemyUnit.getType() == UnitType.Terran_Vulture_Spider_Mine && unit.isInWeaponRange(enemyUnit)) {
						CommandUtils.holdPosition(unit);
					} else {
						CommandUtils.attackUnit(unit, enemyUnit);
					}
				} else {
					MicroUtils.kiting(unit, decision.eui, kOption);
				}
				
			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				if (spiderMineOrderIssue(unit)) {
					continue;
				}
				if (MicroUtils.arrivedToPosition(unit, StrategyIdea.watcherPosition)) {
					if (MicroUtils.timeToRandomMove(unit)) {
						Position randomPosition = PositionUtils.randomPosition(unit.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
						CommandUtils.attackMove(unit, randomPosition);
					}

				} else {
					CommandUtils.attackMove(unit, StrategyIdea.watcherPosition);
				}
			}
		}
	}

	private void regroup(List<Unit> unitList) {
		// 전방에 있는 벌처는 후퇴, 후속 벌처는 전진하여 squad유닛을 정비한다.
		Unit leader = UnitUtils.getClosestUnitToPosition(unitList, StrategyIdea.watcherPosition);
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			if (unit.getID() == leader.getID() || unit.getDistance(leader) <= REGROUP_UNIT_RADIUS) {
				CommandUtils.move(unit, StrategyIdea.mainSquadCenter);
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
