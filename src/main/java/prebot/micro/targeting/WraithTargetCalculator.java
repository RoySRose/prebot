package prebot.micro.targeting;

import java.util.HashMap;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
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
		TYPE_SCORE.put(UnitType.Zerg_Mutalisk, 200);
		TYPE_SCORE.put(UnitType.Zerg_Devourer, 200);
		TYPE_SCORE.put(UnitType.Zerg_Scourge, 350);
		
		TYPE_SCORE.put(UnitType.Terran_Bunker, 100);
		TYPE_SCORE.put(UnitType.Terran_Missile_Turret, 200);
		TYPE_SCORE.put(UnitType.Terran_Medic, 300);
		TYPE_SCORE.put(UnitType.Terran_Marine, 400);
		TYPE_SCORE.put(UnitType.Terran_Ghost, 400);
		TYPE_SCORE.put(UnitType.Terran_Battlecruiser, 400);
		TYPE_SCORE.put(UnitType.Terran_Goliath, 400);
		TYPE_SCORE.put(UnitType.Terran_Wraith, 450);
		TYPE_SCORE.put(UnitType.Terran_Valkyrie, 450);
	}

	@Override
	public int calculate(Unit unit, UnitInfo eui) {
		Unit unitInSight = UnitUtils.unitInSight(eui);
		if (unitInSight == null) {
			return CommonCode.NONE;
		}
		
		if (MicroUtils.airEnemyType(unitInSight.getType())) {
			return caculateForEnemy(unit, unitInSight);
		} else {
			return caculateForFeed(unit, unitInSight);
		}
	}

	private int caculateForEnemy(Unit unit, Unit enemyUnit) {
		Integer targetScore = TYPE_SCORE.get(enemyUnit.getType());
		if (targetScore == null) {
			targetScore = 100;
		}
		int distanceScore = getDistanceScore(unit, enemyUnit.getPosition()) * 10; // 레이쓰 전투는 거리를 중요시한다.
		int hitpointScore = getHitPointScore(enemyUnit);
		
		return targetScore + distanceScore + hitpointScore;
	}

	private int caculateForFeed(Unit unit, Unit enemyUnit) {
		int targetScore = criticalHighestScore(enemyUnit);
		if (targetScore == CommonCode.NONE && strikeLevel < StrikeLevel.CRITICAL_SPOT) {
			targetScore = soreHighestScore(enemyUnit);
			if (targetScore == CommonCode.NONE && strikeLevel < StrikeLevel.SORE_SPOT) {
				targetScore = possibleHighestScore(enemyUnit);
			}
		}
		if (targetScore != CommonCode.NONE) {
			int distanceScore = getDistanceScore(unit, enemyUnit.getPosition());
			return targetScore + distanceScore;
		} else {
			return CommonCode.NONE;
		}
	}

	// CRITICAL_SPOT = 3; // 때리면 죽는 곳 공격 (터렛건설중인 SCV, 아모리 건설중인 SCV, 엔지니어링베이 건설중인 SCV)
	private int criticalHighestScore(Unit enemyUnit) {
		if (InfoUtils.enemyRace() == Race.Terran) {
			if (enemyUnit.getType().isWorker()) {
				if (enemyUnit.isConstructing() && enemyUnit.getOrderTarget() != null) {
					UnitType constructingBuilding = enemyUnit.getOrderTarget().getType();
					if (constructingBuilding == UnitType.Terran_Missile_Turret) {
						return 300;
					} else if (constructingBuilding == UnitType.Terran_Armory) {
						return 290;
					} else if (constructingBuilding == UnitType.Terran_Engineering_Bay) {
						return 280;
					}
				}
			}
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (enemyUnit.getType() == UnitType.Zerg_Overlord) {
				return 300;
			}
		}
		return CommonCode.NONE;
	}

	// SORE_SPOT = 2; // 때리면 아픈 곳 공격 (커맨드센터건설중인 SCV, 팩토리 건설중인 SCV, 뭔가 건설중인 SCV, 체력이 적은 SCV, 가까운 SCV, 탱크)
	private int soreHighestScore(Unit enemyUnit) {
		if (InfoUtils.enemyRace() == Race.Terran) {
			if (enemyUnit.getType().isWorker()) {
				if (enemyUnit.isConstructing() && enemyUnit.getOrderTarget() != null) {
					UnitType constructingBuilding = enemyUnit.getOrderTarget().getType();
					if (constructingBuilding == UnitType.Terran_Command_Center) {
						return 270;
					} else if (constructingBuilding == UnitType.Terran_Factory) {
						return 260;
					} else {
						return 250;
					}
				}
				return 240;
			}
			if (enemyUnit.getType() == UnitType.Terran_Missile_Turret && !enemyUnit.isCompleted()) {
				return 220;
			}
			if (enemyUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
				return 230;
			}
			if (enemyUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
				return 220;
			}
			if (enemyUnit.getType().isBuilding() && enemyUnit.getHitPoints() < 250) {
				return 220;
			}
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (enemyUnit.getType().isWorker()) {
				return 290;
			}
		}
		
		return CommonCode.NONE;
	}

	// POSSIBLE_SPOT = 1; // 때릴 수 있는 곳 공격 (벌처, 건물 등 잡히는 대로)
	private int possibleHighestScore(Unit enemyUnit) {
		if (InfoUtils.enemyRace() == Race.Terran) {
			if (enemyUnit.getType() == UnitType.Terran_Supply_Depot) {
				return 210;
			}
		} else if (InfoUtils.enemyRace() == Race.Zerg) {
			if (enemyUnit.getType() == UnitType.Zerg_Lurker) {
				return 280;
			} else if (enemyUnit.getType() == UnitType.Zerg_Zergling) {
				return 270;
			} else if (enemyUnit.getType() == UnitType.Zerg_Larva || enemyUnit.getType() == UnitType.Zerg_Egg || enemyUnit.getType() == UnitType.Zerg_Lurker_Egg) {
				return CommonCode.NONE;
			}
		}
		
		return 200;
	}

	// 0 ~ 10
	private int getDistanceScore(Unit unit, Position enemyPosition) {
		int distanceScore = 10;
		int substract = unit.getDistance(enemyPosition) / 50;
		return Math.max(distanceScore - substract, 0);
	}

	// 0 ~ 50
	private int getHitPointScore(Unit enemyUnit) {
		int hitPointScore = 50;
		double hitPointPercent = (double) enemyUnit.getHitPoints() / enemyUnit.getType().maxHitPoints();
		return (int) (hitPointScore * hitPointPercent);
	}

	// enemyUnit.isCloaked()
	// enemyUnit.isDefenseMatrixed()
}
