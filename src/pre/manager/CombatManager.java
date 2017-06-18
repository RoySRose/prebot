package pre.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Order;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import pre.MapGrid;
import pre.UnitInfo;
import pre.combat.Squad;
import pre.combat.SquadData;
import pre.combat.SquadOrder;
import pre.combat.SquadOrder.SqaudOrderType;
import pre.main.MyBotModule;
import pre.util.CommandUtil;
import pre.util.MicroUtils;

public class CombatManager {

	private static final int IDLE_PRIORITY = 0;
	private static final int ATTACK_PRIORITY = 2;
	private static final int BASE_DEFENSE_PRIORITY = 3;
	private static final int SCOUT_DEFENSE_PRIORITY = 4;
	
	private List<Unit> combatUnits = new ArrayList<>();
	private SquadData squadData = new SquadData();

	private boolean initialized = false;
	private boolean goAggressive = false; // TODO 어느 시점을 공격타이밍으로 가져갈 것인가? 공격 타이밍마다 다른 공격 패턴 사용 가능성
	
	private static CombatManager instance = new CombatManager();
	
	private CombatManager() {
	}
	
	/// static singleton 객체를 리턴합니다
	public static CombatManager Instance() {
		return instance;
	}
	
	public boolean getAggressive() {
		return goAggressive;
	}

	public void setAggressive(boolean goAggressive) {
		this.goAggressive = goAggressive;
	}

	public void initSquads() {
		
		SquadOrder idleOrder = new SquadOrder(SqaudOrderType.IDLE, MyBotModule.Broodwar.self().getStartLocation().toPosition(), 100, "Chill out");
		squadData.putSquad(new Squad("Idle", idleOrder, IDLE_PRIORITY));
		
		SquadOrder enemyScoutDefense = new SquadOrder(SqaudOrderType.DEFEND, MyBotModule.Broodwar.self().getStartLocation().toPosition(), 600, "Get the scout");
		squadData.putSquad(new Squad("ScoutDefense", enemyScoutDefense, SCOUT_DEFENSE_PRIORITY));
		
		SquadOrder attackOrder = new SquadOrder(SqaudOrderType.ATTACK, getMainAttackLocation(null), 800, "Attack enemy base");
		squadData.putSquad(new Squad("MainAttack", attackOrder, ATTACK_PRIORITY));
		squadData.putSquad(new Squad("Flying", attackOrder, ATTACK_PRIORITY));

		// TODO SQAUD 추가 가능
		
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
		}
		else if (frame8 % 4 == 2) {
			doComsatScan();
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
		if (!goAggressive) {
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
		}

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
			if (unit.getType() == UnitType.Zerg_Larva ||
				!CommandUtil.IsValidUnit(unit) || !unit.isVisible()) {
				continue;
			}

			if (unit.isFlying() && canAttackAir || !unit.isFlying() && canAttackGround) {
				return unit.getPosition();
			}
		}

		// Fourth choice: We can't see anything so explore the map attacking along the way
		return MapGrid.Instance().getLeastExplored();
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
	        		SquadOrder squadOrder = new SquadOrder(SqaudOrderType.DEFEND, regionCenter, 32 * 25, "Defend region");
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
	    Squad flyingSquad = squadData.getSquad("Flying");

		for (Unit unit : combatUnits) {
			if (squadData.canAssignUnitToSquad(unit, flyingSquad)
				  && unit.getType() == UnitType.Terran_Wraith
				  || unit.getType() == UnitType.Terran_Valkyrie
				  || unit.getType() == UnitType.Terran_Battlecruiser) {
				squadData.assignUnitToSquad(unit, flyingSquad);
			}
	        else if (!unit.getType().isWorker() && squadData.canAssignUnitToSquad(unit, mainAttackSquad)) {
				squadData.assignUnitToSquad(unit, mainAttackSquad);// 배슬, 드랍십도 포함됨
	        }
	    }

	    SquadOrder mainAttackOrder = new SquadOrder(SqaudOrderType.ATTACK, getMainAttackLocation(mainAttackSquad), 800, "Attack enemy base");
	    mainAttackSquad.setOrder(mainAttackOrder);

	    SquadOrder flyingAttackOrder = new SquadOrder(SqaudOrderType.ATTACK, getMainAttackLocation(flyingSquad), 800, "Attack enemy base");
		flyingSquad.setOrder(flyingAttackOrder);
		
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

}
