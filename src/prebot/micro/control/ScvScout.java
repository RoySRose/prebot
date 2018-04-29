package prebot.micro.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.brain.Idea;
import prebot.common.code.Code.CommonCode;
import prebot.common.code.ConfigForDebug.UX;
import prebot.common.util.BaseLocationUtils;
import prebot.common.util.CommandUtils;
import prebot.common.util.MapTools;
import prebot.main.Prebot;
import prebot.main.manager.InformationManager;

public class ScvScout extends Control {
	
	private static Vector<Position> enemyBaseRegionVertices = new Vector<Position>();
	private Map<Integer, Integer> scoutVertexIndexMap = new HashMap<>();

	@Override
	public void control() {
		for (Unit unit : unitList) {
			moveScoutUnit(unit);
		}
	}

	/// 정찰 유닛을 이동시킵니다
	// 상대방 MainBaseLocation 위치를 모르는 상황이면, StartLocation 들에 대해 아군의 MainBaseLocation에서 가까운 것부터 순서대로 정찰
	// 상대방 MainBaseLocation 위치를 아는 상황이면, 해당 BaseLocation 이 있는 Region의 가장자리를 따라 계속 이동함 (정찰 유닛이 죽을때까지)
	private void moveScoutUnit(Unit scoutScv) {
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(Prebot.Game.enemy());
		if (enemyBaseLocation == null) {
			BaseLocation scoutBaseLocation = notExloredBaseLocationNearScoutScv(scoutScv);
			CommandUtils.move(scoutScv, scoutBaseLocation.getPosition());
		} else {
			if (!Prebot.Game.isExplored(enemyBaseLocation.getTilePosition())) {
				CommandUtils.move(scoutScv, enemyBaseLocation.getPosition());
			} else {
				Position currentScoutTargetPosition = getScoutFleePositionFromEnemyRegionVertices(scoutScv);
				CommandUtils.move(scoutScv, currentScoutTargetPosition);
				// WorkerManager.Instance().setIdleWorker(scoutScv);
			}
		}
	}
	
	private BaseLocation notExloredBaseLocationNearScoutScv(Unit scoutScv) {
		if (Idea.of().enemeyExpectedBase != null) {
			return Idea.of().enemeyExpectedBase;
		}
		BaseLocation nearestBase = BaseLocationUtils.getClosestBaseToPosition(BWTA.getStartLocations(), scoutScv.getPosition());
		return BaseLocationUtils.getGroundClosestBaseToPositionNotExplored(BWTA.getStartLocations(), nearestBase);
	}

	private Position getScoutFleePositionFromEnemyRegionVertices(Unit scoutWorker) {
		// calculate enemy region vertices if we haven't yet
		if (enemyBaseRegionVertices.isEmpty()) {
			calculateEnemyRegionVertices();
		}
		if (enemyBaseRegionVertices.isEmpty()) {
			return Prebot.Game.self().getStartLocation().toPosition();
		}

		// if this is the first flee, we will not have a previous perimeter index
		Integer vertexIndex = scoutVertexIndexMap.get(scoutWorker.getID());
		if (vertexIndex == null) {
			// so return the closest position in the polygon
			vertexIndex = getClosestVertexIndex(scoutWorker.getPosition());

			if (vertexIndex == CommonCode.INDEX_NOT_FOUND) {
				return Prebot.Game.self().getStartLocation().toPosition();
			} else {
				scoutVertexIndexMap.put(scoutWorker.getID(), vertexIndex);
				return enemyBaseRegionVertices.get(vertexIndex);
			}
		}
		// if we are still fleeing from the previous frame, get the next location if we are close enough
		else {
			double distanceFromCurrentVertex = enemyBaseRegionVertices.get(vertexIndex).getDistance(scoutWorker.getPosition());

			// keep going to the next vertex in the perimeter until we get to one we're far enough from to issue another move command
			int limit = 0;
			while (distanceFromCurrentVertex < 128 && limit < enemyBaseRegionVertices.size()) {
				limit++;
				vertexIndex = (vertexIndex + 1) % enemyBaseRegionVertices.size();
				distanceFromCurrentVertex = enemyBaseRegionVertices.get(vertexIndex).getDistance(scoutWorker.getPosition());
			}
			scoutVertexIndexMap.put(scoutWorker.getID(), vertexIndex);
			return enemyBaseRegionVertices.get(vertexIndex);
		}
	}

	// Enemy MainBaseLocation 이 있는 Region 의 가장자리를 enemyBaseRegionVertices 에 저장한다
	// Region 내 모든 건물을 Eliminate 시키기 위한 지도 탐색 로직 작성시 참고할 수 있다
	private void calculateEnemyRegionVertices() {
		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(Prebot.Game.enemy());
		if (enemyBase == null) {
			return;
		}
		Region enemyRegion = enemyBase.getRegion();
		if (enemyRegion == null) {
			return;
		}
		
		final Position basePosition = Prebot.Game.self().getStartLocation().toPosition();
		final Vector<TilePosition> closestTobase = MapTools.getClosestTilesTo(basePosition);
		Set<Position> unsortedVertices = new HashSet<Position>();

		// check each tile position
		for (final TilePosition tp : closestTobase) {
			if (BWTA.getRegion(tp) != enemyRegion) {
				continue;
			}

			// a tile is 'surrounded' if
			// 1) in all 4 directions there's a tile position in the current region
			// 2) in all 4 directions there's a buildable tile
			boolean surrounded = true;
			if (BWTA.getRegion(new TilePosition(tp.getX() + 1, tp.getY())) != enemyRegion || !Prebot.Game.isBuildable(new TilePosition(tp.getX() + 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() + 1)) != enemyRegion || !Prebot.Game.isBuildable(new TilePosition(tp.getX(), tp.getY() + 1))
					|| BWTA.getRegion(new TilePosition(tp.getX() - 1, tp.getY())) != enemyRegion || !Prebot.Game.isBuildable(new TilePosition(tp.getX() - 1, tp.getY()))
					|| BWTA.getRegion(new TilePosition(tp.getX(), tp.getY() - 1)) != enemyRegion || !Prebot.Game.isBuildable(new TilePosition(tp.getX(), tp.getY() - 1))) {
				surrounded = false;
			}

			// push the tiles that aren't surrounded
			// Region의 가장자리 타일들만 추가한다
			if (!surrounded && Prebot.Game.isBuildable(tp)) {
				if (UX.DrawScoutInfo) {
					int x1 = tp.getX() * 32 + 2;
					int y1 = tp.getY() * 32 + 2;
					int x2 = (tp.getX() + 1) * 32 - 2;
					int y2 = (tp.getY() + 1) * 32 - 2;
					Prebot.Game.drawTextMap(x1 + 3, y1 + 2, "" + BWTA.getGroundDistance(tp, basePosition.toTilePosition()));
					Prebot.Game.drawBoxMap(x1, y1, x2, y2, Color.Green, false);
				}

				unsortedVertices.add(new Position(tp.toPosition().getX() + 16, tp.toPosition().getY() + 16));
			}
		}

		Vector<Position> sortedVertices = new Vector<Position>();
		Position current = unsortedVertices.iterator().next();
		enemyBaseRegionVertices.add(current);
		unsortedVertices.remove(current);

		// while we still have unsorted vertices left, find the closest one remaining to current
		while (!unsortedVertices.isEmpty()) {
			double bestDist = 1000000;
			Position bestPos = null;

			for (final Position pos : unsortedVertices) {
				double dist = pos.getDistance(current);

				if (dist < bestDist) {
					bestDist = dist;
					bestPos = pos;
				}
			}

			current = bestPos;
			sortedVertices.add(bestPos);
			unsortedVertices.remove(bestPos);
		}

		// let's close loops on a threshold, eliminating death grooves
		int distanceThreshold = 100;

		while (true) {
			// find the largest index difference whose distance is less than the threshold
			int maxFarthest = 0;
			int maxFarthestStart = 0;
			int maxFarthestEnd = 0;

			// for each starting vertex
			for (int i = 0; i < (int) sortedVertices.size(); ++i) {
				int farthest = 0;
				int farthestIndex = 0;

				// only test half way around because we'll find the other one on the way back
				for (int j = 1; j < sortedVertices.size() / 2; ++j) {
					int jindex = (i + j) % sortedVertices.size();

					if (sortedVertices.get(i).getDistance(sortedVertices.get(jindex)) < distanceThreshold) {
						farthest = j;
						farthestIndex = jindex;
					}
				}

				if (farthest > maxFarthest) {
					maxFarthest = farthest;
					maxFarthestStart = i;
					maxFarthestEnd = farthestIndex;
				}
			}

			// stop when we have no long chains within the threshold
			if (maxFarthest < 4) {
				break;
			}

			double dist = sortedVertices.get(maxFarthestStart).getDistance(sortedVertices.get(maxFarthestEnd));

			Vector<Position> temp = new Vector<Position>();

			for (int s = maxFarthestEnd; s != maxFarthestStart; s = (s + 1) % sortedVertices.size()) {

				temp.add(sortedVertices.get(s));
			}

			sortedVertices = temp;
		}

		enemyBaseRegionVertices = sortedVertices;
	}

	/// 적군의 Main Base Location 이 있는 Region 의 경계선에 해당하는 Vertex 들의 목록을 리턴합니다
	public Vector<Position> getEnemyRegionVertices() {
		return enemyBaseRegionVertices;
	}

	public int getClosestVertexIndex(Position position) {
		int closestIndex = CommonCode.INDEX_NOT_FOUND;
		double closestDistance = CommonCode.DOUBLE_MAX;

		for (int i = 0; i < enemyBaseRegionVertices.size(); i++) {
			double dist = position.getDistance(enemyBaseRegionVertices.get(i));
			if (dist < closestDistance) {
				closestDistance = dist;
				closestIndex = i;
			}
		}

		return closestIndex;
	}
}
