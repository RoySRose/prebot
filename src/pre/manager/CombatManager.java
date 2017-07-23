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
import pre.MapTools;
import pre.UnitInfo;
import pre.combat.SpiderMineManger;
import pre.combat.Squad;
import pre.combat.SquadData;
import pre.combat.SquadOrder;
import pre.combat.SquadOrder.SquadOrderType;
import pre.combat.VultureTravelManager;
import pre.main.MyBotModule;
import pre.util.CombatExpectation;
import pre.util.CommandUtil;
import pre.util.CommonUtils;
import pre.util.MicroSet;
import pre.util.MicroUtils;

public class CombatManager {

	private static final int IDLE_PRIORITY = 0;
	private static final int ATTACK_PRIORITY = 2;
	private static final int WATCHER_PRIORITY = 3;
	private static final int CHECKER_PRIORITY = 4;
	private static final int GUERILLA_PRIORITY = 5;
	private static final int BASE_DEFENSE_PRIORITY = 6;
	private static final int SCOUT_DEFENSE_PRIORITY = 7;
	private static final int WRAITH_PRIORITY = 100;
	private static final int VESSEL_PRIORITY = 101;

	private static Chokepoint currTargetChoke = null;
	
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

		SquadOrder checkerOrder = new SquadOrder(SquadOrderType.CHECK, getAttackPosition(null), 800, "Check it out");
		squadData.putSquad(new Squad("Checker", checkerOrder, CHECKER_PRIORITY));
		
		SquadOrder wraithOrder = new SquadOrder(SquadOrderType.ATTACK, getAttackPosition(null), 800, "Wraith");
		squadData.putSquad(new Squad("Wraith", wraithOrder, WRAITH_PRIORITY));
		
		SquadOrder vesselOrder = new SquadOrder(SquadOrderType.DEFEND, MyBotModule.Broodwar.self().getStartLocation().toPosition(), 600, "Vessel");
		squadData.putSquad(new Squad("Vessel", vesselOrder, VESSEL_PRIORITY));
		
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

		if (CommonUtils.executeOncePerFrame(8, 0)) {
			updateIdleSquad();
			updateScoutDefenseSquad();
			updateBaseDefenseSquads();
			updateAttackSquads();
			updateWraithSquad();
			updateVesselSquad(); //AttackSquads 뒤에
			updateWatcherSquad();
			updateCheckerSquad();
			updateGuerillaSquad();
			
			SpiderMineManger.Instance().update();
			VultureTravelManager.Instance().update();
		}
		else if (CommonUtils.executeOncePerFrame(4, 1)) {
			doComsatScan();
		}
		if(MyBotModule.Broodwar.getFrameCount() > 50){
			setCombatStrategy(CombatStrategy.ATTACK_ENEMY); 
		}
		
//		if (MyBotModule.Broodwar.getFrameCount() % (24*10) == 0) {
//			squadData.printSquadInfo();
//		}
		
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
			
			//if()
			
			Position enemyPosition = getAttackPosition(squad);
		    return enemyPosition;
		}
	}
	
	private Position getAttackPosition(Squad squad) {
		// What stuff the squad can attack.
		boolean canAttackAir = true;
		boolean canAttackGround = true;
		if (squad != null) {
			Position nextChokePosition = getNextChokePosition(squad);
			if (nextChokePosition != null) {
				return nextChokePosition;
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
		//Position leastExplored = MapGrid.Instance().getLeastExplored(); TODO 렉 유발요소 확인 필요
		//return leastExplored;
		return null;
	}
	
	private Position getNextChokePosition(Squad squad) {
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Chokepoint enemyFirstChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
	    
		if (squad.getName().equals("MainAttack") && enemyBaseLocation != null && enemyFirstChoke != null) {
			if (squad.getUnitSet().isEmpty()) {
				currTargetChoke = null;
				return null;
			}

		    Position squadPosition = MicroUtils.centerOfUnits(squad.getUnitSet());
		    Position enemyFirstChokePosition = enemyFirstChoke.getCenter();
		    if (squadPosition.getDistance(enemyFirstChokePosition) < 320) { // 적의 first chokePoint 도착
		    	return enemyBaseLocation.getPosition(); // TODO 언덕 위로 올라가는 것에 대한 판단. 상대의 주력병력을 소모시키전에 언덕위로 진입하는 것은 위험할 수 있다.
		    }

			// 현재 주력병력이 어느 chokepoint에 위치해있는가?(가까운 chokepoint)
		    if (currTargetChoke == null) {
		    	Chokepoint closestChoke = null;
				double closestDist = 999999;
			    for (Chokepoint choke : BWTA.getChokepoints()) {
			    	double distToChoke = squadPosition.getDistance(choke.getCenter());
			    	if (distToChoke < closestDist) {
			    		closestChoke = choke;
			    		closestDist = distToChoke;
			    	}
			    }
			    currTargetChoke = closestChoke;
		    }

		    // 현재의 chokepoint에 도착했다고 판단되면 next chokepoint를 찾는다.
		    // next chokepoint는 최단거리 상에 있는 가장 가까운 chokepoint이다.
		    if (squadPosition.getDistance(currTargetChoke) < 320) {
			    if (squadPosition.equals(currTargetChoke)) {
			    	return null;
			    }
		    	
		    	int chokeToEnemyChoke = MapTools.Instance().getGroundDistance(currTargetChoke.getCenter(), enemyFirstChokePosition); // 현재chokepoint ~ 목적지chokepoint
		    	
		    	Chokepoint nextChoke = null;
				double closestChokeToNextChoke = 999999;
		    	for (Chokepoint choke : BWTA.getChokepoints()) {
		    		if (choke.equals(currTargetChoke)) {
		    			continue;
		    		}
		    		int chokeToNextChoke = MapTools.Instance().getGroundDistance(currTargetChoke.getCenter(), choke.getCenter()); // 현재chokepoint ~ 다음chokepoint
		    		int nextChokeToEnemyChoke = MapTools.Instance().getGroundDistance(choke.getCenter(), enemyFirstChokePosition); // 다음chokepoint ~ 목적지chokepoint
		    		if (chokeToNextChoke + nextChokeToEnemyChoke < chokeToEnemyChoke + 10 && chokeToNextChoke > 10 && chokeToNextChoke < closestChokeToNextChoke) { // 최단거리 오차범위 10 * 32
		    			nextChoke = choke;
		    			closestChokeToNextChoke = chokeToNextChoke;
		    		}
			    }
		    	if (nextChoke != null) {
		    		currTargetChoke = nextChoke;
		    	}
		    }
		}
		if (currTargetChoke != null) {
			return currTargetChoke.getCenter();
		} else {
			return null;
		}
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
			// readyToAttack position 또는 second chokepoint
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
				if (defenderToAdd.getType().isWorker()) {//TODO 일꾼부터 보는게 타당한가? by KSW
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

		SquadOrder mainAttackOrder = new SquadOrder(SquadOrderType.ATTACK, getMainAttackLocation(mainAttackSquad), 800, "Attack enemy base");
	    mainAttackSquad.setOrder(mainAttackOrder);

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
	
	private void updateWraithSquad() {//TODO 현재는 본진 공격 용 레이스만 있음
		Squad wraithSquad = squadData.getSquad("Wraith");
		
		for (Unit unit : combatUnits) {
	        if (unit.getType() == UnitType.Terran_Wraith && squadData.canAssignUnitToSquad(unit, wraithSquad)) {
				squadData.assignUnitToSquad(unit, wraithSquad);// 레이스만
	        }
	    }
		
		SquadOrder wraithOrder = new SquadOrder(SquadOrderType.ATTACK, getAttackPosition(wraithSquad), UnitType.Terran_Wraith.sightRange(), "Wraith");
		wraithSquad.setOrder(wraithOrder);
	}
	
	private void updateVesselSquad() {//TODO 현재는 본진 공격 용 레이스만 있음
		Squad vesselSquad = squadData.getSquad("Vessel");
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
	        if (unit.getType() == UnitType.Terran_Science_Vessel && squadData.canAssignUnitToSquad(unit, vesselSquad)) {
	        	
	        	System.out.println("assign vessel : " + unit.getID());
				squadData.assignUnitToSquad(unit, vesselSquad);// 레이스만
	        }
	    }
		
//		NONE, IDLE, WATCH, ATTACK, DEFEND, HOLD,
//		CHECK_INACTIVE, CHECK_ACTIVE
		SquadOrder vesselOrder = null;
//		
//		Unit invisibleEnemyUnit = null;
//		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
//			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing) && unit.getPosition().isValid()) {
//				// At most one scan per call. We don't check whether it succeeds.
//				invisibleEnemyUnit = unit;
//				break;
//			}
//		}
//		
//		if(invisibleEnemyUnit != null){
//			vesselOrder = new SquadOrder(SquadOrderType.DEFEND, invisibleEnemyUnit.getPosition(), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
//		}else{
		if (combatStrategy == CombatStrategy.DEFENCE_INSIDE) {
			vesselOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(vesselSquad), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
		} else if (combatStrategy == CombatStrategy.DEFENCE_CHOKEPOINT) {
			vesselOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(vesselSquad), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
		} else if (combatStrategy == CombatStrategy.READY_TO_ATTACK) { // 헌터에서 사용하면 위치에 따라 꼬일 수 있을듯
			vesselOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(vesselSquad), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
		} else if (combatStrategy == CombatStrategy.ATTACK_ENEMY) {
			if(squadData.getSquad("MainAttack") != null){
				List<Unit> units = squadData.getSquad("MainAttack").getUnitSet();
				if(units.size()>0){
					Unit leader = MicroUtils.leaderOfUnit(units, getAttackPosition(vesselSquad));
					vesselOrder = new SquadOrder(SquadOrderType.ATTACK, leader.getPosition(), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
				}
			}else{
				vesselOrder = new SquadOrder(SquadOrderType.DEFEND, getAttackPosition(vesselSquad), UnitType.Terran_Science_Vessel.sightRange(), "Vessel");
			}
		}
			
		
		vesselSquad.setOrder(vesselOrder);
	}
	
	private void updateWatcherSquad() {
	    Squad watcherSquad = squadData.getSquad("Watcher");

		for (Unit unit : combatUnits) {
			if (unit.getType() == UnitType.Terran_Vulture && squadData.canAssignUnitToSquad(unit, watcherSquad)) {
				squadData.assignUnitToSquad(unit, watcherSquad);
			}
		}
	    
		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		if (enemyBase != null) {
			SquadOrder watchOrder = new SquadOrder(SquadOrderType.WATCH, enemyBase.getPosition(), 800, "Over Watcher");
			watcherSquad.setOrder(watchOrder);
		}
	}
	
	private void updateCheckerSquad() {
		Squad checkerSquad = squadData.getSquad("Checker");

//		// 전투상태로 바뀌는 순간 다 없앤다.
//		if (combatStrategy == CombatStrategy.ATTACK_ENEMY) {
//			for (Squad squad : checkerSquads) {
//				squadData.removeSquad(squad.getName());
//				VultureTravelManager.Instance().getSquadSiteMap().remove(squad.getName());
//			}
//			return;
//		}
		
		// checker 유닛 할당( // TODO 기왕이면 마인 있는 놈으로.. 그리고 교체)
		for (Unit unit : combatUnits) {
			if (checkerSquad.getUnitSet().size() < MicroSet.Vulture.maxNumChecker) {
				if (unit.getType() == UnitType.Terran_Vulture && squadData.canAssignUnitToSquad(unit, checkerSquad)) {
					squadData.assignUnitToSquad(unit, checkerSquad);
				}
			}
		}
	    
		SquadOrder checkerOrder = new SquadOrder(SquadOrderType.CHECK, getMainAttackLocation(checkerSquad), 800, "Over Watcher");
		checkerSquad.setOrder(checkerOrder);
	}
	
	private void updateGuerillaSquad() {
		
		List<Unit> assignableVultures = new ArrayList<>();
		for (Unit unit : combatUnits) {
			if (unit.getType() == UnitType.Terran_Vulture) {
				Squad unitSquad = squadData.getUnitSquad(unit);
				if (unitSquad == null || GUERILLA_PRIORITY > unitSquad.getPriority()) {
					assignableVultures.add(unit);
				}
			}
		}
		int vulturePower = CombatExpectation.getVulturePower(assignableVultures);
		
		// 1. Travel Site에 대한 처리
		BaseLocation bestGuerillaSite = VultureTravelManager.Instance().getBestGuerillaSite(assignableVultures);
		if (bestGuerillaSite != null) {
			// 안개속의 적들을 상대로 계산해서 게릴라 타깃이 가능한지 확인한다.
			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(bestGuerillaSite.getPosition(), InformationManager.Instance().enemyPlayer, MicroSet.Vulture.GEURILLA_RADIUS);
			int enemyPower = CombatExpectation.getEnemyPowerByUnitInfo(enemiesInfo);
			
			if (vulturePower > enemyPower) {
				String squadName = "Guerilla_" + bestGuerillaSite.getPosition().toString();
				Squad guerillaSquad = squadData.getSquad(squadName);
				// 게릴라 스쿼드 생성(포지션 별)
				if (guerillaSquad == null) {
	        		SquadOrder squadOrder = new SquadOrder(SquadOrderType.GUERILLA, bestGuerillaSite.getPosition(), MicroSet.Vulture.GEURILLA_RADIUS, "Let's get it on");
	        		guerillaSquad = new Squad(squadName, squadOrder, GUERILLA_PRIORITY);
	        		squadData.putSquad(guerillaSquad);
	        	}
        		// 게릴라 유닛이 남아 있다면 더 할당하지 않는다.
				if (!guerillaSquad.getUnitSet().isEmpty()) {
					System.out.println("guerilla units already exist");
        		} else {
        			for (Unit assignableVulture : assignableVultures) {
    					squadData.assignUnitToSquad(assignableVulture, guerillaSquad);
    					int squadPower = CombatExpectation.getVulturePower(guerillaSquad.getUnitSet());
    					if (squadPower > enemyPower + MicroSet.Vulture.GEURILLA_EXTRA_POWER) { 
    						break; // 충분한 파워
    					}
    				}
        			System.out.println("guerilla squad created!");
        			System.out.println(" - unit size : " + guerillaSquad.getUnitSet().size());
        			System.out.println(" - position  : " + bestGuerillaSite.getPosition());
        		}
			} else {
				System.out.println("not the best guerilla site");
			}
		}
		
		// TODO 2. base가 아닌 지역에 대한 처리
		
		// 게릴라 임무 종료 조건
		// 1. 게릴라 지역이 clear 되었다.
		// 2. 현재 병력으로 이길수 없다.
		List<Squad> guerillaSquads = squadData.getSquadList("Guerilla");
		for (Squad squad : guerillaSquads) {
			if (squad.getUnitSet().isEmpty()) {
				continue;
			}
			
			if (MyBotModule.Broodwar.isVisible(squad.getOrder().getPosition().toTilePosition())) {
				List<Unit> enemies = MapGrid.Instance().getUnitsNear(squad.getOrder().getPosition(), MicroSet.Vulture.GEURILLA_RADIUS, false, true, null);
				if (enemies.isEmpty()) {
					System.out.println("guerillaSquads " + squad.getName() + " clear : no enemy");
					squad.clear();
					continue;
				}
			}
			
			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(squad.getOrder().getPosition(), InformationManager.Instance().enemyPlayer, MicroSet.Vulture.GEURILLA_RADIUS);
			boolean willWin = CombatExpectation.expectVultureVictoryByUnitInfo(squad.getUnitSet(), enemiesInfo);
			if (!willWin) {
				System.out.println("guerillaSquads " + squad.getName() + " clear : mission impossilbe");
				squad.clear();
				continue;
			}

			int guerillaScore = CombatExpectation.getGuerillaScoreByUnitInfo(enemiesInfo);
			if (guerillaScore <= 0) {
				System.out.println("guerillaSquads " + squad.getName() + " clear : worthless");
				squad.clear();
				continue;
			}
		}
		
	}

}
