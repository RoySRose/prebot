package prebot.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.DamageType;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.MirrorBugFixed;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.constant.MicroConfig.Flee;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.strategy.UnitInfo;

/**
 * 마이크로 컨트롤 유틸
 * 
 * @author insaneojw
 *
 */
public class MicroUtils {

	private static final int WRAITH_MOVE_DISTANCE_48_FRAMES = moveDistancePerFrame(UnitType.Terran_Wraith, 48); // 1.5초간 움직이는 거리
	private static final int WRAITH_MOVE_DISTANCE_24_FRAMES = moveDistancePerFrame(UnitType.Terran_Wraith, 24); // 1.0초간 움직이는 거리
	private static final int WRAITH_MOVE_DISTANCE_12_FRAMES = moveDistancePerFrame(UnitType.Terran_Wraith, 12); // 0.5초간 움직이는 거리
	
	private static final int AIR_DRIVING_PRE_EXPECT_SECS = 0;
	private static final Map<UnitType, Integer> RISK_RADIUS_MAP = new HashMap<>();

	/// 레이쓰 전용
	public static Position airDrivingPosition(Position startPosition, Position endPosition, int[] driveAngle) {
		if (startPosition.getDistance(endPosition) <= WRAITH_MOVE_DISTANCE_12_FRAMES) {
			return endPosition;
		}
		
		double radianToMovePosition = oppositeDirectionRadian(endPosition, startPosition);
		double moveRadian = 0.0d;
		Position candiPosition = Position.None;
		
		double minimumDistance = CommonCode.DOUBLE_MAX;
		double minimumDistanceRadian = 0.0d;
		
		for (int angle : driveAngle) {
			moveRadian = rotate(radianToMovePosition, angle);
			candiPosition = getMovePosition(startPosition, moveRadian, WRAITH_MOVE_DISTANCE_48_FRAMES);
			if (!PositionUtils.isValidPosition(candiPosition)) {
				continue;
			}
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(candiPosition, 80, UnitUtils.enemyAirDefenseUnitType());
			if (!enemyDefTowerList.isEmpty()) {
				continue;
			}
			double distance = getDistanceAfterSeconds(candiPosition, endPosition, AIR_DRIVING_PRE_EXPECT_SECS);
			if (distance < minimumDistance) {
				minimumDistance = distance;
				minimumDistanceRadian = moveRadian;
			}
		}
		
		if (minimumDistance == CommonCode.DOUBLE_MAX) {
			return null;
		}
		
		double realMoveRadian = minimumDistanceRadian;
		double adjustedRadian = 0.0d;
		for (int angle : Angles.AIR_FORCE_DRIVE_DETAIL) {
			adjustedRadian = rotate(minimumDistanceRadian, angle);
			candiPosition = getMovePosition(startPosition, adjustedRadian, WRAITH_MOVE_DISTANCE_12_FRAMES);
			if (!PositionUtils.isValidPosition(candiPosition)) {
				continue;
			}
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(candiPosition, 80, UnitUtils.enemyAirDefenseUnitType());
			if (enemyDefTowerList.isEmpty()) {
				realMoveRadian = adjustedRadian;
				break;
			}
		}
		Position airDrivingPosition = getMovePosition(startPosition, realMoveRadian, WRAITH_MOVE_DISTANCE_48_FRAMES);
		if (!PositionUtils.isValidPosition(airDrivingPosition)) {
			airDrivingPosition = getMovePosition(startPosition, realMoveRadian, WRAITH_MOVE_DISTANCE_24_FRAMES);
		}
		if (!PositionUtils.isValidPosition(airDrivingPosition)) {
			airDrivingPosition = getMovePosition(startPosition, realMoveRadian, WRAITH_MOVE_DISTANCE_12_FRAMES);
		}
		return airDrivingPosition;
	}
	
	private static double getDistanceAfterSeconds(Position startPosition, Position endPosition, int seconds) {
		if (seconds == 0) {
			return startPosition.getDistance(endPosition);
		}
		
		double radianToMovePosition = oppositeDirectionRadian(endPosition, startPosition);
		double moveRadian = 0.0d;
		Position candiPosition = Position.None;
		
		double minimumDistance = CommonCode.DOUBLE_MAX;
		
		for (int angle : Angles.AIR_FORCE_DRIVE) {
			moveRadian = rotate(radianToMovePosition, angle);
			candiPosition = getMovePosition(startPosition, moveRadian, WRAITH_MOVE_DISTANCE_48_FRAMES);
			if (!PositionUtils.isValidPosition(candiPosition)) {
				continue;
			}
			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(candiPosition, 0, UnitUtils.enemyAirDefenseUnitType());
			if (!enemyDefTowerList.isEmpty()) {
				continue;
			}
			double distance = getDistanceAfterSeconds(candiPosition, endPosition, seconds - 1);
			if (distance < minimumDistance) {
				minimumDistance = distance;
			}
		}
		return minimumDistance;
	}

	public static void flee(Unit fleeUnit, Position targetPosition, FleeOption fOption) {
		fleeUnit.rightClick(getFleePosition(fleeUnit, targetPosition, fOption));
	}
	
	public static void kiting(Unit rangedUnit, UnitInfo targetInfo, KitingOption kOption) {
		if (UnitUtils.unitInSight(targetInfo) == null) {
			CommandUtils.move(rangedUnit, targetInfo.getLastPosition());
		} else {
			kiting(rangedUnit, targetInfo.getUnit(), kOption);
		}
	}
	
	public static void kiting(Unit rangedUnit, Unit targetUnit, KitingOption kOption) {
		if (!killedByNShot(rangedUnit, targetUnit, 1) && killedByNShot(targetUnit, rangedUnit, 2)) {
			kOption.cooltimeAlwaysAttack = CoolTimeAttack.KEEP_SAFE_DISTANCE;
			kOption.fOption.united = false;
			kOption.fOption.angles = Angles.WIDE;
		} else if (groundUnitFreeKiting(rangedUnit)) {
			kOption.fOption.united = false;
			kOption.fOption.angles = Angles.WIDE;
		}
		
		if (timeToAttack(rangedUnit, targetUnit, kOption.cooltimeAlwaysAttack)) {
			if (targetUnit.isVisible()) {
				CommandUtils.attackUnit(rangedUnit, targetUnit);
			} else {
				// TODO 테스트 필요
				if (!rangedUnit.isInWeaponRange(targetUnit)) {
					flee(rangedUnit, targetUnit.getPosition(), kOption.fOption);
				} else {
					CommandUtils.attackUnit(rangedUnit, targetUnit);
				}
			}
			
			return;
		} else {
			int approachKitingDistance = forwardKitingTargetDistance(rangedUnit, targetUnit);
			if (approachKitingDistance != CommonCode.NONE && rangedUnit.getDistance(targetUnit) >= approachKitingDistance) {
				CommandUtils.attackMove(rangedUnit, targetUnit.getPosition());
			} else {
				flee(rangedUnit, targetUnit.getPosition(), kOption.fOption);
			}
		}
	}

	private static boolean timeToAttack(Unit rangedUnit, Unit targetUnit, CoolTimeAttack cooltimeAttack) {

		// attackUnit, target 각각의 지상/공중 무기를 선택
		WeaponType attackUnitWeapon = targetUnit.isFlying() ? rangedUnit.getType().airWeapon() : rangedUnit.getType().groundWeapon();
		WeaponType targetWeapon = rangedUnit.isFlying() ? targetUnit.getType().airWeapon() : targetUnit.getType().groundWeapon();
		
		// 일꾼의 공격력은 강하지 않다.
		if (targetUnit.getType().isWorker() && !rangedUnit.isUnderAttack()) {
			return true;
		}

		// 벌처는 벌처에게 카이팅하지 않는다.
		if (rangedUnit.getType() == UnitType.Terran_Vulture) {
			if (targetUnit.getType() == UnitType.Terran_Vulture) {
				return true;
			}
		}
		// 벌처가 아닌경우, 자신보다 보다 긴 사정거리를 가진 적에게 카이팅은 무의미하다.
		else if (Prebot.Broodwar.self().weaponMaxRange(attackUnitWeapon) <= Prebot.Broodwar.enemy().weaponMaxRange(targetWeapon)) {
			return true;
		}
		
		int cooltime = rangedUnit.isStartingAttack() ? attackUnitWeapon.damageCooldown() // // 쿨타임시간(frame)
				: (targetUnit.isFlying() ? rangedUnit.getAirWeaponCooldown() : rangedUnit.getGroundWeaponCooldown());
		double distanceToAttack = rangedUnit.getDistance(targetUnit) - Prebot.Broodwar.self().weaponMaxRange(attackUnitWeapon); // 공격하기 위해 이동해야 하는 거리(pixel)
		int catchTime = (int) (distanceToAttack / rangedUnit.getType().topSpeed()); // 상대를 잡기위해 걸리는 시간 (frame) = 거리(pixel) / 속도(pixel per frame)
		
		// 상대가 때리기 위해 거리를 좁히거나 벌려야 하는 경우(coolTime <= catchTime)
		if (cooltime <= catchTime + Prebot.Broodwar.getLatency() * 2) { // 명령에 대한 지연시간(latency)을 더한다. ex) LAN(UDP) : 5
			return true;
		}
		
		// TODO 테스트 필요. 사정거리를 유지
		if (cooltimeAttack == CoolTimeAttack.COOLTIME_ALWAYS_IN_RANGE) {
			if (!rangedUnit.isInWeaponRange(targetUnit)) {
				return true;
			}
		}
		// 쿨타임이 되었을 때 항시 공격할 것인가
		return cooltimeAttack.coolTimeAlwaysAttack && cooltime == 0;
	}
	
	private static int forwardKitingTargetDistance(Unit rangedUnit, Unit targetUnit) {
		if (targetUnit.getType().isBuilding()) { // 해처리 라바때문에 마인 폭사함
			return 70;

		} else if (targetUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
			return 1;

		} else if (targetUnit.getType() == UnitType.Protoss_Carrier || targetUnit.getType() == UnitType.Zerg_Overlord) {
			return 50;
		}
		return CommonCode.NONE;
	}
	
	private static boolean groundUnitFreeKiting(Unit rangedUnit) {
		List<Unit> nearUnits = UnitUtils.getUnitsInRadius(PlayerRange.SELF, rangedUnit.getPosition(), (int) (rangedUnit.getType().topSpeed() * rangedUnit.getType().groundWeapon().damageCooldown() * 0.8));
		boolean freeKiting = true;
		int myGroundUnitCount = 0;
		for (Unit unit : nearUnits) {
			if (unit.getType().isWorker() || unit.isFlying() || unit.getType().isBuilding()) {
				continue;
			}
			if (++myGroundUnitCount > 2) {
				freeKiting = false;
				break;
			}
		}
		return freeKiting;
	}
	
	private static Position getFleePosition(Unit fleeUnit, Position targetPosition, FleeOption fOption) {
		double fleeRadian = oppositeDirectionRadian(fleeUnit.getPosition(), targetPosition);
		Position fleePosition = Position.None;
		int moveDistanceOneSec = moveDistancePerFrame(fleeUnit, TimeUtils.SECOND); // 1초간 움직이는 거리
		int riskRadius = getRiskRadius(fleeUnit.getType());
		
		for (int moveDistance = moveDistanceOneSec; moveDistanceOneSec > 10; moveDistanceOneSec = (int) (moveDistanceOneSec * 0.7)) {
			fleePosition = lowestRiskPosition(fleeUnit, fOption, fleeRadian, moveDistance, riskRadius);
			if (fleePosition != Position.None) {
				break;
			}
		}
		return PositionUtils.isValidPosition(fleePosition) ? fleePosition : fOption.goalPosition;
	}
	
	private static int getRiskRadius(UnitType unitType) {
		if (RISK_RADIUS_MAP.isEmpty()) {
			RISK_RADIUS_MAP.put(UnitType.Terran_Vulture, Flee.RISK_RADIUS_VULTURE);
			RISK_RADIUS_MAP.put(UnitType.Terran_Siege_Tank_Tank_Mode, Flee.RISK_RADIUS_TANK);
			RISK_RADIUS_MAP.put(UnitType.Terran_Goliath, Flee.RISK_RADIUS_GOLIATH);
			RISK_RADIUS_MAP.put(UnitType.Terran_Wraith, Flee.RISK_RADIUS_WRAITH);
			RISK_RADIUS_MAP.put(UnitType.Terran_Science_Vessel, Flee.RISK_RADIUS_VESSEL);
		}
		
		Integer riskRadius = RISK_RADIUS_MAP.get(unitType);
		if (riskRadius == null) {
			riskRadius = Flee.RISK_RADIUS_DEFAULT;
		}
		return riskRadius;
		
	}

	/// 반대 방향의 각도(radian)
	private static double oppositeDirectionRadian(Position myPosition, Position targetPosition) {
		return Math.atan2(myPosition.getY() - targetPosition.getY(), myPosition.getX() - targetPosition.getX());
	}

	private static Position lowestRiskPosition(Unit unit, FleeOption fOption, double standRadian, int moveDistance, int riskRadius) {
		Position bestPosition = Position.None;
		int minimumRisk = CommonCode.INT_MAX;
		int distFromBestToGoal = CommonCode.INT_MAX;
		
		double moveRadian = 0.0d;
		Position candiPosition = Position.None;
		int riskOfCandiPosition = 0;
		int distFromCandiToGoal = 0;

		for (Integer angle : fOption.angles) {
			moveRadian = rotate(standRadian, angle);
			candiPosition = getMovePosition(unit.getPosition(), moveRadian, moveDistance);
			if (!PositionUtils.isValidPositionToMove(candiPosition, unit)) {
				continue;
			}
			
			distFromCandiToGoal = candiPosition.getApproxDistance(fOption.goalPosition);
			riskOfCandiPosition = riskOfPosition(unit.getType(), candiPosition, riskRadius, fOption.united);
			
			if (riskOfCandiPosition < minimumRisk || (riskOfCandiPosition == minimumRisk && distFromCandiToGoal < distFromBestToGoal)) {
				bestPosition = candiPosition;
				distFromBestToGoal = distFromCandiToGoal;
				minimumRisk = riskOfCandiPosition;
			}
		}
		return bestPosition;
	}

	/// sourcePosition에서 moveRadian의 각으로 moveDistance만큼 떨어진 포지션
	private static Position getMovePosition(Position sourcePosition, double moveRadian, int moveDistance) {
		int x = (int) (moveDistance * Math.cos(moveRadian));
		int y = (int) (moveDistance * Math.sin(moveRadian));
		return new Position(sourcePosition.getX() + x, sourcePosition.getY() + y);
	}

	/// * 참조사이트: http://yc0345.tistory.com/45
	/// 공식: radian = (π / 180) * 각도
	/// -> 각도 = (radian * 180) / π
	/// -> 회원 radian = (π / 180) * ((radian * 180) / π + 회전각)
	public static double rotate(double radian, int angle) {
		return (Math.PI / 180) * ((radian * 180 / Math.PI) + angle);
	}

	private static int riskOfPosition(UnitType myUnitType, Position movePosition, int radius, boolean united) {
		int risk = 0;
		List<Unit> unitsInRadius = UnitUtils.getUnitsInRadius(PlayerRange.ALL, movePosition, radius);
		for (Unit unit : unitsInRadius) {
			if (unit.getPlayer() == Prebot.Broodwar.enemy()) { // 적군인 경우
				if (Prebot.Broodwar.getDamageFrom(unit.getType(), myUnitType) > 0) { // 적군이 공격할 수 있으면 위험하겠지
					if (unit.getType().isBuilding()) { // 건물이 공격할 수 있으면 진짜 위험한거겠지
						risk += 15;
					} else if (!unit.getType().isFlyer()) { // 날아다니지 않으면 길막까지 하니까
						risk += 10;
					} else if (unit.getType().isWorker()) { // 일꾼은 그다지 위험하지 않다고 본다.
						risk += 1;
					} else { // 날아다니면 길막은 하지 않으니까
						risk += 5;
					}
				} else { // 적군이 공격할 수 없을 때
					if (unit.getType().isBuilding()) {
						risk += 1;
					} else if (!unit.getType().isFlyer()) {
						risk += 1;
					} else {
						risk += 1;
					}
				}
				
			} else if (unit.getPlayer() == Prebot.Broodwar.self()) { // 아군인 경우, united값에 따라 좋은지 싫은지 판단을 다르게 한다.
				if (!unit.getType().isFlyer()) {
					risk += united ? -2 : 2;
				} else {
					risk += united ? -1 : 1;
				}
				
			} else { // 중립(미네랄, 가스 등)
				risk += 1;
			}
		}
		return risk;
	}

	private static int moveDistancePerFrame(Unit fleeUnit, int frame) {
		double unitSpeed1 = fleeUnit.getPlayer().topSpeed(fleeUnit.getType());
		//double unitSpeed2 = fleeUnit.getType().topSpeed(); // TODO 업그레이드 시 unitSpeed1, unitSpeed2가 차이가 있는지
		return (int) (unitSpeed1 * frame); // frame의 시간동안 몇 pixel 이동 가능한지
	}
	
	private static int moveDistancePerFrame(UnitType unitType, int frame) {
		double unitSpeed1 = unitType.topSpeed();
		return (int) (unitSpeed1 * frame); // frame의 시간동안 몇 pixel 이동 가능한지
	}
	
//	public static int requiredShotToKill(Unit attackUnit, Unit targetUnit) { // TODO 추후 메서드 검증 및 업그레이드 필요
	public static boolean killedByNShot(Unit attackUnit, Unit targetUnit, int shot) {
		UnitType attackerType = attackUnit.getType();
		UnitType targetType = targetUnit.getType();

		int numberOfAttack = shot;
		if (attackerType == UnitType.Protoss_Zealot || attackerType == UnitType.Terran_Goliath && targetType.isFlyer()) {
			numberOfAttack *= 2;
		}
		int damageExpected = Prebot.Broodwar.getDamageFrom(attackerType, targetType, attackUnit.getPlayer(), targetUnit.getPlayer()) * numberOfAttack;
		
		int targetHitPoints = targetUnit.getHitPoints();
		if (targetType.regeneratesHP()) {
			targetHitPoints += 1;
		}
		
		if (targetType.maxShields() == 0) {
			return damageExpected >= targetHitPoints;
		}
		
		int spareDamage = damageExpected - targetHitPoints;
		if (spareDamage < 0) {
			return false;
		}
		
		int targetShields = targetUnit.getShields();
		if (targetShields == 0) {
			return true;
		}
		targetShields += (targetUnit.getPlayer().getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields) * numberOfAttack);
		
		return toShieldDamage(spareDamage, attackUnit, targetUnit) > targetShields;
	}
	
	private static int toShieldDamage(int damage, Unit attackUnit, Unit targetUnit) {
		DamageType explosionType = MirrorBugFixed.getDamageType(attackUnit.getType(), targetUnit);
		UnitSizeType targetUnitSize = MirrorBugFixed.getUnitSize(targetUnit.getType());
		
		if (explosionType == DamageType.Explosive) {
			if (targetUnitSize == UnitSizeType.Small) {
				return damage * 2;
			} else if (targetUnitSize == UnitSizeType.Medium) {
				return damage * 4/3;
			}
		} else if (explosionType == DamageType.Concussive) {
			if (targetUnitSize == UnitSizeType.Medium) {
				return damage * 2;
			} else if (targetUnitSize == UnitSizeType.Large) {
				return damage * 4;
			}
		}
		return damage;
	}
	
	public static boolean isRemovableEnemySpiderMine(Unit unit, UnitInfo eui) {
		Unit target = UnitUtils.unitInSight(eui);
		if (target == null) {
			return false;
		}
		
		return target.getType() == UnitType.Terran_Vulture_Spider_Mine && unit.isAttackFrame() && unit.isInWeaponRange(target);
	}
	
	// (지상유닛 대상) position의 적의 사정거리에서 안전한 지역인지 판단한다.
	public static boolean isSafePlace(Position position) {
		List<UnitInfo> euiList = UnitUtils.getEnemyUnitInfosInRadiusForGround(position, 0);
		
		for (UnitInfo ui : euiList) {
			if (ui.getType().isWorker() || !typeCanAttackGround(ui.getType())) {
				continue;
			}
			
			double distanceToNearEnemy = position.getDistance(ui.getLastPosition());
			WeaponType nearEnemyWeapon = ui.getType().groundWeapon();
			int enemyWeaponMaxRange = Prebot.Broodwar.enemy().weaponMaxRange(nearEnemyWeapon);
			double enemyTopSpeed = Prebot.Broodwar.enemy().topSpeed(ui.getType());
			double backOffDist = ui.getType().isBuilding() ? MicroConfig.Common.BACKOFF_DIST_DEF_TOWER : 0.0;
			
			if (distanceToNearEnemy <= enemyWeaponMaxRange + enemyTopSpeed * 24 + backOffDist) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean typeCanAttackGround(UnitType attacker) {
		return attacker.groundWeapon() != WeaponType.None ||
				attacker == UnitType.Terran_Bunker ||
				attacker == UnitType.Protoss_Carrier ||
				attacker == UnitType.Protoss_Reaver;
	}
	
	public static boolean isMeleeUnit(UnitType unitType) {
		return unitType.groundWeapon().maxRange() <= Tank.SIEGE_MODE_MIN_RANGE; // 시즈모드 최소사정거리 안의 공격범위는 melee 유닛으로 판단
	}
	
	public static boolean arrivedToPosition(Unit unit, Position position) {
		int sightRange = unit.getType().sightRange();
		int distance = unit.getDistance(position);
		
		return sightRange >= distance;
	}
	
	public static boolean timeToRandomMove(Unit unit) {
		return !unit.isBeingHealed() && (unit.isIdle() || unit.isBraking());
	}

	public static boolean canAttack(Unit myUnit, UnitInfo eui) {
		WeaponType weaponType = WeaponType.None;
		Unit enemy = UnitUtils.unitInSight(eui);
		if (enemy != null) {
			weaponType = getWeapon(myUnit, enemy);
		} else {
			weaponType = getWeapon(myUnit.getType(), eui.getType());
		}
		return weaponType != WeaponType.None;
	}

	private static WeaponType getWeapon(Unit attacker, Unit target) {
		return target.isFlying() ? attacker.getType().airWeapon() : attacker.getType().groundWeapon();
	}

	private static WeaponType getWeapon(UnitType attacker, UnitType target) {
		return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
	}

}
