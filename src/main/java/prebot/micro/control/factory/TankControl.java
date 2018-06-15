package prebot.micro.control.factory;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.util.CommandUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.Decision;
import prebot.micro.Decision.DecisionType;
import prebot.micro.DecisionMaker;
import prebot.micro.FleeOption;
import prebot.micro.KitingOption;
import prebot.micro.TargetScoreCalculators;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroConfig.Angles;
import prebot.micro.constant.MicroConfig.Tank;
import prebot.micro.control.Control;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.manage.TankPositionManager;

public class TankControl extends Control {

	private static final int ENOUGH_BACKUP_VULTURE_AND_GOLIATH = 7;
	private static final int POSITION_TO_SIEGE_ARRIVE_DISTANCE = 30;
	private static final int SIEGE_MODE_RANGE_MARGIN_DISTANCE = 5;

	private boolean hasEnoughBackUpUnitToSiege = false;
	private int siegeModeSpreadRadius;

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		List<Unit> vultureAndGoliath = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture, UnitType.Terran_Goliath);
		this.hasEnoughBackUpUnitToSiege = vultureAndGoliath.size() > ENOUGH_BACKUP_VULTURE_AND_GOLIATH;
		this.siegeModeSpreadRadius = UnitType.Terran_Siege_Tank_Siege_Mode.sightRange() + (int) (Math.log(unitList.size()) * 11);
		
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

	private void executeSiegeMode(List<Unit> siegeModeList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forSiegeMode);

		for (Unit siege : siegeModeList) {
			Decision decision = decisionMaker.makeDecisionForSiegeMode(siege, euiList);
			if (decision.type == DecisionType.ATTACK_UNIT) {
				CommandUtils.attackUnit(siege, decision.eui.getUnit());
			} else if (decision.type == DecisionType.STOP) {
				siege.stop();
			} else if (decision.type == DecisionType.HOLD) {
				CommandUtils.holdPosition(siege);
			} else if (decision.type == DecisionType.CHANGE_MODE) {
				CommandUtils.unsiege(siege);
			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				if (siege.getDistance(StrategyIdea.mainPosition) > siegeModeSpreadRadius) {
					CommandUtils.unsiege(siege);
				} else {
					CommandUtils.holdPosition(siege);
				}
			}
		}
	}

	private void executeTankMode(List<Unit> tankModeList, List<UnitInfo> euiList) {
		DecisionMaker decisionMaker = new DecisionMaker(TargetScoreCalculators.forTankMode);
		FleeOption fOption = new FleeOption(StrategyIdea.campPosition, false, Angles.NARROW);
		KitingOption kOption = new KitingOption(fOption, false);

		for (Unit tank : tankModeList) {
			Decision decision = decisionMaker.makeDecision(tank, euiList);
			if (decision.type == DecisionType.FLEE_FROM_UNIT) {
				MicroUtils.flee(tank, decision.eui.getLastPosition(), fOption);

			} else if (decision.type == DecisionType.KITING_UNIT) {
				if (MicroUtils.isRemovableEnemySpiderMine(tank, decision.eui)) {
					CommandUtils.holdPosition(tank);
				} else {
					if (shouldSiege(tank, decision.eui)) {
						CommandUtils.siege(tank);
					} else {
						MicroUtils.kiting(tank, decision.eui, kOption);
					}
				}

			} else if (decision.type == DecisionType.ATTACK_POSITION) {
				Position positionToSiege = TankPositionManager.Instance().getSiegeModePosition(tank.getID());
				boolean arrived = MicroUtils.arrivedToPosition(tank, StrategyIdea.mainPosition);
				if (arrived && positionToSiege == null) {
					positionToSiege = TankPositionManager.Instance().findPositionToSiegeAndReserve(StrategyIdea.mainPosition, tank, siegeModeSpreadRadius);
				}

				if (positionToSiege != null) {
					if (tank.getDistance(positionToSiege) <= POSITION_TO_SIEGE_ARRIVE_DISTANCE) {
						CommandUtils.siege(tank);
					} else {
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

	private boolean shouldSiege(Unit tank, UnitInfo eui) {
		if (!tank.canSiege()) {
			return false;
		}

		if (!hasEnoughBackUpUnitToSiege && MicroUtils.isMeleeUnit(eui.getType())) {
			return false;
		}

		int distanceToTarget = tank.getDistance(eui.getLastPosition());
		if (distanceToTarget <= Tank.SIEGE_MODE_MAX_RANGE + SIEGE_MODE_RANGE_MARGIN_DISTANCE) {
			return true;
		} else {
			List<Unit> siegeModeTanks = UnitUtils.getUnitsInRadius(PlayerRange.SELF, tank.getPosition(), Tank.SIEGE_LINK_DISTANCE, UnitType.Terran_Siege_Tank_Siege_Mode);
			for (Unit siegeModeTank : siegeModeTanks) {
				if (tank.getID() == siegeModeTank.getID()) {
					continue;
				}
				if (siegeModeTank.getDistance(eui.getLastPosition()) <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 5) {
					return true;
				}
			}
		}
		return false;
	}
}
