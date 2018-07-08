package prebot.micro.control.building;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.util.InfoUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.BuildingFlyControl;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategy;

import java.util.List;

public class BarracksControl extends BuildingFlyControl {

    public BarracksControl() {
        super(false, true);
    }

    @Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// TODO Auto-generated method stub
        executeFly(unitList, euiList);
	}

    @Override
    public void checkFlyCondition() {

        if(getBuildingFly() == BuildingFly.DOWN && !marinInBuildManager()){

            if((StrategyIdea.currentStrategy.buildTimeMap.isTwoGate())
                    && UnitUtils.getUnitCount(UnitType.Terran_Vulture) >= 3 ){
                setBuildingFly(BuildingFly.UP);
            }else if(StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK) {
                BaseLocation firstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().selfPlayer);
                if(StrategyIdea.campPosition == firstExpansion.getPosition()){
                    setBuildingFly(BuildingFly.UP);
                }
                BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
                CommonCode.RegionType regionType = PositionUtils.positionToRegionType(myFirstExpansion.getPosition());
                if (regionType != CommonCode.RegionType.MY_BASE) {
                    setBuildingFly(BuildingFly.UP);
                }

            }else{
                if(UnitUtils.getUnitCount(UnitType.Terran_Vulture) >= 1){
                    setBuildingFly(BuildingFly.UP);
                }
            }
        }
    }

//    // TODO-REFACTORING combat으로 이동할 항목.
//    // 빌드큐의 아이템 삭제는 확인후 build로 이동
//		if (Prebot.Broodwar.getFrameCount() < 10000) {
//        if (Prebot.Broodwar.getFrameCount() % 29 == 0) {
//            executeFly();
//        }
//    } else {
//        if (Prebot.Broodwar.getFrameCount() % 281 == 0) {
//            executeFly();
//        }
//    }
//
//    private void executeFly() {
//
//        // 12000프레임 이후 : 배럭, 엔지니어링 베이 띄우기
//        if (Prebot.Broodwar.getFrameCount() > 12000) {
//            // 완성된 factory가 2개 이상이어야 함
//            if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 1) {
//                for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//                    if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
//
//                        unit.lift();
//                        LiftChecker = true;
//                        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//                        BuildOrderItem checkItem = null;
//
//                        if (!tempbuildQueue.isEmpty()) {
//                            checkItem = tempbuildQueue.getHighestPriorityItem();
//                            while (true) {
//                                if (tempbuildQueue.canGetNextItem() == true) {
//                                    tempbuildQueue.canGetNextItem();
//                                } else {
//                                    break;
//                                }
//                                tempbuildQueue.PointToNextItem();
//                                checkItem = tempbuildQueue.getItem();
//
//                                if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
//                                    tempbuildQueue.removeCurrentItem();
//                                }
//                            }
//                        }
//                    }
//                    if (InformationManager.Instance().enemyRace != Race.Zerg) {
//                        if (unit.getType() == UnitType.Terran_Marine) {
//                            CommandUtils.move(unit, InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().selfPlayer).getPoint());
//                        }
//                    }
//                }
//            }
//        }
//
//        // 12000프레임 이하
//        else {
//            // TODO-REFACTORING 적이 테란 또는 프로토스인 경우 배럭스 들고 내리고 -> combat으로 이동
//            if (InformationManager.Instance().enemyRace == Race.Terran) {
//
//                for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//                    if (unit.isLifted() == true && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
//                        if (unit.isLifted()) {
//                            if (unit.canLand(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()))) {
//                                unit.land(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()));
//                                LiftChecker = false;
//                            } else {
//                                unit.land(unit.getTilePosition());
//                                LiftChecker = false;
//                            }
//                        }
//                    }
//                }
//                Boolean lift = false;
//                if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) > InfoUtils.enemyNumUnits(UnitType.Terran_Vulture)) {
//                    lift = true;
//                }
//                if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
//                        + Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) > 2) {
//
//                    lift = true;
//                }
//                if (lift) {
//                    for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//                        if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
//                            unit.lift();
//                            LiftChecker = true;
//                            BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//                            BuildOrderItem checkItem = null;
//
//                            if (!tempbuildQueue.isEmpty()) {
//                                checkItem = tempbuildQueue.getHighestPriorityItem();
//                                while (true) {
//                                    if (tempbuildQueue.canGetNextItem() == true) {
//                                        tempbuildQueue.canGetNextItem();
//                                    } else {
//                                        break;
//                                    }
//                                    tempbuildQueue.PointToNextItem();
//                                    checkItem = tempbuildQueue.getItem();
//
//                                    if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
//                                        tempbuildQueue.removeCurrentItem();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (InformationManager.Instance().enemyRace == Race.Protoss) {
//
//                for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//                    if (unit.isLifted() == true && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
//                        if (unit.isLifted()) {
//                            if (unit.canLand(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()))) {
//                                unit.land(new TilePosition(BlockingEntrance.Instance().barrack.getX(), BlockingEntrance.Instance().barrack.getY()));
//                                LiftChecker = false;
//                            } else {
//                                unit.land(unit.getTilePosition());
//                                LiftChecker = false;
//                            }
//                        }
//                    }
//                }
//
//                int dragooncnt = 0;
//                int zealotcnt = 0;
//
//                // if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) >= 1
//                // && MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 1) {
//
//                bwapi.Position checker = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer).getPoint();
//                List<Unit> eniemies = MapGrid.Instance().getUnitsNear(checker, 500, false, true, null);
//
//                Boolean lift = false;
//                for (Unit enemy : eniemies) {
//
//                    if (enemy.getType() == UnitType.Protoss_Dragoon) {
//                        dragooncnt++;
//                    }
//                    if (enemy.getType() == UnitType.Protoss_Zealot) {
//                        zealotcnt++;
//                    }
//                }
//
//                if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode)
//                        + Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode) >= 3 && dragooncnt + zealotcnt == 0) {
//                    lift = true;
//                }
//
//                EnemyStrategyException currentStrategyException = StrategyManager.Instance().currentStrategyException;
//
//                if (currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
//                        && currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
//                        && currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT
//                        && currentStrategyException != EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON) {
//                    lift = true;
//                }
//
//                if (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
//                        || currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH) {
//                    lift = false;
//                }
//                if (zealotcnt + dragooncnt > 0) {
//                    lift = false;
//                }
//
//                if (zealotcnt + dragooncnt > 8) {
//                    if ((currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
//                            || currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTODRAGOON
//                            || currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
//                            || currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_READYTOZEALOT) && dragooncnt + zealotcnt > 0
//                            && InfoUtils.myNumUnits(UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Tank_Mode) >= dragooncnt
//                            && InfoUtils.myNumUnits(UnitType.Terran_Vulture) > zealotcnt) {
//                        lift = true;
//                    }
//                }
//                if (dragooncnt + zealotcnt == 0) {
//                    lift = true;
//                }
//
//                if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) == 2) {
//                    lift = true;
//                }
//                if (lift) {
//                    for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//                        if (unit.isLifted() == false && (unit.getType() == UnitType.Terran_Barracks || unit.getType() == UnitType.Terran_Engineering_Bay) && unit.isCompleted()) {
//                            unit.lift();
//                            LiftChecker = true;
//                            BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//                            BuildOrderItem checkItem = null;
//
//                            if (!tempbuildQueue.isEmpty()) {
//                                checkItem = tempbuildQueue.getHighestPriorityItem();
//                                while (true) {
//                                    if (tempbuildQueue.canGetNextItem() == true) {
//                                        tempbuildQueue.canGetNextItem();
//                                    } else {
//                                        break;
//                                    }
//                                    tempbuildQueue.PointToNextItem();
//                                    checkItem = tempbuildQueue.getItem();
//
//                                    if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Marine) {
//                                        tempbuildQueue.removeCurrentItem();
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}
