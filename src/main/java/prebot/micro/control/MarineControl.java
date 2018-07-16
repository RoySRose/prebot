package prebot.micro.control;

import java.util.List;

import javax.sound.midi.Synthesizer;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
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
				TilePosition firstSupplePos = BlockingEntrance.Instance().first_supple;
				Position safePosition = firstSupplePos.toPosition();
				for (Unit supple : UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Supply_Depot)) {
					if(supple.getTilePosition().equals(firstSupplePos)){
						safePosition = marineEarlyDefensePosition(marine,supple);
					}
				}
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
					if (marine.getDistance(StrategyIdea.campPosition) < 30) { // TODO 추후 변경
						if(InformationManager.Instance().isBlockingEnterance()){
						//if (MicroUtils.timeToRandomMove(marine)) {
							CommandUtils.attackMove(marine, safePosition);
						//}
						}else if (MicroUtils.timeToRandomMove(marine)) {
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

	public Unit getCompleteBunker(Region campRegion) {
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
			if (UnitUtils.isCompleteValidUnit(loadedUnit) && loadedUnit.getID() == marine.getID()) {
				return true;
			}
		}
		return false;
	}
	
	private static Position marineEarlyDefensePosition(Unit marine, Unit supple) {
		Position firstCheokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();

		int reverseX = supple.getPosition().getX() - firstCheokePoint.getX(); // 타겟과 반대로 가는 x양
		int reverseY = supple.getPosition().getY() - firstCheokePoint.getY(); // 타겟과 반대로 가는 y양
	    final double fleeRadian = Math.atan2(reverseY, reverseX); // 회피 각도
	    
		Position safePosition = null;

		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = (int) (UnitType.Terran_Marine.topSpeed() * 15);
		Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
		Position movePosition = new Position(supple.getPosition().getX() + fleeVector.getX(), supple.getPosition().getY() + fleeVector.getY()); // 회피지점

		return movePosition;
	}

}
