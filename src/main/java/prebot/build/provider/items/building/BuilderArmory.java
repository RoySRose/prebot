package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.build.prebot1.BuildOrderItem;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.main.MyBotModule;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.EnemyBuildTimer;

public class BuilderArmory extends DefaultBuildableItem {

	public BuilderArmory(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
//		setRecoverItemCount(Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Armory));
		if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Terran_Factory) == 0) {
			return false;
		}
		
		if (UnitUtils.hasUnitOrWillBe(UnitType.Terran_Armory)) {
			return false;
		}

		setSeedPositionStrategy(BuildOrderItem.SeedPositionStrategy.NextSupplePoint);

		// 아머리가 조금 빨라야 하는 상황
		if (fastArmoryByStrategy()) {
			setBlocking(true);
			setHighPriority(true);
			return true;
		}
		
		// 긴급할 경우 자원 체크로직이 필요한가? 어차피 맨위 true로 올릴텐데?
		if (UnitUtils.enemyCompleteUnitDiscovered(
				UnitType.Protoss_Scout, UnitType.Protoss_Shuttle, UnitType.Protoss_Carrier,
				UnitType.Zerg_Mutalisk,
				UnitType.Terran_Dropship)) {
			setBlocking(true);
			setHighPriority(true);
//			setRecoverItemCount(1);
			return true;
		}
		
		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Terran_Wraith)) {
			if (!UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Wraith)) {
				setBlocking(true);
				setHighPriority(true);
			}
			return true;
		}

		if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Protoss_Arbiter, UnitType.Protoss_Arbiter_Tribunal)) {
			setBlocking(true);
			setHighPriority(true);
			return true;
		}

		if (TimeUtils.afterTime(9, 0)) {
			return true;
		}
		
		// 활성화 되기 전 2팩이상 가스 여유가 있으면 빠른  아머리
		int commandCenterCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center);
		int factoryCount = MyBotModule.Broodwar.self().allUnitCount(UnitType.Terran_Factory);
		if (commandCenterCount == 2 && factoryCount >= 2 && MyBotModule.Broodwar.self().gas() > 200) {
			return true;
		}

		// 활성화된 커맨드가 2개 이상일 경우
		return UnitUtils.activatedCommandCenterCount() >= 2;
	}
	
	private boolean fastArmoryByStrategy() {
		if (StrategyIdea.factoryRatio.goliath == 0) {
			return false;
		}
		
		// 발키리용 아머리 타이밍
		if (StrategyIdea.currentStrategy == EnemyStrategy.ZERG_VERY_FAST_MUTAL) {
			int compelteStarportCount = UnitUtils.getUnitCount(CommonCode.UnitFindRange.COMPLETE, UnitType.Terran_Starport);
			if (compelteStarportCount > 0) {
				return true;
			}
			
			List<Unit> incompleteStartports = UnitUtils.getUnitList(CommonCode.UnitFindRange.INCOMPLETE, UnitType.Terran_Starport);
			for (Unit starport : incompleteStartports) {
				int remainBuildSeconds = TimeUtils.remainBuildSeconds(starport);
				if (remainBuildSeconds != CommonCode.UNKNOWN && remainBuildSeconds + UnitType.Terran_Control_Tower.buildTime() < UnitType.Terran_Armory.buildTime()) {
					return true;						
				}
			}
		} else if (StrategyIdea.currentStrategy == EnemyStrategy.ZERG_FAST_MUTAL
				|| StrategyIdea.currentStrategy == EnemyStrategy.TERRAN_2STAR
				|| StrategyIdea.currentStrategy == EnemyStrategy.ZERG_LAIR_MIXED) {
			int armoryBuildStartFrame = CommonCode.UNKNOWN;
			if (InfoUtils.enemyRace() == Race.Zerg) {
				armoryBuildStartFrame = EnemyBuildTimer.Instance().mutaliskInMyBaseFrame - UnitType.Terran_Goliath.buildTime() - UnitType.Terran_Armory.buildTime();
			} else if (InfoUtils.enemyRace() == Race.Terran) {
				armoryBuildStartFrame = EnemyBuildTimer.Instance().cloakingWraithFrame - UnitType.Terran_Goliath.buildTime() - UnitType.Terran_Armory.buildTime();
			}
			if (armoryBuildStartFrame != CommonCode.UNKNOWN && TimeUtils.after(armoryBuildStartFrame)) {
				return true;
			}
			
		} else if (StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_STARGATE
				|| StrategyIdea.currentStrategy == EnemyStrategy.PROTOSS_DOUBLE_CARRIER) {
			boolean siegeModeTankDiscovered = UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Siege_Tank_Siege_Mode);
			boolean vultureDiscovered = UnitUtils.myCompleteUnitDiscovered(UnitType.Terran_Vulture);
			if (siegeModeTankDiscovered && vultureDiscovered) {
				return true;
			}
		}
		
		return false;
	}

}
