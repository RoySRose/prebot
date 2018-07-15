package prebot.build.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.rmi.activation.ActivationGroup_Stub;
import java.util.List;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.initialProvider.InitialBuildProvider;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.BuildOrderItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.*;
//import prebot.common.util.UnitUtils;

public abstract class DefaultBuildableItem implements BuildableItem{
	
	

    public final MetaType metaType;

    private int recoverItemCount=-1;
    BuildCondition buildCondition;
    private final UnitType producerOfUnit;

    //5개의 set 함수가 조건을 결정. 기본은 default, set 했을때는 해당 조건 반영
    //4개는 build condition 안에 들어가면 될듯
    public final void setBlocking(boolean blocking) {
        this.buildCondition.blocking = blocking;
    }

    public final void setHighPriority(boolean highPriority) {
        this.buildCondition.highPriority = highPriority;
    }

    public final void setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy seedPositionStrategy) {
    	this.buildCondition.seedPositionStrategy = seedPositionStrategy;
    }

    public final void settilePosition(TilePosition tilePosition) {
    	this.buildCondition.tilePosition = tilePosition;
    }

    //이놈만 유닛 변경 있을때만 확인해 주면 될듯
    public void setRecoverItemCount(int recoverItemCountVar) {
        this.recoverItemCount = recoverItemCount;
    }

    public DefaultBuildableItem(MetaType metaType) {
        buildCondition = new BuildCondition(false, false, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, TilePosition.None);
        this.metaType = metaType;
        //setProducerOfUnit();
        if(metaType.isUnit() && (!metaType.getUnitType().isBuilding() || metaType.getUnitType().isAddon())) {
            producerOfUnit = metaType.getUnitType().whatBuilds().first;
        }else{
            producerOfUnit = null;
        }
    }

    private final void build(){
    	//FileUtils.appendTextToFile("log.txt", "\n test log build()");

        //if(!metaType.isUnit() && (!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation) || !buildCondition.tilePosition.equals(TilePosition.None))) {
    	if(!metaType.isUnit() && 
    			(buildCondition.seedPositionStrategy != BuildOrderItem.SeedPositionStrategy.NoLocation
    			|| buildCondition.tilePosition != TilePosition.None)
    		) {
            System.out.println("Only UnitType can have position attribute");
        }
        //when blocking is false check resource
        if(buildCondition.blocking == false){
        	//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition.blocking == false && metaType ==>> " + metaType.getName());
        	/*FileUtils.appendTextToFile("log.txt", "\n metaType.mineralPrice() : " + metaType.mineralPrice()
        										+ "  /  Prebot.minerals() : " + Prebot.Broodwar.self().minerals()
        										+ "  /  metaType.gasPrice() : " + metaType.gasPrice()
        										+ "  /  Prebot.gas() : " + Prebot.Broodwar.self().gas());*/
            if(metaType.mineralPrice() <= Prebot.Broodwar.self().minerals() && metaType.gasPrice() <= Prebot.Broodwar.self().gas()){
            	//FileUtils.appendTextToFile("log.txt", "\n test log enough mineral == false");
                setBuildQueue();
            }
        }else{
        	//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition.blocking != false");
            setBuildQueue();
        }
    }

    private final void setBuildQueue(){
    	
    	/*if(buildCondition.tilePosition != null) {
    		System.out.println("\n test log ==>>> " + buildCondition.tilePosition.toString());
    		//FileUtils.appendTextToFile("log.txt", "\n test log ==>>> " + buildCondition.tilePosition.toString());

    	}else {
    		System.out.println("buildCondition.tilePosition ==>>> null");
    		//FileUtils.appendTextToFile("log.txt", "\nbuildCondition.tilePosition ==>>> null");
    		buildCondition.tilePosition = TilePosition.None;
    		//FileUtils.appendTextToFile("log.txt", "\nafter input buildCondition.tilePosition ==>>> None ==> " + buildCondition.tilePosition.getLength());
    	}*/
    	//FileUtils.appendTextToFile("log.txt", "\n test log setBuildQueue()==>>> " + metaType.getName());
        if(buildCondition.highPriority){
        	
        	//FileUtils.appendTextToFile("log.txt", "\n test log setBuildQueue() OF buildCondition.highPriority TRUE==>>> " + metaType.getName());
            //if(!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation)){
        	if(buildCondition.seedPositionStrategy != BuildOrderItem.SeedPositionStrategy.NoLocation){
        		//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition.seedPositionStrategy != BuildOrderItem.SeedPositionStrategy.NoLocation ");
                //BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType.getUnitType(), buildCondition.seedPositionStrategy, buildCondition.blocking);
        		BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType, buildCondition.seedPositionStrategy, buildCondition.blocking);
            //}else if(!buildCondition.tilePosition == TilePosition.None){
            }else if(buildCondition.tilePosition != TilePosition.None){
            	//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition.tilePosition != TilePosition.None ");
                BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType, buildCondition.tilePosition, buildCondition.blocking);
            }else{
                BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType, buildCondition.blocking);
                //FileUtils.appendTextToFile("log.txt", "\n test log ELSE!!!!!!!!!!!!!!!!!! " + metaType.getUnitType());
                //FileUtils.appendTextToFile("log.txt", "\n test log ELSE!!!!!!!!!!!!!!!!!! " + buildCondition.blocking);
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType, buildCondition.blocking);
            }
        }else {
        	//FileUtils.appendTextToFile("log.txt", "\n test log setBuildQueue() OF buildCondition.highPriority FALSE==>>> " + metaType.getName());
            //if(!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation)){
        	if(buildCondition.seedPositionStrategy != BuildOrderItem.SeedPositionStrategy.NoLocation){
        		//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition.seedPositionStrategy != BuildOrderItem.SeedPositionStrategy.NoLocation ");
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType, buildCondition.seedPositionStrategy, buildCondition.blocking);
            //}else if(!buildCondition.tilePosition.equals(TilePosition.None)){
            }else if(buildCondition.tilePosition != TilePosition.None){
            	//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition.tilePosition != TilePosition.None ");
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType, buildCondition.tilePosition, buildCondition.blocking);
            }else{
            	//FileUtils.appendTextToFile("log.txt", "\n test log ELSE!!!!!!!!!!!!!!!!!! ");
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType, buildCondition.blocking);
            }
        }
    }

    public final void process(){
    	
    	//FileUtils.appendTextToFile("log.txt", "\n test log process()==>>> " + metaType.getName());

        setDefaultConditions();
        
        ////FileUtils.appendTextToFile("log.txt", "\n build()'s chk_test ==>>> " + buildCondition.tilePosition);

        if(satisfyBasicConditions()){
            if(activateRecovery()){
                //if(activateRecovery() && !baseIsAttacked){ // should we recover when base is attacked?
                build();
            }else{
                if(buildCondition()) {
                	//FileUtils.appendTextToFile("log.txt", "\n test log buildCondition return true");
                    build();
                }
            }
        }
    }

    public final boolean activateRecovery(){

        if(!metaType.isUnit()){
            return false;
        }
        if(recoverItemCount == -1){
            return false;
        }

        if(recoverItemCount > getCurrentItemCount()){
            return true;
        }else{
            return false;
        }
    }

    protected int getCurrentItemCount(){
        int currentItemCount = BuildManager.Instance().buildQueue.getItemCount(metaType.getUnitType()) +
                Prebot.Broodwar.self().allUnitCount(metaType.getUnitType());
        return currentItemCount;
    }

    private final void setDefaultConditions(){
        this.buildCondition.blocking = false;
        this.buildCondition.highPriority = false;
        this.buildCondition.seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
        this.buildCondition.tilePosition = TilePosition.None;
    }

    private final boolean satisfyBasicConditions(){

        if(!checkInitialBuild()) {
            return false;
        }
        //For units check supply
        if(!checkSupplyForUnit()){
            return false;
        }
        //For units check Producers
        if(!checkProducerOfUnit()){
            return false;
        }

        return true;
    }

    public boolean checkInitialBuild(){
        if(!InitialBuildProvider.Instance().InitialBuildFinished){
            return false;
        }
        return true;
    }

    private final boolean checkSupplyForUnit(){
        if(metaType.supplyRequired() > Prebot.Broodwar.self().supplyTotal() -  Prebot.Broodwar.self().supplyUsed()){
            return false;
        }
        return true;
    }

    private final boolean checkProducerOfUnit(){
        if(metaType.isUnit() && !metaType.getUnitType().isBuilding()){
            int availableProducer = 0;
            List<Unit> producerList= UnitUtils.getUnitList(UnitFindRange.COMPLETE, producerOfUnit);

            for(Unit producer : producerList) {
                //TODO check if addon is on the middle of construction
                //if(producer.isTraining() == false && ((producer.getAddon() != null && producer.getAddon().isCompleted() == true) || producer.getAddon() == null)
                if(producer.isTraining() == false && producer.isConstructing() == false && producer.isResearching() == false && producer.isUpgrading() == false) {
                    availableProducer++;
                }
            }

            if(availableProducer == 0){
                return false;
            }
        }
        return true;
    }


}
