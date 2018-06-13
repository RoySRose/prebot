package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.PositionRegion;
import prebot.common.util.UnitUtils;
import prebot.common.util.internal.IConditions.UnitCondition;
import prebot.micro.constant.MicroConfig.MainSquadMode;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.MarineControl;
import prebot.micro.control.factory.GoliathControl;
import prebot.micro.control.factory.TankControl;
import prebot.strategy.StrategyIdea;

public class MainAttackSquad extends Squad {
	
	private MarineControl marineControl = new MarineControl();
	private TankControl tankControl = new TankControl();
	private GoliathControl goliathControl = new GoliathControl();
	
	public MainAttackSquad() {
		super(SquadInfo.MAIN_ATTACK);
	}

	@Override
	public boolean want(Unit unit) {
		if (StrategyIdea.mainSquadMode == MainSquadMode.FD_PRESS) {
			if (unit.getType() == UnitType.Terran_Marine) {
				return true;
			}
		}
		
		return unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
				|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
				|| unit.getType() == UnitType.Terran_Goliath;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void setTargetPosition() {
		targetPosition = StrategyIdea.mainSquadPosition;
	}

	@Override
	public void execute() {
		updateInitiatedFlag();
		
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		List<Unit> marineList = unitListMap.getOrDefault(UnitType.Terran_Marine, new ArrayList<Unit>());
		marineControl.control(marineList, euiList, targetPosition);
		
		List<Unit> tankList = new ArrayList<>();
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Tank_Mode, new ArrayList<Unit>()));
		tankList.addAll(unitListMap.getOrDefault(UnitType.Terran_Siege_Tank_Siege_Mode, new ArrayList<Unit>()));
		tankControl.setSiegeModeSpreadRadius(tankList.size());
		tankControl.control(tankList, euiList, targetPosition);
		
		List<Unit> goliathList = unitListMap.getOrDefault(UnitType.Terran_Goliath, new ArrayList<Unit>());
		goliathControl.control(goliathList, euiList, targetPosition);
	}

	private void updateInitiatedFlag() {
		StrategyIdea.initiated = !euiList.isEmpty();
	}
	
	@Override
	public void findEnemies() {
		if (StrategyIdea.mainSquadMode.isAttackMode) {
			super.findEnemies();
		} else {
			List<Unit> myBuildings = UnitUtils.getUnitsInRegion(PositionRegion.MY_BASE, PlayerRange.SELF, new UnitCondition() {
				@Override public boolean correspond(Unit unit) {
					return unit.getType().isBuilding() && !unit.isFlying();
				}
			});
			euiList.clear();
			for (Unit building : myBuildings) {
				UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, building.getPosition(), building.getType().sightRange() + SquadInfo.MAIN_ATTACK.squadRadius);
			}
		}
	}
}