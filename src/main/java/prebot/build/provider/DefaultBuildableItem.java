package prebot.build.provider;

import bwapi.Game;
import bwapi.Pair;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.information.UnitInfo;
import prebot.build.BuildOrderItem;
import prebot.build.MetaType;
import prebot.build.manager.BuildManager;
import prebot.common.code.Code;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;

import java.util.List;

public abstract class DefaultBuildableItem implements BuildableItem{

    public final MetaType metaType;

    private int recoverItemCount=-1;
    BuildCondition buildCondition;
    private final UnitType producerOfUnit;
    
//    private boolean blocking;
//    private boolean highPriority; //high, low; // needed?
//    private BuildOrderItem.SeedPositionStrategy seedPositionStrategy;
//    private TilePosition tilePostition;


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
    public final void setRecoverItemCount(int recoverItemCount) {
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
            if(metaType.mineralPrice() >= Prebot.Game.self().minerals() && metaType.gasPrice() >= Prebot.Game.self().gas()){
                setBuildQueue();
            }
        }else{
            setBuildQueue();
        }
    }

    private final void setBuildQueue(){
        if(buildCondition.highPriority){

            if(!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation)){
                BuildManager.Instance().buildQueue.qHigh(metaType.getUnitType(), buildCondition.seedPositionStrategy, buildCondition.blocking);
            }else if(!buildCondition.tilePostition.equals(TilePosition.None)){
                BuildManager.Instance().buildQueue.qHigh(metaType.getUnitType(), buildCondition.tilePostition, buildCondition.blocking);
            }else{
                BuildManager.Instance().buildQueue.qHigh(metaType, buildCondition.blocking);
            }
        }else {
            if(!buildCondition.seedPositionStrategy.equals(BuildOrderItem.SeedPositionStrategy.NoLocation)){
                BuildManager.Instance().buildQueue.qLow(metaType.getUnitType(), buildCondition.seedPositionStrategy, buildCondition.blocking);
            }else if(!buildCondition.tilePostition.equals(TilePosition.None)){
                BuildManager.Instance().buildQueue.qLow(metaType.getUnitType(), buildCondition.tilePostition, buildCondition.blocking);
            }else{
                BuildManager.Instance().buildQueue.qLow(metaType, buildCondition.blocking);
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
                Prebot.Game.self().allUnitCount(metaType.getUnitType());
        return currentItemCount;
    }

    private final void setDefaultConditions(){
        this.buildCondition.blocking = false;
        this.buildCondition.highPriority = false;
        this.buildCondition.seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.NoLocation;
        this.buildCondition.tilePostition = TilePosition.None;
    }

    private final boolean satisfyBasicConditions(){

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

    private final boolean checkSupplyForUnit(){
        if(metaType.supplyRequired() > Prebot.Game.self().supplyTotal() -  Prebot.Game.self().supplyUsed()){
            return false;
        }
        return true;
    }

    private final boolean checkProducerOfUnit(){
        if(metaType.isUnit() && !metaType.getUnitType().isBuilding()){
            
            int availableProducer = 0;
            List<Unit> producerList= UnitUtils.getUnitList(producerOfUnit, Code.UnitFindRange.COMPLETE);

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
