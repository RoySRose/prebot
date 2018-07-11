package prebot.micro.targeting;

import java.util.HashMap;
import java.util.Map;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceManager.StrikeLevel;

public class WraithTargetCalculator extends TargetScoreCalculator {
	
	public WraithTargetCalculator() {
		this.strikeLevel = AirForceManager.Instance().getStrikeLevel();
	}

	public void setStrikeLevel(int strikeLevel) {
		this.strikeLevel = strikeLevel;
	}

	private int strikeLevel;
	
	private static final Map<UnitType, Integer> TYPE_SCORE = new HashMap<>();
	static {
		TYPE_SCORE.put(UnitType.Zerg_Spore_Colony, 100);
		TYPE_SCORE.put(UnitType.Zerg_Hydralisk, 200);
		TYPE_SCORE.put(UnitType.Zerg_Mutalisk, 300);
		TYPE_SCORE.put(UnitType.Zerg_Devourer, 400);
		TYPE_SCORE.put(UnitType.Zerg_Scourge, 500);
		
		TYPE_SCORE.put(UnitType.Terran_Bunker, 100);
		TYPE_SCORE.put(UnitType.Terran_Missile_Turret, 200);
		TYPE_SCORE.put(UnitType.Terran_Medic, 300);
		TYPE_SCORE.put(UnitType.Terran_Marine, 400);
		TYPE_SCORE.put(UnitType.Terran_Ghost, 500);
		TYPE_SCORE.put(UnitType.Terran_Battlecruiser, 600);
		TYPE_SCORE.put(UnitType.Terran_Goliath, 700);
		TYPE_SCORE.put(UnitType.Terran_Wraith, 800);
		TYPE_SCORE.put(UnitType.Terran_Valkyrie, 900);
	}

	@Override
	public int calculate(Unit unit, UnitInfo eui) {
		Unit unitInSight = UnitUtils.unitInSight(eui);
		if (unitInSight == null) {
			return CommonCode.NONE;
		}

		if (unitInSight.getType().airWeapon() != WeaponType.None || unitInSight.getType() == UnitType.Protoss_Carrier) {
			return caculateForEnemy(unit, unitInSight);
		} else {
			return caculateForFeed(unit, unitInSight);
		}
	}

	private int caculateForEnemy(Unit unit, Unit enemyUnit) {
		return TYPE_SCORE.get(enemyUnit.getType());
	}

	private int caculateForFeed(Unit unit, Unit enemyUnit) {
		int score = criticalHighestScore(enemyUnit);
		if (score == CommonCode.NONE && strikeLevel < StrikeLevel.CRITICAL_SPOT) {
			score = soreHighestScore(enemyUnit);
			if (score == CommonCode.NONE && strikeLevel < StrikeLevel.SORE_SPOT) {
				score = possibleHighestScore(enemyUnit);
			}
		}
		return score;
	}

	// CRITICAL_SPOT = 3; // 때리면 죽는 곳 공격 (터렛건설중인 SCV, 아모리 건설중인 SCV, 엔지니어링베이 건설중인 SCV)
	private int criticalHighestScore(Unit enemyUnit) {
		if (InfoUtils.enemyRace() == Race.Terran) {
			if (enemyUnit.getType().isWorker()) {
				if (enemyUnit.isConstructing()) {
					UnitType constructingBuilding = enemyUnit.getOrderTarget().getType();
					if (constructingBuilding == UnitType.Terran_Missile_Turret) {
						return 30;
					} else if (constructingBuilding == UnitType.Terran_Armory) {
						return 29;
					} else if (constructingBuilding == UnitType.Terran_Engineering_Bay) {
						return 28;
					}
				}
			}
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (enemyUnit.getType() == UnitType.Zerg_Overlord) {
				return 30;
			}
		}
		return CommonCode.NONE;
	}

	// SORE_SPOT = 2; // 때리면 아픈 곳 공격 (커맨드센터건설중인 SCV, 팩토리 건설중인 SCV, 뭔가 건설중인 SCV, 체력이 적은 SCV, 가까운 SCV, 탱크)
	private int soreHighestScore(Unit enemyUnit) {
		if (InfoUtils.enemyRace() == Race.Terran) {
			if (enemyUnit.getType().isWorker()) {
				if (enemyUnit.isConstructing()) {
					UnitType constructingBuilding = enemyUnit.getOrderTarget().getType();
					if (constructingBuilding == UnitType.Terran_Command_Center) {
						return 27;
					} else if (constructingBuilding == UnitType.Terran_Factory) {
						return 26;
					} else {
						return 25;
					}
				}
				return 24;
			}
			if (enemyUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				return 23;
			}
			if (enemyUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
				return 22;
			}
			if (enemyUnit.getType().isBuilding() && enemyUnit.getHitPoints() < 250) {
				return 22;
			}
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (enemyUnit.getType().isWorker()) {
				return 29;
			}
		}
		
		return CommonCode.NONE;
	}

	// POSSIBLE_SPOT = 1; // 때릴 수 있는 곳 공격 (벌처, 건물 등 잡히는 대로)
	private int possibleHighestScore(Unit enemyUnit) {
		if (InfoUtils.enemyRace() == Race.Terran) {
			if (enemyUnit.getType() == UnitType.Terran_Supply_Depot) {
				return 21;
			}
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (enemyUnit.getType() == UnitType.Zerg_Lurker) {
				return 28;
			} else if (enemyUnit.getType() == UnitType.Zerg_Zergling) {
				return 27;
			} else if (enemyUnit.getType() == UnitType.Zerg_Larva || enemyUnit.getType() == UnitType.Zerg_Egg || enemyUnit.getType() == UnitType.Zerg_Lurker_Egg) {
				return CommonCode.NONE;
			}
		}
		
		return 20;
	}

	// enemyUnit.isCloaked()
	// enemyUnit.isDefenseMatrixed()
}
