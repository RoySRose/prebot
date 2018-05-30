package prebot.micro.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.internal.IConditions.BaseCondition;
import prebot.micro.BaseLocationUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class ScvScoutControl extends Control {

	private Map<Integer, BaseLocation> scoutBaseMap = new HashMap<>();
	private Map<Integer, Integer> scoutVertexIndexMap = new HashMap<>();

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		for (Unit unit : unitList) {
			moveScoutUnit(unit);
		}
	}

	/// 정찰 유닛을 이동시킵니다
	// 상대방 MainBaseLocation 위치를 모르는 상황이면, StartLocation 들에 대해 아군의 MainBaseLocation에서 가까운 것부터 순서대로 정찰
	// 상대방 MainBaseLocation 위치를 아는 상황이면, 해당 BaseLocation 이 있는 Region의 가장자리를 따라 계속 이동함 (정찰 유닛이 죽을때까지)
	private void moveScoutUnit(Unit scoutScv) {
		BaseLocation enemyBaseLocation = InfoUtils.enemyBase();
		if (enemyBaseLocation == null) {
			BaseLocation scoutBaseLocation; 
			if (StrategyIdea.enemyBaseExpected != null) {
				scoutBaseLocation = StrategyIdea.enemyBaseExpected;
			} else {
				scoutBaseLocation = notExloredBaseLocationNearScoutScv(scoutScv);
				scoutBaseMap.put(scoutScv.getID(), scoutBaseLocation);
			}
			CommandUtils.move(scoutScv, scoutBaseLocation.getPosition());
		} else {
			if (!Prebot.Broodwar.isExplored(enemyBaseLocation.getTilePosition())) {
				CommandUtils.move(scoutScv, enemyBaseLocation.getPosition());
			} else {
				Position currentScoutTargetPosition = getScoutFleePositionFromEnemyRegionVertices(scoutScv);
				CommandUtils.move(scoutScv, currentScoutTargetPosition);
				// WorkerManager.Instance().setIdleWorker(scoutScv);
			}
		}
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
		BaseLocation notExploredBase = BaseLocationUtils.getGroundClosestBaseToPosition(BWTA.getStartLocations(), nearestBase, new BaseCondition() {
			@Override
			public boolean correspond(BaseLocation base) {
				return !Prebot.Broodwar.isExplored(base.getTilePosition()) && !otherScvScoutBaseList.contains(base);
			}
		});
		
		if (notExploredBase == null) {
			notExploredBase = BaseLocationUtils.getGroundClosestBaseToPosition(BWTA.getStartLocations(), nearestBase, new BaseCondition() {
				@Override
				public boolean correspond(BaseLocation base) {
					return !Prebot.Broodwar.isExplored(base.getTilePosition());
				}
			});
		}
		return notExploredBase;
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
			return Prebot.Broodwar.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		Integer vertexIndex = scoutVertexIndexMap.get(scoutWorker.getID());
		if (vertexIndex == null) {
			// so return the closest position in the polygon
			vertexIndex = getClosestVertexIndex(scoutWorker.getPosition(), regionVertices);

			if (vertexIndex == CommonCode.INDEX_NOT_FOUND) {
				return Prebot.Broodwar.self().getStartLocation().toPosition();
			} else {
				scoutVertexIndexMap.put(scoutWorker.getID(), vertexIndex);
				return regionVertices.get(vertexIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else {
			double distanceFromCurrentVertex = regionVertices.get(vertexIndex).getDistance(scoutWorker.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			int limit = 0;
			while (distanceFromCurrentVertex < 128 && limit < regionVertices.size()) {
				limit++;
				vertexIndex = (vertexIndex + 1) % regionVertices.size();
				distanceFromCurrentVertex = regionVertices.get(vertexIndex).getDistance(scoutWorker.getPosition());
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
}
