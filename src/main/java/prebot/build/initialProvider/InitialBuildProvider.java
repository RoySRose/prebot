package prebot.build.initialProvider;


import bwapi.Race;
import bwapi.TilePosition;
import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.initialProvider.buildSets.VsProtoss;
import prebot.build.initialProvider.buildSets.VsTerran;
import prebot.build.initialProvider.buildSets.VsZerg;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionManager;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions;

/// 봇 프로그램 설정
public class InitialBuildProvider {

	private static InitialBuildProvider instance = new InitialBuildProvider();
	
	public static InitialBuildProvider Instance() {
		return instance;
	}
	
	public int nowMarine = 0;
	
	public int orderMarine  = 1;

	public boolean InitialBuildFinished = false;
	
	public void onStart() {
		System.out.println("InitialBuildProvider onStart start");
		
		
   	 
        //BlockingEntrance blockingEntrance = new BlockingEntrance();

        TilePosition firstSupplyPos = BlockingEntrance.Instance().first_supple;
        TilePosition barrackPos = BlockingEntrance.Instance().barrack;
        TilePosition secondSupplyPos = BlockingEntrance.Instance().second_supple;
        TilePosition factoryPos = BlockingEntrance.Instance().factory;
        TilePosition bunkerPos = BlockingEntrance.Instance().bunker;
        //TilePosition entranceTurretPos = blockingEntrance.entrance_turret;
//        FileUtils.appendTextToFile("log.txt", "\n InitialBuildProvider firstSupplyPos ==>> (" + firstSupplyPos.getX() +" , "+firstSupplyPos.getX()+" ) ");

		if (InformationManager.Instance().enemyRace == Race.Terran) {
			new VsTerran(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			new VsProtoss(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
		} else {
			new VsZerg(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
		}
		
		System.out.println("InitialBuildProvider onStart end");
	}

    public void updateInitialBuild(){
        if(BuildManager.Instance().buildQueue.isEmpty()){
        	
            InitialBuildFinished = true;
//            FileUtils.appendTextToFile("log.txt", "\n updateInitialBuild end ==>> " + InitialBuildFinished);
        }
        
        if(InitialBuildFinished == false) {
        	
        	System.out.println("nowMarine ==>> " + nowMarine + " / orderMarine ==>> " + orderMarine);
        	
        	BuildOrderQueue iq = BuildManager.Instance().buildQueue;
        	
        	nowMarine = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Marine);
        	
        	
//        	20180721. hkk
//        	마린을 프레임돌때마다 큐에 넣는것이 아니고. 생산이 될때마다 큐에 추가.
//        	현재마린(nowMarine) 과 생산명령을 마린 숫자(orderMarine)를 비교
//        	빌드큐에 넣으면 orderMarine++ 을 하고 현재 마린 숫자가 그것과 같아지면 다시 큐에 넣는다.
        	if(nowMarine == orderMarine) {
        	
		        if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Marine, null)
						+ ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Marine, null) 
						+ nowMarine < StrategyIdea.marineCount
		        		&& Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) > 0) {
		        	
		        		iq.queueAsHighestPriority(UnitType.Terran_Marine, false);
		        		orderMarine++;
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
}