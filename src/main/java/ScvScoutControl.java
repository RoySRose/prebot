

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class ScvScoutControl extends Control {

	private Map<Integer, BaseLocation> scoutBaseMap = new HashMap<>();
	private Map<Integer, Integer> scoutVertexIndexMap = new HashMap<>();
	private boolean scoutFirstExpansionFlag = false;

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			moveScoutUnit(unit,euiList);
		}
	}

	/// 정찰 유닛을 이동시킵니다
	// 상대방 MainBaseLocation 위치를 모르는 상황이면, StartLocation 들에 대해 아군의 MainBaseLocation에서 가까운 것부터 순서대로 정찰
	// 상대방 MainBaseLocation 위치를 아는 상황이면, 해당 BaseLocation 이 있는 Region의 가장자리를 따라 계속 이동함 (정찰 유닛이 죽을때까지)
	private void moveScoutUnit(Unit scoutScv, Collection<UnitInfo> euiList) {
		BaseLocation enemyBaseLocation = InfoUtils.enemyBase();
		
		//BaseLocation enemyFirstExpansionLocation = InfoUtils.enemyFirstExpansion();
			
		
		if (enemyBaseLocation == null) {
			BaseLocation scoutBaseLocation;
			if (InfoUtils.enemyRace() == Race.Terran) {
				scoutBaseLocation = notExloredFarthestBaseLocation(scoutScv);
			} else {
				scoutBaseLocation = notExloredBaseLocationNearScoutScv(scoutScv);
			}
			scoutBaseMap.put(scoutScv.getID(), scoutBaseLocation);
			for (UnitInfo eui : euiList) {
				if (isCloseDangerousTarget(scoutScv, eui)) {
					FleeOption fOption = new FleeOption(scoutBaseLocation.getPoint(), false, MicroConfig.Angles.WIDE);
					MicroUtils.fleeScout(scoutScv, eui.getLastPosition(), fOption);
					return;
				}else{
					CommandUtils.move(scoutScv, scoutBaseLocation.getPosition());
					return;
				}
			}
			
			CommandUtils.move(scoutScv, scoutBaseLocation.getPosition());
		} else {
			if (!MyBotModule.Broodwar.isExplored(enemyBaseLocation.getTilePosition())) {
				CommandUtils.move(scoutScv, enemyBaseLocation.getPosition());
			} else {
				Position currentScoutTargetPosition = getScoutFleePositionFromEnemyRegionVertices(scoutScv);
				if(MyBotModule.Broodwar.getFrameCount() % 2000 == 0){
					scoutFirstExpansionFlag = true;
				}
				if(scoutFirstExpansionFlag){
					if(canMoveFirstExpansion(scoutScv,enemyBaseLocation)){
						BaseLocation enemyFisrtExpansionPosition = getClosestFirstExpansionBase(enemyBaseLocation);
						if (!MyBotModule.Broodwar.isVisible(enemyFisrtExpansionPosition.getTilePosition())) {
							CommandUtils.move(scoutScv, enemyFisrtExpansionPosition.getPosition());
						}else{
							CommandUtils.move(scoutScv, currentScoutTargetPosition);
							scoutFirstExpansionFlag = false;
						}
					}else{
						CommandUtils.move(scoutScv, currentScoutTargetPosition);
					}
				}else{
					CommandUtils.move(scoutScv, currentScoutTargetPosition);
				}
				// WorkerManager.Instance().setIdleWorker(scoutScv);
			}
		}
		/*if (enemyBaseLocation == null) {
			BaseLocation scoutBaseLocation; 
			if (StrategyIdea.enemyBaseExpected != null) {
				scoutBaseLocation = StrategyIdea.enemyBaseExpected;
			} else {
				scoutBaseLocation = notExloredBaseLocationNearScoutScv(scoutScv);
				scoutBaseMap.put(scoutScv.getID(), scoutBaseLocation);
			}
			if(scoutBaseLocation != null){
				BaseLocation enemyFisrtExpansionPosition = getClosestFirstExpansionBase(scoutBaseLocation);
				if (!Prebot.Broodwar.isExplored(enemyFisrtExpansionPosition.getTilePosition())) {
					CommandUtils.move(scoutScv, enemyFisrtExpansionPosition.getPosition());
				}else{
					CommandUtils.move(scoutScv, scoutBaseLocation.getPosition());
				}
			}else{
				CommandUtils.move(scoutScv, scoutBaseLocation.getPosition());
			}
			
		} else {
			BaseLocation enemyFisrtExpansionPosition = getClosestFirstExpansionBase(enemyBaseLocation);
			if (!Prebot.Broodwar.isExplored(enemyFisrtExpansionPosition.getTilePosition())) {
				CommandUtils.move(scoutScv, enemyFisrtExpansionPosition.getPosition());
			}else if (!Prebot.Broodwar.isExplored(enemyBaseLocation.getTilePosition())) {
				CommandUtils.move(scoutScv, enemyBaseLocation.getPosition());
			} else {
				Position currentScoutTargetPosition = getScoutFleePositionFromEnemyRegionVertices(scoutScv);
				CommandUtils.move(scoutScv, currentScoutTargetPosition);
				// WorkerManager.Instance().setIdleWorker(scoutScv);
			}
		}*/
	}

	private boolean canMoveFirstExpansion(Unit scoutScv, BaseLocation enemyBaseLocation) {
		// TODO Auto-generated method stub
		Chokepoint nearestChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer);
		if (nearestChoke.getCenter().getDistance(scoutScv) < 300 && !scoutScv.isUnderAttack()) {
			return true;
		}
		
		return false;
	}

	/** 정찰되지 않은 SCV근처에 있는 base (정찰 scv가 2기 이상인 경우 겹치지 않도록 한다.) */
	private BaseLocation notExloredBaseLocationNearScoutScv(Unit scoutScv) {
		final List<BaseLocation> otherScvScoutBaseList = new ArrayList<>();
		for (Integer unitId : scoutBaseMap.keySet()) {
			if (unitId == scoutScv.getID()) {
				continue;
			}
			BaseLocation base = scoutBaseMap.get(unitId);
			if (base != null) {
				otherScvScoutBaseList.add(base);
			}
		}
		
		BaseLocation nearestBase = BaseLocationUtils.getClosestBaseToPosition(BWTA.getStartLocations(), scoutScv.getPosition());
		BaseLocation notExploredBase = BaseLocationUtils.getGroundClosestBaseToPosition(BWTA.getStartLocations(), nearestBase, new IConditions.BaseCondition() {
			@Override
			public boolean correspond(BaseLocation base) {
				return !MyBotModule.Broodwar.isExplored(base.getTilePosition()) && !otherScvScoutBaseList.contains(base);
			}
		});
		
		if (notExploredBase == null) {
			notExploredBase = BaseLocationUtils.getGroundClosestBaseToPosition(BWTA.getStartLocations(), nearestBase, new IConditions.BaseCondition() {
				@Override
				public boolean correspond(BaseLocation base) {
					return !MyBotModule.Broodwar.isExplored(base.getTilePosition());
				}
			});
		}
		return notExploredBase;
	}
	
	/** 본진에서 먼 곳부터 정찰 (대각선 정찰) */
	private BaseLocation notExloredFarthestBaseLocation(Unit scv) {
		BaseLocation notExploredFarthestBase = BaseLocationUtils.getGroundFarthestBaseToPosition(BWTA.getStartLocations(), InfoUtils.myBase(), new IConditions.BaseCondition() {
			@Override
			public boolean correspond(BaseLocation base) {
				return !MyBotModule.Broodwar.isExplored(base.getTilePosition());
			}
		});
		return notExploredFarthestBase;
	}

	private Position getScoutFleePositionFromEnemyRegionVertices(Unit scoutWorker) {
		BaseLocation enemyBase = InfoUtils.enemyBase();
		
		// calculate enemy region vertices if we haven't yet
		Vector<Position> regionVertices = InfoUtils.getRegionVertices(enemyBase);
		if (regionVertices == null || regionVertices.isEmpty()) {
			InformationManager.Instance().calculateEnemyRegionVertices(enemyBase);
			regionVertices = InfoUtils.getRegionVertices(enemyBase);
		}
		
		if (regionVertices.isEmpty()) {
			return MyBotModule.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		Integer vertexIndex = scoutVertexIndexMap.get(scoutWorker.getID());
		if (vertexIndex == null) {
			// so return the closest position in the polygon
			vertexIndex = getClosestVertexIndex(scoutWorker.getPosition(), regionVertices);

			if (vertexIndex == CommonCode.INDEX_NOT_FOUND) {
				return MyBotModule.Broodwar.self().getStartLocation().toPosition();
			} else {
				scoutVertexIndexMap.put(scoutWorker.getID(), vertexIndex);
				return regionVertices.get(vertexIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else {
			if (regionVertices.size() - 1 < vertexIndex) {
				// scout scv가 오래 살아남았을 때 regionVertices가 변경되어 ArrayIndexOutOfBoundsException 발생 방지
				vertexIndex = 0;
				
			} else {
				double distanceFromCurrentVertex = regionVertices.get(vertexIndex).getDistance(scoutWorker.getPosition());

				// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
				int limit = 0;
				while (distanceFromCurrentVertex < 128 && limit < regionVertices.size()) {
					limit++;
					vertexIndex = (vertexIndex + 1) % regionVertices.size();
					distanceFromCurrentVertex = regionVertices.get(vertexIndex).getDistance(scoutWorker.getPosition());
				}
			}
			scoutVertexIndexMap.put(scoutWorker.getID(), vertexIndex);
			return regionVertices.get(vertexIndex);
		}
	}

	public int getClosestVertexIndex(Position position, Vector<Position> regionVertices) {
		int closestIndex = CommonCode.INDEX_NOT_FOUND;
		double closestDistance = CommonCode.DOUBLE_MAX;

		for (int i = 0; i < regionVertices.size(); i++) {
			double dist = position.getDistance(regionVertices.get(i));
			if (dist < closestDistance) {
				closestDistance = dist;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
	
	public BaseLocation getClosestFirstExpansionBase(BaseLocation scoutBaseLocation) {
		double tempDistance;
		double closestDistance = 1000000000;
		BaseLocation expansionBase = scoutBaseLocation;
		for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
		{
			if (targetBaseLocation.getTilePosition().equals(scoutBaseLocation.getTilePosition())) continue;

			tempDistance = PositionUtils.getGroundDistance(scoutBaseLocation.getPosition(), targetBaseLocation.getPosition());
			if (tempDistance < closestDistance && tempDistance > 0) {
				closestDistance = tempDistance;
				expansionBase = targetBaseLocation;
			}
		}
		return expansionBase;
		
	}
	
	private boolean isCloseDangerousTarget(Unit myUnit, UnitInfo eui) {
		boolean enemyIsComplete = eui.isCompleted();
		Position enemyPosition = eui.getLastPosition();
		UnitType enemyUnitType = eui.getType();
		
		Unit enemyUnit = UnitUtils.unitInSight(eui);
		if (UnitUtils.isValidUnit(enemyUnit)) {
			enemyIsComplete = enemyUnit.isCompleted();
			enemyPosition = enemyUnit.getPosition();
			enemyUnitType = enemyUnit.getType();
		}

		// 접근하면 안되는 거리인지 있는지 판단
		int distanceToNearEnemy = myUnit.getDistance(enemyPosition);
		int enemyWeaponRange = 0;

		if (enemyUnitType == UnitType.Terran_Bunker) {
			enemyWeaponRange = MyBotModule.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 96;
		} else {
			enemyWeaponRange = MyBotModule.Broodwar.enemy().weaponMaxRange(enemyUnitType.groundWeapon());
		}
		return distanceToNearEnemy <= enemyWeaponRange + 64;
	}
}
