package prebot.build.initialProvider;


import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.initialProvider.buildSets.BaseBuild;
import prebot.build.initialProvider.buildSets.VsProtoss;
import prebot.build.initialProvider.buildSets.VsTerran;
import prebot.build.initialProvider.buildSets.VsZerg;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.prebot1.BuildOrderQueue;
import prebot.build.prebot1.ConstructionPlaceFinder;
import prebot.strategy.InformationManager;

/// 봇 프로그램 설정
public class InitialBuildProvider {

	private static InitialBuildProvider instance = new InitialBuildProvider();
	
	public static InitialBuildProvider Instance() {
		return instance;
	}

	public boolean InitialBuildFinished = false;

    public InitialBuildProvider(){
    	
    	System.out.println("InitialBuildProvider start");
    	 
        BlockingEntrance blockingEntrance = new BlockingEntrance();

        TilePosition firstSupplyPos = blockingEntrance.first_supple;
        TilePosition barrackPos = blockingEntrance.barrack;
        TilePosition secondSupplyPos = blockingEntrance.second_supple;
        TilePosition factoryPos = blockingEntrance.factory;
        TilePosition bunkerPos = blockingEntrance.bunker;
        //TilePosition entranceTurretPos = blockingEntrance.entrance_turret;

        if (InformationManager.Instance().enemyRace == Race.Terran) {
            new VsTerran(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
        }else if (InformationManager.Instance().enemyRace == Race.Protoss) {
        	System.out.println("ememy is protoss");
            new VsProtoss(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
        }else{
            new VsZerg(firstSupplyPos, barrackPos, secondSupplyPos, factoryPos, bunkerPos);
        }
    }

    public void updateInitialBuild(){
        if(BuildManager.Instance().buildQueue.isEmpty()){
            InitialBuildFinished = true;
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