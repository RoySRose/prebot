package pre.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwta.BaseLocation;
import pre.UnitInfo;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.CombatExpectation;
import pre.util.MicroSet;

public class VultureTravelManager {

	private Map<Integer, TravelSite> vultureSiteMap = new HashMap<>();
	
	public Map<Integer, TravelSite> getSquadSiteMap() {
		return vultureSiteMap;
	}

	private final List<TravelSite> travelSites = new ArrayList<>();
	
	private boolean initialized = false;

	private static VultureTravelManager instance = new VultureTravelManager();
	
	private VultureTravelManager() {}
	
	public static VultureTravelManager Instance() {
		return instance;
	}
	
	public void init() {
		List<BaseLocation> otherBases = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
		
		if (!otherBases.isEmpty()) {
			travelSites.clear();
			for (BaseLocation base : otherBases) {
				travelSites.add(new TravelSite(base, 0, 0));
			}
			initialized = true;
		}
	}
	
	public void update() {
		if (!initialized) {
			init();
			return;
		}
		
		for (TravelSite travelSite : travelSites) {
			if (MyBotModule.Broodwar.isVisible(travelSite.baseLocation.getTilePosition())) {
				// 1. 시야가 밝혀졌다면 visitFrame을 계속 업데이트 한다.
				travelSite.visitFrame = MyBotModule.Broodwar.getFrameCount();
				
				// 2. 시야가 밝혀진 travelSite를 방문예정인 스쿼드의 travelSite를 변경한다.
				Integer relatedVultureId = null;
				for (Integer id : vultureSiteMap.keySet()) {
					TravelSite site = vultureSiteMap.get(id);
					if (site.baseLocation.getPosition().equals(travelSite.baseLocation.getPosition())) {
						relatedVultureId = id; // 방문예정 스쿼드 명
						break;
					}
				}
				
				if (relatedVultureId != null) {
					System.out.println("change travel site");
					BaseLocation currentBase = vultureSiteMap.get(relatedVultureId).baseLocation;
					vultureSiteMap.remove(relatedVultureId);
					getBestTravelSite(relatedVultureId, currentBase); // travelSite 변경
				}
			}
		}
	}

	// 벌처스쿼드가 정찰할 base를 선택한다.
	// 1. 방문한지 가장 오래된 location으로 이동
	// 2. 방문한지 가장 오래된 장소가 복수개이면(아예 정찰이 안된) 할당된지 가장 오래된 location으로 이동
	// 3. 방문 및 할당된지 동일하게 오래된 장소가 복수개이면, 가까운 곳부터 간다. (currentBase에서 가장 가까운 곳, squadName만 입력받는 경우 적 앞마당에서 가장 가까운 곳)
	public BaseLocation getBestTravelSite(Integer vultureId) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		return getBestTravelSite(vultureId, firstExpansion);
	}
	
	public BaseLocation getBestTravelSite(Integer vultureId, BaseLocation currentBase) {
		if (!initialized) {
			return null;
		}
		
		TravelSite site = vultureSiteMap.get(vultureId);
		if (site != null) {
			return site.baseLocation;
		}
		
		int longestVisitPassedFrame = MicroSet.Vulture.CHECKER_INTERVAL_FRAME;
		double shortestDistance = 999999.0;
		
		TravelSite bestTravelSite = null;
		
		int currentFrame = MyBotModule.Broodwar.getFrameCount();
		for (TravelSite travelSite : travelSites) {
			if (vultureSiteMap.values().contains(travelSite)) {
				continue;
			}
			
			int visitPassedFrame = currentFrame - travelSite.visitFrame;
			double distance = currentBase.getGroundDistance(travelSite.baseLocation);
			
			if (visitPassedFrame > longestVisitPassedFrame ||
					(visitPassedFrame == longestVisitPassedFrame && distance < shortestDistance)) {
				longestVisitPassedFrame = visitPassedFrame;
				shortestDistance = distance;
				bestTravelSite = travelSite;
			}
		}
		
		if (bestTravelSite != null) {
			vultureSiteMap.put(vultureId, bestTravelSite);
			return bestTravelSite.baseLocation;
		} else {
			vultureSiteMap.remove(vultureId);
			return null;
		}
	}
	
	// 1) 게릴라지역은 적군이 존재하는 travelSites이어야 한다.
	// 2) 게릴라지격은 guerillaFrame이(타깃으로 결정된 frame) 일정시간 이상 지났어야 한다.(게릴라로 벌처가 계속 소모되는 것을 방지하기 위함)
	// 3) 일꾼이 많으면 우선순위가 높다. 병력이 많으면 우선순위가 낮다.(특히 방어타워가 많으면)
	public BaseLocation getBestGuerillaSite(List<Unit> assignableVultures) {
		int vulturePower = CombatExpectation.getVulturePower(assignableVultures);
		int currFrame = MyBotModule.Broodwar.getFrameCount();
		
		int bestScore = 0;
		TravelSite bestTravelSite = null;
		
		for (TravelSite travelSite : travelSites) {
			if (currFrame - travelSite.guerillaFrame < MicroSet.Vulture.GEURILLA_INTERVAL_FRAME) {
				continue;
			}

			List<UnitInfo> enemiesInfo = InformationManager.Instance().getNearbyForce(travelSite.baseLocation.getPosition(), InformationManager.Instance().enemyPlayer, MicroSet.Vulture.GEURILLA_RADIUS);
			if (enemiesInfo.isEmpty()) { // 적군이 존재하지 않음
				continue;
			}
			
			// 안개속의 적 구성을 가늠해 게릴라 타게팅이 가능한지 확인한다.			
			int enemyPower = CombatExpectation.getEnemyPowerByUnitInfo(enemiesInfo);
			int score = CombatExpectation.getGuerillaScoreByUnitInfo(enemiesInfo);
			
			if (vulturePower > enemyPower && score > bestScore) {
				bestScore = score;
				bestTravelSite = travelSite;
			}
		}
		
		if (bestTravelSite != null) {
			bestTravelSite.guerillaFrame = currFrame;
			return bestTravelSite.baseLocation;
		} else {
			return null;
		}
	}
}

class TravelSite {
	TravelSite(BaseLocation baseLocation, int visitFrame, int guerillaFrame) {
		this.baseLocation = baseLocation;
		this.visitFrame = visitFrame;
		this.guerillaFrame = guerillaFrame;
	}
	BaseLocation baseLocation;
	int visitFrame;
	int guerillaFrame;

	@Override
	public String toString() {
		return "TravelSite [baseLocation=" + baseLocation.getPosition() + ", visitFrame=" + visitFrame + ", guerillaFrame=" + guerillaFrame + "]";
	}
}