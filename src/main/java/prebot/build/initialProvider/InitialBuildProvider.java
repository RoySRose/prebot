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
import prebot.common.util.FileUtils;
import prebot.strategy.InformationManager;

/// 봇 프로그램 설정
public class InitialBuildProvider {

	private static InitialBuildProvider instance = new InitialBuildProvider();
	
	public static InitialBuildProvider Instance() {
		return instance;
	}

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