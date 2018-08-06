package prebot.build.initialProvider;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.initialProvider.buildSets.AdaptNewStrategy;
import prebot.build.initialProvider.buildSets.VsProtoss;
import prebot.build.initialProvider.buildSets.VsTerran;
import prebot.build.initialProvider.buildSets.VsZerg;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.prebot1.ConstructionTask;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.AddOnOption;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;

/// 봇 프로그램 설정
public class InitialBuildProvider {
	
	public enum AdaptStrategyStatus {
		BEFORE, PROGRESSING, COMPLETE
	}

	private static InitialBuildProvider instance = new InitialBuildProvider();
	
	public static InitialBuildProvider Instance() {
		return instance;
	}
	
	private AdaptStrategyStatus adaptStrategyStatus = AdaptStrategyStatus.BEFORE;

	public AdaptStrategyStatus getAdaptStrategyStatus() {
		return adaptStrategyStatus;
	}
	
	public int nowMarine = 0;
	
	public int orderMarine  = 0;
	
	public ExpansionOption nowStrategy, bfStrategy;
	public EnemyStrategy dbgNowStg, dbgBfStg;

	public TilePosition firstSupplyPos = TilePosition.None;
	public TilePosition barrackPos = TilePosition.None;
	public TilePosition secondSupplyPos = TilePosition.None;
	public TilePosition factoryPos = TilePosition.None;
	public TilePosition bunkerPos = TilePosition.None;
	public TilePosition starport1 = TilePosition.None;
	public TilePosition starport2 = TilePosition.None;
	
	
	public void onStart() {
		System.out.println("InitialBuildProvider onStart start");
		
		nowStrategy = null;
		bfStrategy = null;
		dbgNowStg = null;
		dbgBfStg = null;
   	 
//		StrategyIdea.currentStrategy.expansionOption == ExpansionOption.TWO_STARPORT
        //BlockingEntrance blockingEntrance = new BlockingEntrance();

        firstSupplyPos = BlockingEntrance.Instance().first_supple;
        barrackPos = BlockingEntrance.Instance().barrack;
        secondSupplyPos = BlockingEntrance.Instance().second_supple;
        factoryPos = BlockingEntrance.Instance().factory;
        bunkerPos = BlockingEntrance.Instance().bunker;
        starport1 = BlockingEntrance.Instance().starport1;
        starport2 = BlockingEntrance.Instance().starport2;
        //TilePosition entranceTurretPos = blockingEntrance.entrance_turret;
//        FileUtils.appendTextToFile("log.txt", "\n InitialBuildProvider firstSupplyPos ==>> (" + firstSupplyPos.getX() +" , "+firstSupplyPos.getX()+" ) ");
        
//        ConstructionPlaceFinder.Instance().freeTiles(firstSupplyPos, 3, 2);
//        ConstructionPlaceFinder.Instance().freeTiles(secondSupplyPos, 3, 2);
//        ConstructionPlaceFinder.Instance().freeTiles(barrackPos, 4, 3);
//        ConstructionPlaceFinder.Instance().freeTiles(barrackPos, 4, 3);
//        ConstructionPlaceFinder.Instance().freeTiles(bunkerPos, 3, 2);
//        ConstructionPlaceFinder.Instance().freeTiles(starport1, 4, 3);
//        ConstructionPlaceFinder.Instance().freeTiles(starport2, 4, 3);

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			new VsTerran(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, starport1, starport2);
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			new VsProtoss(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, starport1, starport2);
		} else {
			new VsZerg(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, starport1, starport2);
		}
		
		System.out.println("InitialBuildProvider onStart end");
	}

    public void updateInitialBuild(){
    	
		if (adaptStrategyStatus == AdaptStrategyStatus.BEFORE) {
			if (BuildManager.Instance().buildQueue.isEmpty()) {
				nowStrategy = StrategyIdea.currentStrategy.expansionOption;
				if (nowStrategy == ExpansionOption.TWO_FACTORY || nowStrategy == ExpansionOption.TWO_STARPORT) {
	        		new AdaptNewStrategy().adapt(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, starport1, starport2, nowStrategy);
	        	}
				adaptStrategyStatus = AdaptStrategyStatus.PROGRESSING;
			}

		
		} else if (adaptStrategyStatus == AdaptStrategyStatus.PROGRESSING) {
        	if (nowStrategy != StrategyIdea.currentStrategy.expansionOption) {
        		nowStrategy = StrategyIdea.currentStrategy.expansionOption;
 
        		// 폭파하기
        		cancelConstructionAndRemoveFromBuildQueue();
        		new AdaptNewStrategy().adapt(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos, starport1, starport2, nowStrategy);
				// adaptStrategyStatus = AdaptStrategyStatus.COMPLETE; // 2번은 취소하지 않도록
        		
        	} else {
            	if (nowStrategy == ExpansionOption.TWO_FACTORY) {
            		List<Unit> factoryList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
            		if (factoryList.size() == 2) {
            			adaptStrategyStatus = AdaptStrategyStatus.COMPLETE;
            		}
            	} else if (nowStrategy == ExpansionOption.TWO_STARPORT) {
            		List<Unit> starportList = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Starport);
            		if (starportList.size() == 2) {
            			adaptStrategyStatus = AdaptStrategyStatus.COMPLETE;
            		}
            	} else if (nowStrategy == ExpansionOption.ONE_FACTORY) {
            		List<Unit> starportList = UnitUtils.getUnitList(UnitFindRange.ALL, UnitType.Terran_Command_Center);
            		if (starportList.size() == 2) {
            			adaptStrategyStatus = AdaptStrategyStatus.COMPLETE;
            		}
            	}
        	}
        }
        	
//    	BuildOrderQueue iq = BuildManager.Instance().buildQueue;
    	
//    	if(addMachineShopInitial()) {
//    		iq.queueAsHighestPriority(UnitType.Terran_Machine_Shop, false);
//    	}
    	
//    	if(addMarineInitial()) {
//    		int deadMarine = Prebot.Broodwar.self().deadUnitCount(UnitType.Terran_Marine);
//        	iq.queueAsHighestPriority(UnitType.Terran_Marine, false);
//         	orderMarine = orderMarine + 1 - deadMarine;
//         	
//        }
    	
//    	if(addSupplyInitial()) {
//    		
////        		int nowSupply = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Supply_Depot);
//    		//완성됐거나 지어지고 있는 서플라이 디포
//    		int nowSupply = UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Supply_Depot);
////        		if(nowSupply == 0) {
////        			iq.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BlockingEntrance.Instance().first_supple, true);
////        		}else
//    		//1개까지는 이니셜 빌드에 있다 치고 이거 괜찮나..........
//    		if(nowSupply == 1) {
//    			iq.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BlockingEntrance.Instance().second_supple, true);
//    		}else {
//    			iq.queueAsHighestPriority(UnitType.Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.NextSupplePoint, false);
//    		}
//    	}


        
        //TODO 여기에 조건별 다 때려 넣으소서.......... 몇개 안되는거 굳이 나눌 필요가 있나싶다.




    }

    /// 폭파취소시킨 건물이 있으면 true
	private boolean cancelConstructionAndRemoveFromBuildQueue() {
		List<Unit> cancelBuildings = new ArrayList<>();
		if (nowStrategy == ExpansionOption.TWO_FACTORY) {
			deleteFromBuildQueue(UnitType.Terran_Starport, false);
			deleteFromConstructionQueue(UnitType.Terran_Starport, false);
			cancelBuildings = UnitUtils.getUnitList(UnitFindRange.INCOMPLETE, UnitType.Terran_Starport);
			
		} else if (nowStrategy == ExpansionOption.TWO_STARPORT || nowStrategy == ExpansionOption.ONE_FACTORY) {
			Unit completeFirstFactory = null;
			List<Unit> completeBuildings = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
			List<Unit> incompleteBuildings = UnitUtils.getUnitList(UnitFindRange.INCOMPLETE, UnitType.Terran_Factory);
			if (completeBuildings.size() >= 1) {
				completeFirstFactory = completeBuildings.get(0);
			}
			
			Unit incompleteFirstFactory = null;
			if (completeFirstFactory == null) {
				int minimumRemainingBuildTime = CommonCode.INT_MAX;
				for (Unit incompleteBuilding : incompleteBuildings) {
					if (incompleteBuilding.getRemainingBuildTime() < minimumRemainingBuildTime) {
						incompleteFirstFactory = incompleteBuilding;
						minimumRemainingBuildTime = incompleteBuilding.getRemainingBuildTime();
					}
				}
			}

			boolean notFirstOne = true;
			if (completeFirstFactory != null) {
				notFirstOne = false;
			}
			
			int deleteCount = deleteFromConstructionQueue(UnitType.Terran_Factory, notFirstOne);
			if (deleteCount == 0) {
				if (incompleteFirstFactory != null || incompleteFirstFactory != null) {
					notFirstOne = false;
				} else {
					notFirstOne = true;
				}
				BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Factory);
				deleteFromBuildQueue(UnitType.Terran_Factory, notFirstOne);
			}
			for (Unit incompleteBuilding : incompleteBuildings) {
				if (incompleteFirstFactory == null || incompleteFirstFactory.getID() != incompleteBuilding.getID()) {
					cancelBuildings.add(incompleteBuilding);
				}
			}
			
		}
		
		for (Unit cancelBuilding : cancelBuildings) {
			ConstructionManager.Instance().addCancelBuildingId(cancelBuilding.getID());
		}
		return !cancelBuildings.isEmpty();
	}
    
    public void debugingFromQueue(String setStr){
        BuildOrderItem dbgCheckItem= null;
        BuildOrderQueue dbgTempbuildQueue = BuildManager.Instance().getBuildQueue();

        int cnt =0;
//        FileUtils.appendTextToFile("log.txt", "\n "+ setStr + " debugingFromQueue start =============================");
        if (!dbgTempbuildQueue.isEmpty()) {
        	dbgCheckItem= dbgTempbuildQueue.getHighestPriorityItem();
            while(true){
                if(dbgTempbuildQueue.canGetNextItem() == true){
                	dbgTempbuildQueue.canGetNextItem();
                }else{
                    break;
                }
                dbgTempbuildQueue.PointToNextItem();
                dbgCheckItem = dbgTempbuildQueue.getItem();
                cnt ++;
//                FileUtils.appendTextToFile("log.txt", "\n debugingFromQueue ::  unitType : " + cnt + " :: " + dbgCheckItem.metaType.getUnitType());
                //tempbuildQueue.removeCurrentItem();
            }
        }
//        FileUtils.appendTextToFile("log.txt", "\n " + setStr + " debugingFromQueue end =============================");
    }
    
    public int deleteFromBuildQueue(UnitType unitType, boolean notFirstOne){
        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
        if (tempbuildQueue.isEmpty()) {
        	return 0;
        }
        
        return tempbuildQueue.removeUnitTypeItems(unitType, notFirstOne);
    }
    
    public int deleteFromConstructionQueue(UnitType deleteType, boolean notFirstOne){
        boolean haveToSkipFirstOne = notFirstOne;
        
        Vector<ConstructionTask> removeFromQueue = new Vector<>();
    	Vector<ConstructionTask> constructionQueue = ConstructionManager.Instance().getConstructionQueue();
    	for (ConstructionTask constructionTask : constructionQueue) {
    		if (constructionTask.getType() == deleteType) {
    			if (haveToSkipFirstOne) {
    				haveToSkipFirstOne = false;
    			} else {
    				removeFromQueue.add(constructionTask);
    			}
    		}
    	}
    	
    	int count = 0;
    	for (ConstructionTask constructionTask : removeFromQueue) {
    		ConstructionManager.Instance().cancelConstructionTask(constructionTask.getType(), constructionTask.getDesiredPosition());
    		count++;
    	}
    	return count;
    }
    
    public void deleteFromQueueCnt(UnitType unitType, int chkCnt){
        BuildOrderItem checkItem= null;
        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//        BuildOrderQueue tempbuildQueue = BuildManager.Instance().buildQueue;

        
        int cnt =0;
        
        if(unitType == UnitType.Terran_SCV) {
        	chkCnt = chkCnt-4;
        }
//        FileUtils.appendTextToFile("log.txt", "\n delete of new build order ::  unitType : " + unitType.toString() + " & chkCnt : " + chkCnt);

        if (!tempbuildQueue.isEmpty()) {
//            checkItem= tempbuildQueue.getHighestPriorityItem();
            while(true){
            	System.out.println("unitType :: " + unitType + " while true");
                if(tempbuildQueue.canGetNextItem() == true){
                    tempbuildQueue.canGetNextItem();
                }else{
                    break;
                }
                checkItem= tempbuildQueue.getHighestPriorityItem();
//                tempbuildQueue.PointToNextItem();
//                checkItem = tempbuildQueue.getItem();

                if(checkItem.metaType.isUnit() && checkItem.metaType.getUnitType() == unitType){
//                    FileUtils.appendTextToFile("log.txt", "\n delete unit ::  unitType : " + checkItem.metaType.getUnitType() + " to " + cnt);
                    tempbuildQueue.removeCurrentItem();
                    cnt++;
//                    debugingFromQueue(" deleteFromQueueCnt "+ cnt + " :: " + unitType.toString());
//                    if(cnt >= chkCnt) break;
                }else {
                	cnt = cnt + deleteFromQueueNext(tempbuildQueue, unitType);
                }
                if(cnt >= chkCnt) return;
            }
        }
//        return cnt;
    }
    
    public int deleteFromQueueNext(BuildOrderQueue tempQueue, UnitType unit){
    	if(tempQueue.canGetNextItem() == true){
	    	tempQueue.PointToNextItem();
//	    	FileUtils.appendTextToFile("log.txt", "\n deleteFromQueueNext ::  queue unitType : " + tempQueue.getItem().metaType.getUnitType() + " &&  deleteUnit ::" + unit);
	    	if( tempQueue.getItem().metaType.getUnitType() == unit) {
	    		tempQueue.removeCurrentItem();
	    		return 1;
	    	}else {
	    		deleteFromQueueNext(tempQueue, unit);
	    	}
	    	return 0;
    	}
    	return 0;
    }
    
    
    public void deleteFromQueueAll(){
    	BuildManager.Instance().buildQueue = new BuildOrderQueue();
//    	FileUtils.appendTextToFile("log.txt", "\n deleteFromQueueAll");
//    	BuildOrderItem checkItem= null;
//        BuildOrderQueue tempbuildQueue = BuildManager.Instance().getBuildQueue();
//
//        if (!tempbuildQueue.isEmpty()) {
//            checkItem= tempbuildQueue.getHighestPriorityItem();
//            while(true){
//	            if(tempbuildQueue.canGetNextItem() == true){
//	                tempbuildQueue.canGetNextItem();
//	            }else{
//	                break;
//	            }
//	            tempbuildQueue.PointToNextItem();
//	            checkItem = tempbuildQueue.getItem();
//	            if(checkItem.metaType.isUnit()){
//	            	FileUtils.appendTextToFile("log.txt", "\n tempbuildQueue.removeCurrentItem() => " + checkItem.metaType.getUnitType());
//	                tempbuildQueue.removeCurrentItem();
//	            }
//            }
//        }
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
    	
    	
//    	마린이 2마리가 생산된 상태에서 팩토리가 없다면 팩토리 먼저
    	if(nowMarine == 2 && UnitUtils.getUnitCount(UnitFindRange.ALL_AND_CONSTRUCTION_QUEUE, UnitType.Terran_Factory) == 0) {
    		return false;
    	}
    	
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
    
    
    public boolean addMachineShopInitial(){
    	
    	if (Prebot.Broodwar.self().minerals() < 50 || Prebot.Broodwar.self().gas() < 50) {
			return false;
		}
		
		
		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop, null);
		if (buildQueueCount > 0) {
			return false;
		}
		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null);
		if (constructionCount > 0) {
			return false;
		}
		
		List<Unit> factories = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Factory);
		
		
		if(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Machine_Shop) == 0) {
		
			if(StrategyIdea.currentStrategy.addOnOption == AddOnOption.VULTURE_FIRST) {
//				FileUtils.appendTextToFile("log.txt", "\n BuilderMachineShop AddOnOption.VULTURE_FIRST");
				if(UnitUtils.myUnitDiscovered(UnitType.Terran_Vulture) && !UnitUtils.hasUnitOrWillBe(UnitType.Terran_Machine_Shop)){
//					FileUtils.appendTextToFile("log.txt", "\n BuilderMachineShop have vulture & not have machineShop:: return true");
					for (Unit factory : factories) {
						if (factory.getAddon() != null || !factory.canBuildAddon()) {
							continue;
						}
						return true;
					}
				}
			}else {
				return true;
			}
		}
    	return false;
    }
}









