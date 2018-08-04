package prebot.micro.control.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.LagObserver;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMakerPrebot1;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.KitingOption.CoolTimeAttack;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.constant.MicroConfig.Common;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.TankPositionManager;

public class TankControl extends Control {

	private static final int ENOUGH_BACKUP_VULTURE_AND_GOLIATH = 7;
	private static final int POSITION_TO_SIEGE_ARRIVE_DISTANCE = 0;
	private static final int SIEGE_MODE_RANGE_MARGIN_DISTANCE = 5;

	private boolean hasEnoughBackUpUnitToSiege = false;
	private int siegeModeSpreadRadius;
	private int saveUnitLevel;
	private Collection<UnitInfo> flyingEnemisInfos;

	public void setSaveUnitLevel(int saveUnitLevel) {
		this.saveUnitLevel = saveUnitLevel;
	}
	
	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		
		List<Unit> vultureAndGoliath = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture, UnitType.Terran_Goliath);
		this.hasEnoughBackUpUnitToSiege = vultureAndGoliath.size() > ENOUGH_BACKUP_VULTURE_AND_GOLIATH;
		this.siegeModeSpreadRadius = StrategyIdea.mainSquadCoverRadius;
		this.flyingEnemisInfos = MicroUtils.filterFlyingTargetInfos(euiList);
		
		List<Unit> tankModeList = new ArrayList<>();
		List<Unit> siegeModeList = new ArrayList<>();

		for (Unit unit : unitList) {
			if (skipControl(unit)) {
				continue;
			}
			if (unit.isSieged()) {
				siegeModeList.add(unit);
			} else {
				tankModeList.add(unit);
			}
		}

		executeSiegeMode(siegeModeList, euiList);
		executeTankMode(tankModeList, euiList);
	}

	private void executeSiegeMode(List<Unit> siegeModeList, Collection<UnitInfo> euiList) {

//		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());

		for (Unit siege : siegeModeList) {
//			Decision decision = decisionMaker.makeDecisionForSiegeMode(siege, euiList);
			Decision decision = DecisionMakerPrebot1.makeDecisionForSiegeMode(siege, euiList, siegeModeList, saveUnitLevel);
			if (decision.type == DecisionType.ATTACK_UNIT) {
				CommandUtils.attackUnit(siege, decision.eui.getUnit());
				
			} else if (decision.type == DecisionType.STOP) {
				siege.stop();
				
			} else if (decision.type == DecisionType.HOLD) {
				CommandUtils.holdPosition(siege);
				
			} else if (decision.type == DecisionType.CHANGE_MODE) {
				if (siege.canUnsiege()) {
					CommandUtils.unsiege(siege);
				}
				
			} else if (decision.type == DecisionType.ATTACK_POSITION) { // NO ENEMY
				if (!TankPositionManager.Instance().isProperPositionToSiege(siege.getPosition(), true)) {
					CommandUtils.unsiege(siege);
					TankPositionManager.Instance().siegeModeReservedMap.remove(siege.getID());
				} else {
					int distance = siege.getDistance(StrategyIdea.mainPosition);
					if (InfoUtils.enemyRace() == Race.Terran) { // 테란전용 go
						if (distance > Tank.SIEGE_MODE_MAX_RANGE) {
							siege.unsiege();
						}
						
					} else {
						if (distance > siegeModeSpreadRadius) {
							CommandUtils.unsiege(siege);
							TankPositionManager.Instance().siegeModeReservedMap.remove(siege.getID());
						}
					}
					
				}
			}
		}
	}

	private void executeTankMode(List<Unit> tankModeList, Collection<UnitInfo> euiList) {
//		DecisionMaker decisionMaker = new DecisionMaker(new DefaultTargetCalculator());
		FleeOption fOption = new FleeOption(StrategyIdea.campPosition, false, Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, CoolTimeAttack.COOLTIME_ALWAYS);

		for (Unit tank : tankModeList) {
			if (dangerousOutOfMyRegion(tank)) {
				CommandUtils.move(tank, StrategyIdea.campPosition);
				continue;
			}
			
			Decision decision = DecisionMakerPrebot1.makeDecisionPrebot1(tank, euiList, flyingEnemisInfos, saveUnitLevel);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(tank, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (MicroUtils.isRemovableEnemySpiderMine(tank, decision.eui)) {
					MicroUtils.holdControlToRemoveMine(tank, decision.eui.getLastPosition(), fOption);
					
				} else {
					if (shouldSiege(tank, decision.eui)) {
						CommandUtils.siege(tank);
					} else {
						MicroUtils.kiting(tank, decision.eui, kOption);
					}
				}

			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				
//				if (TankPositionManager.Instance().isProperPositionToSiege(siege.getPosition())) {
//					CommandUtils.unsiege(siege);
//					TankPositionManager.Instance().siegeModeReservedMap.remove(siege.getID());
//				}

				if (InfoUtils.enemyRace() == Race.Terran) { // 테란전용 go
					int distToOrder = tank.getDistance(StrategyIdea.mainPosition);
					if (distToOrder <= Tank.SIEGE_MODE_MAX_RANGE
							&& TankPositionManager.Instance().isProperPositionToSiege(tank.getPosition(), true)) { // orderPosition의 둘러싼 대형을 만든다.
						if (tank.canSiege()) {
							tank.siege();
						} else {
							if (MicroUtils.timeToRandomMove(tank)) {
								Position randomPosition = PositionUtils.randomPosition(tank.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
								CommandUtils.attackMove(tank, randomPosition);
							}
						}
					} else {
						CommandUtils.attackMove(tank, StrategyIdea.mainPosition);
					}
					
				} else {
					boolean arrived = MicroUtils.arrivedToPosition(tank, StrategyIdea.mainPosition);
					Position positionToSiege = TankPositionManager.Instance().getSiegeModePosition(tank.getID());
					if (arrived && positionToSiege == null) {
						positionToSiege = TankPositionManager.Instance().findPositionToSiegeAndReserve(StrategyIdea.mainPosition, tank, siegeModeSpreadRadius);
					}

					if (positionToSiege != null) {
						int stayCnt = TankPositionManager.Instance().isSiegeStayCnt(tank);
						
						if(tank.getDistance(positionToSiege) <= POSITION_TO_SIEGE_ARRIVE_DISTANCE  && TankPositionManager.Instance().isProperPositionToSiege(tank.getPosition(), true)) {
							CommandUtils.siege(tank);
							TankPositionManager.Instance().siegePositionMap.remove(tank.getID());
						}else if(stayCnt > 100){
							CommandUtils.siege(tank);
							TankPositionManager.Instance().siegePositionMap.remove(tank.getID());
						}else{
							CommandUtils.attackMove(tank, positionToSiege);
						}
					} else {
						if (arrived) {
							if (MicroUtils.timeToRandomMove(tank)) {
								Position randomPosition = PositionUtils.randomPosition(tank.getPosition(), MicroConfig.RANDOM_MOVE_DISTANCE);
								CommandUtils.attackMove(tank, randomPosition);
							}

						} else {
							CommandUtils.attackMove(tank, StrategyIdea.mainPosition);
						}
					}
				}
			}
		}
	}

	private boolean shouldSiege(Unit tank, UnitInfo eui) {
		if (!tank.canSiege()) {
			return false;
		}
		
		// 렉 있으면 그냥 퉁퉁포로 조지기
		if (LagObserver.groupsize() > 20 && eui.getType().groundWeapon().maxRange() < UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange()) {
			return false;
		}
		
		if (eui.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || eui.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
			
			int distanceToTarget;
			Unit enemy = UnitUtils.unitInSight(eui);
			if (enemy != null) {
				distanceToTarget = tank.getDistance(enemy);
			} else {
				distanceToTarget = tank.getDistance(eui.getLastPosition());
			}

			if (saveUnitLevel == 0 && distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE + 5) {
				return true;
			} else if (saveUnitLevel == 1 && distanceToTarget <= Tank.SIEGE_MODE_SIGHT + 80) {
				return true;
			} else if (saveUnitLevel == 2 && distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE + Common.BACKOFF_DIST_SIEGE_TANK + 10) {
				return true;
			}
			
		} else {
			if (!hasEnoughBackUpUnitToSiege && MicroUtils.isMeleeUnit(eui.getType())) {
				return false;
			}

			int distanceToTarget = tank.getDistance(eui.getLastPosition());
			int siegeModeDistance;
			if (eui.getType().isBuilding()) {
				siegeModeDistance = Tank.SIEGE_MODE_SIGHT;
			} else {
				siegeModeDistance = Tank.SIEGE_MODE_MAX_RANGE + SIEGE_MODE_RANGE_MARGIN_DISTANCE;
			}
			
			if (distanceToTarget <= siegeModeDistance && TankPositionManager.Instance().isProperPositionToSiege(tank.getPosition(), false)) {
				return true;
			} else {
				if (!eui.getType().isBuilding()) {
					List<Unit> siegeModeTanks = UnitUtils.getUnitsInRadius(PlayerRange.SELF, tank.getPosition(), Tank.SIEGE_LINK_DISTANCE, UnitType.Terran_Siege_Tank_Siege_Mode);
					for (Unit siegeModeTank : siegeModeTanks) {
						if (tank.getID() == siegeModeTank.getID()) {
							continue;
						}
						if (siegeModeTank.getDistance(eui.getLastPosition()) <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 5 && TankPositionManager.Instance().isProperPositionToSiege(tank.getPosition(), false)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
