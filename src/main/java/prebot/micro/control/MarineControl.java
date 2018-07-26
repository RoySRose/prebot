package prebot.micro.control;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Synthesizer;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.targeting.DefaultTargetCalculator;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class MarineControl extends Control {
	private static final int NEAR_BASE_DISTANCE = 200;
	
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
		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
		FleeOption fOption = new FleeOption(fleePosition, true, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS);
		
		// TODO 초반 저글링 디펜스가 필요한 경우 일꾼 사이로 위치 고정
		if (bunker == null) {
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				
				Position safePosition = InformationManager.Instance().isSafePosition();
				safePosition = (InformationManager.Instance().isSafePosition() == null) ? BlockingEntrance.Instance().first_supple.toPosition() : safePosition;
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == DecisionType.FLEE_FROM_UNIT) {
					MicroUtils.flee(marine, decision.eui.getLastPosition(), fOption);
				} else if (decision.type == DecisionType.KITING_UNIT) {
					if(InformationManager.Instance().isBlockingEnterance()){
						MicroUtils.BlockingKiting(marine, decision.eui, kOption, safePosition);
					}else{
						MicroUtils.kiting(marine, decision.eui, kOption);
					}
				} else {
					if(InformationManager.Instance().isBlockingEnterance()){
						//if (MicroUtils.timeToRandomMove(marine)) {
							CommandUtils.attackMove(marine, StrategyIdea.campPosition);
						//}
					}else if (marine.getDistance(StrategyIdea.campPosition) < 30) { // TODO 추후 변경
						if (MicroUtils.timeToRandomMove(marine)) {
							Position randomPosition = PositionUtils.randomPosition(marine.getPosition(), 20);
							CommandUtils.attackMove(marine, randomPosition);
						}
					} else {
						CommandUtils.attackMove(marine, StrategyIdea.campPosition);
					}
				}
				
				if(!isInsidePositionToBase(marine.getPosition())){
					MicroUtils.flee(marine, safePosition, fOption);
				}
			}
		} else {
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == DecisionType.KITING_UNIT) {
					//Unit enemyInSight = UnitUtils.unitInSight(decision.eui);
					List<Unit> enemyInSightList = new ArrayList<>();
					for (UnitInfo eui : euiList) {
						Unit enemy = UnitUtils.unitInSight(eui);
						if (enemy != null) {
							enemyInSightList.add(enemy);
						}
					}
					Unit closeEnemyUnit = UnitUtils.getClosestUnitToPosition(enemyInSightList, marine.getPosition());
					if (closeEnemyUnit != null) {
						if(closeEnemyUnit.isInWeaponRange(marine) || marine.isInWeaponRange(closeEnemyUnit) || closeEnemyUnit.isInWeaponRange(bunker) || bunker.isInWeaponRange(closeEnemyUnit)) {
							intoTheBunker(bunker, marine);
						} else {
							if(bunker.getLoadedUnits().size() > (unitList.size() / 2) ){
								outOfTheBunker(marine, bunker, decision.eui, kOption);
							}else {
								MicroUtils.kiting(marine, decision.eui, kOption);
							}
						}
					} else {
						intoTheBunker(bunker, marine);
					}

				} else {
					intoTheBunker(bunker, marine);
				}
				
				if(!isInsidePositionToBase(marine.getPosition())){
					intoTheBunker(bunker, marine);
				}
			}
		}
	}

	public Unit getCompleteBunker(Region campRegion) {
		Unit bunkerInRegion = null;
		for (Unit bunker : UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Bunker)) {
			List<Unit> loadedUnits = bunker.getLoadedUnits();
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
			if (UnitUtils.isCompleteValidUnit(loadedUnit) && loadedUnit.getID() == marine.getID()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isInsidePositionToBase(Position position) {
		BaseLocation expansionBase = InfoUtils.myFirstExpansion();
		System.out.println("position.getDistance(expansionBase.getPosition() : " + position.getDistance(expansionBase.getPosition()));
		if (position.getDistance(expansionBase.getPosition()) < NEAR_BASE_DISTANCE) {
 			return false;
		}
    	return true;
	}
	
}
