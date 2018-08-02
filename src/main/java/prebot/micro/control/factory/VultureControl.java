package prebot.micro.control.factory;

import java.util.Collection;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMakerPrebot1;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.SpiderMineManger;
import prebot.strategy.manage.SpiderMineManger.MinePositionLevel;
import prebot.strategy.manage.VultureTravelManager;

public class VultureControl extends Control {
	
	private Position targetPosition;
	
	public void setTargetPosition(Position targetPosition) {
		this.targetPosition = targetPosition;
	}

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		euiList = MicroUtils.filterTargetInfos(euiList, false);
		
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, false, Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			int saveUnitLevel = 2;
			if (this.targetPosition != null) {
				saveUnitLevel = 0;
			}
			
			Decision decision = DecisionMakerPrebot1.makeDecisionPrebot1(unit, euiList, null, saveUnitLevel);
			
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
			} else if (decision.type == DecisionType.KITING_UNIT) {
				MicroUtils.kiting(unit, decision.eui, kOption);
			} else {
				if (spiderMineOrderIssue(unit)) {
					continue;
				}
				
				Position targetPosition;
				if (this.targetPosition != null) {
					targetPosition = this.targetPosition; // 게릴라 등
				} else {
					targetPosition = getCheckerTargetPosition(unit); // 체커
				}
				CommandUtils.attackMove(unit, targetPosition);
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

	
	private boolean spiderMineOrderIssue(Unit vulture) {
		Region vultureRegion = BWTA.getRegion(vulture.getPosition());
		if (vultureRegion == BWTA.getRegion(InfoUtils.myBase().getPosition())) {
			return false;
		}
		
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine == null) {
			positionToMine = SpiderMineManger.Instance().reserveSpiderMine(vulture, MinePositionLevel.ONLY_GOOD_POSITION);
		}
		if (positionToMine != null) {
			CommandUtils.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
			return true;
		}
		
		Unit spiderMineToRemove = SpiderMineManger.Instance().mineToRemove(vulture);
		if (spiderMineToRemove != null) {
			CommandUtils.attackUnit(vulture, spiderMineToRemove);
			return true;
		}
		
		return false;
	}
}
