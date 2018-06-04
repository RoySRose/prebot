package prebot.build.provider;

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
import prebot.common.util.UnitUtils;

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

    public final void setTilePostition(TilePosition tilePostition) {
        this.buildCondition.tilePostition = tilePostition;
    }

    //이놈만 유닛 변경 있을때만 확인해 주면 될듯
    public void setRecoverItemCount(int recoverItemCount) {
        this.recoverItemCount = recoverItemCount;
    }

    public DefaultBuildableItem(MetaType metaType) {
        buildCondition= new BuildCondition(false, false, BuildOrderItem.SeedPositionStrategy.NoLocation, TilePosition.None);
        this.metaType = metaType;
        //setProducerOfUnit();
        if(metaType.isUnit() && (!metaType.getUnitType().isBuilding() || metaType.getUnitType().isAddon())) {
            producerOfUnit = metaType.getUnitType().whatBuilds().first;
        }else{
            producerOfUnit = null;
        }
    }

    private final void build(){

        if(!metaType.isUnit() && (!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation) || !buildCondition.tilePostition.equals(TilePosition.None))) {
            System.out.println("Only UnitType can have position attribute");
        }
        //when blocking is false check resource
        if(buildCondition.blocking == false){
            if(metaType.mineralPrice() >= Prebot.Broodwar.self().minerals() && metaType.gasPrice() >= Prebot.Broodwar.self().gas()){
                setBuildQueue();
            }
        }else{
            setBuildQueue();
        }
    }

    private final void setBuildQueue(){
        if(buildCondition.highPriority){

            if(!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation)){
                BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType.getUnitType(), buildCondition.seedPositionStrategy, buildCondition.blocking);
            }else if(!buildCondition.tilePostition.equals(TilePosition.None)){
                BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType.getUnitType(), buildCondition.tilePostition, buildCondition.blocking);
            }else{
                BuildManager.Instance().buildQueue.queueAsHighestPriority(metaType, buildCondition.blocking);
            }
        }else {
            if(!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation)){
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType.getUnitType(), buildCondition.seedPositionStrategy, buildCondition.blocking);
            }else if(!buildCondition.tilePostition.equals(TilePosition.None)){
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType.getUnitType(), buildCondition.tilePostition, buildCondition.blocking);
            }else{
                BuildManager.Instance().buildQueue.queueAsLowestPriority(metaType, buildCondition.blocking);
            }
        }
    }

    public final void process(){

        setDefaultConditions();

        if(satisfyBasicConditions()){
            if(activateRecovery()){
                //if(activateRecovery() && !baseIsAttacked){ // should we recover when base is attacked?
                build();
            }else{
                if(buildCondition()) {
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
        this.buildCondition.seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.NoLocation;
        this.buildCondition.tilePostition = TilePosition.None;
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
