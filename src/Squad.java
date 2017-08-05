

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class Squad {
	
	public Squad(String name, SquadOrder order, int priority) {
		this.name = name;
		this.order = order;
		this.priority = priority;
		this.attackAtMax = this.lastRetreatSwitchVal = false;
	}
	
	private String name;
	private SquadOrder order;
	private int priority;
	private boolean attackAtMax;
    private int lastRetreatSwitch;
	private boolean lastRetreatSwitchVal;
	
	private MicroScv microScv = new MicroScv();
	private MicroMarine microMarine = new MicroMarine();
	private MicroVulture microVulture = new MicroVulture();
	private MicroTank microTank = new MicroTank();
	private MicroGoliath microGoliath = new MicroGoliath();
	private MicroWraith microWraith = new MicroWraith();
	private MicroVessel microVessel = new MicroVessel();
	
	// **** 아래변수들은 팩토리 유닛으로 구성된 Squad 전용이다.
	private int initFrame = 0;
	private int saveUnitFrameCount = 0;
	private MechanicMicroVulture mechanicVulture = new MechanicMicroVulture();
	private MechanicMicroTank mechanicTank = new MechanicMicroTank();
	private MechanicMicroGoliath mechanicGoliath = new MechanicMicroGoliath();

	private List<Unit> unitSet = new ArrayList<>();
//	private Map<Integer, Boolean> nearEnemy = new HashMap<>();

	public String getName() {
		return name;
	}
	public SquadOrder getOrder() {
		return order;
	}
	public void setOrder(SquadOrder order) {
		this.order = order;
	}
	public int getPriority() {
		return priority;
	}

	public List<Unit> getUnitSet() {
		return unitSet;
	}
	
	public void addUnit(Unit unit) {
		unitSet.add(unit);
	}
	
	public void update() {
		updateUnits();
		if (unitSet.isEmpty()) {
			return;
		}
		
		if (name.equals(SquadName.MAIN_ATTACK)) {
			updateMainAttackSquad();
			return;
		} else if (name.equals(SquadName.CHECKER) || name.startsWith(SquadName.GUERILLA_)) {
			updateVultureSquad();
			return;
		} else if (name.startsWith(SquadName.BASE_DEFENSE_)) {
			updateDefenseSquad();
			return;
		}
		
		if (microVulture.getUnits().size() > 0
				|| microTank.getUnits().size() > 0
				|| microGoliath.getUnits().size() > 0) {
			MyBotModule.Broodwar.sendText("!!!!!! MECHANIC UNIT ASSIGN ERROR !!!!!! 010 2626 9786");
		}
		
		// 방어병력은 눈앞의 적을 무시하고 방어를 위해 이동해야 한다.
		List<Unit> nearbyEnemies = new ArrayList<>();
		if (order.getType() == SquadOrderType.DEFEND) {
			MapGrid.Instance().getUnitsNear(nearbyEnemies, order.getPosition(), order.getRadius(), false, true);
		} else {
			for (Unit unit : unitSet) {
				MapGrid.Instance().getUnitsNear(nearbyEnemies, unit.getPosition(), order.getRadius(), false, true);
			}
		}
		
		microScv.setMicroInformation(order, nearbyEnemies);
		microMarine.setMicroInformation(order, nearbyEnemies);
		microVulture.setMicroInformation(order, nearbyEnemies);
		microTank.setMicroInformation(order, nearbyEnemies);
		microGoliath.setMicroInformation(order, nearbyEnemies);
		microWraith.setMicroInformation(order, nearbyEnemies);
		microVessel.setMicroInformation(order, nearbyEnemies);
		
		microScv.execute();
		microMarine.execute();
		microVulture.execute();
		microTank.execute();
		microGoliath.execute();
		microWraith.execute();
		microVessel.execute();
	}
	
	private void updateMainAttackSquad() {
		List<UnitInfo> vultureEnemies = new ArrayList<>();
		List<UnitInfo> attackerEnemies = new ArrayList<>();
		List<UnitInfo> closeTankEnemies = new ArrayList<>();
		
		// 명령 준비
		SquadOrder attackerOrder = this.order;
		SquadOrder watchOrder = null;
		
		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Position watchPosition = order.getPosition();
		if (enemyBase != null && !microVulture.getUnits().isEmpty()) {
			watchPosition = enemyBase.getPosition();
		}
		watchOrder = new SquadOrder(SquadOrderType.WATCH, watchPosition, Combat.WATCHER_RADIUS, "Watch over");
		// WATCHER'S ENEMY
		for (Unit vulture : microVulture.getUnits()) {
			InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(), InformationManager.Instance().enemyPlayer, watchOrder.getRadius());
		}
		// TANK & GOLIATH'S ENEMY
		for (Unit tank : microTank.getUnits()) {
			InformationManager.Instance().getNearbyForce(attackerEnemies, tank.getPosition(), InformationManager.Instance().enemyPlayer, attackerOrder.getRadius());
		}
		for (Unit goliath : microGoliath.getUnits()) {
			InformationManager.Instance().getNearbyForce(attackerEnemies, goliath.getPosition(), InformationManager.Instance().enemyPlayer, attackerOrder.getRadius());
		}
		InformationManager.Instance().getNearbyForce(attackerEnemies, order.getPosition(), InformationManager.Instance().enemyPlayer, attackerOrder.getRadius());
		
		// TANK 거리재기 상대
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			List<UnitInfo> nearTankEnemies = new ArrayList<>();
			for (Unit tank : microTank.getUnits()) {
				InformationManager.Instance().getNearbyForce(nearTankEnemies, tank.getPosition(), InformationManager.Instance().enemyPlayer, 0);
			}
			for (UnitInfo enemyInfo : nearTankEnemies) {
				Unit enemy = MicroUtils.getUnitIfVisible(enemyInfo);
				if (enemy != null) {
					if (!CommandUtil.IsValidUnit(enemy)) {
						continue;
					}
				} else {
					if (MyBotModule.Broodwar.getFrameCount() - enemyInfo.getUpdateFrame() > MicroSet.Common.NO_UNIT_FRAME(enemyInfo.getType())) { // skip old unit
						continue;
					}
				}
				
				if (enemyInfo.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || enemyInfo.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					closeTankEnemies.add(enemyInfo);
				}
			}
		}
		
//		List<Unit> peloton = new ArrayList<>();
//		Unit leader = MicroUtils.leaderOfUnit(microVulture.getUnits(), watchOrder.getPosition());
//		for (Unit vulture : microVulture.getUnits()) {
//			if (vulture == leader || vulture.getDistance(leader.getPosition()) < MicroSet.Vulture.PELOTON_RADIUS) {
//				peloton.add(vulture);
//			}
//		}
//		boolean saveUnit = !CombatExpectation.expectByUnitInfo(peloton, vultureEnemies);
//		mechanicVulture.prepareMechanic(watchOrder, vultureEnemies);
//		mechanicVulture.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), saveUnit);
//		for (Unit vulture : microVulture.getUnits()) {
//			mechanicVulture.executeMechanicMicro(vulture);
//		}
		
		if (attackerEnemies.isEmpty()) {
			if (initFrame > 0) {
				initFrame = 0;
				MyBotModule.Broodwar.sendText("finished");
			}
		} else {
			if (initFrame == 0) {
				initFrame = MyBotModule.Broodwar.getFrameCount();
				MyBotModule.Broodwar.sendText("initiated");
			}
		}
		
		// 벌처 전투 여부 판단
		int saveUnitLevelVulture = 1;
		if (initFrame > 0) {
			List<UnitInfo> allEnemies = new ArrayList<>();
			allEnemies.addAll(vultureEnemies);
			allEnemies.addAll(attackerEnemies);
			
			List<Unit> allMyUnits = new ArrayList<>();
			allMyUnits.addAll(microVulture.getUnits());
			allMyUnits.addAll(microTank.getUnits());
			allMyUnits.addAll(microGoliath.getUnits());
			
			Result result = CombatExpectation.expectByUnitInfo(microVulture.getUnits(), vultureEnemies, false);
			if (result == Result.Loss || result == Result.Win) {
				saveUnitLevelVulture = 1;
			} else if (result == Result.BigWin) {
				saveUnitLevelVulture = 0;
			}
			
		} else {
			if (saveUnitFrameCount > 0) {
				watchOrder.setPosition(attackerOrder.getPosition());
				vultureEnemies = attackerEnemies;
				saveUnitFrameCount--;
				saveUnitLevelVulture = 2;
			} else {
				Result result = CombatExpectation.expectByUnitInfo(microVulture.getUnits(), vultureEnemies, false);
				if (result == Result.Loss) {
					CombatManager.Instance().setDetailStrategy(CombatStrategyDetail.VULTURE_JOIN_SQUAD, 15 * 24);
					saveUnitFrameCount = 24 * 10;
					saveUnitLevelVulture = 2;
				} else if (result == Result.Win) {
					saveUnitLevelVulture = 1;
				} else if (result == Result.BigWin) {
					saveUnitLevelVulture = 0;
				} 
			}
		}
		mechanicVulture.prepareMechanic(watchOrder, vultureEnemies);
		mechanicVulture.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), saveUnitLevelVulture);
		for (Unit vulture : microVulture.getUnits()) {
			mechanicVulture.executeMechanicMicro(vulture);
		}
		
		// 탱크 vs 탱크 전투 판단여부
		int saveUnitLevelTank = 1;
		if (InformationManager.Instance().enemyRace == Race.Terran) {
			if (microTank.getUnits().size() > 1 && closeTankEnemies.size() * 2 <= microTank.getUnits().size()) {
				saveUnitLevelTank = 1; // 거리재기 전진
			} else {
				saveUnitLevelTank = 2; // 안전거리 유지
			}
		}
		
		mechanicTank.prepareMechanic(attackerOrder, attackerEnemies);
		mechanicTank.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), initFrame, saveUnitLevelTank);
		mechanicGoliath.prepareMechanic(attackerOrder, attackerEnemies);
		
		for (Unit tank : microTank.getUnits()) {
			mechanicTank.executeMechanicMicro(tank);
		}
		for (Unit goliath : microGoliath.getUnits()) {
			mechanicGoliath.executeMechanicMicro(goliath);
		}
	}
	
	// checker, guerilla squad
	private void updateVultureSquad() {
		List<UnitInfo> vultureEnemies = new ArrayList<>();

		// 적 정보 수집. 명령이 하달된 3초간은 근처 적을 무시
		if (name.equals(SquadName.CHECKER)) {
			for (Unit vulture : microVulture.getUnits()) {
				TravelSite site = VultureTravelManager.Instance().getSquadSiteMap().get(vulture.getID());
				if (site != null) {
					if (site.visitAssignedFrame != 0 && site.visitAssignedFrame > MyBotModule.Broodwar.getFrameCount() - MicroSet.Vulture.IGNORE_MOVE_FRAME) {
						continue;
					}
				}
				InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
			}
		} else if (name.startsWith(SquadName.GUERILLA_)) {
			if (!VultureTravelManager.Instance().guerillaIgnoreModeEnabled(name)) {
				for (Unit vulture : microVulture.getUnits()) {
					InformationManager.Instance().getNearbyForce(vultureEnemies, vulture.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
				}
			}
			InformationManager.Instance().getNearbyForce(vultureEnemies, order.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
		}

//		List<Unit> peloton = new ArrayList<>();
//		Unit leader = MicroUtils.leaderOfUnit(microVulture.getUnits(), order.getPosition());
//		for (Unit vulture : microVulture.getUnits()) {
//			if (vulture == leader || vulture.getDistance(leader.getPosition()) < MicroSet.Vulture.PELOTON_RADIUS) {
//				peloton.add(vulture);
//			}
//		}

//		boolean saveUnit = !CombatExpectation.expectByUnitInfo(microVulture.getUnits(), vultureEnemies);
		int saveUnitLevel = 1;
		if (name.startsWith(SquadName.GUERILLA_)) {
			saveUnitLevel = 0;
		}
		mechanicVulture.prepareMechanic(order, vultureEnemies);
		mechanicVulture.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), saveUnitLevel);
		for (Unit vulture : microVulture.getUnits()) {
			mechanicVulture.executeMechanicMicro(vulture);
		}
	}
	
	private void updateDefenseSquad() {
		List<UnitInfo> nearbyEnemiesInfo = new ArrayList<>();
		if (order.getType() == SquadOrderType.DEFEND) {
			InformationManager.Instance().getNearbyForce(nearbyEnemiesInfo, order.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
		}
		
		mechanicVulture.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicVulture.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), 1);
		mechanicTank.prepareMechanic(order, nearbyEnemiesInfo);
		mechanicTank.prepareMechanicAdditional(microTank.getUnits(), microGoliath.getUnits(), 0, 1);
		mechanicGoliath.prepareMechanic(order, nearbyEnemiesInfo);
		
		for (Unit vulture : microVulture.getUnits()) {
			mechanicVulture.executeMechanicMicro(vulture);
		}
		for (Unit tank : microTank.getUnits()) {
			mechanicTank.executeMechanicMicro(tank);
		}
		for (Unit goliath : microGoliath.getUnits()) {
			mechanicGoliath.executeMechanicMicro(goliath);
		}

		List<Unit> nearbyEnemies = new ArrayList<>();
		if (order.getType() == SquadOrderType.DEFEND) {
			MapGrid.Instance().getUnitsNear(nearbyEnemies, order.getPosition(), order.getRadius(), false, true);
		}
		microMarine.setMicroInformation(order, nearbyEnemies);
		microMarine.execute();
	}
	
	private void updateUnits() {
		setAllUnits();
//		setNearEnemyUnits();
		addUnitsToMicroManagers();
	}
	
	public void setAllUnits() {
		// TOOD 필요시 사용
//		_hasAir = false;
//		_hasGround = false;
//		_hasAntiAir = false;
//		_hasAntiGround = false;
		List<Unit> validUnits = new ArrayList<>();
		for (Unit unit : unitSet) {
			if (CommandUtil.IsValidUnit(unit)) {
				validUnits.add(unit);
			}
		}
		unitSet = validUnits;
	}
	
//	public void setNearEnemyUnits() {
//		nearEnemy.clear();
//		
//		for (Unit unit : unitSet) {
//			if (!unit.exists() || unit.isLoaded()) {
//				continue;
//			}
//			nearEnemy.put(unit.getID(), unitNearEnemy(unit));
//		}
//	}
	
	public boolean unitNearEnemy(Unit unit) {
//		List<Unit> enemyNear = MapGrid.Instance().getUnitsNear(unit.getPosition(), 400, false, true);
//		return enemyNear.size() > 0;
		List<Unit> unitsInRadius = unit.getUnitsInRadius(400);
		for (Unit u : unitsInRadius) {
			if (u.getPlayer() == InformationManager.Instance().enemyPlayer) {
				return true;
			}
		}
		return false;
	}
	
	public void addUnitsToMicroManagers() {
		List<Unit> scvUnits = new ArrayList<>();
		List<Unit> marineUnits = new ArrayList<>();
		List<Unit> vultureUnits = new ArrayList<>();
		List<Unit> tankUnits = new ArrayList<>();
		List<Unit> goliathUnits = new ArrayList<>();
		List<Unit> wraithUnits = new ArrayList<>();
		List<Unit> vesselUnits = new ArrayList<>();

		for (Unit unit : unitSet) {
			if (!CommandUtil.IsValidUnit(unit) || unit.isLoaded()) {
				continue;
			}
			
			if (unit.getType() == UnitType.Terran_SCV) {
				scvUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Marine) {
				marineUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Vulture) {
				vultureUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				tankUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Goliath) {
				goliathUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Wraith) {
				wraithUnits.add(unit);
			} else if (unit.getType() == UnitType.Terran_Science_Vessel) {
				vesselUnits.add(unit);
			}
		}

		microScv.setUnits(scvUnits);
		microMarine.setUnits(marineUnits);
		microVulture.setUnits(vultureUnits);
		microTank.setUnits(tankUnits);
		microGoliath.setUnits(goliathUnits);
		microWraith.setUnits(wraithUnits);
		microVessel.setUnits(vesselUnits);
		
	}
	
	public void removeUnit(Unit unit) {
		// TODO Unit Object로 찾으면 제대로 못찾는 현상이 발생하는 것 같다. -> 추가확인필요
		// 그래서 일단은 unit ID로 index를 찾아 remove로 한다.
		for (int idx = 0; idx < unitSet.size(); idx ++) {
			if (unitSet.get(idx).getID() == unit.getID()) {
				unitSet.remove(idx);
				return;
			}
		}
	}
	
	public boolean isEmpty() {
		return unitSet.isEmpty();
	}
	
	public void clear() {
		// 전투에 참여한 일꾼이 있다면 idle 상태로 변경
		for (Unit unit : unitSet) {
			if (unit.getType().isWorker()) {
				WorkerManager.Instance().setIdleWorker(unit);
			}
		}
		unitSet.clear();
	}
	
	
	private boolean needsToRegroupChecker() {
		if (unitSet.isEmpty()) {
			return false;
		}
		Unit leader = MicroUtils.leaderOfUnit(unitSet, order.getPosition());
		for (Unit unit : unitSet) {
			if (unit.getID() == leader.getID()) {
				continue;
			}
			if (leader.getDistance(unit) > 500) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean needsToRegroup() {
		if (unitSet.isEmpty()) {
			return false;
		}

		if (order.getType() != SquadOrderType.ATTACK) {
			return false;
		}

		// If we're nearly maxed and have good income or cash, don't retreat.
		if (MyBotModule.Broodwar.self().supplyUsed() >= 390 &&
			(MyBotModule.Broodwar.self().minerals() > 1000 || WorkerManager.Instance().getNumMineralWorkers() > 12)) {
			attackAtMax = true;
		}

		if (attackAtMax) {
			if (MyBotModule.Broodwar.self().supplyUsed() < 320) {
				attackAtMax = false;
			} else {
				return false;
			}
		}

		UnitData unitData = InformationManager.Instance().getUnitData(InformationManager.Instance().enemyPlayer);
		Map<Integer, UnitInfo> enemyUnitInfo = unitData.getUnitAndUnitInfoMap();
		
		// if none of our units are in range of any enemy units, don't retreat
		boolean anyInRange = false;
		
		for (Integer unitId : enemyUnitInfo.keySet()) {
			UnitInfo eui = enemyUnitInfo.get(unitId);
			
			for (Unit u : unitSet) {
				if (!u.exists() || u.isLoaded()) {
					continue;
				}
				
				int range = 0;
				if (CommandUtil.CanAttack(eui.getUnit(), u)) {
					if (eui.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || eui.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
						range = UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 64;
					} else {
						range = eui.getType().sightRange();
					}
					range += 128;
					
					if (range >= eui.getLastPosition().getDistance(u.getPosition())) {
						anyInRange = true;
						break;
					}
				}
			}

			if (anyInRange) {
	            break;       // break out of outer loop
	        }
		}
		
		if (!anyInRange) {
			return false;
	    }

		int score = 0;

		//TODO do the SparCraft Simulation!
//		CombatSimulation sim;
//	    
//		sim.setCombatUnits(unitClosest->getPosition(), Config::Micro::CombatRegroupRadius);
//		score = sim.simulateCombat();

	    boolean retreat = score < 0;
	    int switchTime = 100;

	    // we should not attack unless 5 seconds have passed since a retreat
	    if (retreat != lastRetreatSwitchVal) {
	        if (!retreat && (MyBotModule.Broodwar.getFrameCount() - lastRetreatSwitch < switchTime)) {
	            retreat = lastRetreatSwitchVal;
	        } else {
	            lastRetreatSwitch = MyBotModule.Broodwar.getFrameCount();
	            lastRetreatSwitchVal = retreat;
	        }
	    }
		
		if (retreat) {
			MyBotModule.Broodwar.sendText("retreat");
		} else {
//			MyBotModule.Broodwar.sendText("attack");
		}

		return retreat;
	}
	
//	private Unit unitClosestToEnemy() {
//		Unit closest = null;
//		int closestDist = 100000;
//
//		for (Unit unit : unitSet) {
//			if (unit.getType().isDetector() || unit.isLoaded()) {
//				continue;
//			}
//
//			// the distance to the order position
//			int dist = MapTools.Instance().getGroundDistance(unit.getPosition(), order.getPosition());
//			if (dist != -1 && dist < closestDist) {
//				closest = unit;
//				closestDist = dist;
//			}
//		}
//
//		return closest;
//	}
	
//	private Position calcRegroupPosition() {
//		Position regroup = null;
//
//		int minDist = 100000;
//		for (Unit unit : unitSet) {
//			if (!nearEnemy.get(unit.getID()) && !unit.getType().isDetector() && !unit.isLoaded()) {
//				int dist = unit.getDistance(order.getPosition());
//				if (dist < minDist) {
//					minDist = dist;
//					regroup = unit.getPosition();
//				}
//			}
//		}
//
//		// Failing that, retreat to a base we own.
//		if (regroup == null) {
//			// Retreat to the main base (guaranteed not null, even if the buildings were destroyed).
//			BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//
//			// If the natural has been taken, retreat there instead.
//			BaseLocation natural = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
//			if (natural != null) {
//				List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);
//				for (BaseLocation b : occupiedBases) {
//					if (b == natural) {
//						base = natural;
//						break;
//					}
//				}
//			}
//			return BWTA.getRegion(base.getTilePosition()).getCenter();
//		}
//		
//		return regroup;
//	}
	
	@Override
	public String toString() {
		return "Squad [name=" + name + ", unitSet.size()=" + unitSet.size() + ", order=" + order + "]";
	}
	
}
