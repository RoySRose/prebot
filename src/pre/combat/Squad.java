package pre.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import pre.MapGrid;
import pre.UnitData;
import pre.UnitInfo;
import pre.combat.SquadOrder.SquadOrderType;
import pre.combat.micro.MicroGoliath;
import pre.combat.micro.MicroMarine;
import pre.combat.micro.MicroScv;
import pre.combat.micro.MicroTank;
import pre.combat.micro.MicroVessel;
import pre.combat.micro.MicroVulture;
import pre.combat.micro.MicroWraith;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.manager.WorkerManager;
import pre.util.CommandUtil;
import pre.util.MicroSet;
import pre.util.MicroUtils;

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

//		boolean needToRegroup = false; //needsToRegroup();
//		if (order.getType() == SquadOrderType.CHECK_ACTIVE) {
//			needToRegroup = needsToRegroupChecker();
//		}
		
		// * 공격스쿼드 : 탱크가 주력인 메카닉의 경우, 탱크 중심으로 squad지역을 설정해 유닛이 분산되지 않도록 한다.
		// TODO 1. 로템 센터 지형물 등에 낑기어 탱크자체가 분산되는 현상
		// TODO 2. 골리앗 중심 부대에도 문제가 없는지 확인 필요
		List<Unit> tanks = microTank.getUnits();
		Position centerOfUnits = null;
		int squadAreaRange = 0;
		if (tanks.size() >= MicroSet.Common.TANK_SQUAD_SIZE) {
			centerOfUnits = MicroUtils.centerOfUnits(tanks);
			squadAreaRange = MicroUtils.calcArriveDecisionRange(UnitType.Terran_Siege_Tank_Tank_Mode, unitSet.size());
		} else {
			centerOfUnits = MicroUtils.centerOfUnits(unitSet);
			squadAreaRange = MicroUtils.calcArriveDecisionRange(UnitType.Terran_Vulture, unitSet.size()); // 유닛별 시야 : SCV:224, 저글링:160, 벌처:256, 탱크:320, 배틀크루져:352
		}
		
		// 방어병력은 눈앞의 적을 무시하고 방어를 위해 이동해야 한다.
		List<Unit> nearbyEnemies = new ArrayList<>();
		if (order.getType() == SquadOrderType.DEFEND) {
//			InformationManager.Instance().getNearbyForce(unitInfoList, order.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
			MapGrid.Instance().getUnitsNear(nearbyEnemies, order.getPosition(), order.getRadius(), false, true);
		} else {
			for (Unit unit : unitSet) {
//				InformationManager.Instance().getNearbyForce(unitInfoList, unit.getPosition(), InformationManager.Instance().enemyPlayer, order.getRadius());
				MapGrid.Instance().getUnitsNear(nearbyEnemies, unit.getPosition(), order.getRadius(), false, true);
			}
		}
		
//		System.out.println("** Squad = " + name);
//		System.out.println("vulture : " + microVulture.getUnits().size());
//		System.out.println("tank : " + microTank.getUnits().size());
//		System.out.println("goliath : " + microGoliath.getUnits().size());
		
		microScv.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		microMarine.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		microVulture.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		microTank.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		microGoliath.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		microWraith.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		microVessel.setMicroInformation(order, nearbyEnemies, centerOfUnits, squadAreaRange, tanks.size());
		
		microScv.execute();
		microMarine.execute();
		microVulture.execute();
		microTank.execute();
		microGoliath.execute();
		microWraith.execute();
		microVessel.execute();
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
		return "Squad [name=" + name + ", order=" + order + ", unitSet.size()=" + unitSet.size() + "]";
	}
	
}
