package prebot.build.provider.items.building;

import bwapi.Race;
import bwapi.UnitType;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;

public class BuilderBunker extends DefaultBuildableItem {
	
	private int entranceOpenFrame = CommonCode.NONE;
	private int bunkerBuildLeftSecond = CommonCode.NONE;

    public BuilderBunker(MetaType metaType){
        super(metaType);
    }

    public final boolean buildCondition(){
		// TODO
    	// inital에 들어가야 하고 들어가야 한다.
    	// 입구가 막히지 않았을 때 건설. 케이스별 생각해볼게 있음
    	
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Barracks) == 0) {
    		return false;
    	}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Vulture) >= 3) {
    		return false;
    	}
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Bunker)) {
			return false;
		}

		if (InfoUtils.enemyRace() == Race.Protoss) {
			if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_INIT
					|| StrategyIdea.buildTimeMap.featureEnabled(Feature.TWOGATE)) {
				int waitingBlockSeconds = 8;
				if (StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.TWOGATE)) {
					waitingBlockSeconds = 5;
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
							Prebot.Broodwar.sendText("Bunker.");
						} else {
							Prebot.Broodwar.sendText("" + bunkerBuildLeftSecond);
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
