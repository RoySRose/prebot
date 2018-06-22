package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.action.Action;
import prebot.strategy.action.RaceAction;

public class RaceActionManager {

	private static RaceActionManager instance = new RaceActionManager();
	
	public static RaceActionManager Instance() {
		return instance;
	}
	
	public Set<UnitType> enemyUnitTypes = new HashSet<>(); // TODO diplay
	public Map<UnitType, List<UnitInfo>> foundEuiMap = new HashMap<>(); // TODO display 유닛타입별 발견시점의 UnitInfo
	public Map<Integer, Integer> buildStartFrameMap = new HashMap<>(); // TODO display 유닛ID별 빌드시작 시작(건물)
	
	public int lastCheckFrameBase = 0;
	public int lastCheckFrameGas = 0;
	public int lastCheckFrameFirstExpansion = 0;
	
	public Set<Object> clues = new HashSet<>();
	
	private Action action = null;
	
	public void setAction(Action action) {
		if (!(action instanceof RaceAction)) {
			return;
		}
		
		List<UnitType> unitTypes = new ArrayList<>();
		List<UnitType> buildingTypes = new ArrayList<>();
		
		if (((RaceAction) action).getRace() == Race.Protoss) {
			unitTypes = Arrays.asList(UnitType.Protoss_Probe, UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon, UnitType.Protoss_Dark_Templar, UnitType.Protoss_High_Templar,
					UnitType.Protoss_Archon, UnitType.Protoss_Dark_Archon, UnitType.Protoss_Shuttle, UnitType.Protoss_Reaver, UnitType.Protoss_Scarab, UnitType.Protoss_Observer,
					UnitType.Protoss_Scout, UnitType.Protoss_Corsair, UnitType.Protoss_Carrier, UnitType.Protoss_Arbiter, UnitType.Protoss_Interceptor);

			buildingTypes = Arrays.asList(UnitType.Protoss_Nexus, UnitType.Protoss_Assimilator, UnitType.Protoss_Pylon, UnitType.Protoss_Gateway, UnitType.Protoss_Shield_Battery,
					UnitType.Protoss_Forge, UnitType.Protoss_Photon_Cannon, UnitType.Protoss_Cybernetics_Core, UnitType.Protoss_Citadel_of_Adun, UnitType.Protoss_Templar_Archives,
					UnitType.Protoss_Robotics_Facility, UnitType.Protoss_Robotics_Support_Bay, UnitType.Protoss_Observatory, UnitType.Protoss_Stargate,
					UnitType.Protoss_Fleet_Beacon, UnitType.Protoss_Arbiter_Tribunal);

		} else if (((RaceAction) action).getRace() == Race.Zerg) {
			unitTypes = Arrays.asList(UnitType.Zerg_Drone, UnitType.Zerg_Overlord, UnitType.Zerg_Larva, UnitType.Zerg_Egg, UnitType.Zerg_Zergling, UnitType.Zerg_Hydralisk,
					UnitType.Zerg_Lurker, UnitType.Zerg_Lurker_Egg, UnitType.Zerg_Cocoon, UnitType.Zerg_Mutalisk, UnitType.Zerg_Scourge, UnitType.Zerg_Guardian,
					UnitType.Zerg_Devourer, UnitType.Zerg_Queen, UnitType.Zerg_Broodling, UnitType.Zerg_Infested_Terran, UnitType.Zerg_Ultralisk, UnitType.Zerg_Defiler);

			buildingTypes = Arrays.asList(UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive, UnitType.Zerg_Extractor, UnitType.Zerg_Spawning_Pool,
					UnitType.Zerg_Hydralisk_Den, UnitType.Zerg_Evolution_Chamber, UnitType.Zerg_Creep_Colony, UnitType.Zerg_Sunken_Colony, UnitType.Zerg_Spore_Colony,
					UnitType.Zerg_Spire, UnitType.Zerg_Greater_Spire, UnitType.Zerg_Queens_Nest, UnitType.Zerg_Infested_Command_Center, UnitType.Zerg_Ultralisk_Cavern,
					UnitType.Zerg_Defiler_Mound, UnitType.Zerg_Nydus_Canal);

		} else if (((RaceAction) action).getRace() == Race.Terran) {
			unitTypes = Arrays.asList(UnitType.Terran_SCV, UnitType.Terran_Marine, UnitType.Terran_Medic, UnitType.Terran_Firebat, UnitType.Terran_Ghost, UnitType.Terran_Vulture,
					UnitType.Terran_Vulture_Spider_Mine, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Goliath,
					UnitType.Terran_Wraith, UnitType.Terran_Science_Vessel, UnitType.Terran_Dropship, UnitType.Terran_Valkyrie, UnitType.Terran_Battlecruiser,
					UnitType.Terran_Nuclear_Missile);

			buildingTypes = Arrays.asList(UnitType.Terran_Command_Center, UnitType.Terran_Comsat_Station, UnitType.Terran_Nuclear_Silo, UnitType.Terran_Refinery,
					UnitType.Terran_Supply_Depot, UnitType.Terran_Machine_Shop, UnitType.Terran_Barracks, UnitType.Terran_Bunker, UnitType.Terran_Academy,
					UnitType.Terran_Engineering_Bay, UnitType.Terran_Missile_Turret, UnitType.Terran_Factory, UnitType.Terran_Armory, UnitType.Terran_Starport,
					UnitType.Terran_Control_Tower, UnitType.Terran_Science_Facility, UnitType.Terran_Covert_Ops, UnitType.Terran_Physics_Lab);
		}

		enemyUnitTypes.addAll(unitTypes);
		enemyUnitTypes.addAll(buildingTypes);

		this.action = action;
	}

	public void update() {
		updateMap();
		updateVisitFrame();
		
		if (action != null) {
			if (action.exitCondition()) {
				action = action.getNextAction();
			} else {
				action.action();
			}
		}
	}
	
	private void updateMap() {
		for (UnitType unitType : enemyUnitTypes) {
			List<UnitInfo> euiListVisible = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, unitType);
			if (euiListVisible.isEmpty()) {
				continue;
			}

			List<UnitInfo> euiListSaved = foundEuiMap.get(unitType);
			if (euiListSaved == null || euiListSaved.isEmpty()) {
				euiListSaved = new ArrayList<>();
			}

			for (UnitInfo euiVisible : euiListVisible) {
				boolean containUnit = false;
				for (UnitInfo euiSaved : euiListSaved) {
					if (euiVisible.getUnitID() == euiSaved.getUnitID()) {
						containUnit = true;
						break;
					}
				}
				if (!containUnit) {
					euiListSaved.add(euiVisible);
					if (euiVisible.getType().isBuilding()) {
						buildStartFrameMap.put(euiVisible.getUnitID(), TimeUtils.buildStartSeconds(euiVisible.getUnit())); // 빌드시작한 시잔 (초). 이미 완성되어 알 수 없다면 UNKNOWN
					}
				}
			}
			foundEuiMap.put(unitType, euiListSaved);
		}
	}

	/// 유닛 발견 맵을 업데이트한다.
	private void updateVisitFrame() {
		if (InfoUtils.enemyBase() == null) {
			return;
		}
		
		TilePosition enemyBaseTile = InfoUtils.enemyBase().getTilePosition();
		TilePosition enemyGasTile = InfoUtils.enemyBase().getTilePosition(); // TODO 가스 위치로 수정 (INFOMATION MANAGER)
		TilePosition enemyFirstExpansionTile = InfoUtils.enemyFirstExpansion().getTilePosition();
		
		if (Prebot.Broodwar.isVisible(enemyBaseTile)) {
			lastCheckFrameBase = TimeUtils.elapsedFrames();
		}
		if (Prebot.Broodwar.isVisible(enemyGasTile)) {
			lastCheckFrameGas = TimeUtils.elapsedFrames();
		}
		if (Prebot.Broodwar.isVisible(enemyFirstExpansionTile)) {
			lastCheckFrameFirstExpansion = TimeUtils.elapsedFrames();
		}
	}
}
