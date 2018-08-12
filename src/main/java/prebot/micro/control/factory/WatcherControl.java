package prebot.micro.control.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMakerPrebot1;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.SpiderMineManger;

/// MainSquad <-> 적 기지 or 주력병력 주둔지 이동하여 마인 매설 
public class WatcherControl extends Control {

	private Unit regroupLeader;
	private int saveUnitLevel;
	private Position avoidBunkerPosition;

	public void setRegroupLeader(Unit regroupLeader) {
		this.regroupLeader = regroupLeader;
	}
	
	public void setSaveUnitLevel(int saveUnitLevel) {
		this.saveUnitLevel = saveUnitLevel;
	}

	public void setAvoidBunkerPosition(Position avoidBunkerPosition) {
		this.avoidBunkerPosition = avoidBunkerPosition;
	}

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (TimeUtils.before(StrategyIdea.findRatFinishFrame)) {
			findRat(unitList);
			return;
		}
		
		Position fleePosition = StrategyIdea.mainSquadCenter;
		int coverRadius = StrategyIdea.mainSquadCoverRadius;
		if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK || StrategyIdea.currentStrategy == EnemyStrategy.ZERG_FAST_LURKER) {
			List<Unit> turretList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Missile_Turret);
			Unit closeTurret = UnitUtils.getClosestUnitToPosition(turretList, StrategyIdea.mainSquadCenter);
			if (closeTurret != null) {
				fleePosition = closeTurret.getPosition();
				coverRadius = 50;
			}
		}
		
		if (regroupLeader != null) {
			regroup(unitList, euiList, fleePosition, coverRadius);
		} else {
			fight(unitList, euiList, fleePosition, coverRadius);	
		}
	}

	private void fight(Collection<Unit> unitList, Collection<UnitInfo> euiList, Position fleePosition, int coverRadius) {
		FleeOption fOption = new FleeOption(fleePosition, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.KEEP_SAFE_DISTANCE);
		
		FleeOption fOptionMainBattle = new FleeOption(fleePosition, true, Angles.WIDE);
		KitingOption kOptionMainBattle = new KitingOption(fOptionMainBattle, CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE);
		
		List<Unit> otherMechanics = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
//			Decision decision = decisionMaker.makeDecision(unit, euiList, smallFightPredict == SmallFightPredict.OVERWHELM);
			Decision decision = DecisionMakerPrebot1.makeDecisionPrebot1(unit, euiList, null, saveUnitLevel);
			
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (spiderMineOrderIssue(unit)) {
					continue;
				}
				Unit enemyUnit = UnitUtils.unitInSight(decision.eui);
				if (enemyUnit != null) {
					if (MicroUtils.isRemovableEnemySpiderMine(unit, decision.eui)) {
						MicroUtils.holdControlToRemoveMine(unit, decision.eui.getLastPosition(), fOption);
					
					} else {
						if (unit.getDistance(fleePosition) < coverRadius) {
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
					if (avoidBunkerPosition != null) {
						Position bunkerAvoidPosition = new Position(avoidBunkerPosition.getX(), avoidBunkerPosition.getY() + 80).makeValid();
						CommandUtils.attackMove(unit, bunkerAvoidPosition);
					} else {
						if (MicroUtils.timeToRandomMove(unit)) {
							Position randomPosition = PositionUtils.randomPosition(unit.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
							CommandUtils.attackMove(unit, randomPosition);
						}
					}

				} else {
					CommandUtils.attackMove(unit, StrategyIdea.watcherPosition);
				}
			}
		}
	}

	/// 전방에 있는 벌처는 후퇴, 후속 벌처는 전진하여 squad유닛을 정비한다.
	private void regroup(Collection<Unit> unitList, Collection<UnitInfo> euiList, Position fleePosition, int coverRadius) {
		int regroupRadius = Math.min(UnitType.Terran_Vulture.sightRange() + unitList.size() * 80, 1000);
//		System.out.println("regroupRadius : " + regroupRadius);
		
		List<Unit> fightUnitList = new ArrayList<>();
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			if (unit.getDistance(fleePosition) < coverRadius * 3 / 5) {
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
			fight(fightUnitList, euiList, fleePosition, coverRadius);
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
