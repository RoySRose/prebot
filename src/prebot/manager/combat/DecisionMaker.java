package prebot.manager.combat;

import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.ConfigForMicro.Flee;
import prebot.main.PreBot;
import prebot.manager.information.UnitInfo;

public class DecisionMaker {
	
	private CalcTargetScore calcTargetScore;
	
	public DecisionMaker(CalcTargetScore calcTargetScore) {
		this.calcTargetScore = calcTargetScore;
	}

	public Decision makeDecisionForRangedUnit(Unit rangedUnit, List<UnitInfo> euiList, int aggressivity) {
		return null;
	}
	
	public Decision makeDecisionForFlyer(Unit myUnit, List<UnitInfo> euiList, Position targetPosition, KitingOption kOption, int aggressivity) {
		if (aggressivity == 0) {
			UnitInfo eui = getAvoidTarget(myUnit, euiList);
			if (eui != null) {
				return Decision.fleeFromUnit(myUnit, eui, kOption);
			}
		} else {
			UnitInfo eui = getBestTarget(myUnit, euiList);
			if (eui != null) {
				return Decision.kitingUnit(myUnit, eui, kOption);
			}
		}
		return Decision.attackPosition(myUnit, targetPosition);
	}
	
	private UnitInfo getAvoidTarget(Unit myUnit, List<UnitInfo> euiList) {
		for (UnitInfo eui : euiList) {
			int distanceToNearEnemy = myUnit.getDistance(eui.lastPosition);
			int enemyWeaponRange = 0;
			
			if (eui.type == UnitType.Terran_Bunker) {
				enemyWeaponRange = PreBot.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 32;
			} else {
				if (!myUnit.isFlying()) {
					enemyWeaponRange = PreBot.Broodwar.enemy().weaponMaxRange(eui.type.airWeapon());
				} else {
					enemyWeaponRange = PreBot.Broodwar.enemy().weaponMaxRange(eui.type.groundWeapon());
				}
			}
			
			if (distanceToNearEnemy < enemyWeaponRange + Flee.BACKOFF_DIST) {
				return eui;
			}
		}
		return null;
	}
	
	private UnitInfo getBestTarget(Unit myUnit, List<UnitInfo> euiList) {
		UnitInfo bestEui = null;
		int highestScore = 0;
		for (UnitInfo eui : euiList) {
			 int score = calcTargetScore.calculate(eui);
			 if (score > highestScore) {
				 bestEui = eui;
				 highestScore = score;
			 }
		}
		return bestEui;
	}
}
