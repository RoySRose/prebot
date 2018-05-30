package prebot.micro.old.control;


import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.LagObserver;
import prebot.common.util.CommandUtils;
import prebot.common.util.TimeUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.old.OldMicroUtils;
import prebot.micro.old.OldKitingOption;
import prebot.micro.old.OldSquadOrder;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

public class MechanicMicroGoliath extends MechanicMicroAbstract {

	private OldSquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> tankList = new ArrayList<>();
	private List<Unit> goliathList = new ArrayList<>();
	
	private int saveUnitLevel = 1;
	
	private boolean attackWithTank = false;
	private int stickToTankRadius = 0;
	
	public void prepareMechanic(OldSquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = enemiesInfo;
	}
	
	public void prepareMechanicAdditional(List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel) {
		this.tankList = tankList;
		this.goliathList = goliathList;
		this.saveUnitLevel = saveUnitLevel;
		
		this.attackWithTank = tankList.size() * 6 >= goliathList.size();
		if (this.attackWithTank) {
			this.stickToTankRadius = 140 + (int) (Math.log(goliathList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToTankRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit goliath) {
		if (!TimeUtils.executeUnitRotation(goliath, LagObserver.groupsize())) {
			return;
		}

		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(goliath, enemiesInfo, saveUnitLevel); // 0: flee, 1: kiting, 2: attack
		OldKitingOption kOpt = OldKitingOption.defaultKitingOption();
		switch (decision.getDecision()) {
		case 0: // flee
			Position retreatPosition = order.getPosition();
			BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
			if (myBase != null) {
				retreatPosition = myBase.getPosition();
			}
			kOpt.setGoalPosition(retreatPosition);
			OldMicroUtils.preciseFlee(goliath, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			boolean haveToFight = true;
			Unit closeTank = null;
			if (attackWithTank) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit tank : tankList) {
					int dist = goliath.getDistance(tank.getPosition());
					if (dist < closeDist) {
						closeTank = tank;
						closeDist = dist;
						// 가까운 곳에 탱크가 있으면 싸운다.
						if (closeDist < stickToTankRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			}
			if (haveToFight) {
				Unit enemy = OldMicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && goliath.isInWeaponRange(enemy)) {
					goliath.holdPosition();
				} else {
					kOpt.setGoalPosition(order.getPosition());
					OldMicroUtils.preciseKiting(goliath, decision.getTargetInfo(), kOpt);
				}
			} else {
				// 가까운 곳에 없으면 탱크로 이동
//				kOpt.setGoalPosition(closeTank.getPosition());
//				kOpt.setCooltimeAlwaysAttack(false);
				CommandUtils.move(goliath, closeTank.getPosition());
			}
			break;
			
		case 2: // attack move
			if (MicroConfig.Common.versusMechanicSet()) {
				// 테란전용 go
				int distToOrder = goliath.getDistance(order.getPosition());
				if (distToOrder <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition의 둘러싼 대형을 만든다.
					if (goliath.isIdle() || goliath.isBraking()) {
						if (!goliath.isBeingHealed()) {
							Position randomPosition = OldMicroUtils.randomPosition(goliath.getPosition(), 100);
							CommandUtils.attackMove(goliath, randomPosition);
						}
					}
				} else {
					CommandUtils.attackMove(goliath, order.getPosition());
				}
				
			} else {
				Position movePosition = order.getPosition();
				
				// 이동지역까지 attackMove로 간다.
				if (goliath.getDistance(movePosition) > order.getRadius()) {
//					CommandUtil.attackMove(goliath, movePosition);
					CommandUtils.move(goliath, movePosition);
					
				} else { // 목적지 도착
					if (goliath.isIdle() || goliath.isBraking()) {
						if (!goliath.isBeingHealed()) {
							Position randomPosition = OldMicroUtils.randomPosition(goliath.getPosition(), 100);
							CommandUtils.attackMove(goliath, randomPosition);
						}
					}
				}
			}
			break;
		}
	}

}
