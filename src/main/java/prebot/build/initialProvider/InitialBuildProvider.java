package prebot.build.initialProvider;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.initialProvider.buildSets.VsProtoss;
import prebot.build.initialProvider.buildSets.VsTerran;
import prebot.build.initialProvider.buildSets.VsZerg;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/// 봇 프로그램 설정
public class InitialBuildProvider {

	private static InitialBuildProvider instance = new InitialBuildProvider();
	
	public static InitialBuildProvider Instance() {
		return instance;
	}
	
	public int nowMarine = 0;
	
	public int orderMarine  = 0;
	
	public ExpansionOption nowStrategy, bfStrategy;

	public boolean InitialBuildFinished = false;
	
	public TilePosition firstSupplyPos = TilePosition.None;
	public TilePosition barrackPos = TilePosition.None;
	public TilePosition secondSupplyPos = TilePosition.None;
	public TilePosition factoryPos = TilePosition.None;
	public TilePosition bunkerPos = TilePosition.None; 
	
	public void onStart() {
		System.out.println("InitialBuildProvider onStart start");
		
		nowStrategy = null;
		bfStrategy = null;
   	 
//		StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT
        //BlockingEntrance blockingEntrance = new BlockingEntrance();

        firstSupplyPos = BlockingEntrance.Instance().first_supple;
        barrackPos = BlockingEntrance.Instance().barrack;
        secondSupplyPos = BlockingEntrance.Instance().second_supple;
        factoryPos = BlockingEntrance.Instance().factory;
        bunkerPos = BlockingEntrance.Instance().bunker;
        //TilePosition entranceTurretPos = blockingEntrance.entrance_turret;
//        FileUtils.appendTextToFile("log.txt", "\n InitialBuildProvider firstSupplyPos ==>> (" + firstSupplyPos.getX() +" , "+firstSupplyPos.getX()+" ) ");

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			new VsTerran(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			new VsProtoss(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
		} else {
			new VsZerg(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, ExpansionOption.TWO_STARPORT);
		}
		
		System.out.println("InitialBuildProvider onStart end");
	}

    public void updateInitialBuild(){
        if(BuildManager.Instance().buildQueue.isEmpty()){
        	
            InitialBuildFinished = true;
//            FileUtils.appendTextToFile("log.txt", "\n updateInitialBuild end ==>> " + InitialBuildFinished);
        }
        
        if(InitialBuildFinished == false) {
        	
        	if(bfStrategy == null)	{
        		bfStrategy = StrategyIdea.currentStrategy.expansionOption;
        		FileUtils.appendTextToFile("log.txt", "\n bfStrategy is null & update ==>> " + bfStrategy.toString());
        	}
    		nowStrategy = StrategyIdea.currentStrategy.expansionOption;
    		
//    		최초 전략과 현재 전략이 다르면 빌드오더 날리고 새로 심기
    		if(bfStrategy != nowStrategy) {
    			FileUtils.appendTextToFile("log.txt", "\n strategy is diffrent ::  nowStrategy : " + StrategyIdea.currentStrategy.toString());
    			FileUtils.appendTextToFile("log.txt", "\n bfStrategy : " + bfStrategy.toString() + " & nowStrategy : " + nowStrategy.toString());
    			deleteFromQueueAll();
    			new VsZerg(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, nowStrategy);
    			
    			List<Unit> unitList = Prebot.Broodwar.self().getUnits();
				
				Collections.sort(unitList, new Comparator<Unit>() {
					@Override
					public int compare(Unit p1, Unit p2) {
						return p1.getType().toString().compareTo(p2.getType().toString());
					}
				});
				UnitType nowUnit = UnitType.None;
    			for(Unit unit : unitList) {
    				if(nowUnit != unit.getType()) {
    					nowUnit = unit.getType();
    					deleteFromQueueCnt(nowUnit, UnitUtils.getUnitCount(nowUnit));
    				}
    			}
    			bfStrategy = nowStrategy;
    		}
        	
//        	System.out.println("nowMarine ==>> " + nowMarine + " / orderMarine ==>> " + orderMarine);
        	
        	BuildOrderQueue iq = BuildManager.Instance().buildQueue;
        	
        	 if(addMarineInitial()) {
             	iq.queueAsHighestPriority(UnitType.Terran_Marine, false);
             	orderMarine++;
             }
        	
        	if(addSupplyInitial()) {
        		
        		int nowSupply = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Supply_Depot);
//        		if(nowSupply == 0) {
//        			iq.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BlockingEntrance.Instance().first_supple, true);
//        		}else 
        		if(nowSupply == 1) {
        			iq.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BlockingEntrance.Instance().second_supple, true);
        		}else {
        			iq.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextSupplePoint, false);
        		}
        	}
        }
        
        
       


        
        //TODO 여기에 조건별 다 때려 넣으소서.......... 몇개 안되는거 굳이 나눌 필요가 있나싶다.




    }

    public int deleteFromQueue(UnitType unitType){
        BuildOrderItem checkItem= null;
        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();

        int cnt =0;

        if (!tempbuildQueue.isEmpty()) {
            checkItem= tempbuildQueue.getHighestPriorityItem();
            while(true){
                if(tempbuildQueue.canGetNextItem() == true){
                    tempbuildQueue.canGetNextItem();
                }else{
                    break;
                }
                tempbuildQueue.PointToNextItem();
                checkItem = tempbuildQueue.getItem();

                if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == unitType){
                    cnt++;
                    tempbuildQueue.removeCurrentItem();
                }
            }
        }
        return cnt;
    }
    
    public void deleteFromQueueCnt(UnitType unitType, int chkCnt){
        BuildOrderItem checkItem= null;
        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();

        FileUtils.appendTextToFile("log.txt", "\n 새 빌드오더에서 삭제할 유닛  ::  unitType : " + unitType.toString() + " & chkCnt : " + chkCnt);
        int cnt =0;

        if (!tempbuildQueue.isEmpty()) {
            checkItem= tempbuildQueue.getHighestPriorityItem();
            while(true){
                if(tempbuildQueue.canGetNextItem() == true){
                    tempbuildQueue.canGetNextItem();
                }else{
                    break;
                }
                tempbuildQueue.PointToNextItem();
                checkItem = tempbuildQueue.getItem();

                if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == unitType){
                    cnt++;
                    tempbuildQueue.removeCurrentItem();
                    if(cnt >= chkCnt) break;
                }
            }
        }
//        return cnt;
    }
    
    
    public void deleteFromQueueAll(){
        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();

//        int cnt =0;

        if (!tempbuildQueue.isEmpty()) {
            while(true){
                if(tempbuildQueue.canGetNextItem() == true){
                    tempbuildQueue.canGetNextItem();
                }else{
                    break;
                }
                tempbuildQueue.PointToNextItem();
                tempbuildQueue.removeCurrentItem();
//                cnt++;
//                checkItem = tempbuildQueue.getItem();
//
//                if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == unitType){
//                    cnt++;
//                    tempbuildQueue.removeCurrentItem();
//                }
            }
        }
//        return cnt;
    }
    
    
    public boolean addSupplyInitial(){
    	
    	BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
		BuildOrderItem checkItem = null;
    	
    	
//    	frame으로 처리되어 있는 이유. initial이 끝난 후로 하면 되지 않나?
//    	일단 기존 조건대로 처리. 셀렉터는 이니셜 빌드 이후에 도므로 아래 조건의 필요 유무 판단
    	
    	/*if (!(Prebot.Broodwar.getFrameCount() % 29 == 0 && Prebot.Broodwar.getFrameCount() > 4500)) {
    		
    		return false;
    	}*/
		
		

        if (Prebot.Broodwar.self().supplyTotal() >= 400) {
            return false;
        }
        
        if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Supply_Depot) < 2) {
            return false;
        }
        
        if (!tempbuildQueue.isEmpty()) {
			checkItem = tempbuildQueue.getHighestPriorityItem();
			while (true) {
				if (checkItem.blocking == true) {
					break;
				}
				// if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType().isAddon()){
				// return;
				// }
				if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Missile_Turret) {
					return false;
				}
				if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
					return false;
				}
				if (tempbuildQueue.canSkipCurrentItem() == true) {
					tempbuildQueue.skipCurrentItem();
				} else {
					break;
				}
				checkItem = tempbuildQueue.getItem();
			}
			if (checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == UnitType.Terran_Supply_Depot) {
				return false;
			}
		}
        
        

        // 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
        // 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
        int supplyMargin = 4;
        boolean barrackflag = false;
        boolean factoryflag = false;
        boolean starportflag = false;
//        int barrackMargin = 4;
//        int facMargin = 2;
//        int satrportMargin = 2;

        
        if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) > 0) {
        	barrackflag = true;
        }
        
        if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) > 0) {
        	factoryflag = true;
        }
        
        if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Starport) > 0) {
        	starportflag = true;
        }
        
        /*if(factoryflag==false){
            for (Unit unit : Prebot.Broodwar.self().getUnits()) {
                if (unit.getType() == UnitType.Terran_Factory  && unit.isCompleted()) {
                    factoryflag = true;
                }
                if (unit.getType() == UnitType.Terran_Barracks && unit.isCompleted()) {
                    barrackflag = true;
                }
            }
        }*/

        int Faccnt =0;
        int Starportcnt =0;
        int CCcnt  = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
        int facFullOperating =0;
        int starportOperating =0;
        
//        Factory 와 Starport 에서 유닛이 생산되는중인지 체크.
//        기본적으로 유닛생산 건물수 만큼의 여유분이 있어야 하고, 현재 생산되고 있는 유닛만큼 여유분이 더 있어야 한다.
        
        List<Unit> factory = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
        for (Unit unit : factory)
        {

            Faccnt ++;
            if(unit.isTraining() == true){
                facFullOperating++;
            }

        }
        
        List<Unit> starport = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Starport);
        for (Unit unit : starport)
        {

        	Starportcnt ++;
            if(unit.isTraining() == true){
            	starportOperating++;
            }

        }

        if(CCcnt == 1){//TODO 이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨
        	supplyMargin = 4;
            if(barrackflag==true){
                supplyMargin++;
            }
            if(factoryflag==true){
                supplyMargin = supplyMargin+2+(4*Faccnt)+(facFullOperating*2);
            }
            if(starportflag==true){
            	if(factoryflag==false) {
            		supplyMargin = supplyMargin+2+(4*Starportcnt)+(starportOperating*2);
            	}else {
            		supplyMargin = supplyMargin+(4*Starportcnt)+(starportOperating*2);
            	}
            }
        }else{ //if((MyBotModule.Broodwar.getFrameCount()>=6000 && MyBotModule.Broodwar.getFrameCount()<10000) || (Faccnt > 3 && CCcnt == 2)){
            supplyMargin = 11+4*Faccnt+facFullOperating*2;
        }

        // currentSupplyShortage 를 계산한다
        int currentSupplyShortage = Prebot.Broodwar.self().supplyUsed() + supplyMargin + 1 - Prebot.Broodwar.self().supplyTotal();

        if (currentSupplyShortage > 0) {
            // 생산/건설 중인 Supply를 센다
            int onBuildingSupplyCount = 0;
            // 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
            onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
                    UnitType.Terran_Supply_Depot, null)
                    * UnitType.Terran_Supply_Depot.supplyProvided();

            if (currentSupplyShortage > onBuildingSupplyCount) {
//                setHighPriority(true);
//                setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextSupplePoint);
                //this.setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextSupplePoint);
                //System.out.println("return supply true");
                //FileUtils.appendTextToFile("log.txt", "\n return supply true ==>>> ");
                return true;
            }
        }

        return false;
    }
    
    public boolean addMarineInitial(){
    	
    	if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
    	
    	nowMarine = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine);
    	
    	
//    	20180721. hkk
//    	마린을 프레임돌때마다 큐에 넣는것이 아니고. 생산이 될때마다 큐에 추가.
//    	현재마린(nowMarine) 과 생산명령을 마린 숫자(orderMarine)를 비교
//    	빌드큐에 넣으면 orderMarine++ 을 하고 현재 마린 숫자가 그것과 같아지면 다시 큐에 넣는다.
    	if(nowMarine == orderMarine) {
    	
	        if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine, null)
					+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Marine, null) 
					+ nowMarine < StrategyIdea.marineCount) {
	        	
	        		return true;
			}
    	}
    	return false;
    	
    }
}









