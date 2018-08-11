package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Unit;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.BaseLocationUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
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
	private List<BaseLocation> baseLocationsCheckerOrdered = new ArrayList<>();

	public List<TravelSite> getTravelSites() {
		return travelSites;
	}

//	private Map<Integer, TravelSite> checkerSiteMap = new HashMap<>(); // key: checker unit id
	private Map<Integer, Integer> checkerSiteMap2 = new HashMap<>(); // key: checker unit id
	private Map<String, Integer> guerillaIgnoreMap = new HashMap<>(); // key : squad name
	
	public Map<Integer, Integer> getCheckerSiteMap2() {
		return checkerSiteMap2;
	}
	public List<BaseLocation> getBaseLocationsCheckerOrdered() {
		return baseLocationsCheckerOrdered;
	}

	private Map<Integer, Integer> checkerRetiredTimeMap = new HashMap<>();
	private int gurillaFailFrame = CommonCode.NONE;
	
	public int getGurillaFailFrame() {
		return gurillaFailFrame;
	}

	public void setGurillaFailFrame(int gurillaFailFrame) {
		this.gurillaFailFrame = gurillaFailFrame;
	}

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
		if (!travelSites.isEmpty()) {
			return;
		}
		List<BaseLocation> otherBases = InfoUtils.enemyOtherExpansionsSorted();
		if (otherBases.isEmpty()) {
			return;
		}

		baseLocationsCheckerOrdered = new ArrayList<>();
		Set<BaseLocation> baseSet = new HashSet<>();
		BaseLocation beforeBase = null; 
		for (int i = 0; i < otherBases.size(); i++) {
			if (beforeBase == null) {
				beforeBase = InfoUtils.myFirstExpansion();
			}
			BaseLocation closeBase = BaseLocationUtils.getGroundClosestBaseToPosition(otherBases, beforeBase, new BaseCondition() {
				@Override public boolean correspond(BaseLocation base) {
					return !baseSet.contains(base);
				}
			});
			baseLocationsCheckerOrdered.add(closeBase);
			beforeBase = closeBase;
			baseSet.add(closeBase);
		}
		System.out.println("baseLocationsCheckerOrdered initiated (size=" + baseLocationsCheckerOrdered.size()+ ")");
		
		// old
		System.out.println("travel site initiated (size=" + otherBases.size()+ ")");
		for (int i = 0; i < baseLocationsCheckerOrdered.size(); i++) {
			BaseLocation baseLocation = baseLocationsCheckerOrdered.get(i);
			travelSites.add(new TravelSite(baseLocation, 0, 0, 0, i));
			System.out.print(baseLocation.getPosition() + " / ");
		}
		System.out.println();
		initialized = true;
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

		for (Integer vultureId : checkerSiteMap2.keySet()) {
			// 유효하지 않은 벌처
			Unit checker = Prebot.Broodwar.getUnit(vultureId);
			if (!UnitUtils.isCompleteValidUnit(checker)) {
				invalidList.add(checker.getID());
				continue;
			}
			Integer index = checkerSiteMap2.get(checker.getID());
			if (index == null) {
				index = 0;
			}
			BaseLocation baseLocation = baseLocationsCheckerOrdered.get(index);
			if (MicroUtils.arrivedToPosition(checker, baseLocation.getPosition())) {
				int nextIndex = (index + 1) % baseLocationsCheckerOrdered.size();
				checkerSiteMap2.put(checker.getID(), nextIndex);
			}
		}
		
		for (Integer invalidCheckerId : invalidList) {
			checkerSiteMap2.remove(invalidCheckerId);
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

	public BaseLocation getCheckerTravelSite(Integer checkerId) {
		if (!initialized) {
			return null;
		}
		Integer index = checkerSiteMap2.get(checkerId);
		if (index != null) {
			return baseLocationsCheckerOrdered.get(index);
		} else {
			if (!baseLocationsCheckerOrdered.isEmpty()) {
				checkerSiteMap2.put(checkerId, 0);
				return baseLocationsCheckerOrdered.get(0);
			} else {
				return null;
			}
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
	
	public boolean guerillaIgnoreModeEnabled(String squadName) {
		return guerillaIgnoreMap.containsKey(squadName);
	}

	public void guerillaStart(String squadName) {
		guerillaIgnoreMap.put(squadName, Prebot.Broodwar.getFrameCount());
	}
}
