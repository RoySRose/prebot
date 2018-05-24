package prebot.micro.squad.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.brain.Idea;
import prebot.brain.Info;
import prebot.brain.information.UnitInfo;
import prebot.common.code.Code.PlayerRange;
import prebot.common.code.Code.SquadName;
import prebot.common.code.ConfigForMicro.Angles;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.Decision.DecisionType;
import prebot.micro.squad.Squad;

public class EarlyDefenseSquad extends Squad {

	private static final int REACT_RADIUS = 50;

	public EarlyDefenseSquad(int priority, int squadRadius) {
		super(SquadName.EARLY_DEFENSE, priority, squadRadius);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_SCV && unit.getHitPoints() > 16;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		BaseLocation myBase = Info.of().myBase;
		Region myRegion = BWTA.getRegion(myBase.getPosition());
		List<UnitInfo> euiList = Info.of().euiListInMyRegion.get(myRegion);
		
		if (euiList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Unit> recruitList = defenseForUnit(assignableUnitList, myBase, euiList);
//		List<Unit> recruitList = defenseForBuilding(assignableUnitList, myBase, euiList);
		
		return recruitList;
	}

	// 저글링 러시면 저글링 당 xxx기를 준비한다.
	// 건물 공격이면 건물 당 3기를 준비한다.
	private List<Unit> defenseForUnit(List<Unit> assignableUnitList, BaseLocation myBase, List<UnitInfo> euiList) {
		List<Unit> enemyUnitList = new ArrayList<>();
		for (UnitInfo eui : euiList) {
			Unit enemy = UnitUtils.unitInSight(eui);
			if (enemy != null) {
				enemyUnitList.add(enemy);
			}
		}
		
		// 메인베이스와 가장 가까운 적 유닛이, 아군유닛의 REACT_RADIUS 내로 들어왔으면 유닛 할당
		Unit closeEnemyUnit = UnitUtils.getClosestUnitToPosition(enemyUnitList, myBase.getPosition());
		if (closeEnemyUnit == null) {
			return Collections.emptyList();
		}
		
		List<Unit> myUnitList = UnitUtils.getUnitsInRadius(closeEnemyUnit.getPosition(), REACT_RADIUS, PlayerRange.SELF); 
		if (myUnitList.isEmpty()) {
			return Collections.emptyList();
		}
		
		double scvCountForDefense = 0.0;
		for (Unit enemy : enemyUnitList) {
			if (UnitUtils.isValidUnit(enemy)) {
				if (enemy.getType() == UnitType.Protoss_Probe || enemy.getType() == UnitType.Zerg_Drone) {
					scvCountForDefense += 1.0;
				} else if (enemy.getType() == UnitType.Terran_SCV) {
					scvCountForDefense += 1.5;
				} else if (enemy.getType() == UnitType.Protoss_Zealot) {
					scvCountForDefense += 4;
				} else if (enemy.getType() == UnitType.Zerg_Zergling) {
					scvCountForDefense += 2;
				}  else if (enemy.getType() == UnitType.Terran_Marine) {
					scvCountForDefense += 2;
				}
			}
		}
		
		List<Unit> recruitList = new ArrayList<>();
		List<Unit> candidateUnitList = new ArrayList<>(assignableUnitList);
		while (recruitList.size() + unitList.size() < scvCountForDefense) {
			Unit defenseScv = UnitUtils.getClosestMineralWorkerToPosition(candidateUnitList, closeEnemyUnit.getPosition());
			if (defenseScv == null) {
				break;
			}
			recruitList.add(defenseScv);
			candidateUnitList.remove(defenseScv);
		}
		return recruitList;
	}

	@Override
	public void execute() {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forVulture);
		FleeOption fOption = new FleeOption(Idea.of().campPosition, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, false);
		
		for (Unit unit : unitList) {
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == DecisionType.KITING_UNIT) {
				MicroUtils.kiting(unit, decision.eui, kOption);

			} else {
				CommandUtils.attackMove(unit, Idea.of().attackPosition);
			}
		}
	}

}
