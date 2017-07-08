package pre.util;

import java.util.ArrayList;
import java.util.List;

import bwapi.DamageType;
import bwapi.Player;
import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.Region;
import pre.MapGrid;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.util.MicroSet.FleeAngle;
import pre.util.MicroSet.Network;

public class MicroUtils {
	
	public static boolean smartScan(Position targetPosition) {
		if (targetPosition.isValid()) {
			MyBotModule.Broodwar.sendText("SmartScan : bad position");
			return false;
		}
		if (MapGrid.Instance().scanIsActiveAt(targetPosition)) {
			return false;
		}

		// Choose the comsat with the highest energy.
		// If we're not terran, we're unlikely to have any comsats....
		int maxEnergy = 49;      // anything greater is enough energy for a scan
		Unit comsat = null;
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getType() == UnitType.Terran_Comsat_Station &&
				unit.getEnergy() > maxEnergy &&
				unit.canUseTech(TechType.Scanner_Sweep, targetPosition)) {
				maxEnergy = unit.getEnergy();
				comsat = unit;
			}
		}

		if (comsat != null) {
			MapGrid.Instance().scanAtPosition(targetPosition);
			return comsat.useTech(TechType.Scanner_Sweep, targetPosition);
		}

		return false;
	}
	
	@Deprecated
	public static int groupKite(List<Unit> rangedUnits, List<Unit> targets, int coolTime) {
		UnitType rangedUnitType = rangedUnits.get(0).getType();
		int totalCount = rangedUnits.size();
		int readyToCount = 0;
		
		if (totalCount == 0) {
			MyBotModule.Broodwar.sendText("groupKite : totalCount is zero");
			return 0;
		}
		
		for (Unit rangedUnit : rangedUnits) {
			if (rangedUnit.getPlayer() != MyBotModule.Broodwar.self() ||
					!CommandUtil.IsValidUnit(rangedUnit) ||
					rangedUnit.getType() != rangedUnitType) {
				MyBotModule.Broodwar.sendText("groupKite : bad arg");
				return 0;
			}
			
			if (rangedUnit.getGroundWeaponCooldown() == 0) {
				readyToCount++;
			}
		}
		
		Position rangedUnitsPosition = centerOfUnits(rangedUnits);
		Position targetsPosition = centerOfUnits(targets);
		
		if (coolTime > 0) {
			return coolTime - 1;
			
		} else if (readyToCount > totalCount * 0.7) {
			return rangedUnitType.groundWeapon().damageCooldown(); // 골리앗 쿨타운 : 22 (frame)
			
		} else {
			int reverseX = rangedUnitsPosition.getX() - targetsPosition.getX(); // 타겟과 반대로 가는 x양
			int reverseY = rangedUnitsPosition.getY() - targetsPosition.getY(); // 타겟과 반대로 가는 y양
		    double fleeRadian = Math.atan2(reverseY, reverseX);

		    Position fleeVector = new Position((int)(200 * Math.cos(fleeRadian)), (int)(200 * Math.sin(fleeRadian)));
//		    if (timeToCatch >= 0) { // 진격하라
//		    	fleeVector = new Position(-fleeVector.getX(), -fleeVector.getY());
//		    }
			for (Unit rangedUnit : rangedUnits) {
				
				int x = rangedUnit.getPosition().getX() + fleeVector.getX();
				int y = rangedUnit.getPosition().getY() + fleeVector.getY();
				Position movePosition = new Position(x, y);
				rangedUnit.rightClick(movePosition);
			}
			return 0;
		}
	}
	
	/**
	 * rangeUnit은 target에 대한 카이팅을 한다.
	 */
	public static void preciseKiting(Unit rangedUnit, Unit target, KitingOption kitingOption) {
		// 유닛 유효성 검사
		if (rangedUnit.getPlayer() != MyBotModule.Broodwar.self() ||
				!CommandUtil.IsValidUnit(rangedUnit) ||
				!CommandUtil.IsValidUnit(target)) {
			MyBotModule.Broodwar.sendText("smartKiteTarget : bad arg");
			return;
		}

		// rangedUnit, target 각각의 지상/공중 무기를 선택
		WeaponType rangedUnitWeapon = target.isFlying() ? rangedUnit.getType().airWeapon() : rangedUnit.getType().groundWeapon();
		WeaponType targetWeapon = rangedUnit.isFlying() ? target.getType().airWeapon() : target.getType().groundWeapon();

		// 무기 사정거리
		double weaponUpgradeRange = 0;
		if (rangedUnit.getType() == UnitType.Terran_Goliath && rangedUnitWeapon.targetsAir()) { // 골리앗 무기 업그레이드된 경우 사정거리++
			weaponUpgradeRange += MicroSet.Upgrade.getUpgradeAdvantageAmount(UpgradeType.Charon_Boosters);
		}
		
		// 1. 보다 긴 사정거리를 가진 적에게 카이팅은 무의미하다.
		// 2. 카이팅
		if (rangedUnitWeapon.maxRange() + weaponUpgradeRange <= targetWeapon.maxRange()) {
			CommandUtil.attackUnit(rangedUnit, target);
			
		} else {
			double distanceToTarget = rangedUnit.getDistance(target);
			double distanceToAttack = distanceToTarget - (rangedUnitWeapon.maxRange() + weaponUpgradeRange); // 거리(pixel)
			int timeToCatch = (int) (distanceToAttack / rangedUnit.getType().topSpeed()); // 상대를 잡기위해 걸리는 시간 (frame) = 거리(pixel) / 속도(pixel per frame)
			
			// 명령에 대한 지연시간(latency)을 더한다.
			timeToCatch += Network.LATENCY * 2; // 후퇴해야 하는 경우, 지연시간을 더하면 도망을 더 늦게갈 수도 있다. if (distanceToAttack > 0) // TODO 조절가능

			int currentCooldown = rangedUnit.isStartingAttack() ? rangedUnitWeapon.damageCooldown() // // 쿨타임시간(frame)
					: (target.isFlying() ? rangedUnit.getAirWeaponCooldown() : rangedUnit.getGroundWeaponCooldown());

			boolean survivalInstinct = !killedByNShot(rangedUnit, target, 1) && killedByNShot(target, rangedUnit, 2); // 생존본능(딸피)
			
			// 1. 공격
			//  - 상대가 때리기 위해 거리를 좁혀야 할때(currentCooldown <= timeToCatch)
			//  - 쿨타임이 되었을때 (cooltimeAlwaysAttack && currentCooldown) (파라미터로 설정가능)
			//  - 타깃이 건물일 경우
			// 2. 회피
			//  - getFleePosition을 통해 최적의 회피지역을 선정하여 이동한다.
			//  - watcher인 경우는 안전하게 회피
			boolean haveToAttack = false;
			if (kitingOption.getSpecialSquadType() > 0 && distanceToTarget <= targetWeapon.maxRange() + target.getType().topSpeed() * 36) { // watcer 안전거리확보 : target.getType().topSpeed() * 36 (적이 1.5초 이동거리)
				haveToAttack = false;
			} else if (target.getType().isBuilding()) {
				haveToAttack = true;
			} else if (currentCooldown <= timeToCatch) {
				haveToAttack = true;
			} else if (!survivalInstinct && kitingOption.isCooltimeAlwaysAttack() && currentCooldown == 0) {
				haveToAttack = true;
			}
			
			if (haveToAttack) {
				// TODO P컨이 잘안되어 사용하지 않는다.
//			    double rad = Math.atan2(vulture.getPosition().getY() - target.getPosition().getY()
//			    		, vulture.getPosition().getX() - target.getPosition().getX());
//			    Position enemyFrontVec = new Position((int)(5 * Math.cos(rad)), (int)(5 * Math.sin(rad)));
//				int pConX = target.getPosition().getX() + enemyFrontVec.getX();
//				int pConY = target.getPosition().getY() + enemyFrontVec.getY();
//				Position pConPos = new Position(pConX, pConY);
//				CommandUtil.patrolMove(vulture, pConPos);
				CommandUtil.attackUnit(rangedUnit, target);
				
			} else {
				double rangedUnitSpeed = rangedUnit.getType().topSpeed() * 24.0; // 1초(24frame)에 몇 pixel가는지
				if (rangedUnit.getType() == UnitType.Terran_Vulture) {
					rangedUnitSpeed += MicroSet.Upgrade.getUpgradeAdvantageAmount(UpgradeType.Ion_Thrusters);
				}
				
				// 회피지역을 선정한다.
				Position fleePosition = null;
				if (kitingOption.getSpecialSquadType() == 1) { // watcher는 본진방향으로 후퇴한다.
					fleePosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
				} else {
					fleePosition = getFleePosition(rangedUnit, target, (int) rangedUnitSpeed, (survivalInstinct? false : kitingOption.isUnitedKiting())
							, kitingOption.getGoalPosition(), kitingOption.getFleeAngle());
				}
				
				CommandUtil.rightClick(rangedUnit, fleePosition);
			}
		}
	}
	
	/**
	 * 
	 * rangedUnit은 target으로부터 회피한다.
	 * 
	 * @param rangedUnit
	 * @param target
	 * @param moveDistPerSec : 초당 이동거리(pixel per 24frame). 해당 pixel만큼의 거리를 회피지점으로 선정한다. 찾지 못했을 경우 거리를 좁힌다.
	 * @param unitedKiting
	 * @param goalPosition
	 * @return
	 */
	private static Position getFleePosition(Unit rangedUnit, Unit target, int moveDistPerSec, boolean unitedKiting, Position goalPosition, Integer[] fleeAngle) {
		int reverseX = rangedUnit.getPosition().getX() - target.getPosition().getX(); // 타겟과 반대로 가는 x양
		int reverseY = rangedUnit.getPosition().getY() - target.getPosition().getY(); // 타겟과 반대로 가는 y양
	    final double fleeRadian = Math.atan2(reverseY, reverseX); // 회피 각도
	    
		Position safePosition = null;
		int minimumRisk = 99999;
		int minimumDistanceToGoal = 99999;

//		Integer[] FLEE_ANGLE = MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType()); // MicroData.FleeAngle에 저장된 유닛타입에 따른 회피 각 범위(골리앗 새끼들은 뚱뚱해서 각이 넓으면 지들끼리 낑김)
		Integer[] FLEE_ANGLE = fleeAngle != null ? fleeAngle : MicroSet.FleeAngle.getFleeAngle(rangedUnit.getType());
		double fleeRadianAdjust = fleeRadian; // 회피 각(radian)
		int moveCalcSize = moveDistPerSec; // 이동 회피지점의 거리 = 유닛의 초당이동거리
		
		while (safePosition == null && moveCalcSize > 10) {
			for(int i = 0 ; i< FLEE_ANGLE.length; i ++) {
			    Position fleeVector = new Position((int)(moveCalcSize * Math.cos(fleeRadianAdjust)), (int)(moveCalcSize * Math.sin(fleeRadianAdjust))); // 이동벡터
				Position movePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX(), rangedUnit.getPosition().getY() + fleeVector.getY()); // 회피지점
				Position middlePosition = new Position(rangedUnit.getPosition().getX() + fleeVector.getX() / 2, rangedUnit.getPosition().getY() + fleeVector.getY() / 2); // 회피중간지점
				
				int risk = riskOfFleePosition(rangedUnit.getType(), movePosition, moveCalcSize, unitedKiting); // 회피지점에서의 예상위험도
				int distanceToGoal = movePosition.getApproxDistance(goalPosition); // 위험도가 같을 경우 2번째 고려사항: 목표지점까지의 거리

				// 회피지점은 유효하고, 걸어다닐 수 있어야 하고, 안전해야 하고 등등
				if (movePosition.isValid() && BWTA.getRegion(movePosition) != null
						&& middlePosition.isValid() && BWTA.getRegion(middlePosition) != null
						&& MyBotModule.Broodwar.isWalkable(movePosition.getX() / 8, movePosition.getY() / 8)
						&& (risk < minimumRisk || (risk == minimumRisk && distanceToGoal < minimumDistanceToGoal))) {
					
					safePosition =  movePosition;
					minimumRisk = risk;
					minimumDistanceToGoal = distanceToGoal;
				} else { // 회피각 조절
					fleeRadianAdjust = rotate(fleeRadian, FLEE_ANGLE[i]); // 각도변경
				}
		    }
			if (safePosition == null) { // 회피지역이 없을 경우 1) 회피거리 짧게 잡고 다시 조회
		    	MyBotModule.Broodwar.sendText("safe is null : " + moveCalcSize);
		    	moveCalcSize = moveCalcSize * 2 / 3;
		    	
		    	if (moveCalcSize <= 10 && FLEE_ANGLE.equals(FleeAngle.NARROW_ANGLE)) { // 회피지역이 없을 경우 2) 각 범위를 넓힘
			    	MyBotModule.Broodwar.sendText("wider angle");
					FLEE_ANGLE = FleeAngle.WIDE_ANGLE;
					unitedKiting = false;
					moveCalcSize = moveDistPerSec;
				}
			}
		}
		if (safePosition == null) { // 회피지역이 없을 경우 3) 목표지점으로 간다. 이 경우는 거의 없다.
			safePosition = goalPosition;
		}
		
	    return safePosition;
	}
	
	/**
	 * 회피지점의 위험도
	 * 
	 * @param unitType
	 * @param position
	 * @param radius
	 * @param united
	 * @return
	 */
	public static int riskOfFleePosition(UnitType unitType, Position position, int radius, boolean united) {
		int risk = 0;
		List<Unit> unitsInRadius = MyBotModule.Broodwar.getUnitsInRadius(position, radius);
		for (Unit u : unitsInRadius) {
			if (u.getPlayer() == InformationManager.Instance().enemyPlayer) { // 적군인 경우
				if (MyBotModule.Broodwar.getDamageFrom(u.getType(), unitType) > 0) { // 적군이 공격할 수 있으면 위험하겠지
					if (u.getType().isWorker()) { // 일꾼은 그다지 위험하지 않다고 본다.
						risk += 1;
					} else if (u.getType().isBuilding()) { // 건물이 공격할 수 있으면 진짜 위험한거겠지
						risk += 15;
					} else if (!u.getType().isFlyer()) { // 날아다니지 않으면 길막까지 하니까
						risk += 10;
					} else { // 날아다니면 길막은 하지 않으니까
						risk += 5;
					}
				} else { // 적군이 공격할 수 없을 때
					if (u.getType().isBuilding()) {
						risk += 5;
					} else if (!u.getType().isFlyer()) {
						risk += 3;
					} else {
						risk += 1;
					}
				}
				
			} else if (u.getPlayer() == InformationManager.Instance().selfPlayer) { // 아군인 경우, united값에 따라 좋은지 싫은지 판단을 다르게 한다.
				if (!u.getType().isFlyer()) {
					risk += united ? -3 : 3;
				} else {
					risk += united ? -1 : 1;
				}
				
			} else { // 중립(미네랄, 가스 등)
				risk += 1;
			}
		}
		return risk;
	}

	// * 참조사이트: http://yc0345.tistory.com/45
	// 공식: radian = (π / 180) * 각도 
	// -> 각도 = (radian * 180) / π
	// -> 회원 radian = (π / 180) * ((radian * 180) / π + 회전각)
	public static double rotate(double radian, int angle) { 
		double rotatedRadian = (Math.PI / 180) * ((radian * 180 / Math.PI) + angle);
		return rotatedRadian;
	}
	
	public static boolean isUnitContainedInUnitSet(Unit unit, List<Unit> unitSet) {
		for (Unit u : unitSet) {
			if (u.getID() == unit.getID())
				return true;
		}
		return false;
	}
	
	public static boolean addUnitToUnitSet(Unit unit, List<Unit> unitSet) {
		if (!isUnitContainedInUnitSet(unit, unitSet)) {
			return unitSet.add(unit);
		}
		return false;
	}
	
	public static List<Unit> getUnitsInRegion(Region region, Player player) {
		List<Unit> units = new ArrayList<>();
	    for (Unit unit : player.getUnits()) {
	        if (region == BWTA.getRegion(unit.getPosition())) {
	            units.add(unit);
	        }
	    }
		return units;
	}
	
	public static boolean typeCanAttackGround(UnitType attacker) {
		return attacker.groundWeapon() != WeaponType.None ||
				attacker == UnitType.Terran_Bunker ||
				attacker == UnitType.Protoss_Carrier ||
				attacker == UnitType.Protoss_Reaver;
	}
	
	public static boolean killedByNShot(Unit attacker, Unit target, int shot) {
		UnitType attackerType = attacker.getType();
		UnitType targetType = target.getType();
		
		int multiply = shot;
		if (attackerType == UnitType.Protoss_Zealot
				|| attackerType == UnitType.Terran_Goliath && targetType.isFlyer()) {
			multiply *= 2;
		}
		
		int damageExpected = MyBotModule.Broodwar.getDamageFrom(attackerType, targetType, attacker.getPlayer(), target.getPlayer()) * multiply;
		
		int targetHitPoints = target.getHitPoints();
		
		if (targetType.maxShields() == 0 && !targetType.regeneratesHP()) {
			if (damageExpected >= targetHitPoints) {
				return true;
			}
		}
		
		int spareDamage = damageExpected - targetHitPoints;
		if (spareDamage > 0) {
			int targetShields = target.getShields();
			if (targetShields == 0) {
				return true;
			}
			
			DamageType explosionType = getDamageType(attacker, target);
			UnitSizeType targetUnitSize = getUnitSize(targetType);
			
			if (explosionType == DamageType.Explosive) {
				if (targetUnitSize == UnitSizeType.Small) {
					spareDamage *= 2;
				} else if (targetUnitSize == UnitSizeType.Medium) {
					spareDamage *= 4/3;
				}
			} else if (explosionType == DamageType.Concussive) {
				if (targetUnitSize == UnitSizeType.Medium) {
					spareDamage *= 2;
				} else if (targetUnitSize == UnitSizeType.Large) {
					spareDamage *= 4;
				}
			}
			
			if (spareDamage > targetShields + target.getPlayer().getUpgradeLevel(UpgradeType.Protoss_Plasma_Shields)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static List<Unit> filterTargets(List<Unit> targets, boolean includeFlyer) {
		List<Unit> newTargets = new ArrayList<>();
		for (Unit target : targets) {
			if (!includeFlyer && target.isFlying()) {
				continue;
			}
			
			if (target.isVisible() && !target.isStasised()) {
				newTargets.add(target);
			}
		}
		return newTargets;
	}
	
	public static Position centerOfUnits(List<Unit> units) {
		if (units.isEmpty()) {
			return null;
		}

		int unitCount = units.size();
		int x = 0;
		int y = 0;
		for (Unit unit : units) {
			Position pos = unit.getPosition();
			if (pos.isValid()) {
				x += pos.getX();
				y += pos.getY();
			}
		}

		return new Position(x / unitCount, y / unitCount);
	}
	
	public static int calcArriveDecisionRange(UnitType unitType, int numOfUnits) {
		
		// 도착판정거리 : 유닛시야 + 유닛타입별 개체수에 비례하는 로그값 (ex: 320pixel(시즈시야) + 25 * log12)
		int arriveDecisionRange = unitType.sightRange();
		if (numOfUnits > 0) {
			arriveDecisionRange += Math.log(numOfUnits) * 25;
		}
		return arriveDecisionRange;
	}
	
	public static Position randomPosition(Position sourcePosition, int dist) {
		int x = sourcePosition.getX() + (int) (Math.random() * dist) - dist / 2;
		int y = sourcePosition.getY() + (int) (Math.random() * dist) - dist / 2;
		Position destPosition = new Position(x, y);
		return destPosition;
	}
	
	
	public static Unit leaderOfUnit(List<Unit> units, Position goalPosition) {
		Unit leader = null;
		int minimumDistance = 999999;
		for (Unit unit : units) {
			int dist = unit.getDistance(goalPosition);
			if (dist < minimumDistance) {
				leader = unit;
				minimumDistance = dist;
			}
		}
		return leader;
		
	}
	
	// weapon.damageType() bwapi 오류발생하여 구현함.
	private static DamageType getDamageType(Unit attacker, Unit target) {
		UnitType attackerType = attacker.getType();
		WeaponType weapon = target.isFlying()? attackerType.airWeapon() : attackerType.groundWeapon();
		if (weapon == null || weapon == WeaponType.Unknown) {
			MyBotModule.Broodwar.sendText("no weapon. no war.");
			return DamageType.None;
		}
		
		if (attackerType == UnitType.Terran_Siege_Tank_Tank_Mode
			|| attackerType == UnitType.Terran_Siege_Tank_Siege_Mode) {
			return DamageType.Explosive;
		}
		if (attackerType == UnitType.Terran_Vulture
				|| attackerType == UnitType.Terran_Firebat
				|| attackerType == UnitType.Terran_Goliath) {
			return DamageType.Concussive;
		}
		if (attackerType == UnitType.Terran_Goliath
				|| attackerType == UnitType.Terran_Wraith
				|| attackerType == UnitType.Terran_Valkyrie) {
			if (weapon.targetsAir()) {
				return DamageType.Explosive;
			} 
		}
		return DamageType.Normal;
	}
	
	// unitType.size() bwapi 오류발생하여 구현함.
	private static UnitSizeType getUnitSize(UnitType unitType) {
		if (unitType.isBuilding()) {
			return UnitSizeType.Large;
		} else if (unitType.isWorker()
				|| unitType == UnitType.Terran_Marine
				|| unitType == UnitType.Terran_Firebat
				|| unitType == UnitType.Terran_Ghost
				|| unitType == UnitType.Terran_Medic
				|| unitType == UnitType.Protoss_Zealot
				|| unitType == UnitType.Protoss_High_Templar
				|| unitType == UnitType.Hero_Dark_Templar
				|| unitType == UnitType.Protoss_Observer
				|| unitType == UnitType.Zerg_Larva
				|| unitType == UnitType.Zerg_Zergling
				|| unitType == UnitType.Zerg_Infested_Terran
				|| unitType == UnitType.Zerg_Broodling
				|| unitType == UnitType.Zerg_Scourge
				|| unitType == UnitType.Zerg_Mutalisk
				) {
			return UnitSizeType.Small;
		} else if (unitType == UnitType.Terran_Vulture
				|| unitType == UnitType.Protoss_Corsair
				|| unitType == UnitType.Zerg_Hydralisk
				|| unitType == UnitType.Zerg_Defiler
				|| unitType == UnitType.Zerg_Queen
				|| unitType == UnitType.Zerg_Lurker
				) {
			return UnitSizeType.Medium;
		} else if (unitType == UnitType.Terran_Siege_Tank_Tank_Mode
				|| unitType == UnitType.Terran_Siege_Tank_Siege_Mode
				|| unitType == UnitType.Terran_Goliath
				|| unitType == UnitType.Terran_Wraith
				|| unitType == UnitType.Terran_Dropship
				|| unitType == UnitType.Terran_Science_Vessel
				|| unitType == UnitType.Terran_Battlecruiser
				|| unitType == UnitType.Terran_Valkyrie
				|| unitType == UnitType.Protoss_Dragoon
				|| unitType == UnitType.Protoss_Archon
				|| unitType == UnitType.Protoss_Reaver
				|| unitType == UnitType.Protoss_Shuttle
				|| unitType == UnitType.Protoss_Scout
				|| unitType == UnitType.Protoss_Carrier
				|| unitType == UnitType.Protoss_Arbiter
				|| unitType == UnitType.Zerg_Overlord
				|| unitType == UnitType.Zerg_Guardian
				|| unitType == UnitType.Zerg_Devourer
				) {
			return UnitSizeType.Large;
		}
		
		return UnitSizeType.Small;
	}
}
