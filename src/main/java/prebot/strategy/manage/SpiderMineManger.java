package prebot.strategy.manage;

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
import bwta.Chokepoint;
import bwta.Region;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.PositionReserveInfo;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Vulture;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class SpiderMineManger {
	
	private static final int RESV_EXPIRE_FRAME = 24 * 3;
	private static final int MINE_REMOVE_TANK_DIST = 150;
	private static final int MAX_MINE_COUNT = 150;
//	private static final int MINE_BETWEEN_DIST = 50;
	
	public enum MinePositionLevel {
		 ANYWHERE,
		 NOT_MY_OCCUPIED,
		 ONLY_GOOD_POSITION
	}

	private static final List<Position> GOOD_POSITIONS = new ArrayList<>(); // 마인 심기 좋은 지역
	
	private Map<Integer, PositionReserveInfo> mineRemoveMap = new HashMap<>(); // key : spider mine id
	private Map<Integer, PositionReserveInfo> mineReservedMap = new HashMap<>(); // key : vulture id

	private static SpiderMineManger instance = new SpiderMineManger();
	
	private boolean initialized = false;
	
	private SpiderMineManger() {}
	
	public static SpiderMineManger Instance() {
		return instance;
	}
	
	// TODO goodPositions 단계적으로 변화. ex) 초반에는 세번째, 네번째 멀티, 그 후에는 점차 증가 
	public boolean init() {
		if (!MicroConfig.Upgrade.hasResearched(TechType.Spider_Mines)) {
			return false;
		}

		List<BaseLocation> otherBases = InfoUtils.enemyOtherExpansionsSorted();
		Position myReadyToAttackPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().selfPlayer);
		Chokepoint mySecondChoke = InfoUtils.mySecondChoke();
		
		Position enemyReadyToAttackPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().enemyPlayer);
		BaseLocation enemyFirstExpansion = InfoUtils.enemyFirstExpansion();
		Chokepoint enemySecondChoke = InfoUtils.enemySecondChoke();
		
		if (!otherBases.isEmpty() && myReadyToAttackPos != null && mySecondChoke != null
				&& enemyReadyToAttackPos != null && enemyFirstExpansion != null && enemySecondChoke != null) {
			List<BaseLocation> myExpansions = this.getMyExpansionBaseLocation();
			
			// 3rd 멀티지역
			for (BaseLocation base : otherBases) {
				if (!myExpansions.contains(base)) {
					GOOD_POSITIONS.add(base.getPosition());
				}
			}
			
			// 공격준비지역
//			GOOD_POSITIONS.add(myReadyToAttackPos);
//			GOOD_POSITIONS.add(mySecondChoke.getCenter());
			
			GOOD_POSITIONS.add(enemyReadyToAttackPos);
			GOOD_POSITIONS.add(enemySecondChoke.getCenter());
			GOOD_POSITIONS.add(enemyFirstExpansion.getPosition());
			
			return true;
		}
		return false;
	}
	
	public void update() {
		if (!initialized) {
			initialized = init();
			return;
		}
		
		updateMineReservedMap(); // 만료 매설 만료시간 관리
		updateMineRemoveMap(); // 만료 제거 만료시간 관리
		updateVulturePolicy(); // 벌처 정책 관리
	}

	private void updateVulturePolicy() {
		if (!initialized) {
			return;
		}

		int vultureCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Vulture);
		int mineCount = UnitUtils.getUnitCount(UnitFindRange.COMPLETE, UnitType.Terran_Vulture_Spider_Mine);

		int mineNumPerPosition = Math.min(vultureCount / 3 + 1, 8);
		if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DEFENSE_FRONT)) {
			mineNumPerPosition += 2;
		} else if (mineCount > MAX_MINE_COUNT) {
			mineNumPerPosition = 1;
		}

		MinePositionLevel mLevel = MinePositionLevel.NOT_MY_OCCUPIED;
		if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DEFENSE_DROP)
				|| StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DETECT_IMPORTANT)) {
			mLevel = MinePositionLevel.ANYWHERE;
		}
		StrategyIdea.watcherMinePositionLevel = mLevel;
		StrategyIdea.spiderMineNumberPerPosition = mineNumPerPosition;
		StrategyIdea.spiderMineNumberPerGoodPosition = Math.min(vultureCount / 15 + 1, 3);
		int checkerCount = Math.min(vultureCount / 8, Vulture.CHECKER_MAX_COUNT);
		if (UnitUtils.getUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath, UnitType.Terran_Wraith) >= 5 && checkerCount == 0) {
			checkerCount = 1;
		}
		StrategyIdea.checkerMaxNumber = checkerCount;
	}

	private void updateMineReservedMap() {
		List<Integer> expiredList = new ArrayList<>();
		for (Integer vultureId : mineReservedMap.keySet()) {
			PositionReserveInfo mineReserved = mineReservedMap.get(vultureId);
			if (TimeUtils.elapsedFrames(mineReserved.reservedFrame) > RESV_EXPIRE_FRAME) {
				expiredList.add(vultureId);
			}
		}

		for (Integer vultureId : expiredList) {
			mineReservedMap.remove(vultureId);
		}
	}

	private void updateMineRemoveMap() {
		List<Integer> expiredRemoveList = new ArrayList<>();
		for (Integer spiderMineId : mineRemoveMap.keySet()) {
			PositionReserveInfo removeReserved = mineRemoveMap.get(spiderMineId);
			if (TimeUtils.elapsedFrames(removeReserved.reservedFrame) > RESV_EXPIRE_FRAME) {
				expiredRemoveList.add(spiderMineId);
			}
		}
		for (Integer spiderMineId : expiredRemoveList) {
			mineRemoveMap.remove(spiderMineId);
		}
		
		List<Unit> siegeList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Siege_Tank_Siege_Mode);
		for (Unit siegeTank : siegeList) {
			List<Unit> nearMineList = UnitUtils.getUnitsInRadius(PlayerRange.SELF, siegeTank.getPosition(), MINE_REMOVE_TANK_DIST, UnitType.Terran_Vulture_Spider_Mine);
			for (Unit mine : nearMineList) {
				if (mineRemoveMap.get(mine.getID()) == null) {
					mineRemoveMap.put(mine.getID(), new PositionReserveInfo(mine.getID(), mine.getPosition(), Prebot.Broodwar.getFrameCount()));
				}
			}
		}
		for (Unit siegeTank : siegeList) {
			List<Unit> nearMineList = UnitUtils.getUnitsInRadius(PlayerRange.SELF, siegeTank.getPosition(), MINE_REMOVE_TANK_DIST, UnitType.Terran_Vulture_Spider_Mine);
			for (Unit mine : nearMineList) {
				if (mineRemoveMap.get(mine.getID()) == null) {
					mineRemoveMap.put(mine.getID(), new PositionReserveInfo(mine.getID(), mine.getPosition(), Prebot.Broodwar.getFrameCount()));
				}
			}
		}
		
		if (LagObserver.groupsize() <= 10) {
			if (StrategyIdea.watcherMinePositionLevel == MinePositionLevel.NOT_MY_OCCUPIED) {
				List<Unit> spiderMineList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture_Spider_Mine);
				
				Region myBaseRegion = BWTA.getRegion(InfoUtils.myBase().getPosition());
				for (Unit spiderMine : spiderMineList) {
					Region mineRegion = BWTA.getRegion(spiderMine.getPosition());
					if (myBaseRegion == mineRegion) {
						mineRemoveMap.put(spiderMine.getID(), new PositionReserveInfo(spiderMine.getID(), spiderMine.getPosition(), Prebot.Broodwar.getFrameCount()));
					}
				}
			}
		}
	}

	public Position getPositionReserved(Unit vulture) {
		if (!initialized || vulture.getSpiderMineCount() <= 0) {
			return null;
		}
		PositionReserveInfo mineReserved = mineReservedMap.get(vulture.getID());
		if (mineReserved != null) {
			return mineReserved.position;
		} else {
			return null;
		}
	}
	
	public Unit mineToRemove(Unit vulture) {
		if (!initialized) {
			return null;
		}
		for (Integer mineId : mineRemoveMap.keySet()) {
			PositionReserveInfo removeReserved = mineRemoveMap.get(mineId);
			
			if (vulture.getDistance(removeReserved.position) < UnitType.Terran_Vulture.groundWeapon().maxRange()) {
				Unit spiderMine = Prebot.Broodwar.getUnit(removeReserved.unitId);
				if (UnitUtils.isValidUnit(spiderMine)) {
					return spiderMine;
				}
			}
		}
		return null;
	}

	public Position reserveSpiderMine(Unit vulture, MinePositionLevel minePositionLevel) {
		if (!initialized || vulture.getSpiderMineCount() == 0) {
			return null;
		}
		
		if (MinePositionLevel.ONLY_GOOD_POSITION.equals(minePositionLevel)) {
			Position position = positionToMineOnlyGoodPosition(vulture, StrategyIdea.spiderMineNumberPerGoodPosition);
			if (position != null) {
				mineReservedMap.put(vulture.getID(), new PositionReserveInfo(vulture.getID(), position, Prebot.Broodwar.getFrameCount()));
			}
			return position;
		} else {
			boolean vultureInMyOccupied = false;
			Region vultureRegion = BWTA.getRegion(vulture.getPosition());
			for (BaseLocation occupiedBase : InfoUtils.myOccupiedBases()) {
				if (vultureRegion == BWTA.getRegion(occupiedBase.getPosition())) {
					vultureInMyOccupied = true;
					break;
				}
			}
			
			int mineNumberPerPosition = StrategyIdea.spiderMineNumberPerPosition;
			if (vultureInMyOccupied) {
				if (MinePositionLevel.NOT_MY_OCCUPIED.equals(minePositionLevel)) {
					return null;
				} else {
					mineNumberPerPosition = Math.min(StrategyIdea.spiderMineNumberPerPosition, 2); // 자신의 진영이라면 최대 2개
				}
			}
			
			Position position = positionToMineNearPosition(vulture, vulture.getPosition(), mineNumberPerPosition);
			if (position != null) {
				mineReservedMap.put(vulture.getID(), new PositionReserveInfo(vulture.getID(), position, Prebot.Broodwar.getFrameCount()));
			}
			return position;
		}
	}
	
	private Position positionToMineOnlyGoodPosition(Unit vulture, int mineNumberPerPosition) {
		Position positionToMine = null;
		Position nearestGoodPosition = PositionUtils.getClosestPositionToPosition(GOOD_POSITIONS, vulture.getPosition(), 200.0);
		if (nearestGoodPosition != null) {
			positionToMine = positionToMineNearPosition(vulture, nearestGoodPosition, mineNumberPerPosition);
		}
		return positionToMine;
	}
	
	private Position positionToMineNearPosition(Unit vulture, Position position, int mineNumberPerPosition) {
		Position positionToMine = null;
		List<Unit> unitsOnTile = Prebot.Broodwar.getUnitsOnTile(position.toTilePosition());
		if (unitsOnTile.isEmpty()) {
			positionToMine = findMinePosition(position, Vulture.MINE_EXACT_RADIUS, mineNumberPerPosition);
		}
		if (positionToMine == null) {
			positionToMine = findMinePosition(position, Vulture.MINE_SPREAD_RADIUS, mineNumberPerPosition);
		}
		return positionToMine;
	}
	
	// position 기준으로 radius 범위내에 mineNumberPerPosition 숫자만큼 마인이 매설되어야 할때 마인매설 위치를 리턴
	private Position findMinePosition(Position position, int radius, int mineNumberPerPosition) {
		List<Unit> spiderMinesCount = UnitUtils.getUnitsInRadius(PlayerRange.SELF, position, radius, UnitType.Terran_Vulture_Spider_Mine);
		int reservedCount = numOfMineReserved(position, radius);
		if (spiderMinesCount.size() + reservedCount >= mineNumberPerPosition) {
			return null;
		}
		
		for (int i = 0; i < 3; i++) {
			Position minePosition = PositionUtils.randomPosition(position, radius);
			if (noProblemToMine(minePosition) && MicroUtils.isSafePlace(minePosition)) { // 문제없다면 없다면 매설
				return minePosition;
			}
		}
		return null;
	}
	
	private int numOfMineReserved(Position position, int radius) {
		int reservedMineNum = 0;
		for (PositionReserveInfo minReserved : mineReservedMap.values()) {
			if (minReserved.position.getDistance(position) <= radius) {
				reservedMineNum++;
			}
		}
		return reservedMineNum;
	}

	private boolean noProblemToMine(Position positionToMine) {
		// 마인을 심을 수 있는 장소가 아니다.
		if (!PositionUtils.isValidGroundPosition(positionToMine)) {
			return false;
		}

		// 아미 가까운 곳에 마인 매설이예약되었다.
		// for (MineReserved mineReserved : mineReservedMap.values()) {
		// if (position.getDistance(mineReserved.positionToMine) <= MicroConfig.Vulture.MINE_BETWEEN_DIST) {
		// return false;
		// }
		// }

		// 해당 지역에 마인이 매설되어 있다.
		// int exactPosMineNum = UnitUtils.getUnitsInRadius(PlayerRange.SELF, position, MicroConfig.Vulture.MINE_EXACT_RADIUS, UnitType.Terran_Vulture_Spider_Mine).size();
		// int overlapMine = InformationManager.Instance().enemyRace == Race.Terran ? 2 : 1;
		// if (exactPosMineNum >= overlapMine) {
		// return false;
		// }

		// 해당 지역에 아군 시즈탱크, 컴셋 스테이션, SCV 등이 있다면 금지
		List<Unit> units = UnitUtils.getUnitsInRadius(PlayerRange.SELF, positionToMine, MINE_REMOVE_TANK_DIST,
				UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_SCV, UnitType.Terran_Comsat_Station);
		if (!units.isEmpty()) {
			return false;
		}

		// 첫번째 확장지역 마인매설 금지 처리
		if (positionToMine.getDistance(InfoUtils.myFirstExpansion()) < MINE_REMOVE_TANK_DIST) {
			return false;
		}

		return true;
	}
	
	public List<BaseLocation> getMyExpansionBaseLocation() {
		List<BaseLocation> bases = new ArrayList<>();
		BaseLocation expansion1 = InformationManager.Instance().getSecondStartPosition();
		List<BaseLocation> addbase = InformationManager.Instance().getFutureCloseButFarFromEnemyLocation();
		bases.addAll(addbase);
		bases.add(expansion1);
		
		return bases;
	}

}
