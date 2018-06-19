package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.action.Action;
import prebot.strategy.analyse.ProtossBuildPhase1;

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
		this.action = action;
		setUnitType();
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
	
	public void setUnitType() {
		if (action.getClass().equals(ProtossBuildPhase1.class)) {
			List<UnitType> enemyTypeList = Arrays.asList(UnitType.Protoss_Probe, UnitType.Protoss_Pylon, UnitType.Protoss_Zealot, UnitType.Protoss_Nexus, UnitType.Protoss_Gateway,
					UnitType.Protoss_Forge, UnitType.Protoss_Photon_Cannon, UnitType.Protoss_Assimilator, UnitType.Protoss_Cybernetics_Core);
			enemyUnitTypes.addAll(enemyTypeList);
			
		} else if (action.getClass().equals(ProtossBuildPhase1.class)) {
			
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

//						if (enemyGasFoundSec == CommonCode.UNKNOWN && unitType.isRefinery()) {
//							enemyGasFoundSec = TimeUtils.elapsedSeconds();
//							enemyGasBuildSec = TimeUtils.buildStartSeconds(euiVisible.getUnit());
//						}
					}
//					if (enemyGasFoundSec == CommonCode.UNKNOWN) {
//						BaseLocation enemyBase = InfoUtils.enemyBase();
//						if (enemyBase != null) {
//							List<Unit> geysers = enemyBase.getGeysers();
//							if (!geysers.isEmpty()) {
//								Unit enemyGas = geysers.get(0);
//								if (Prebot.Broodwar.isVisible(enemyGas.getTilePosition())) {
//									enemyGasNotFoundSec = TimeUtils.elapsedSeconds();
//								}
//							}
//						}
//					}
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
