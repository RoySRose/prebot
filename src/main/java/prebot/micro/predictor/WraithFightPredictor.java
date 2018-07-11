package prebot.micro.predictor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.SmallFightPredict;

public class WraithFightPredictor {
	
	public static SmallFightPredict airForcePredictByUnitInfo(List<Unit> wraithList, List<UnitInfo> euiList, boolean cloakingBonus) {
		int wraithPower = powerOfAirForce(wraithList, cloakingBonus);
		int enemyPower = powerOfEnemiesByUnitInfo(euiList);
		
		if (wraithPower > enemyPower) {
			return SmallFightPredict.ATTACK;
		} else {
			return SmallFightPredict.BACK;
		}
	}

	// 벌처 한기 최대 점수 : 70점 (업그레이드시 100점)
	public static int powerOfAirForce(List<Unit> wraithList, boolean cloakingBonus) {
		int totalPower = 0;
		for (Unit wraith : wraithList) {
			double hitPointRate = (double) wraith.getHitPoints() / UnitType.Terran_Wraith.maxHitPoints();
			totalPower += POWER_WRAITH * hitPointRate;
			
			if (cloakingBonus) {
				totalPower += POWER_CLOAKING;
			}
		}
		return totalPower;
	}
	
	public static int powerOfEnemiesByUnitInfo(List<UnitInfo> euiList) {
		int enemyTotalPower = 0;
		for (UnitInfo eui : euiList) {
			enemyTotalPower += powerOfUnit(eui);
		}
		return enemyTotalPower;
	}

	private static int powerOfUnit(UnitInfo eui) {
		Integer enemyPower = WRAITH_TARGET.get(eui.getType());
		if (enemyPower == null) {
			return 0;
		}
		Unit unitInSight = UnitUtils.unitInSight(eui);
		if (unitInSight != null) {
			double hitPointRate = (double) unitInSight.getHitPoints() / eui.getType().maxHitPoints();
			return (int) (enemyPower * hitPointRate);
		} else {
			return enemyPower;
		}
	}

	
	private static final int POWER_WRAITH = 100;
	private static final int POWER_CLOAKING = 200;
	private static final Map<UnitType, Integer> WRAITH_TARGET = new HashMap<>();
	
	static {
		WRAITH_TARGET.put(UnitType.Zerg_Hydralisk, 150);
		WRAITH_TARGET.put(UnitType.Zerg_Mutalisk, 150);
		WRAITH_TARGET.put(UnitType.Zerg_Scourge, 500);
		WRAITH_TARGET.put(UnitType.Zerg_Devourer, 500);
		WRAITH_TARGET.put(UnitType.Zerg_Sunken_Colony, 1500);
		
		WRAITH_TARGET.put(UnitType.Terran_Marine, 100);
		WRAITH_TARGET.put(UnitType.Terran_Medic, 200);
		WRAITH_TARGET.put(UnitType.Terran_Ghost, 200);
		WRAITH_TARGET.put(UnitType.Terran_Goliath, 600);
		WRAITH_TARGET.put(UnitType.Terran_Wraith, 100);
		WRAITH_TARGET.put(UnitType.Terran_Valkyrie, 400);
		WRAITH_TARGET.put(UnitType.Terran_Battlecruiser, 600);
		WRAITH_TARGET.put(UnitType.Terran_Bunker, 1500);
		WRAITH_TARGET.put(UnitType.Terran_Missile_Turret, 1000);
	}

}
