

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Order;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;

public class MicroBuilding extends MicroManager {
	
	// 유닛별 실행시 orderMap에서 unitId를 key로 하여 각 unit별 order를 사용한다.
	private static Map<Integer, SquadOrder> orderMap = new HashMap<>();
	
	public static void setUnitOrder(Integer unitId, SquadOrder unitOrder) {
		orderMap.put(unitId, unitOrder);
	}
	public static void getUnitOrder(Integer unitId) {
		orderMap.get(unitId);
	}
	
	public static void removeInvalidUnitOrder() {
		List<Integer> removeKeys = new ArrayList<>();
		for (Integer unitId : orderMap.keySet()) {
			if (!CommandUtil.IsValidUnit(MyBotModule.Broodwar.getUnit(unitId))) {
				removeKeys.add(unitId);
			}
		}
		for (Integer unitId : removeKeys) {
			orderMap.remove(unitId);
		}
	}
	
	 
	private final static int sVesselCheckRadius = UnitType.Terran_Science_Vessel.sightRange()+400;

	@Override
	protected void executeMicro(List<Unit> targets) {
	    List<Unit> Buildings = getUnits();

	    Unit leader = null;
		List<Unit> units = CombatManager.Instance().squadData.getSquad(SquadName.MAIN_ATTACK).getUnitSet();
		if (units != null && !units.isEmpty()) {
			leader = MicroUtils.leaderOfUnit(units, CombatManager.Instance().getMainAttackLocation(CombatManager.Instance().squadData.getSquad(SquadName.MAIN_ATTACK)));
		}
		
		for (Unit flyBuilding : Buildings) {
			
			if(leader == null){
				flyBuilding.move(order.getPosition());
				return;
			}
			
			Unit mostDangerousTarget = null;
			double mostDangercheck = -99999;
			
			List<Unit> unitsAroundList = new ArrayList<Unit>();
			unitsAroundList = MyBotModule.Broodwar.getUnitsInRadius(flyBuilding.getPosition(), sVesselCheckRadius);
			
			List<Unit> dangerous_targets = new ArrayList<Unit>();
			
			for(Unit check_targets : unitsAroundList){
				if(check_targets == null){break;}
				if(check_targets.getPlayer() == InformationManager.Instance().enemyPlayer){
					if(check_targets.getType().airWeapon() != null){
						dangerous_targets.add(check_targets);
					}
				}
//				else{
//					myUnits.add(check_targets);
//				}
			}
			
			for (Unit target : dangerous_targets) {
				if(target == null){break;}
				double temp = target.getType().airWeapon().maxRange() - target.getPosition().getDistance(flyBuilding.getPosition()); 
				if(temp > mostDangercheck){
					mostDangerousTarget = target;
					mostDangercheck = temp;
				}
			}
			
			boolean fleeing = false;
			if(mostDangerousTarget != null){
				if(mostDangerousTarget.isInWeaponRange(flyBuilding)){
					if(leader.isInWeaponRange(mostDangerousTarget)){
						fleeing = false;
					}else{
						fleeing = true;
					}
				}
			}
			
			List<UnitInfo> foggedEnemyUnits = null;
			foggedEnemyUnits = InformationManager.Instance().getEnemyBuildingUnitsNear(leader, 1000, true, true, true);
			foggedEnemyUnits.addAll(InformationManager.Instance().getEnemyUnits(UnitType.Terran_Siege_Tank_Siege_Mode));
			
			UnitInfo closestTarget = null;
			if(foggedEnemyUnits.size() > 0){
				int minimumDistance = 999999;
				for(UnitInfo fogUnit : foggedEnemyUnits){
					int dist = MapTools.Instance().getGroundDistance(fogUnit.getLastPosition(), leader.getPosition());
					if (dist < minimumDistance) {
						closestTarget = fogUnit;
						minimumDistance = dist;
					}
				}
			}
			
			if(fleeing){
				order.setPosition(leader.getPosition());
			}else{
				
				if(mostDangerousTarget != null){
					if(flyBuilding.getDistance(leader) > 128){
						order.setPosition(flyBuilding.getPosition());
					}
				}
				if(closestTarget != null){
					if(closestTarget.getUnit().isVisible() == false){
						order.setPosition(closestTarget.getLastPosition());
					}
				}
				if(flyBuilding.getDistance(leader) > 220){
					order.setPosition(leader.getPosition());
				}
			}
			
			CommandUtil.move(flyBuilding,order.getPosition());
		}
	}
}
