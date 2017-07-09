package pre.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import pre.MapGrid;
import pre.UnitInfo;
import pre.combat.SpiderMineManger;
import pre.combat.Squad;
import pre.combat.SquadData;
import pre.combat.SquadOrder;
import pre.combat.VultureTravelManager;
import pre.combat.SquadOrder.SquadOrderType;
import pre.main.MyBotModule;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

public class CombatManager {

	private static final int IDLE_PRIORITY = 0;
	private static final int ATTACK_PRIORITY = 2;
	private static final int CHECKER_PRIORITY = 3;
	private static final int WATCHER_PRIORITY = 4;
	private static final int BASE_DEFENSE_PRIORITY = 5;
	private static final int SCOUT_DEFENSE_PRIORITY = 6;
	
	private List<Unit> combatUnits = new ArrayList<>();
	private SquadData squadData = new SquadData();

	private boolean initialized = false;
	
	// TODO 어느 시점을 공격타이밍으로 가져갈 것인가? 공격 타이밍마다 다른 공격 패턴 사용 가능성
	public enum CombatStrategy { DEFENCE_INSIDE, DEFENCE_CHOKEPOINT, READY_TO_ATTACK, ATTACK_ENEMY };
	
	private CombatStrategy combatStrategy;
	
	public CombatStrategy getCombatStrategy() {
		return combatStrategy;
	}

	public void setCombatStrategy(CombatStrategy combatStrategy) {
		this.combatStrategy = combatStrategy;
	}

	private static CombatManager instance = new CombatManager();
	
	private CombatManager() {
	}
	
	/// static singleton 객체를 리턴합니다
	public static CombatManager Instance() {
		return instance;
	}

	public void initSquads() {
		
		combatStrategy = CombatStrategy.DEFENCE_INSIDE;
		
		SquadOrder idleOrder = new SquadOrder(SquadOrderType.IDLE, MyBotModule.Broodwar.self().getStartLocation().toPosition(), 100, "Chill out");
		squadData.putSquad(new Squad("Idle", idleOrder, IDLE_PRIORITY));
		
		SquadOrder enemyScoutDefense = new SquadOrder(SquadOrderType.DEFEND, MyBotModule.Broodwar.self().getStartLocation().toPosition(), 600, "Get the scout");
		squadData.putSquad(new Squad("ScoutDefense", enemyScoutDefense, SCOUT_DEFENSE_PRIORITY));
		
		SquadOrder attackOrder = new SquadOrder(SquadOrderType.ATTACK, getMainAttackLocation(null), 800, "Attack enemy base");
		squadData.putSquad(new Squad("MainAttack", attackOrder, ATTACK_PRIORITY));
//		squadData.putSquad(new Squad("Flying", attackOrder, ATTACK_PRIORITY));

		SquadOrder watcherOrder = new SquadOrder(SquadOrderType.WATCH, getAttackPosition(null), 800, "Over Watcher");
		squadData.putSquad(new Squad("Watcher", watcherOrder, WATCHER_PRIORITY));
		
		initialized = true;
	}
	
	public void update() {
		if (!initialized) {
			initSquads();
		}
		
		combatUnits = this.getCombatUnits();
		if (combatUnits.isEmpty()) {
			return;
		}

		int frame8 = MyBotModule.Broodwar.getFrameCount() % 8;
		if (frame8 == 1) {
			// General Squads -> 일정시간에는 다음 squad 동작을 끈다.
			updateIdleSquad();
			updateScoutDefenseSquad();
			updateBaseDefenseSquads();
			updateAttackSquads();
			updateWatcherSquad();
			updateCheckerSquad();
			
			SpiderMineManger.Instance().update();
			VultureTravelManager.Instance().update();
		}
		else if (frame8 % 4 == 2) {
			doComsatScan();
		}
		
		if (MyBotModule.Broodwar.getFrameCount() % (24*10) == 0) {
			squadData.printSquadInfo();
		}
		
		squadData.update();
	}
	
	private List<Unit> getCombatUnits() {
		List<Unit> combatUnits = new ArrayList<>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (CommandUtil.IsValidUnit(unit) && !MicroUtils.isUnitContainedInUnitSet(unit, combatUnits) && (CommandUtil.IsCombatUnit(unit) || unit.getType().isWorker())) {
				MicroUtils.addUnitToUnitSet(unit, combatUnits);
			}
		}
		return combatUnits;
	}
	
	private void doComsatScan() {
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Comsat_Station) == 0) {
			return;
		}
		
		// Does the enemy have undetected cloaked units that we may be able to engage?
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
				// At most one scan per call. We don't check whether it succeeds.
				MicroUtils.smartScan(unit.getPosition());
				break;
			}
		}
	}
	
	private Position getMainAttackLocation(Squad squad) {
		
		if (combatStrategy == CombatStrategy.DEFENCE_INSIDE) {
			// We are guaranteed to always have a main base location, even if it has been destroyed.
			BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
			List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
			for (BaseLocation occupied : occupiedBases) {
				if (occupied == firstExpansion) {
					base = firstExpansion;
					break;
				}
			}
			return base.getPosition();
			
		} else if (combatStrategy == CombatStrategy.DEFENCE_CHOKEPOINT) {
			Chokepoint choke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
			BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
			List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
			for (BaseLocation occupied : occupiedBases) {
				if (occupied == firstExpansion) {
					choke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
					break;
				}
			}
			return choke.getCenter();
			
		} else if (combatStrategy == CombatStrategy.READY_TO_ATTACK) { // 헌터에서 사용하면 위치에 따라 꼬일 수 있을듯
			Position readyToAttack = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
			return readyToAttack;
			
		} else { //if (combatStrategy == CombatStrategy.ATTACK) { 
		    Position enemyPosition = getAttackPosition(squad);
		    return enemyPosition;
		    
		}
	}
	
	private Position getAttackPosition(Squad squad) {
		// What stuff the squad can attack.
		boolean canAttackAir = true;
		boolean canAttackGround = true;
		if (squad != null) {
//			canAttackAir = squad.hasAntiAir();
//			canAttackGround = squad.hasAntiGround();
		}
		
		// First choice: Attack the enemy main unless we think it's empty.
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		if (enemyBaseLocation != null) {
			Position enemyBasePosition = enemyBaseLocation.getPosition();

			if (!MyBotModule.Broodwar.isExplored(enemyBasePosition.toTilePosition())) {
				return enemyBasePosition;
			}

			// get all known enemy units in the area
			List<Unit> enemyUnitsInArea = new ArrayList<>();
			MapGrid.Instance().getUnitsNear(enemyUnitsInArea, enemyBasePosition, 800, false, true);

			for (Unit unit : enemyUnitsInArea) {
				if (unit.getType() != UnitType.Zerg_Larva && (unit.isFlying() && canAttackAir || !unit.isFlying() && canAttackGround)) {
					// Enemy base is not empty: Something interesting is in the enemy base area.
					return enemyBasePosition;
				}
			}
		}
		
		// Second choice: Attack known enemy buildings.
		// We assume that a terran can lift the buildings; otherwise, the squad must be able to attack ground.
		if (canAttackGround || MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			Map<Integer, UnitInfo> unitAndUnitInfoMap = InformationManager.Instance().getUnitAndUnitInfoMap(MyBotModule.Broodwar.enemy());
			for (Integer unitId : unitAndUnitInfoMap.keySet()) {
				UnitInfo ui = unitAndUnitInfoMap.get(unitId);

				if (ui.getType().isBuilding() && ui.getLastPosition() != Position.None) {
					return ui.getLastPosition();
				}
			}
		}

		// Third choice: Attack visible enemy units.
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (unit.getType() == UnitType.Zerg_Larva || !CommandUtil.IsValidUnit(unit, false, true) || !unit.isVisible()) {
				continue;
			}

			if (unit.isFlying() && canAttackAir || !unit.isFlying() && canAttackGround) {
				return unit.getPosition();
			}
		}

		// Fourth choice: We can't see anything so explore the map attacking along the way
		return MapGrid.Instance().getLeastExplored();
	}
	
	private Position watchersPosition(Squad squad) {
		Player enemyPlayer = InformationManager.Instance().enemyPlayer;
		
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(enemyPlayer);
		if (enemyBaseLocation != null) {
			boolean expansionOccupied = false;
			BaseLocation expansionBase = InformationManager.Instance().getFirstExpansionLocation(enemyPlayer);
			for (BaseLocation occupied : InformationManager.Instance().getOccupiedBaseLocations(enemyPlayer)) {
				if (expansionBase.equals(occupied)) {
					expansionOccupied = true;
					break;
				}
			}
			if (expansionOccupied) {
				if (!InformationManager.Instance().getMapSpecificInformation().notUseReadyToAttackPosition()) {
					Position readyToAttack = InformationManager.Instance().getReadyToAttackPosition(enemyPlayer);
					if (readyToAttack != null) {
						return readyToAttack;
					}
				} else {
					Chokepoint secondChoke = InformationManager.Instance().getSecondChokePoint(enemyPlayer);
					if (secondChoke != null) {
						return secondChoke.getCenter();
					}
				}
			} else {
				Chokepoint secondChoke = InformationManager.Instance().getSecondChokePoint(enemyPlayer);
				if (secondChoke != null) {
					return secondChoke.getCenter();
				} 
			}
		}
		
		return getMainAttackLocation(squad);
	}

	private void updateIdleSquad() {
		Squad squad = squadData.getSquad("Idle");
		
		for (Unit unit : combatUnits) {
			if (squadData.canAssignUnitToSquad(unit, squad)) {
				squadData.assignUnitToSquad(unit, squad);
			}
		}
	}
	
	private void updateScoutDefenseSquad() {
		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		bwta.Region myRegion = base.getRegion();
		if (myRegion == null || !myRegion.getCenter().isValid()) {
	        return;
	    }
		
		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
	    boolean assignScoutDefender = enemyUnitsInRegion.size() == 1 && enemyUnitsInRegion.get(0).getType().isWorker(); // 일꾼 한마리가 내 지역에서 돌아다닌다.

		Squad scoutDefenseSquad = squadData.getSquad("ScoutDefense");		
	    if (scoutDefenseSquad.isEmpty() && assignScoutDefender) {
	    	Unit enemyWorker = enemyUnitsInRegion.get(0);
	        Unit workerDefender = findClosestWorkerToTarget(enemyWorker);
	        
            if (squadData.canAssignUnitToSquad(workerDefender, scoutDefenseSquad)) {
			    WorkerManager.Instance().setCombatWorker(workerDefender);
			    squadData.assignUnitToSquad(workerDefender, scoutDefenseSquad);
            }
	    	
	    } else if (!scoutDefenseSquad.isEmpty() && !assignScoutDefender) {
	    	scoutDefenseSquad.clear();
	    }
	}
	
	private void updateBaseDefenseSquads() {
		
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Region enemyRegion = null;
		if (enemyBaseLocation != null) {
			enemyRegion = BWTA.getRegion(enemyBaseLocation.getPosition());
		}
		
		for (Region myRegion : InformationManager.Instance().getOccupiedRegions(InformationManager.Instance().selfPlayer)) {
			if (myRegion == enemyRegion) { // 가스러시 예외처리
				continue;
			}
			
			Position regionCenter = myRegion.getCenter();
			if (!regionCenter.isValid()) {
				continue;
			}
			
			List<Unit> enemyUnitsInRegion = new ArrayList<>();
	        for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
	            if (unit.getType() == UnitType.Zerg_Overlord ||
					unit.getType() == UnitType.Protoss_Observer ||
					unit.isLifted()) { // floating terran building
	            	continue;
	            }
	            
	            if (BWTA.getRegion(unit.getTilePosition()) == myRegion) {
	                enemyUnitsInRegion.add(unit);
	            }
	        }

	        // 초반에 일꾼을 잡기 힘들어서 넣어놓은 코드
//	        for (Unit unit : enemyUnitsInRegion) {
//	        	if(unit.getType().isWorker()) {
//	        		enemyUnitsInRegion.remove(unit);
//	        		break;
//	        	}
//	        }
	        
	        String squadName = "Base Defense " + regionCenter.getX() + " " + regionCenter.getY();
        	Squad squad = squadData.getSquad(squadName);
	        if (enemyUnitsInRegion.isEmpty()) {
				if (squad != null) {
	        		squad.clear();
	        	}
				continue;
	        } else {
	        	if (squad == null) {
	        		SquadOrder squadOrder = new SquadOrder(SquadOrderType.DEFEND, regionCenter, 32 * 25, "Defend region");
	        		squadData.putSquad(new Squad(squadName, squadOrder, BASE_DEFENSE_PRIORITY));
	        	}
	        }

			int numDefendersPerEnemyUnit = 2;

			int numEnemyFlyingInRegion = 0;
			int numEnemyGroundInRegion = 0;
			for (Unit unit : enemyUnitsInRegion) {
				if(unit.isFlying()) {
					numEnemyFlyingInRegion++;
				} else {
					numEnemyGroundInRegion++;
				}
			}
			
			
			Squad defenseSquad = squadData.getSquad(squadName);
		    int flyingDefendersNeeded = numDefendersPerEnemyUnit * numEnemyFlyingInRegion;
		    int groundDefendersNeeded = numDefendersPerEnemyUnit * numEnemyGroundInRegion;
		    
		    // Count static defense as air defenders.
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Missile_Turret &&
					unit.isCompleted() && unit.isPowered() && BWTA.getRegion(unit.getTilePosition()) == myRegion) {
					flyingDefendersNeeded -= 2;
				}
			}
			
			// Pull workers only in narrow conditions.
			// Pulling workers (as implemented) can lead to big losses.
			boolean pullWorkers = false; // TODO 일꾼을 빼야할 때.. numZerglingsInOurBase() > 0 || buildingRush());
			
			updateDefenseSquadUnits(defenseSquad, flyingDefendersNeeded, groundDefendersNeeded, pullWorkers);
		}
		
	}
	
	private Unit findClosestWorkerToTarget(Unit enemyWorker) {
		Unit closestMineralWorker = null;
		int closestDist = 600 + 128;
		for (Unit unit : combatUnits) {
			if (unit.getType().isWorker() && WorkerManager.Instance().isMineralWorker(unit)) {
				int dist = unit.getDistance(enemyWorker);
				if (unit.isCarryingMinerals()) {
					dist += 96;
				}
				if (dist < closestDist) {
					closestMineralWorker = unit;
					closestDist = dist; // TODO 참조 소스 이상함. 이 메소드 동작은  잘 하는건지 의심.
				}
			}
		}
		
		return closestMineralWorker;
	}
	
	private void updateDefenseSquadUnits(Squad defenseSquad, int flyingDefendersNeeded, int groundDefendersNeeded, boolean pullWorkers) {
		// if there's nothing left to defend, clear the squad
		if (flyingDefendersNeeded <= 0 && groundDefendersNeeded <= 0) {
			defenseSquad.clear();
			return;
		}
		
		List<Unit> squadUnits = defenseSquad.getUnitSet();
		
		int flyingDefendersInSquad = 0;
		int groundDefendersInSquad = 0;
		for (Unit unit : squadUnits) {
			if (CommandUtil.CanAttackAir(unit)) {
				flyingDefendersInSquad++;
			}
			if (CommandUtil.CanAttackGround(unit)) {
				groundDefendersInSquad++;
			}
		}

		// add flying defenders if we still need them
		int flyingDefendersAdded = 0;
		while (flyingDefendersNeeded > flyingDefendersInSquad + flyingDefendersAdded) {
			Unit defenderToAdd = findClosestDefender(defenseSquad, defenseSquad.getOrder().getPosition(), true, false);

			// if we find a valid flying defender, add it to the squad
			if (defenderToAdd != null) {
				squadData.assignUnitToSquad(defenderToAdd, defenseSquad);
				flyingDefendersAdded++;
			}
			// otherwise we'll never find another one so break out of this loop
			else {
				break;
			}
		}

		// add ground defenders if we still need them
		int groundDefendersAdded = 0;
		while (groundDefendersNeeded > groundDefendersInSquad + groundDefendersAdded) {
			Unit defenderToAdd = findClosestDefender(defenseSquad, defenseSquad.getOrder().getPosition(), true, false);

			// If we find a valid ground defender, add it.
			if (defenderToAdd != null) {
				if (defenderToAdd.getType().isWorker()) {
					WorkerManager.Instance().setCombatWorker(defenderToAdd);
				}
				squadData.assignUnitToSquad(defenderToAdd, defenseSquad);
				groundDefendersAdded++;
			}
			// otherwise we'll never find another one so break out of this loop
			else {
				break;
			}
		}
	}
	
	private void updateAttackSquads() {
	    Squad mainAttackSquad = squadData.getSquad("MainAttack");
//	    Squad flyingSquad = squadData.getSquad("Flying");

		for (Unit unit : combatUnits) {
//			if (squadData.canAssignUnitToSquad(unit, flyingSquad)
//				  && unit.getType() == UnitType.Terran_Wraith
//				  || unit.getType() == UnitType.Terran_Valkyrie
//				  || unit.getType() == UnitType.Terran_Battlecruiser) {
//				squadData.assignUnitToSquad(unit, flyingSquad);
//			} else
	        if (!unit.getType().isWorker() && squadData.canAssignUnitToSquad(unit, mainAttackSquad)) {
				squadData.assignUnitToSquad(unit, mainAttackSquad);// 배슬, 드랍십도 포함됨
	        }
	    }

		if (mainAttackSquad.getOrder().getType() != SquadOrderType.BATTLE) {
			SquadOrder mainAttackOrder = new SquadOrder(SquadOrderType.ATTACK, getMainAttackLocation(mainAttackSquad), 800, "Attack enemy base");
		    mainAttackSquad.setOrder(mainAttackOrder);
		}

//		if (flyingSquad.getOrder().getType() != SqaudOrderType.BATTLE) {
//		    SquadOrder flyingAttackOrder = new SquadOrder(SqaudOrderType.ATTACK, getMainAttackLocation(flyingSquad), 800, "Attack enemy base");
//			flyingSquad.setOrder(flyingAttackOrder);
//		}
	}
	
	private Unit findClosestDefender(Squad defenseSquad, Position pos, boolean flyingDefender, boolean pullWorkers) {
		Unit closestDefender = null;
		int minDistance = 99999;

		for (Unit unit : combatUnits) {
			if ((flyingDefender && !CommandUtil.CanAttackAir(unit)) ||
				(!flyingDefender && !CommandUtil.CanAttackGround(unit))) {
	            continue;
	        }

	        if (!squadData.canAssignUnitToSquad(unit, defenseSquad)) {
	            continue;
	        }

			int dist = unit.getDistance(pos);

			// Pull workers only if requested, and not from distant bases.
			if (unit.getType().isWorker() && (!pullWorkers || dist > 1000)) {
	            continue;
	        }

			if (dist < minDistance) {
	            closestDefender = unit;
	            minDistance = dist;
	        }
		}

		return closestDefender;
	}


    int watcherMaxNum = 1; // TODO 변동 숫자 
	char checkerIdx = 'A'; // TODO 이거 수정필요
	int maxCheckerSquadNum = 1; // TODO 변동 숫자 
	int checkcerMaxNum = 1; // TODO 변동 숫자 
	
	private void updateWatcherSquad() {
	    Squad watcherSquad = squadData.getSquad("Watcher");

		if (combatStrategy != CombatStrategy.ATTACK_ENEMY) {
			for (Unit unit : combatUnits) {
		        if (unit.getType() == UnitType.Terran_Vulture && squadData.canAssignUnitToSquad(unit, watcherSquad)) {
					if (watcherSquad.getUnitSet().size() < watcherMaxNum) {
						squadData.assignUnitToSquad(unit, watcherSquad);
		        	}
		        }
		    }
		}

		SquadOrder watchOrder = new SquadOrder(SquadOrderType.WATCH, watchersPosition(watcherSquad), 800, "Over Watcher");
		watcherSquad.setOrder(watchOrder);
	}
	
	private void updateCheckerSquad() {
		List<Squad> checkerSquads = squadData.getSquadList("Checker");
		
		// 대기중(inactive)인 checker squad에 unit을 할당한다.
		// 활동중(active) 상태인 checker는 이미 돌아다니고 있으니 병력 할당을 하지 않는다.
		for (Unit unit : combatUnits) {
			for (Squad squad : checkerSquads) {
				if (squad.getOrder().getType() == SquadOrderType.CHECK_ACTIVE) {
					// active squad의 병력이 모두 죽었다면 squad를 삭제한다.
					if (squad.getUnitSet().size() == 0) {
						squadData.removeSquad(squad.getName());
						VultureTravelManager.Instance().getSquadSiteMap().remove(squad.getName());
					}
					continue;
				}
				
				// unit을 할당한다.
		        if (unit.getType() == UnitType.Terran_Vulture && squadData.canAssignUnitToSquad(unit, squad)) {
		        	if (squad.getUnitSet().size() < checkcerMaxNum) {
						squadData.assignUnitToSquad(unit, squad);
		        	}
		        }
		        
		        // 최대 숫자만큼 unit이 할당되었으니 active 상태로 변경한다.
		        // 전투상태이면 inactive -> active 변경
		        if (squad.getUnitSet().size() >= checkcerMaxNum || combatStrategy == CombatStrategy.ATTACK_ENEMY) {
		        	squad.getOrder().setType(SquadOrderType.CHECK_ACTIVE);
		        }
		    }
		}
		
		boolean inactiveSquadExist = false;
		for (Squad squad : checkerSquads) {
			if (squad.getOrder().getType() == SquadOrderType.CHECK_ACTIVE) {
				BaseLocation baseLocation = VultureTravelManager.Instance().getBestTravelSite(squad.getName());
				Position orderPosition = null;
	        	if (baseLocation != null) {
	        		orderPosition = baseLocation.getPosition();
	        	} else {
	        		orderPosition = getMainAttackLocation(squad);
	        	}
	        	squad.getOrder().setPosition(orderPosition);
			} else {
				inactiveSquadExist = true;
			}
		}
		
		if (!inactiveSquadExist && combatStrategy != CombatStrategy.ATTACK_ENEMY) {
			// 새로운 checker squad를 생성한다. 전투상태이면 스쿼드를 생성하지 않는다.
			if (checkerSquads.size() < maxCheckerSquadNum) {
	    		SquadOrder squadOrder = new SquadOrder(SquadOrderType.CHECK_INACTIVE, getMainAttackLocation(null), 100, "Check it out");
	    		Squad newCheckerSquad = new Squad("Checker " + checkerIdx++, squadOrder, CHECKER_PRIORITY);
	    		squadData.putSquad(newCheckerSquad);
			}
		}
		
	}

}
