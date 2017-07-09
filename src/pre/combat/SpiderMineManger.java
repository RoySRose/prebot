package pre.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import pre.MapGrid;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.MicroSet;
import pre.util.MicroUtils;

public class SpiderMineManger {
	
	private static int MINE_EXACT_RADIUS = 10;
	private static int MINE_SPREAD_RADIUS = 500;

	private static int MINE_BETWEEN_DIST = 50;
	private static int MINE_NUM_PER_POSITION = 3;
	private static int RESV_EXPIRE_FRAME = 24 * 3;
	
	private Map<Integer, MineReserved> mineReservedMap;
	private List<Position> goodPositions;

	private static SpiderMineManger instance = new SpiderMineManger();
	
	private SpiderMineManger() {}
	
	public static SpiderMineManger Instance() {
		return instance;
	}
	
	public void update() {
		if (!MicroSet.Upgrade.hasResearched(TechType.Spider_Mines)) {
			return;
		}
		List<Integer> expiredList = new ArrayList<>();
		for (Integer unitId : mineReservedMap.keySet()) {
			MineReserved mineReserved = mineReservedMap.get(unitId);
			if (mineReserved.reservedFrame + RESV_EXPIRE_FRAME < MyBotModule.Broodwar.getFrameCount()) { // 5초 지났으면 삭제
				System.out.println("expired mine position : " + mineReserved.positionToMine);
				expiredList.add(unitId);
			}
//			MyBotModule.Broodwar.drawCircleScreen(mineReserved.positionToMine, 100, Color.White);
		}
		
		for (Integer unitId : expiredList) {
			mineReservedMap.remove(unitId);
		}
	}

	public Position getPositionReserved(Unit vulture) {
		if (!MicroSet.Upgrade.hasResearched(TechType.Spider_Mines) || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		MineReserved mineReserved = mineReservedMap.get(vulture.getID());
		if (mineReserved != null) {
			return mineReserved.positionToMine;
		} else {
			return null;
		}
	}
	
	
	public void setGoodPosition() {
		mineReservedMap = new HashMap<>();
		goodPositions = new ArrayList<>();
		
		BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer); // 하지마라
		BaseLocation myFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer); // ㄴㄴ
//		Chokepoint myFirstChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer); // 별로인 위치
//		Chokepoint mySecondChoke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer); // 앞마당 멀티가 늦으면 좋음
//		Position myReadyToAttack = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer); // 좋은 위치지만 헌터에서도 유용할지는 의문
//		
//		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer); // region이 좋음
//		BaseLocation enemyFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer); // 좋다.
//		Chokepoint enemyFirstChoke = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer); // 좋음
//		Chokepoint enemySecondChoke = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer); // 좋음
		
//		Position center = new Position(2048, 2048); // 128x128 맵의 센터
		
		for (BaseLocation base : BWTA.getBaseLocations()) {
			if (base.equals(myBase) || base.equals(myFirstExpansion)) {
				continue;
			}
			
			goodPositions.add(base.getPosition());
		}
	}
	
	public Position getPositionToMine(Unit vulture) {
		if (!MicroSet.Upgrade.hasResearched(TechType.Spider_Mines) || vulture == null || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		
		Position minePosition = getPositionReserved(vulture);
		if (minePosition != null) {
			return minePosition;
		}
		
		int nearestDistance = 999999;
		Position nearestGoodPosition = null;
		for (Position position : goodPositions) {
			int distance = vulture.getDistance(position);
			if (distance < nearestDistance && distance < MINE_SPREAD_RADIUS) {
				nearestDistance = distance;
				nearestGoodPosition = position;
			}
		}
		
		if (nearestGoodPosition == null) {
			return null;
		}
		
		List<Unit> spiderMines = MapGrid.Instance().getUnitsNear(nearestGoodPosition, MINE_EXACT_RADIUS, true, false, UnitType.Terran_Vulture_Spider_Mine);
		if (spiderMines.size() == 0) {
			for (int i=0; i<3; i++) {
				minePosition = MicroUtils.randomPosition(nearestGoodPosition, MINE_EXACT_RADIUS);
				if (noProblemToMine(minePosition)) {
					mineReservedMap.put(vulture.getID(), new MineReserved(minePosition, MyBotModule.Broodwar.getFrameCount()));
					return minePosition;
				}
			}
		}
		
		spiderMines = MapGrid.Instance().getUnitsNear(nearestGoodPosition, MINE_SPREAD_RADIUS, true, false, UnitType.Terran_Vulture_Spider_Mine);
		if (spiderMines.size() < MINE_NUM_PER_POSITION) {
			for (int i=0; i<3; i++) {
				minePosition = MicroUtils.randomPosition(nearestGoodPosition, MINE_SPREAD_RADIUS);
				if (noProblemToMine(minePosition)) {
					mineReservedMap.put(vulture.getID(), new MineReserved(minePosition, MyBotModule.Broodwar.getFrameCount()));
					return minePosition;
				}
			}
		}
		return null;
	}
	
	private boolean noProblemToMine(Position position) {
		for (MineReserved mineReserved : mineReservedMap.values()) {
			if (position.getDistance(mineReserved.positionToMine) <= MINE_BETWEEN_DIST) {
				return false;
			}
		}
		
		if (!position.isValid() || BWTA.getRegion(position) == null || !MyBotModule.Broodwar.isWalkable(position.getX() / 8, position.getY() / 8)) {
			return false;
		}
		
		if (MapGrid.Instance().getUnitsNear(position, MINE_EXACT_RADIUS, true, true, UnitType.Terran_Vulture_Spider_Mine).size() > 0) {
			return false;
		}
		
		return true;
	}

}

class MineReserved {
	MineReserved(Position positionToMine, int reservedFrame) {
		this.positionToMine = positionToMine;
		this.reservedFrame = reservedFrame;
	}
	Position positionToMine;
	int reservedFrame;

	@Override
	public String toString() {
		return "MineReserved [positionToMine=" + positionToMine + ", reservedFrame=" + reservedFrame + "]";
	}
}

