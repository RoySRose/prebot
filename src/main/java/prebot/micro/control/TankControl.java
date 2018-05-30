package prebot.micro.control;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.Decision.DecisionType;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class TankControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		
		List<Unit> tankModeList = new ArrayList<>();
		List<Unit> siegeModeList = new ArrayList<>();
		
		for (Unit unit : unitList) {
			if (unit.isSieged()) {
				siegeModeList.add(unit);
			} else {
				tankModeList.add(unit);
			}
		}
		
		executeSiegeMode(siegeModeList, euiList);
		executeTankMode(tankModeList, euiList);
	}
	
	private void executeSiegeMode(List<Unit> siegeModeList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forSiegeMode);
		
		for (Unit siege : siegeModeList) {
			Decision decision = decisionMaker.makeDecision(siege, euiList);
			
//			if (decision.type == DecisionType) {
//				
//			}
			
		}
		
	}
	
	private void executeTankMode(List<Unit> tankModeList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forTankMode);
		FleeOption fOption = new FleeOption(StrategyIdea.campPosition, false, Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, false);
		
		for (Unit tank : tankModeList) {
			Decision decision = decisionMaker.makeDecision(tank, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(tank, decision.eui.getLastPosition(), fOption);
				
			} else if (decision.type == DecisionType.KITING_UNIT) {
				MicroUtils.kiting(tank, decision.eui, kOption);
				
			} else {
				CommandUtils.attackMove(tank, StrategyIdea.campPosition);
			}
		}
	}
}
