package prebot.micro.control.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.MicroDecision;
import prebot.micro.MicroDecisionMakerPrebot1;
import prebot.micro.constant.MicroConfig;
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
		if (InfoUtils.enemyRace() == Race.Terran) {
			int tankCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
			if (tankCount >= 3 && StrategyIdea.mainSquadCrossBridge) {
				int watcherBackEnoughDistance = (int) (StrategyIdea.mainSquadCoverRadius * (1 + (Math.log(unitList.size()) * 0.3)));
				double radian = MicroUtils.targetDirectionRadian(fleePosition, InfoUtils.myBase().getPosition());
				fleePosition = MicroUtils.getMovePosition(fleePosition, radian, watcherBackEnoughDistance);
				
//				List<Unit> centers = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL, UnitType.Terran_Command_Center);
//				Region myBaseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
//				Region myExpansionRegion = BWTA.getRegion(InfoUtils.myFirstExpansion().getPosition());
//				for (Unit center : centers) {
//					Region centerRegion = BWTA.getRegion(center.getPosition());
//					if (centerRegion != myBaseRegion
//							|| centerRegion != myExpansionRegion) {
//						fleePosition = center.getPosition();
//						break;
//					}
//				}
			}
			
		} else {
			if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK || StrategyIdea.currentStrategy == EnemyStrategy.ZERG_FAST_LURKER) {
				List<Unit> turretList = UnitUtils.getUnitList(CommonCode.UnitFindRange.ALL, UnitType.Terran_Missile_Turret);
				Unit closeTurret = UnitUtils.getClosestUnitToPosition(turretList, StrategyIdea.mainSquadCenter);
				if (closeTurret != null) {
					fleePosition = closeTurret.getPosition();
					coverRadius = 50;
				}
			}	
		}
		
		if (regroupLeader != null) {
			regroup(unitList, euiList, fleePosition, coverRadius);
		} else {
			fight(unitList, euiList, fleePosition, coverRadius);	
		}
	}

	private void fight(Collection<Unit> unitList, Collection<UnitInfo> euiList, Position fleePosition, int coverRadius) {
		FleeOption fOption = new FleeOption(fleePosition, false, MicroConfig.Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, KitingOption.CoolTimeAttack.KEEP_SAFE_DISTANCE);
		
		FleeOption fOptionMainBattle = new FleeOption(fleePosition, true, MicroConfig.Angles.WIDE);
		KitingOption kOptionMainBattle = new KitingOption(fOptionMainBattle, KitingOption.CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE);
		
		List<Unit> otherMechanics = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
//			Decision decision = decisionMaker.makeDecision(unit, euiList, smallFightPredict == StrategyCode.SmallFightPredict.OVERWHELM);
			MicroDecision decision = MicroDecisionMakerPrebot1.makeDecisionPrebot1(unit, euiList, null, saveUnitLevel);
			
			if (decision.type == MicroDecision.MicroDecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT) {
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
				
			} else if (decision.type == MicroDecision.MicroDecisionType.ATTACK_POSITION) {
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
							
							boolean avoidExpansionLocation = false;
							if (UnitUtils.getUnitCount(UnitFindRange.ALL, UnitType.Terran_Command_Center) <= 1) {
								BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
								if (randomPosition.getDistance(myFirstExpansion) < 200) {
									avoidExpansionLocation = true;
								}
							}
							if (!avoidExpansionLocation) {
								CommandUtils.attackMove(unit, randomPosition);
							}
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
			if (unit.getDistance(fleePosition) < coverRadius * 7 / 10) {
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
