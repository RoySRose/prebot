package prebot.micro.control;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class MarineControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		Region campRegion = BWTA.getRegion(StrategyIdea.campPosition);
		Unit bunker = getCompleteBunker(campRegion);
		if (bunker == null) {
			bunker = getIncompleteBunker(campRegion);
		}

		// 벙커가 있다면 벙커 주위로 회피
		Position fleePosition = StrategyIdea.campPosition;
		if (bunker != null) {
			fleePosition = bunker.getPosition();
		}
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forMarine);
		FleeOption fOption = new FleeOption(fleePosition, true, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS);

		// TODO 초반 저글링 디펜스가 필요한 경우 일꾼 사이로 위치 고정
		if (bunker == null) {
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == DecisionType.FLEE_FROM_UNIT) {
					MicroUtils.flee(marine, decision.eui.getLastPosition(), fOption);
				} else if (decision.type == DecisionType.KITING_UNIT) {
					MicroUtils.kiting(marine, decision.eui, kOption);
				} else {
					if (marine.getDistance(StrategyIdea.campPosition) < 30) { // TODO 추후 변경
						if (MicroUtils.timeToRandomMove(marine)) {
							Position randomPosition = PositionUtils.randomPosition(marine.getPosition(), 20);
							CommandUtils.attackMove(marine, randomPosition);
						}
					} else {
						CommandUtils.attackMove(marine, StrategyIdea.campPosition);
					}
				}
			}
			
		} else {
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == DecisionType.KITING_UNIT) {
					Unit enemyInSight = UnitUtils.unitInSight(decision.eui);
					if (enemyInSight != null) {
						if(enemyInSight.isInWeaponRange(marine) || marine.isInWeaponRange(enemyInSight)) {
							intoTheBunker(bunker, marine);
						} else {
							outOfTheBunker(marine, bunker, decision.eui, kOption);
						}
					} else {
						intoTheBunker(bunker, marine);
					}

				} else {
					intoTheBunker(bunker, marine);
				}
			}
		}
	}

	private Unit getCompleteBunker(Region campRegion) {
		Unit bunkerInRegion = null;
		for (Unit bunker : UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Bunker)) {
			List<Unit> loadedUnits = bunker.getLoadedUnits();
			if (loadedUnits != null && loadedUnits.size() > 4) {
				continue;
			}
			Region bunkerRegion = BWTA.getRegion(bunker.getPosition());
			if (bunkerRegion == campRegion) {
				bunkerInRegion = bunker;
				break;
			}
		}
		return bunkerInRegion;
	}

	private Unit getIncompleteBunker(Region campRegion) {
		Unit bunkerInRegion = null;
		for (Unit bunker : UnitUtils.getUnitList(UnitFindRange.INCOMPLETE, UnitType.Terran_Bunker)) {
			if (bunker.getRemainingBuildTime() > 3 * TimeUtils.SECOND) {
				continue;
			}
			Region bunkerRegion = BWTA.getRegion(bunker.getPosition());
			if (bunkerRegion == campRegion) {
				bunkerInRegion = bunker;
				break;
			}
		}
		return bunkerInRegion;
	}

	private void intoTheBunker(Unit bunker, Unit marine) {
		if (bunker.isCompleted()) {
			CommandUtils.load(bunker, marine);
		} else {
			CommandUtils.move(marine, bunker.getPosition());
		}
	}

	private void outOfTheBunker(Unit marine, Unit bunker, UnitInfo eui, KitingOption kOption) {
		if (unitIsInBunker(marine, bunker)) {
			CommandUtils.unload(bunker, marine);// unload and attack
		} else {
			MicroUtils.kiting(marine, eui, kOption);
		}
	}

	private boolean unitIsInBunker(Unit marine, Unit bunker) {
		List<Unit> loadedUnits = bunker.getLoadedUnits();
		for (Unit loadedUnit : loadedUnits) {
			if (UnitUtils.isValidUnit(loadedUnit) && loadedUnit.getID() == marine.getID()) {
				return true;
			}
		}
		return false;
	}

}
