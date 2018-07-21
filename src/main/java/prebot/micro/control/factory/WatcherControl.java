package prebot.micro.control.factory;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.micro.targeting.DefaultTargetCalculator;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;
import prebot.strategy.manage.SpiderMineManger;

/// MainSquad <-> 적 기지 or 주력병력 주둔지 이동하여 마인 매설 
public class WatcherControl extends Control {

	private Unit regroupLeader;
	private SmallFightPredict smallFightPredict;

	public void setRegroupLeader(Unit regroupLeader) {
		this.regroupLeader = regroupLeader;
	}

	public void setSmallFightPredict(SmallFightPredict smallFightPredict) {
		this.smallFightPredict = smallFightPredict;
	}
	
	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		Position fleePosition = null;
		if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK || StrategyIdea.currentStrategy == EnemyStrategy.ZERG_FAST_LURKER) {
			List<Unit> turretList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Missile_Turret);
			Unit closeTurret = UnitUtils.getClosestUnitToPosition(turretList, StrategyIdea.mainSquadCenter);
			if (closeTurret != null) {
				fleePosition = closeTurret.getPosition();
			}
		}
		if (fleePosition == null) {
			fleePosition = StrategyIdea.mainSquadCenter;
		}
		
		if (regroupLeader != null) {
			regroup(unitList, euiList, fleePosition);
		} else {
			fight(unitList, euiList, fleePosition);	
		}
	}

	private void fight(List<Unit> unitList, List<UnitInfo> euiList, Position fleePosition) {
		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());

		FleeOption fOption = new FleeOption(fleePosition, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.KEEP_SAFE_DISTANCE);
		
		FleeOption fOptionMainBattle = new FleeOption(fleePosition, true, Angles.WIDE);
		KitingOption kOptionMainBattle = new KitingOption(fOptionMainBattle, CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE);
		
		List<Unit> otherMechanics = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			Decision decision = decisionMaker.makeDecision(unit, euiList, smallFightPredict == SmallFightPredict.OVERWHELM);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (spiderMineOrderIssue(unit)) {
					continue;
				}
				Unit enemyUnit = UnitUtils.unitInSight(decision.eui);
				if (enemyUnit != null) {
					if (enemyUnit.getType() == UnitType.Terran_Vulture_Spider_Mine && unit.isInWeaponRange(enemyUnit)) {
						CommandUtils.holdPosition(unit);
					} else {
						if (unit.getDistance(fleePosition) < StrategyIdea.mainSquadCoverRadius) {
							if (otherMechanics.size() >= 10) {
								MicroUtils.kiting(unit, decision.eui, kOptionMainBattle);
							} else if (otherMechanics.size() >= 3) {
								MicroUtils.kiting(unit, decision.eui, kOptionMainBattle);
							} else {
								MicroUtils.kiting(unit, decision.eui, kOption);
							}
						} else {
							MicroUtils.kiting(unit, decision.eui, kOption);
						}
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

	/// 전방에 있는 벌처는 후퇴, 후속 벌처는 전진하여 squad유닛을 정비한다.
	private void regroup(List<Unit> unitList, List<UnitInfo> euiList, Position fleePosition) {
		int regroupRadius = Math.min(UnitType.Terran_Vulture.sightRange() + unitList.size() * 80, 1000);
//		System.out.println("regroupRadius : " + regroupRadius);
		
		List<Unit> fightUnitList = new ArrayList<>();
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			if (unit.getDistance(fleePosition) < StrategyIdea.mainSquadCoverRadius) {
				fightUnitList.add(unit);
				continue;
			}
			if (unit.getID() != regroupLeader.getID() && unit.getDistance(regroupLeader) > regroupRadius) {
				fightUnitList.add(unit);
				continue;
			}
			CommandUtils.move(unit, fleePosition);
		}
		
		if (!fightUnitList.isEmpty()) {
			fight(fightUnitList, euiList, fleePosition);
		}
		
	}
	
	private boolean spiderMineOrderIssue(Unit vulture) {
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine == null) {
			positionToMine = SpiderMineManger.Instance().reserveSpiderMine(vulture, StrategyIdea.watcherMinePositionLevel);
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
