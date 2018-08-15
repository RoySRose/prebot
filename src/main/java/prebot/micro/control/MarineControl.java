package prebot.micro.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.synth.SynthSpinnerUI;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode.UnitFindRange;
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
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.targeting.DefaultTargetCalculator;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder.CampType;

public class MarineControl extends Control {
	private static final int NEAR_BASE_DISTANCE = 120;
	private static Unit kitingMarine = null; //마린 한마리만 카이팅
	
	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		
		Region campRegion = BWTA.getRegion(StrategyIdea.campPosition);
		Unit bunker = getCompleteBunker(campRegion);
		Unit inCompleteBunker = getCompleteBunker(campRegion);
		if (bunker == null) {
			inCompleteBunker = getIncompleteBunker(campRegion);
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
		if (bunker == null && inCompleteBunker == null) {
			CampType campType = StrategyIdea.campType;
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				
				Position safePosition = InformationManager.Instance().isSafePosition();
				Position holdConPosition = InformationManager.Instance().isHoldConPosition();
				safePosition = (InformationManager.Instance().isSafePosition() == null) ? BlockingEntrance.Instance().first_supple.toPosition() : safePosition;
				holdConPosition = (InformationManager.Instance().isHoldConPosition() == null) ? InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint() : holdConPosition;
				Position firstCheokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getCenter();
				
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				
				if(marineDangerousOutOfMyRegion(marine,decision.eui)){
					Position randomPosition = PositionUtils.randomPosition(safePosition, 5);
					marine.move(randomPosition);
					continue;
				}
				/*while(marine.getDistance(safePosition) < 30) { // TODO 추후 변경
					CommandUtils.attackMove(marine, safePosition);
				} */
				if (decision.type == DecisionType.FLEE_FROM_UNIT) {
					//if(InformationManager.Instance().isBlockingEnterance()){
					if(decision.eui.getUnit().getDistance(safePosition) > 30){
						CommandUtils.attackMove(marine, safePosition);
					}else{
						MicroUtils.flee(marine, decision.eui.getLastPosition(), fOption);
					}
				} else if (decision.type == DecisionType.KITING_UNIT) {
					//if(InformationManager.Instance().isBlockingEnterance()){
					// 베이스 지역 OK
						if((campType == CampType.INSIDE || campType == CampType.FIRST_CHOKE)
								&& decision.eui.getUnit().getDistance(safePosition) > 30){
							if(MicroUtils.isRangeUnit(decision.eui.getType()) && !decision.eui.getType().isWorker()){
								//MicroUtils.BlockingKiting(marine, decision.eui, kOption, safePosition);
								if (marine.getDistance(holdConPosition) < 20){
									CommandUtils.holdPosition(marine);
								}else{
									CommandUtils.attackMove(marine, holdConPosition);
								}
							}else if(decision.eui.getType().isBuilding() || decision.eui.getType().isWorker()){
	                            MicroUtils.kiting(marine, decision.eui, kOption);
	                        }else{
								if(kitingMarine == null || !kitingMarine.exists()){//마린 한마리만 왔다갔다 카이팅
									kitingMarine = marine;
								}else if(kitingMarine == marine){
									MicroUtils.BlockingKiting(marine, decision.eui, kOption, safePosition);
								}else{
									if (marine.getDistance(safePosition) < 30){
										CommandUtils.holdPosition(marine);
									}else{
										CommandUtils.attackMove(marine, safePosition);
									}
								}
							}
						}else{
						//MicroUtils.BlockingKiting(marine, decision.eui, kOption, safePosition);
							MicroUtils.kiting(marine, decision.eui, kOption);
						}
				} else {
					if(campType != CampType.INSIDE  && campType != CampType.FIRST_CHOKE){
						CommandUtils.attackMove(marine, StrategyIdea.mainSquadLeaderPosition);
					}else{
						CommandUtils.attackMove(marine, StrategyIdea.campPosition);
					}
				}
				
			}
		} else if(bunker == null && inCompleteBunker != null){
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == DecisionType.KITING_UNIT){
					if(marineDangerousOutOfMyRegion(marine,decision.eui)){
						CommandUtils.attackMove(marine, inCompleteBunker.getPosition());
					}
					
					MicroUtils.kiting(marine, decision.eui, kOption);
				}else{
					CommandUtils.attackMove(marine, inCompleteBunker.getPosition());
				}
			}
		}else {
			boolean rangeUnit = false;
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				
				
				Decision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == DecisionType.KITING_UNIT) {
					Unit enemyInSight = UnitUtils.unitInSight(decision.eui);
				
					if(marineDangerousOutOfMyRegion(marine,decision.eui)){
						intoTheBunker(bunker, marine);
						continue;
					}
					
					if (enemyInSight != null) {
						if(MicroUtils.isRangeUnit(enemyInSight.getType())){
							rangeUnit = true;
						}
						if(rangeUnit && !enemyInSight.getType().isWorker()){
							intoTheBunker(bunker, marine);
							continue;
						}
						
						if(enemyInSight.getType().isWorker() || enemyInSight.getType() == UnitType.Zerg_Overlord){
							outOfTheBunker(marine, bunker, decision.eui, kOption);
							continue;
						}
						
						if(enemyInSight.isInWeaponRange(marine) || marine.isInWeaponRange(enemyInSight) || enemyInSight.isInWeaponRange(bunker) || bunker.isInWeaponRange(enemyInSight)) {
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
					if(bunker.getLoadedUnits().size() >=  4){
						CommandUtils.attackMove(marine, bunker.getPosition());
					}else{
						intoTheBunker(bunker, marine);
					}
				}
				
				
			}
		}
	}

	public Unit getCompleteBunker(Region campRegion) {
		Unit bunkerInRegion = null;
		for (Unit bunker : UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Bunker)) {
				bunkerInRegion = bunker;
				break;
		}
		return bunkerInRegion;
	}

	private Unit getIncompleteBunker(Region campRegion) { 
		Unit bunkerInRegion = null;
		for (Unit bunker : UnitUtils.getUnitList(UnitFindRange.INCOMPLETE, UnitType.Terran_Bunker)) {
			/*if (bunker.getRemainingBuildTime() > 3 * TimeUtils.SECOND) {
				continue;
			}*/
			/*Region bunkerRegion = BWTA.getRegion(bunker.getPosition());
			if (bunkerRegion == campRegion) {*/
				bunkerInRegion = bunker;
				//break;
			//}
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
	
	//public boolean isInsidePositionToBase(Position position) {
	public boolean marineDangerousOutOfMyRegion(Unit marine,UnitInfo eui) {
		if (LagObserver.groupsize() > 20) {
			return false;
		}
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			return false;
		}
		
		Region unitRegion = BWTA.getRegion(marine.getPosition());
		Region baseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
		
		CampType campType = StrategyIdea.campType;
		// 베이스 지역 OK
		if (campType == CampType.INSIDE || campType == CampType.FIRST_CHOKE) {
			Position firstCheokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
			
			if(eui != null && !MicroUtils.isRangeUnit(eui.getType())){
				if(marine.getDistance(firstCheokePoint) < NEAR_BASE_DISTANCE){
					return true;
				}
			}
			if (unitRegion == baseRegion) {
				return false;
			}
			if(InformationManager.Instance().enemyRace != Race.Terran){
				return true;
			}else{
				return false;
			}
		}
				
		// 앞마당 지역, 또는 앞마당 반경이내 OK
		Position expansionPosition = InfoUtils.myFirstExpansion().getPosition();
		Region expansionRegion = BWTA.getRegion(expansionPosition);
		if (unitRegion == expansionRegion) {
			return false;
		} else if (marine.getDistance(expansionPosition) < 100) {
			return false;
		}
		/*
		if (campType == CampType.EXPANSION) {
			return true;
		}
		 */
		
		
		
		// 세번째 지역까지 OK
		if (unitRegion == InfoUtils.myThirdRegion()) {
			return true;
		}
		if (campType == CampType.SECOND_CHOKE) {
			return true;
		}
		// 세번째 지역 반경 OK
		if (marine.getDistance(InfoUtils.myThirdRegion()) < 500) {
			return true;
		}

		return false;
	}
	
}
