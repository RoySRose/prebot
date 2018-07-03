package prebot.micro.control.factory;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwta.BaseLocation;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.micro.targeting.DefaultTargetCalculator;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.VultureTravelManager;

public class VultureControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		
		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.KEEP_SAFE_DISTANCE);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			Decision decision = decisionMaker.makeDecision(unit, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
			} else if (decision.type == DecisionType.KITING_UNIT) {
				MicroUtils.kiting(unit, decision.eui, kOption);
			} else {
				Position checkerTargetPosition = getCheckerTargetPosition(unit);
				CommandUtils.attackMove(unit, checkerTargetPosition);
			}
		}
	}

	private Position getCheckerTargetPosition(Unit unit) {
		BaseLocation checkerTargetBase = VultureTravelManager.Instance().getCheckerTravelSite(unit.getID());
		if (checkerTargetBase != null) {
			return checkerTargetBase.getPosition();
		} else {
			return StrategyIdea.mainSquadCenter;
		}
	}
}
