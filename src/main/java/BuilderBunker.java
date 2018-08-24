

import bwapi.Race;
import bwapi.UnitType;

public class BuilderBunker extends DefaultBuildableItem {
	
	private int entranceOpenFrame = CommonCode.NONE;
	private int bunkerBuildLeftSecond = CommonCode.NONE;

    public BuilderBunker(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 3) {
    		return false;
    	}
		
		int bunkerWillBeCount = UnitUtils.hasUnitOrWillBeCount(UnitType.Terran_Bunker);
		if (InfoUtils.enemyRace() == Race.Zerg) {
			if (bunkerWillBeCount <= 1
					&& StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.ZERG_FAST_ALL_IN)
					&& (StrategyIdea.campType == PositionFinder.CampType.INSIDE || StrategyIdea.campType == PositionFinder.CampType.FIRST_CHOKE)
					&& !UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Siege_Tank_Tank_Mode)) {
				setBlocking(true);
				setHighPriority(true);
				setTilePosition(BlockingEntrance.Instance().bunker2);
				return true;
			}
			
		} else if (bunkerWillBeCount >= 1) {
			return false;
		}

		if (InfoUtils.enemyRace() == Race.Protoss) {
			if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_INIT
					|| (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_FAST_DARK && UnitUtils.myFactoryUnitSupplyCount() <= 2 * 4)
					|| StrategyIdea.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.TWOGATE)) {
				int waitingBlockSeconds = 12;
				if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(EnemyStrategyOptions.BuildTimeMap.Feature.TWOGATE)) {
					waitingBlockSeconds = 8;
				}
				boolean entranceBlocked = InformationManager.Instance().isBlockingEnterance();
				if (entranceBlocked) {
					entranceOpenFrame = CommonCode.UNKNOWN;
					bunkerBuildLeftSecond = CommonCode.UNKNOWN;
				} else {
					if (entranceOpenFrame == CommonCode.UNKNOWN) {
						entranceOpenFrame = TimeUtils.elapsedFrames();
					}
					
					int leftSeconds = Math.max(waitingBlockSeconds - TimeUtils.elapsedSeconds(entranceOpenFrame), 0);
					if (bunkerBuildLeftSecond == CommonCode.UNKNOWN || leftSeconds < bunkerBuildLeftSecond) {
						bunkerBuildLeftSecond = leftSeconds;
						if (bunkerBuildLeftSecond == 0) {
							//MyBotModule.Broodwar.sendText("Bunker.");
						} else {
							//MyBotModule.Broodwar.sendText("" + bunkerBuildLeftSecond);
						}
					}
				}
			}
			
			if (bunkerBuildLeftSecond == 0) {
				setBlocking(true);
				setHighPriority(true);
				setTilePosition(BlockingEntrance.Instance().bunker1);
				return true;
			}
			
		} else if (InfoUtils.enemyRace() == Race.Terran) {
			if (StrategyIdea.currentStrategy == EnemyStrategy.TERRAN_BBS || StrategyIdea.currentStrategy == EnemyStrategy.TERRAN_2BARRACKS) {
				if (!UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Factory)) {
					setBlocking(true);
					setHighPriority(true);
					setTilePosition(BlockingEntrance.Instance().bunker1);
					return true;
				}
				
			} else {
//				if (InitialBuildProvider.Instance().getAdaptStrategyStatus() != AdaptStrategyStatus.BEFORE) {
				if (UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Vulture)) {
					setBlocking(true);
					setHighPriority(true);
					setTilePosition(BlockingEntrance.Instance().bunker2);
					return true;
				}
			}
		}
		
		return false;
    }
    
    @Override
    public boolean checkInitialBuild(){
    	return true;
    }
}
