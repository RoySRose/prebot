package prebot.micro.squad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.IConditions.UnitCondition;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.GoliathControl;
import prebot.micro.control.factory.TankControl;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class MainAttackSquad extends Squad {

	private Set<UnitInfo> euisNearUnit = new HashSet<>();
	private Set<UnitInfo> euisNearBuilding = new HashSet<>();
	
	public Set<UnitInfo> getEuiListNearUnit() {
		return euisNearUnit;
	}

	public Set<UnitInfo> getEuiListNearBuilding() {
		return euisNearBuilding;
	}

	private TankControl tankControl = new TankControl();
	private GoliathControl goliathControl = new GoliathControl();
	
	public MainAttackSquad() {
		super(SquadInfo.MAIN_ATTACK);
		setUnitType(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath);
	}

	@Override
	public boolean want(Unit unit) {
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		this.updateInitiatedFlag();
		
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		
		List<Unit> tankList = new ArrayList<>();
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Tank_Mode, new ArrayList<Unit>()));
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Siege_Mode, new ArrayList<Unit>()));
		tankControl.control(tankList, euiList);
		
		List<Unit> goliathList = unitListMap.getOrDefault(UnitType.Terran_Goliath, new ArrayList<Unit>());
		goliathControl.control(goliathList, euiList);
	}

	private void updateInitiatedFlag() {
		if (euiList.isEmpty()) {
			StrategyIdea.initiated = false;
		}
		
		for (UnitInfo eui : euiList) {
			if (eui.getType() != UnitType.Terran_Vulture_Spider_Mine
					&& eui.getType() != UnitType.Zerg_Larva
					&& !eui.getType().isBuilding()
					&& !eui.getType().isWorker()
					&& !eui.getType().isFlyer()) {
				StrategyIdea.initiated = true;
			}
		}
	}
	
	@Override
	public void findEnemies() {
		euisNearUnit.clear();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euisNearUnit, unit.getPosition(), unit.getType().sightRange() + SquadInfo.MAIN_ATTACK.squadRadius);
		}
		
		euisNearBuilding.clear();
		List<Unit> myBuildings = UnitUtils.getUnitsInRegion(RegionType.MY_BASE, PlayerRange.SELF, new UnitCondition() {
			@Override public boolean correspond(Unit unit) {
				return unit.getType().isBuilding() && !unit.isFlying();
			}
		});
		for (Unit building : myBuildings) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euisNearBuilding, building.getPosition(), building.getType().sightRange() + SquadInfo.MAIN_ATTACK.squadRadius);
		}
		
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			euiList = euisNearUnit;
		} else {
			euiList = euisNearBuilding;
			euiList.addAll(InfoUtils.euiListInThirdRegion());
		}
	}
	
}