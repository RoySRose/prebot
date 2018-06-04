package prebot.micro.old.control;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroCode.OldSquadName;
import prebot.micro.old.OldMicroUtils;
import prebot.micro.old.OldCombatManager;
import prebot.micro.old.OldSquadOrder;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public class MicroBuilding extends MicroManager {
	
	// 유닛별 실행시 orderMap에서 unitId를 key로 하여 각 unit별 order를 사용한다.
	private static Map<Integer, OldSquadOrder> orderMap = new HashMap<>();
	
	public static void setUnitOrder(Integer unitId, OldSquadOrder unitOrder) {
		orderMap.put(unitId, unitOrder);
	}
	public static void getUnitOrder(Integer unitId) {
		orderMap.get(unitId);
	}
	
	public static void removeInvalidUnitOrder() {
		List<Integer> removeKeys = new ArrayList<>();
		for (Integer unitId : orderMap.keySet()) {
			if (!UnitUtils.isValidUnit(Prebot.Broodwar.getUnit(unitId))) {
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
		List<Unit> units = OldCombatManager.Instance().squadData.getSquad(OldSquadName.MAIN_ATTACK).getUnitSet();
		
		Position GoalPos = order.getPosition();
		
		
		if (units != null && !units.isEmpty()) {
			leader = OldMicroUtils.leaderOfUnit(units, OldCombatManager.Instance().getMainAttackLocation(OldCombatManager.Instance().squadData.getSquad(OldSquadName.MAIN_ATTACK)));
		}
		
		Position LeaderPos = null;
		
		
		if(leader != null){
			LeaderPos = leader.getPosition();
		}
		
		Position halfway = null;
		Chokepoint SC = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer);
		if(InformationManager.Instance().enemyRace == Race.Terran) {
			halfway = new Position((SC.getX()*3+2048)/4, (SC.getY()*3+2048)/4);
		}else{
			halfway = new Position((SC.getX()*5+2048)/6, (SC.getY()*5+2048)/6);
		}
		
		//Position halfwaychecker = new Position((SC.getX()+2048)/2, (SC.getY()+2048)/2);
		
		for (Unit flyBuilding : Buildings) {
			if (!TimeUtils.executeUnitRotation(flyBuilding, LagObserver.groupsize())) {
				continue;
			}
			
			if(leader == null){
				GoalPos = halfway;
				CommandUtils.move(flyBuilding,GoalPos);
				return;
			}
			
			Unit mostDangerousTarget = null;
			double mostDangercheck = -99999;
			
			List<Unit> unitsAroundList = new ArrayList<Unit>();
			unitsAroundList = Prebot.Broodwar.enemy().getUnits();
			
			List<Unit> dangerous_targets = new ArrayList<Unit>();
			
			for(Unit check_targets : unitsAroundList){
				if(check_targets == null){break;}
				if(check_targets.getType().airWeapon() != null){
					dangerous_targets.add(check_targets);
				}
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
			
			List<UnitInfo> foggedEnemyUnits = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL,
					UnitType.Protoss_Photon_Cannon,
					UnitType.Zerg_Sunken_Colony,
					UnitType.Terran_Missile_Turret,
					UnitType.Terran_Siege_Tank_Siege_Mode,
					UnitType.Terran_Bunker);
			
			UnitInfo closestTarget = null;
			if(foggedEnemyUnits.size() > 0){
				int minimumDistance = 999999;
				for(UnitInfo fogUnit : foggedEnemyUnits){
					int dist = leader.getDistance(fogUnit.getLastPosition());
					if (dist < minimumDistance) {
						closestTarget = fogUnit;
						minimumDistance = dist;
					}
				}
			}
			
			if(fleeing){
				GoalPos = LeaderPos;
			}else{
				
				if(mostDangerousTarget != null){
					if(flyBuilding.getDistance(leader) > 128){
						GoalPos = flyBuilding.getPosition();
					}
				}
				if(closestTarget != null){
					if(closestTarget.getUnit().isVisible() == false){
						GoalPos = closestTarget.getLastPosition();
					}
				}
				if(flyBuilding.getDistance(leader) > 350){
					GoalPos = LeaderPos;
				}
				if(LeaderPos.getDistance(order.getPosition()) > SC.getDistance(order.getPosition())){
					GoalPos = halfway;
				}
				
				if(LeaderPos.getDistance(order.getPosition()) < flyBuilding.getDistance(order.getPosition())){
					GoalPos = order.getPosition();
				}
			}
			
			CommandUtils.move(flyBuilding,GoalPos);
		}
	}
}
