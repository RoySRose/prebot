package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import prebot.common.main.Prebot;
import prebot.common.util.BaseLocationUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.IConditions.BaseCondition;
import prebot.micro.constant.MicroConfig.Vulture;
import prebot.micro.predictor.GuerillaScore;
import prebot.micro.predictor.VultureFightPredictor;
import prebot.strategy.TravelSite;
import prebot.strategy.UnitInfo;

// 벌처 이동지 관리 - 벌처 정찰 max, 마인수 등의 정책도 함께 관리한다.
public class VultureTravelManager {

	private static VultureTravelManager instance = new VultureTravelManager();
	
	private VultureTravelManager() {}
	
	public static VultureTravelManager Instance() {
		return instance;
	}
	
	private List<TravelSite> travelSites = new ArrayList<>(); // other exapnsions

	public List<TravelSite> getTravelSites() {
		return travelSites;
	}

	private Map<Integer, TravelSite> checkerSiteMap = new HashMap<>(); // key: checker unit id
	private Map<String, Integer> guerillaIgnoreMap = new HashMap<>(); // key : squad name
	
	public Map<Integer, TravelSite> getCheckerSiteMap() {
		return checkerSiteMap;
	}

	private Map<Integer, Integer> checkerRetiredTimeMap = new HashMap<>();

	private boolean initialized = false;
	BaseLocation enemyCloseStartBase = null;

	public void update() {
		initializeTravelSite();
		updateVisitFrame();
		updateCheckerJob();
		updateGuerillaIgnoreTime();
		refreshRetiredCheckers();
	}

	private void initializeTravelSite() {
		if (travelSites.isEmpty()) {
			List<BaseLocation> otherBases = InfoUtils.enemyOtherExpansionsSorted();
			if (otherBases.isEmpty()) {
				return;
			}

			for (BaseLocation base : otherBases) {
				travelSites.add(new TravelSite(base, 0, 0, 0));
			}
			initialized = true;
		}
	}

	private void updateVisitFrame() {
		// 시야가 밝혀졌다면 visitFrame을 계속 업데이트 한다.
		for (TravelSite travelSite : travelSites) {
			if (Prebot.Broodwar.isVisible(travelSite.baseLocation.getTilePosition())) {
				travelSite.visitFrame = Prebot.Broodwar.getFrameCount();
			}
		}
	}

	private void updateCheckerJob() {
		if (!initialized) {
			return;
		}

		List<Integer> invalidList = new ArrayList<>(); // 유효하지 않은 벌처
		List<Integer> jobFinishedList = new ArrayList<>(); // 목적지 시야가 밝혀짐
		List<Integer> expiredList = new ArrayList<>(); // 어떤 이유에서 너무 오래걸림(취소)

		for (Integer vultureId : checkerSiteMap.keySet()) {
			TravelSite travelSite = checkerSiteMap.get(vultureId);
			// 유효하지 않은 벌처
			if (!UnitUtils.isCompleteValidUnit(Prebot.Broodwar.getUnit(vultureId))) {
				invalidList.add(vultureId);
			}
			// 시야가 밝혀진 목적지
			else if (TimeUtils.elapsedFrames(travelSite.visitFrame) == 0) {
				jobFinishedList.add(vultureId);
			}
			// 방문시간 만료
			else if (TimeUtils.elapsedFrames(travelSite.visitAssignedFrame) > Vulture.CHECKER_INTERVAL_FRAME) {
				expiredList.add(vultureId);
			}
		}

		for (Integer unitId : invalidList) {
			checkerSiteMap.remove(unitId);
		}
		for (Integer unitId : jobFinishedList) {
			TravelSite travelSite = checkerSiteMap.get(unitId);
			if (travelSite == null || TimeUtils.elapsedFrames(travelSite.visitFrame) > 0) {
				continue;
			}
			checkerSiteMap.remove(unitId);
			BaseLocation newTravelSite = getCheckerTravelSite(unitId, travelSite.baseLocation); // travelSite 변경(currentBase에서 가까운 곳)
			if (newTravelSite == null) {
				// no site to travel
				System.out.println("no site to travel");
				Unit vulture = Prebot.Broodwar.getUnit(unitId);
				if (vulture.getSpiderMineCount() == 0) {
					checkerRetiredTimeMap.put(vulture.getID(), TimeUtils.elapsedFrames());
				}
			}
		}
		for (Integer unitId : expiredList) {
			checkerSiteMap.remove(unitId);
		}
	}

	private void updateGuerillaIgnoreTime() {
		if (!initialized) {
			return;
		}

		String ignoreExpiredSquadName = null;
		for (String squadName : guerillaIgnoreMap.keySet()) {
			Integer startTime = guerillaIgnoreMap.get(squadName);
			if (startTime != null && TimeUtils.elapsedFrames(startTime) > Vulture.GEURILLA_IGNORE_FRAME) {
				ignoreExpiredSquadName = squadName;
				break;
			}
		}
		if (ignoreExpiredSquadName != null) {
			guerillaIgnoreMap.remove(ignoreExpiredSquadName);
		}
	}

	private void refreshRetiredCheckers() {
		List<Integer> removeList = new ArrayList<>();
		for (Integer checkerId : checkerRetiredTimeMap.keySet()) {
			Unit checker = Prebot.Broodwar.getUnit(checkerId);
			if (!UnitUtils.isCompleteValidUnit(checker)) {
				removeList.add(checkerId);
			} else {
				int retiredTime = checkerRetiredTimeMap.get(checkerId);
				if (TimeUtils.elapsedFrames(retiredTime) > Vulture.CHECKER_RETIRE_FRAME) {
					removeList.add(checkerId);
				}
			}
		}
		
		for (Integer checkerId : removeList) {
			checkerRetiredTimeMap.remove(checkerId);
		}
	}

	// 벌처스쿼드가 정찰할 base를 선택한다.
	// 1. 방문한지 가장 오래된 location으로 이동
	// 2. 방문한지 가장 오래된 location이 복수개이면, 가까운 곳부터 간다.
	// (currentBase에서 가장 가까운 곳, checker unit id만 입력받은 경우 적 앞마당에서 가장 가까운 곳)
	public BaseLocation getCheckerTravelSite(Integer checkerId) {
		if (!initialized) {
			return null;
		}

		TravelSite site = checkerSiteMap.get(checkerId);
		if (site != null) {
			return site.baseLocation;
		}
		
		if (enemyCloseStartBase == null) {
			enemyCloseStartBase = BaseLocationUtils.getClosestBaseToPosition(BWTA.getStartLocations(), InfoUtils.enemyBase().getPosition(), new BaseCondition() {
				@Override public boolean correspond(BaseLocation base) {
					return !base.equals(InfoUtils.enemyBase()) && !base.equals(InfoUtils.myBase());
				}
			});
		}
		
		return getCheckerTravelSite(checkerId, enemyCloseStartBase);
	}

	public BaseLocation getCheckerTravelSite(Integer checkerId, BaseLocation currentBase) {
		if (!initialized) {
			return null;
		}

		TravelSite site = checkerSiteMap.get(checkerId);
		if (site != null) {
			return site.baseLocation;
		}

		int longestVisitPassedFrame = Vulture.CHECKER_INTERVAL_FRAME;
		double shortestDistance = 999999.0;

		TravelSite bestTravelSite = null;

		for (TravelSite travelSite : travelSites) {
			if (checkerSiteMap.values().contains(travelSite)) {
				continue;
			}

			int visitPassedFrame = TimeUtils.elapsedFrames(travelSite.visitFrame);
			double distance = currentBase.getGroundDistance(travelSite.baseLocation);

			if (visitPassedFrame > longestVisitPassedFrame || (visitPassedFrame == longestVisitPassedFrame && distance < shortestDistance)) {
				longestVisitPassedFrame = visitPassedFrame;
				shortestDistance = distance;
				bestTravelSite = travelSite;
			}
		}

		if (bestTravelSite != null) {
			checkerSiteMap.put(checkerId, bestTravelSite);
			bestTravelSite.visitAssignedFrame = Prebot.Broodwar.getFrameCount();
			return bestTravelSite.baseLocation;
		} else {
			checkerSiteMap.remove(checkerId);
			return null;
		}
	}

	// 1) 게릴라지역은 적군이 존재하는 travelSites이어야 한다.
	// 2) 게릴라지격은 guerillaFrame이(타깃으로 결정된 frame) 일정시간 이상 지났어야 한다.(게릴라로 벌처가 계속 소모되는 것을 방지하기 위함)
	// 3) 일꾼이 많으면 우선순위가 높다. 병력이 많으면 우선순위가 낮다.(특히 방어타워가 많으면)
	public BaseLocation getBestGuerillaSite(List<Unit> assignableVultures) {
		if (!initialized) {
			return null;
		}

		int vulturePower = VultureFightPredictor.powerOfWatchers(assignableVultures);
		int currFrame = Prebot.Broodwar.getFrameCount();

		int bestScore = 0;
		TravelSite bestTravelSite = null;

		for (TravelSite travelSite : travelSites) {
			if (assignableVultures.size() < Vulture.GEURILLA_FREE_VULTURE_COUNT && TimeUtils.elapsedFrames(travelSite.guerillaExamFrame) < Vulture.GEURILLA_INTERVAL_FRAME) {
				continue;
			}

			Set<UnitInfo> euiList = UnitUtils.getAllEnemyUnitInfosInRadiusForGround(travelSite.baseLocation.getPosition(), Vulture.GEURILLA_ENEMY_RADIUS);
			if (euiList.isEmpty()) { // 적군이 존재하지 않음
				continue;
			}

			// 안개속의 적 구성을 가늠해 게릴라 타게팅이 가능한지 확인한다.
			int enemyPower = VultureFightPredictor.powerOfEnemiesByUnitInfo(euiList);
			int score = GuerillaScore.guerillaScoreByUnitInfo(euiList);

			if (vulturePower > enemyPower && score > bestScore) {
				bestScore = score;
				bestTravelSite = travelSite;
			}
		}

		if (bestTravelSite != null) {
			bestTravelSite.guerillaExamFrame = currFrame;
			return bestTravelSite.baseLocation;
		} else {
			return null;
		}
	}

	public boolean checkerRetired(int unitId) {
		return checkerRetiredTimeMap.containsKey(unitId);
	}
	
	public boolean checkerIgnoreModeEnabled(int unitId) {
		TravelSite travelSite = checkerSiteMap.get(unitId);
		if (travelSite != null) {
			if (travelSite.visitAssignedFrame != 0 && TimeUtils.elapsedFrames(travelSite.visitAssignedFrame) < Vulture.CHECKER_IGNORE_MOVE_FRAME) {
				return true;
			}
		}
		return false;
	}
	
	public boolean guerillaIgnoreModeEnabled(String squadName) {
		return guerillaIgnoreMap.containsKey(squadName);
	}

	public void guerillaStart(String squadName) {
		guerillaIgnoreMap.put(squadName, Prebot.Broodwar.getFrameCount());
	}
}
