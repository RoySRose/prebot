package pre.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwta.BaseLocation;
import pre.main.MyBotModule;
import pre.manager.InformationManager;

public class VultureTravelManager {

	private static final int ASSIGNED_FRAME = 24 * 20; // 20초
	private static final int VISIT_FRAME = 24 * 30; // 30초

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
		travelSites.clear();
		List<BaseLocation> otherBases = InformationManager.Instance().getOtherExpansionLocations(InformationManager.Instance().enemyPlayer);
		for (BaseLocation base : otherBases) {
			travelSites.add(new TravelSite(base, 0, 0));
		}
		initialized = true;
	}
	
	public void update() {
		if (!initialized) {
			init();
		}
		
		for (TravelSite travelSite : travelSites) {
			if (MyBotModule.Broodwar.isVisible(travelSite.baseLocation.getTilePosition())) {
				travelSite.visitFrame = MyBotModule.Broodwar.getFrameCount();
				
				// 해당 travelSite를 방문예정인 스쿼드의 travelSite를 변경한다.
				String relatedSquadName = null;
				for (String squadName : squadSiteMap.keySet()) {
					TravelSite site = squadSiteMap.get(squadName);
					if (site.baseLocation.equals(travelSite.baseLocation)) {
						relatedSquadName = squadName;
						break;
					}
				}
				
				if (relatedSquadName != null) {
					BaseLocation currentBase = squadSiteMap.get(relatedSquadName).baseLocation;
					squadSiteMap.remove(relatedSquadName);
					getBestTravelSite(relatedSquadName, currentBase);
				}
			}
		}
	}
	
	public BaseLocation getBestTravelSite(String squadName) {
		BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
		return getBestTravelSite(squadName, firstExpansion);
	}
	
	
	public BaseLocation getBestTravelSite(String squadName, BaseLocation currentBase) {
		TravelSite site = squadSiteMap.get(squadName);
		if (site != null) {
			return site.baseLocation;
		}
		
		int longestVisitPassedFrame = VISIT_FRAME;
		int longestAssignedPassedFrame = ASSIGNED_FRAME;
		double shortestDistance = 999999.0;
		
		TravelSite bestTravelSite = null;
		
		int currentFrame = MyBotModule.Broodwar.getFrameCount();
		for (TravelSite travelSite : travelSites) {
			
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
		
		bestTravelSite.assignedFrame = currentFrame;
		squadSiteMap.put(squadName, bestTravelSite);
		return bestTravelSite.baseLocation;
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
		return "TravelSite [baseLocation=" + baseLocation + ", visitFrame=" + visitFrame + ", assignedFrame=" + assignedFrame + "]";
	}
}