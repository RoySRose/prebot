package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.CombatManager;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.airforce.ValkyrieControl;
import prebot.micro.control.factory.GoliathControl;
import prebot.micro.control.factory.TankControl;
import prebot.micro.targeting.TargetFilter;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.PositionFinder.CampType;

public class MainAttackSquad extends Squad {

//	private Set<UnitInfo> euisNearUnit = new HashSet<>();
//	private Set<UnitInfo> euisNearBaseRegion = new HashSet<>();
//	
//	public Set<UnitInfo> getEuisNearUnit() {
//		return euisNearUnit;
//	}
//
//	public Set<UnitInfo> getEuisNearBaseRegion() {
//		return euisNearBaseRegion;
//	}

	private TankControl tankControl = new TankControl();
	private GoliathControl goliathControl = new GoliathControl();
	private ValkyrieControl valkyrieControl = new ValkyrieControl();
	
	public MainAttackSquad() {
		super(SquadInfo.MAIN_ATTACK);
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath, UnitType.Terran_Valkyrie);
	}

	@Override
	public boolean want(Unit unit) {
		if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
			Squad squad = CombatManager.Instance().squadData.getSquad(unit);
			if (squad instanceof MultiDefenseSquad) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		List<Unit> tankList = new ArrayList<>();
		List<Unit> goliathList = new ArrayList<>();
		List<Unit> valkyrieList = new ArrayList<>();
		
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Tank_Mode, new ArrayList<Unit>()));
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Siege_Mode, new ArrayList<Unit>()));
		goliathList.addAll(unitListMap.getOrDefault(UnitType.Terran_Goliath, new ArrayList<Unit>()));
		valkyrieList.addAll(unitListMap.getOrDefault(UnitType.Terran_Valkyrie, new ArrayList<Unit>()));

		StrategyIdea.initiated = this.updateInitiatedFlag();
		int saveUnitLevel = this.saveUnitLevel(tankList, goliathList);
		int goliathSaveUnitLevel = Math.min(saveUnitLevel, 1);
		
		tankControl.setSaveUnitLevel(saveUnitLevel);
		tankControl.setMainPosition(StrategyIdea.mainSquadMode.isAttackMode ? StrategyIdea.mainPosition : StrategyIdea.campPositionSiege);
		goliathControl.setSaveUnitLevel(goliathSaveUnitLevel);

//		System.out.println(euiList);
		Set<UnitInfo> groundEuiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.AIR_UNIT);
		if (!tankList.isEmpty()) {
			tankControl.controlIfUnitExist(tankList, groundEuiList);
		}
		goliathControl.controlIfUnitExist(goliathList, euiList);
		if (!valkyrieList.isEmpty()) {
			Set<UnitInfo> airEuiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.GROUND_UNIT);
			valkyrieControl.controlIfUnitExist(valkyrieList, airEuiList);
		}
	}

	private boolean updateInitiatedFlag() {
		if (euiList.isEmpty()) {
			return false;
		}
		
		for (UnitInfo eui : euiList) {
			if (eui.getType() != UnitType.Terran_Vulture_Spider_Mine
					&& eui.getType() != UnitType.Zerg_Larva
					&& !eui.getType().isBuilding()
					&& !eui.getType().isWorker()
					&& !eui.getType().isFlyer()) {
				if (StrategyIdea.mainSquadCenter.getDistance(eui.getLastPosition()) < StrategyIdea.mainSquadCoverRadius) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int saveUnitLevel(List<Unit> tankList, List<Unit> goliathList) {
		int saveUnitLevel = 1; // 거리재기 전진
		if (InfoUtils.enemyRace() == Race.Terran) {
			List<UnitInfo> closeTankEnemies = new ArrayList<>();
			for (UnitInfo eui : euiList) {
				if (eui.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || eui.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					closeTankEnemies.add(eui);
				}
			}
			if (closeTankEnemies.size() * 4 > tankList.size()) {
				// System.out.println("keep in line");
				saveUnitLevel = 2; // 안전거리 유지
			}
		}
		
		if (StrategyIdea.mainSquadMode == MainSquadMode.NO_MERCY) { // strategy manager 판단
			saveUnitLevel = 0;
		} else { // combat manager 자체 판단
			if (InformationManager.Instance().enemyRace != Race.Terran) {
				if (Prebot.Broodwar.self().supplyUsed() >= 380) { // || pushLine) {
					saveUnitLevel = 0;
				}
			}
		}
		return saveUnitLevel;
	}
	
	@Override
	public void findEnemies() {
		euiList.clear();

		UnitUtils.addEnemyUnitInfosInRadius(TargetFilter.UNFIGHTABLE|TargetFilter.LARVA_LURKER_EGG, euiList, StrategyIdea.mainSquadCenter, StrategyIdea.mainSquadCoverRadius + 50, true, false);
		
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			for (Unit unit : unitList) {
				if (unit.getDistance(StrategyIdea.mainSquadCenter) > StrategyIdea.mainSquadCoverRadius + 50) {
					UnitUtils.addEnemyUnitInfosInRadius(TargetFilter.UNFIGHTABLE|TargetFilter.LARVA_LURKER_EGG, euiList, unit.getPosition(), unit.getType().sightRange() + MicroConfig.COMMON_ADD_RADIUS, true, false);
				}
			}
			
		}
		
		if (!StrategyIdea.mainSquadMode.isAttackMode || InfoUtils.enemyRace() == Race.Terran) {
			if (StrategyIdea.campType == CampType.INSIDE) {
				euiList.addAll(InfoUtils.euiListInBase());
			} else if (StrategyIdea.campType == CampType.FIRST_CHOKE || StrategyIdea.campType == CampType.EXPANSION) {
				euiList.addAll(InfoUtils.euiListInBase());
				euiList.addAll(InfoUtils.euiListInExpansion());
			} else {
				euiList.addAll(InfoUtils.euiListInBase());
				euiList.addAll(InfoUtils.euiListInExpansion());
				euiList.addAll(InfoUtils.euiListInThirdRegion());
			}
		}
		
		if (TimeUtils.beforeTime(8, 0)) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, StrategyIdea.mainSquadCenter, StrategyIdea.mainSquadCoverRadius);
			List<Unit> myBuildings = UnitUtils.myBuildingsInMainSquadRegion();
			for (Unit building : myBuildings) {
				UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, building.getPosition(), building.getType().sightRange() + MicroConfig.COMMON_ADD_RADIUS);
			}
		}

//		System.out.println("###");
//		System.out.println(unitList);
//		System.out.println(euiList);
	}	
}