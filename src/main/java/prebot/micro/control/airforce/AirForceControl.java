package prebot.micro.control.airforce;

import java.util.Arrays;
import java.util.Collection;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.control.Control;
import prebot.micro.targeting.WraithTargetCalculator;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.AirForceManager;
import prebot.strategy.manage.AirForceTeam;
import prebot.strategy.manage.PositionFinder;

public class AirForceControl extends Control {

	@Override
	public void control(Collection<Unit> airunits, Collection<UnitInfo> euiList) {
		if (airunits.isEmpty()) {
			return;
		}
		
		if (StrategyIdea.letsFindRat || (Prebot.Broodwar.self().supplyUsed() > 300 && UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.ALL).size() <= 3)) {
			findRat(airunits);
			return;
		}
		
		// 팀 단위로 wraithList가 세팅되어야 한다.
		int memberId = airunits.iterator().next().getID();
		AirForceTeam airForceTeam = AirForceManager.Instance().airForTeamOfUnit(memberId);
		if (airForceTeam.leaderUnit == null) {
			System.out.println(memberId + "'s airSquad has no leader. member.size=" + airForceTeam.memberList.size());
			return;
		}
		if (AirForceManager.Instance().getTargetPositions().isEmpty()) {
			System.out.println("AirForceManager targetPositions is empty");
			return;
		}
		
		DecisionMaker decisionMaker = new DecisionMaker(new WraithTargetCalculator());

		// 결정: 도망(FLEE_FROM_UNIT), 공격(ATTACK_UNIT), 카이팅(KITING_UNIT), 클로킹(CHANGE_MODE), 이동(ATTACK_POSITION)
		// 결정상세(공격, 카이팅, 이동시): 공격(ATTACK_UNIT), 뭉치기(UNITE), 카이팅(KITING_UNIT), 이동(ATTACK_POSITION)
		Decision decision = null;
		Decision decisionDetail = null;
		
		boolean applyDefenseModeFree = false;
		if (AirForceManager.Instance().isAirForceDefenseMode()) {
			if (dangerousOutOfMyRegion(airForceTeam.leaderUnit)) {
				if (StrategyIdea.mainSquadMode.isAttackMode) {
					applyDefenseModeFree = StrategyIdea.mainSquadCenter.getDistance(airForceTeam.leaderUnit) > StrategyIdea.mainSquadCoverRadius;
				} else {
					applyDefenseModeFree = true;
				}
			}

			if (applyDefenseModeFree) {
				decision = Decision.fleeFromUnit(airForceTeam.leaderUnit, null);

				// apply airforce decision
				Position airFleePosition = PositionFinder.Instance().baseFirstChokeMiddlePosition();
				Position airDrivingPosition = airDrivingPosition(airForceTeam, airFleePosition, Angles.AIR_FORCE_FREE);
				airForceTeam.leaderOrderPosition = airDrivingPosition;
			}
		}
		
		if (!applyDefenseModeFree) {
			if (!airForceTeam.retreating()) {
				decision = decisionMaker.makeDecisionForAirForce(airForceTeam, euiList, AirForceManager.Instance().getStrikeLevel());
			} else {
				decision = Decision.fleeFromUnit(airForceTeam.leaderUnit, null);
			}

			if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
				decisionDetail = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, Arrays.asList(decision.eui), false);
			} else if (decision.type == DecisionType.ATTACK_POSITION) { // 목적지 이동시 준수할 세부사항
				boolean movingAttack = true;
				if (airForceTeam.repairCenter != null) {
					movingAttack = false;
				}
				decisionDetail = decisionMaker.makeDecisionForAirForceMovingDetail(airForceTeam, euiList, movingAttack);
			}

			// System.out.println("decision: " + decision + " / " + decisionDetail);
			this.applyAirForceDecision(airForceTeam, decision, decisionDetail);
		}
		

		if (decisionDetail != null) {
			// 공격 쿨타임
			if (decisionDetail.type == DecisionType.ATTACK_UNIT) {
				// ATTACK_UNIT, KITING_UNIT 동일
				if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
					for (Unit airunit : airunits) {
						if (airunit.getType() == UnitType.Terran_Wraith) {
							CommandUtils.attackUnit(airunit, decisionDetail.eui.getUnit());
						} else if (airunit.getType() == UnitType.Terran_Valkyrie) {
							CommandUtils.attackMove(airunit, decisionDetail.eui.getLastPosition());
						}
					}
				}
				// 지나가면서 한대씩 때리기
				else if (decision.type == DecisionType.ATTACK_POSITION) {
					for (Unit airunit : airunits) {
						airunit.rightClick(decisionDetail.eui.getUnit());
					}
				}
			}
			// 뭉치기
			else if (decisionDetail.type == DecisionType.UNITE) {
				for (Unit airunit : airunits) {
					airunit.rightClick(airForceTeam.leaderUnit.getPosition());
				}
			}
			// 전진 카이팅, 카이팅
			else if (decisionDetail.type == DecisionType.ATTACK_POSITION || decisionDetail.type == DecisionType.KITING_UNIT) {
				if (airForceTeam.repairCenter != null) {
					Position insidePosition = PositionFinder.Instance().commandCenterInsidePosition(airForceTeam.repairCenter);
					for (Unit airunit : airunits) {
						if (!MicroUtils.isBeingHealed(airunit) && airunit.getDistance(insidePosition) > 30) {
							CommandUtils.rightClick(airunit, airForceTeam.leaderOrderPosition);
						}
						// if (MicroUtils.timeToRandomMove(wraith)) {
						// Position randomPosition = PositionUtils.randomPosition(wraith.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
						// CommandUtils.attackMove(wraith, randomPosition);
						// }
					}
				} else {
					for (Unit airunit : airunits) {
						airunit.rightClick(airForceTeam.leaderOrderPosition);
					}
				}
			}

		} else if (decision.type == DecisionType.FLEE_FROM_UNIT) { // 도망
			for (Unit airunit : airunits) {
				airunit.rightClick(airForceTeam.leaderOrderPosition);
			}
			
		} else if (decision.type == DecisionType.CHANGE_MODE) { // 클로킹
			if (airForceTeam.cloakingMode) {
				airForceTeam.decloak();
				for (Unit airunit : airunits) {
					if (airunit.getType() == UnitType.Terran_Wraith) {
						airunit.decloak();
					}
				}
			} else {
				airForceTeam.cloak();
				for (Unit airunit : airunits) {
					if (airunit.getType() == UnitType.Terran_Wraith) {
						airunit.cloak();
					}
				}
			}
		}
	}

	// 결정: 도망(FLEE_FROM_UNIT), 공격(ATTACK_UNIT), 카이팅(KITING_UNIT), 클로킹(CHANGE_MODE), 이동(ATTACK_POSITION)
	// 결정상세(공격, 카이팅, 이동시): 공격(ATTACK_UNIT), 뭉치기(UNITE), 카이팅(KITING_UNIT), 이동(ATTACK_POSITION)
	private void applyAirForceDecision(AirForceTeam airForceTeam, Decision decision, Decision decisionDetail) {
		Position airDrivingPosition = null;
		
		if (decision.type == DecisionType.ATTACK_UNIT || decision.type == DecisionType.KITING_UNIT) {
			if (wraithKitingType(decision.eui)) {
				if (decisionDetail.type == DecisionType.KITING_UNIT) { // 카이팅 후퇴
					if (AirForceManager.Instance().isAirForceDefenseMode()) {
						Position airFleePosition = PositionFinder.Instance().baseFirstChokeMiddlePosition();
						airDrivingPosition = airDrivingPosition(airForceTeam, airFleePosition, Angles.AIR_FORCE_FREE);
						
					} else {
						// airDrivingPosition = airDrivingPosition(airForceTeam, AirForceManager.Instance().getRetreatPosition(), airForceTeam.driveAngle); // kiting flee
						Position airFleePosition = airFeePosition(airForceTeam, decision.eui);
						airDrivingPosition = airDrivingPosition(airForceTeam, airFleePosition, Angles.AIR_FORCE_FREE);
					}
					
				} else {
					if (decision.type == DecisionType.ATTACK_UNIT) { // 제자리 공격
						airDrivingPosition = airForceTeam.leaderUnit.getPosition();
					} else if (decision.type == DecisionType.KITING_UNIT) { // 따라가서 공격
						Unit enemyInSight = UnitUtils.unitInSight(decision.eui);
						if (enemyInSight == null) {
							airDrivingPosition = decision.eui.getLastPosition();
						} else {
							Unit leaderUnit = airForceTeam.leaderUnit;
							double distanceToAttack = leaderUnit.getDistance(enemyInSight) - UnitType.Terran_Wraith.airWeapon().maxRange();
							int catchTime = (int) (distanceToAttack / UnitType.Terran_Wraith.topSpeed());
							if (catchTime > 0) { 
								airDrivingPosition = decision.eui.getLastPosition();
								
							} else {
								Position airFleePosition = airFeePosition(airForceTeam, decision.eui);
								airDrivingPosition = airDrivingPosition(airForceTeam, airFleePosition, airForceTeam.driveAngle);
							}
						}
					}
				}
				
			} else {
				if (decision.type == DecisionType.ATTACK_UNIT) {
					airDrivingPosition = airForceTeam.leaderUnit.getPosition();
				} else if (decision.type == DecisionType.KITING_UNIT) {
					airDrivingPosition = decision.eui.getLastPosition();
				}
			}
			
		} else if (decision.type == DecisionType.ATTACK_POSITION) {
			if (airForceTeam.repairCenter != null) {
				Position repairCenter = PositionFinder.Instance().commandCenterInsidePosition(airForceTeam.repairCenter);
				airDrivingPosition = airDrivingPosition(airForceTeam, repairCenter, airForceTeam.driveAngle);
				
			} else {
				airDrivingPosition = airDrivingPosition(airForceTeam, airForceTeam.getTargetPosition(), airForceTeam.driveAngle);
				if (airForceTeam.leaderUnit.getPosition().getDistance(airForceTeam.getTargetPosition()) < 100) {
					airForceTeam.changeTargetIndex();
				}
			}
			
		} else if (decision.type == DecisionType.FLEE_FROM_UNIT) {
			airForceTeam.retreat(decision.eui);
			Position airFleePosition = airFeePosition(airForceTeam, airForceTeam.fleeEui);
			airDrivingPosition = airDrivingPosition(airForceTeam, airFleePosition, airForceTeam.driveAngle);
		}
		
		airForceTeam.leaderOrderPosition = airDrivingPosition;
	}

//	private Unit closestAssistant(Unit wraith, UnitInfo eui) {
//		List<Unit> assistUnitList = null;
//		if (eui.getType().isFlyer()) {
//			assistUnitList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Missile_Turret, UnitType.Terran_Goliath);
//		} else {
//			assistUnitList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Goliath);
//		}
//		Unit closeAssistant = UnitUtils.getClosestUnitToPosition(assistUnitList, wraith.getPosition());
//		if (closeAssistant == null || MicroUtils.isInWeaponRange(closeAssistant, eui)) { // 도와줄 아군이 없거나 이미 근처에 있는 경우
//			return null;
//		}
//		
//		return closeAssistant;
//	}

	private Position airDrivingPosition(AirForceTeam airForceTeam, Position goalPosition, int[] angle) {
		Position leaderPosition = airForceTeam.leaderUnit.getPosition();
		Position airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, goalPosition, angle, true);
		if (airDrivingPosition == null) {
			airForceTeam.switchDriveAngle();
			airDrivingPosition = MicroUtils.airDrivingPosition(leaderPosition, goalPosition, angle, true);
		}
		if (airDrivingPosition == null) {
			airDrivingPosition = goalPosition;
		}
		return airDrivingPosition;
	}

	private Position airFeePosition(AirForceTeam airForceTeam, UnitInfo eui) {
		Position enemyPosition;
		Unit enemyUnit = Prebot.Broodwar.getUnit(eui.getUnitID());
		if (enemyUnit != null) {
			enemyPosition = enemyUnit.getPosition();
		} else {
			enemyPosition = airForceTeam.fleeEui.getLastPosition();
		}
		Position airFleePosition = MicroUtils.airFleePosition(airForceTeam.leaderUnit.getPosition(), enemyPosition);
		if (airFleePosition == null) {
			airFleePosition = AirForceManager.Instance().getRetreatPosition();
		}
		return airFleePosition;
	}

	private boolean wraithKitingType(UnitInfo eui) {
		return eui.getType().airWeapon() != WeaponType.None && eui.getType().airWeapon().maxRange() < UnitType.Terran_Wraith.groundWeapon().maxRange();
	}
}
