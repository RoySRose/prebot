

import java.util.Collection;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class VultureControl extends Control {
	
	private Position targetPosition;
	
	public void setTargetPosition(Position targetPosition) {
		this.targetPosition = targetPosition;
	}

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		FleeOption fOption = new FleeOption(StrategyIdea.mainSquadCenter, false, MicroConfig.Angles.WIDE);
		KitingOption kOption = new KitingOption(fOption, KitingOption.CoolTimeAttack.COOLTIME_ALWAYS);
		
		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			
			int saveUnitLevel;
			if (this.targetPosition != null) {
				saveUnitLevel = 0;
			} else {
				if (InfoUtils.enemyRace() == Race.Protoss) {
					saveUnitLevel = 2;					
				} else {
					saveUnitLevel = 1;
				}
			}
			
			MicroDecision decision = MicroDecisionMakerPrebot1.makeDecisionPrebot1(unit, euiList, null, saveUnitLevel);
			
			if (decision.type == MicroDecision.MicroDecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(unit, decision.eui.getLastPosition(), fOption);
			} else if (decision.type == MicroDecision.MicroDecisionType.KITING_UNIT) {
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
			positionToMine = SpiderMineManger.Instance().reserveSpiderMine(vulture, SpiderMineManger.MinePositionLevel.ONLY_GOOD_POSITION);
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
