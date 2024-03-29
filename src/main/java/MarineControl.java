

import java.util.Collection;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;

public class MarineControl extends Control {
	private static final int NEAR_BASE_DISTANCE = 100;
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
		MicroDecisionMaker decisionMaker = new MicroDecisionMaker(new DefaultTargetCalculator());
		FleeOption fOption = new FleeOption(fleePosition, true, MicroConfig.Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, KitingOption.CoolTimeAttack.COOLTIME_ALWAYS);
		
		// TODO 초반 저글링 디펜스가 필요한 경우 일꾼 사이로 위치 고정
		if (bunker == null && inCompleteBunker == null) {
			PositionFinder.CampType campType = StrategyIdea.campType;
			for (Unit marine : unitList) {
				if (skipControl(marine)) {
					continue;
				}
				
				Position safePosition = InformationManager.Instance().isSafePosition();
				Position holdConPosition = InformationManager.Instance().isHoldConPosition();
				safePosition = (InformationManager.Instance().isSafePosition() == null) ? BlockingEntrance.Instance().first_supple.toPosition() : safePosition;
				holdConPosition = (InformationManager.Instance().isHoldConPosition() == null) ? InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint() : holdConPosition;
				Position firstCheokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getCenter();
				
				MicroDecision decision = decisionMaker.makeDecision(marine, euiList);
				
				if(marineDangerousOutOfMyRegion(marine,decision.eui)){
					Position randomPosition = PositionUtils.randomPosition(safePosition, 5);
					marine.move(randomPosition);
					continue;
				}
				/*while(marine.getDistance(safePosition) < 30) { // TODO 추후 변경
					CommandUtils.attackMove(marine, safePosition);
				} */
				if (decision.type == MicroDecision.MicroDecisionType.FLEE_FROM_UNIT) {
					//if(InformationManager.Instance().isBlockingEnterance()){
					if(decision.eui.getUnit().getDistance(safePosition) > 30){
						CommandUtils.attackMove(marine, safePosition);
					}else{
						MicroUtils.flee(marine, decision.eui.getLastPosition(), fOption);
					}
				} else if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT) {
					//if(InformationManager.Instance().isBlockingEnterance()){
					// 베이스 지역 OK
						if((campType == PositionFinder.CampType.INSIDE || campType == PositionFinder.CampType.FIRST_CHOKE)
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
							//MicroUtils.kiting(marine, decision.eui, kOption);
							if (MicroUtils.arrivedToPosition(marine,StrategyIdea.mainSquadLeaderPosition)) {
								if (MicroUtils.timeToRandomMove(marine)) {
									Position randomPosition = PositionUtils.randomPosition(marine.getPosition(), 70);
									CommandUtils.attackMove(marine, randomPosition);
								}
							}else{
								CommandUtils.attackMove(marine, StrategyIdea.mainSquadLeaderPosition);
							}
						}
				} else {
					if(campType != PositionFinder.CampType.INSIDE  && campType != PositionFinder.CampType.FIRST_CHOKE){
						if (MicroUtils.arrivedToPosition(marine,StrategyIdea.mainSquadLeaderPosition)) {
							if (MicroUtils.timeToRandomMove(marine)) {
								Position randomPosition = PositionUtils.randomPosition(marine.getPosition(), 70);
								CommandUtils.attackMove(marine, randomPosition);
							}
						}else{
							CommandUtils.attackMove(marine, StrategyIdea.mainSquadLeaderPosition);
						}
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
				
				MicroDecision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT){
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
				
				
				MicroDecision decision = decisionMaker.makeDecision(marine, euiList);
				if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT) {
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
						}else if(enemyInSight.getType().isWorker() || enemyInSight.getType() == UnitType.Zerg_Overlord){
							outOfTheBunker(marine, bunker, decision.eui, kOption);
							continue;
						}else if(enemyInSight.isInWeaponRange(marine) || marine.isInWeaponRange(enemyInSight) || enemyInSight.isInWeaponRange(bunker) || bunker.isInWeaponRange(enemyInSight)) {
							intoTheBunker(bunker, marine);
						} else {
							if(!enemyInSight.getType().isWorker() && enemyInSight.getType() != UnitType.Zerg_Overlord
									&& marine.getDistance(bunker) > 300){
								intoTheBunker(bunker, marine);
							}
							if(bunker.getLoadedUnits().size() > Math.round((unitList.size() / 2))){
								outOfTheBunker(marine, bunker, decision.eui, kOption);
							}else {
								//MicroUtils.BlockingKiting(marine, decision.eui, kOption, bunker.getPosition());
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
		Collection<Unit> bunkers = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Bunker);
		return UnitUtils.getClosestUnitToPosition(bunkers, InfoUtils.mySecondChoke().getCenter());
	}

	private Unit getIncompleteBunker(Region campRegion) {
		Collection<Unit> bunkers = UnitUtils.getUnitList(CommonCode.UnitFindRange.INCOMPLETE, UnitType.Terran_Bunker);
		return UnitUtils.getClosestUnitToPosition(bunkers, InfoUtils.mySecondChoke().getCenter());
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
		
		PositionFinder.CampType campType = StrategyIdea.campType;
		// 베이스 지역 OK
		if (campType == PositionFinder.CampType.INSIDE || campType == PositionFinder.CampType.FIRST_CHOKE) {
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
		if (marine.getPosition().getDistance(expansionPosition) < 80) {
 			return true;
		}
		
		if (unitRegion == expansionRegion) {
			return false;
		} else if (marine.getDistance(expansionPosition) < 100) {
			return false;
		}
		/*
		if (campType == PositionFinder.CampType.EXPANSION) {
			return true;
		}
		 */
		
		
		
	/*	// 세번째 지역까지 OK
		if (unitRegion == InfoUtils.myThirdRegion()) {
			return true;
		}
		if (campType == PositionFinder.CampType.SECOND_CHOKE) {
			return true;
		}
		// 세번째 지역 반경 OK
		if (marine.getDistance(InfoUtils.myThirdRegion()) < 500) {
			return true;
		}*/

		return false;
	}
	
}
