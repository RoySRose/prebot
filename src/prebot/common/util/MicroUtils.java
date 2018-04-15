package prebot.common.util;

import bwapi.DamageType;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import prebot.common.code.Code.CommonCode;
import prebot.common.code.ConfigForMicro.Angles;
import prebot.common.util.internal.MirrorBugFixed;
import prebot.information.UnitInfo;
import prebot.main.PreBot;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;

/**
 * 마이크로 컨트롤 유틸
 * 
 * @author insaneojw
 *
 */
public class MicroUtils {
	
	public static void flee(Unit fleeUnit, Position targetPosition, FleeOption fOption) {
		fleeUnit.rightClick(getFleePosition(fleeUnit, targetPosition, fOption));
	}
	
	public static void kiting(Unit rangedUnit, UnitInfo targetInfo, KitingOption kOption) {
		if (enemyUnitInSight(targetInfo) == null) {
			CommandUtils.move(rangedUnit, targetInfo.lastPosition);
		} else {
			kiting(rangedUnit, targetInfo.unit, kOption);
		}
	}
	
	public static void kiting(Unit unit, Unit target, KitingOption kOption) {
		if (!killedByNShot(unit, target, 1) && killedByNShot(target, unit, 2)) {
			kOption.cooltimeAlwaysAttack = false;
			kOption.fOption.united = false;
			kOption.fOption.angles = Angles.WIDE;
		} else if (groundUnitFreeKiting(unit)) {
			kOption.fOption.united = false;
			kOption.fOption.angles = Angles.WIDE;
		}
		
		if (timeToAttack(unit, target, kOption.cooltimeAlwaysAttack)) {
			CommandUtils.attackUnit(unit, target);
			return;
		} else {
			if (forwardKiting(unit, target)) {
				unit.move(target.getPosition());
			} else {
				flee(unit, target.getPosition(), kOption.fOption);
			}
		}
	}

	private static boolean timeToAttack(Unit attackUnit, Unit targetUnit, boolean cooltimeAlwaysAttack) {

		// attackUnit, target 각각의 지상/공중 무기를 선택
		WeaponType attackUnitWeapon = targetUnit.isFlying() ? attackUnit.getType().airWeapon() : attackUnit.getType().groundWeapon();
		WeaponType targetWeapon = attackUnit.isFlying() ? targetUnit.getType().airWeapon() : targetUnit.getType().groundWeapon();
		
		// 일꾼의 공격력은 강하지 않다.
		if (targetUnit.getType().isWorker() && !attackUnit.isUnderAttack()) {
			return true;
		}

		// 건물 또는 보다 긴 사정거리를 가진 적에게 카이팅은 무의미하다.
		if (PreBot.Broodwar.self().weaponMaxRange(attackUnitWeapon) <= PreBot.Broodwar.enemy().weaponMaxRange(targetWeapon)) {
			return true;
		}
		
		int cooltime = attackUnit.isStartingAttack() ? attackUnitWeapon.damageCooldown() // // 쿨타임시간(frame)
				: (targetUnit.isFlying() ? attackUnit.getAirWeaponCooldown() : attackUnit.getGroundWeaponCooldown());
		double distanceToAttack = attackUnit.getDistance(targetUnit) - PreBot.Broodwar.self().weaponMaxRange(attackUnitWeapon); // 공격하기 위해 이동해야 하는 거리(pixel)
		int catchTime = (int) (distanceToAttack / attackUnit.getType().topSpeed()); // 상대를 잡기위해 걸리는 시간 (frame) = 거리(pixel) / 속도(pixel per frame)
		
		// 상대가 때리기 위해 거리를 좁히거나 벌려야 하는 경우(coolTime <= catchTime)
		if (cooltime <= catchTime + PreBot.Broodwar.getLatency() * 2) { // 명령에 대한 지연시간(latency)을 더한다. ex) LAN(UDP) : 5
			return true;
		}
		
		// 쿨타임이 되었을 때 항시 공격할 것인가
		if (cooltimeAlwaysAttack && cooltime == 0) { 
			return true;
		}
		return false;
	}
	
	public static boolean forwardKiting(Unit rangedUnit, Unit target) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static boolean groundUnitFreeKiting(Unit rangedUnit) {
		// TODO Auto-generated method stub
		return false;
	}

	/** 시야에 있는 적인가? */
	public static Unit enemyUnitInSight(UnitInfo enemyUnitInfo) {
		Unit enemyUnit = PreBot.Broodwar.getUnit(enemyUnitInfo.unitID);
		if (enemyUnit != null && enemyUnit.getType() != UnitType.Unknown) {
			return enemyUnit;
		} else {
			return null;
		}
	}

	private static Position getFleePosition(Unit fleeUnit, Position targetPosition, FleeOption fOption) {
		double fleeRadian = oppositeDirectionRadian(fleeUnit.getPosition(), targetPosition);
		Position fleePosition = Position.None;
		for (int moveDistance = moveDistancePerFrame(fleeUnit, TimeUtils.SECOND); // 1초간 움직이는 거리
				moveDistance > 10; moveDistance = (int) (moveDistance * 0.7)) {
			fleePosition = lowestRiskPosition(fleeUnit, fleeRadian, fOption, moveDistance);
			if (fleePosition != Position.None) {
				break;
			}
		}
		return PositionUtils.isValidPosition(fleePosition) ? fleePosition : fOption.goalPosition;
	}

	/// 반대 방향의 각도(radian)
	private static double oppositeDirectionRadian(Position myPosition, Position targetPosition) {
		return Math.atan2(myPosition.getX() - targetPosition.getX(), myPosition.getY() - targetPosition.getY());
	}

	private static Position lowestRiskPosition(Unit unit, double standRadian, FleeOption fOption, int moveDistance) {
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
			riskOfCandiPosition = riskOfPosition(unit, candiPosition, fOption.united);
			
			if (riskOfCandiPosition < minimumRisk
					|| (riskOfCandiPosition == minimumRisk && distFromCandiToGoal < distFromBestToGoal)) {
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
	private static double rotate(double radian, int angle) {
		return (Math.PI / 180) * ((radian * 180 / Math.PI) + angle);
	}

	private static int riskOfPosition(Unit unit, Position movePosition, boolean united) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int moveDistancePerFrame(Unit fleeUnit, int frame) {
		double unitSpeed1 = fleeUnit.getPlayer().topSpeed(fleeUnit.getType());
		double unitSpeed2 = fleeUnit.getType().topSpeed(); // TODO 업그레이드 시 unitSpeed1, unitSpeed2가 차이가 있는지
		
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
		int damageExpected = PreBot.Broodwar.getDamageFrom(attackerType, targetType, attackUnit.getPlayer(), targetUnit.getPlayer()) * numberOfAttack;
		
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
}
