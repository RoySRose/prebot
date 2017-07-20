package pre.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwta.BaseLocation;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.MicroSet;

public class VultureTravelManager {

	private Map<String, TravelSite> squadSiteMap = new HashMap<>();
	
	public Map<String, TravelSite> getSquadSiteMap() {
		return squadSiteMap;
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
				String relatedSquadName = null;
				for (String squadName : squadSiteMap.keySet()) {
					TravelSite site = squadSiteMap.get(squadName);
					if (site.baseLocation.getPosition().equals(travelSite.baseLocation.getPosition())) {
						relatedSquadName = squadName; // 방문에정 스쿼드 명
						break;
					}
				}
				
				if (relatedSquadName != null) {
					System.out.println("change travel site");
					BaseLocation currentBase = squadSiteMap.get(relatedSquadName).baseLocation;
					squadSiteMap.remove(relatedSquadName);
					getBestTravelSite(relatedSquadName, currentBase, true); // travelSite 변경
				}
			}
		}
	}

	// 벌처스쿼드가 정찰할 base를 선택한다.
	// 1. 방문한지 가장 오래된 location으로 이동
	// 2. 방문한지 가장 오래된 장소가 복수개이면(아예 정찰이 안된) 할당된지 가장 오래된 location으로 이동
	// 3. 방문 및 할당된지 동일하게 오래된 장소가 복수개이면, 가까운 곳부터 간다. (currentBase에서 가장 가까운 곳, squadName만 입력받는 경우 적 앞마당에서 가장 가까운 곳)
	public BaseLocation getBestTravelSite(String squadName, boolean realAssign) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		return getBestTravelSite(squadName, firstExpansion, realAssign);
	}
	
	public BaseLocation getBestTravelSite(String squadName, BaseLocation currentBase, boolean realAssign) {
		if (!initialized) {
			return null;
		}
		
		TravelSite site = squadSiteMap.get(squadName);
		if (site != null) {
			return site.baseLocation;
		}
		
		int longestVisitPassedFrame = MicroSet.Vulture.visitFrame;
		int longestAssignedPassedFrame = MicroSet.Vulture.assignedFrame;
		double shortestDistance = 999999.0;
		
		TravelSite bestTravelSite = null;
		
		int currentFrame = MyBotModule.Broodwar.getFrameCount();
		for (TravelSite travelSite : travelSites) {
			if (squadSiteMap.values().contains(travelSite)) {
				continue;
			}
			
			int visitPassedFrame = currentFrame - travelSite.visitFrame;
			int assignPassedFrame = currentFrame - travelSite.assignedFrame;
			double distance = (currentBase == null) ? 0.0 : currentBase.getGroundDistance(travelSite.baseLocation);
			
			if (visitPassedFrame > longestVisitPassedFrame ||
					(visitPassedFrame == longestVisitPassedFrame && assignPassedFrame > longestAssignedPassedFrame) ||
					(visitPassedFrame == longestVisitPassedFrame && assignPassedFrame == longestAssignedPassedFrame && distance < shortestDistance)) {
				longestVisitPassedFrame = visitPassedFrame;
				longestAssignedPassedFrame = assignPassedFrame;
				shortestDistance = distance;
				bestTravelSite = travelSite;
			}
		}
		
		if (bestTravelSite != null) {
			bestTravelSite.assignedFrame = currentFrame;
			if (realAssign) squadSiteMap.put(squadName, bestTravelSite);
			return bestTravelSite.baseLocation;
		} else {
			if (realAssign) squadSiteMap.remove(squadName);
			return null;
		}
	}

}

class TravelSite {
	TravelSite(BaseLocation baseLocation, int visitFrame, int assignedFrame) {
		this.baseLocation = baseLocation;
		this.visitFrame = visitFrame;
		this.assignedFrame = assignedFrame;
	}
	BaseLocation baseLocation;
	int visitFrame;
	int assignedFrame;

	@Override
	public String toString() {
		return "TravelSite [baseLocation=" + baseLocation.getPosition() + ", visitFrame=" + visitFrame + ", assignedFrame=" + assignedFrame + "]";
	}
}