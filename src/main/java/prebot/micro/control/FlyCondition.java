package prebot.micro.control;

import bwapi.Position;
import bwapi.TilePosition;

public class FlyCondition{


    private TilePosition landPosition;
    private Position flyPosition;
    private BuildingFly buildingFly;
    private boolean flyAlways;
    private boolean isGateway;

    public FlyCondition(boolean flyAlways, boolean isGateway, TilePosition tilePosition) {
        this.flyAlways = flyAlways;
        this.isGateway = isGateway;
        if(isGateway){
            if(tilePosition == null){
                System.out.println("TilePosition must not be null");
            }
            this.landPosition = tilePosition;
        }else {
            this.landPosition = TilePosition.None;
        }
        this.flyPosition = Position.None;
    }

    public FlyCondition() {
        this.flyAlways = false;
        this.isGateway = false;
        this.landPosition = TilePosition.None;
        this.flyPosition = Position.None;
    }

    public TilePosition getLandPosition() {
        return landPosition;
    }

    public void setLandPosition(TilePosition landPosition) {
        this.landPosition = landPosition;
    }

    public Position getFlyPosition() {
        return flyPosition;
    }

    public void setFlyPosition(Position flyPosition) {
        this.flyPosition = flyPosition;
    }

    public BuildingFly getBuildingFly() {
        return buildingFly;
    }

    public void setBuildingFly(BuildingFly buildingFly) {
        this.buildingFly = buildingFly;
    }

    public boolean isFlyAlways() {
        return flyAlways;
    }

    public void setFlyAlways(boolean flyAlways) {
        this.flyAlways = flyAlways;
    }

    public boolean isGateway() {
        return isGateway;
    }

    public void setGateway(boolean gateway) {
        isGateway = gateway;
    }

    @Override
    public String toString() {
        return "FlyCondition{" +
                "landPosition=" + landPosition +
                ", flyPosition=" + flyPosition +
                ", buildingFly=" + buildingFly +
                ", flyAlways=" + flyAlways +
                ", isGateway=" + isGateway +
                '}';
    }
}
