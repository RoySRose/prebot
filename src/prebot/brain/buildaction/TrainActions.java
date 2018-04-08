package prebot.brain.buildaction;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.code.GameConstant;
import prebot.common.util.UnitUtils;
import prebot.manager.build.BuildOrderItem;
import prebot.manager.build.MetaType;

public class TrainActions {
	
	/**
	 * BuildOrderItem : SCV
	 * 
	 * 1. 생산할 자원과 서플라이가 있어야 함
	 * 2. 현재 일꾼 수가 일꾼 max를 초과하면 안됨
	 *    : 일꾼 max = 각 커맨드센터 근처의 미네랄 수 x 2 (단, 커맨드센터가 완성되지 않았으면 체력대비 미네랄 수를 적게 카운트)
	 * 3. 가동중이지 않은 커맨드센터가 있어야 한다.
	 */
	public static final class TrainSCV extends BuildAction {
		
		public TrainSCV() {
			super(new BuildOrderItem(new MetaType(UnitType.Terran_SCV), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, 0, true, -1));
		}
		
		@Override
		public boolean buildCondition() {
			// 1. 생산할 자원과 서플라이가 있어야 함
			if (!UnitUtils.isProduceableImmediately(UnitType.Terran_SCV)) {
				return false;
			}
			
			// 2. 현재 일꾼 수가 일꾼 max를 초과하면 안됨
			int currentCount = UnitUtils.getUnitCount(UnitType.Terran_SCV, UnitFindRange.ALL);
			int maxCount = getMaxScvCount();
			if (currentCount >= maxCount) {
				return false;
			}
			
			// 3. 가동중이지 않은 커맨드센터가 있어야 한다.
			boolean existIdleCommandCenter = false;
			for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE)) {
				if (!commandCenter.isTraining() && commandCenter.getHitPoints() > GameConstant.UNIT_PRODUCE_HITPOINT) {
					existIdleCommandCenter = true;
					break;
				}
			}
			if (!existIdleCommandCenter) {
				return false;
			}
			
			return true;
		}
		
		private int getMaxScvCount() {
			// 미네랄 수(selfMineralCount) = 커맨드센터 근처 미네랄 (미완성 커맨드센터는 체력대비 적게 계산)
			int selfMineralCount = 0;
			for (Unit commandCenter : UnitUtils.getUnitList(UnitType.Terran_Command_Center, UnitFindRange.ALL)) {
				// getMineralsNearDepot 메소드의 미네랄 검색 범위에 따라 중복으로 카운트 될 수 있음
				int minerals = UnitUtils.getNearMineralsCount(commandCenter);
				if (minerals > 0) {
					if (!commandCenter.isCompleted()) {
						// 완성되지 않은 resource depot이면 체력비율로 미네랄 수를 적게 카운트
						double completionDegree = commandCenter.getHitPoints() / commandCenter.getType().maxHitPoints();
						minerals = (int) (minerals * completionDegree);
					}
					selfMineralCount += minerals;
				}
			}
			
			//TODO 아래 계산식은 오류로 보임. selfMineralCount * 2로 변경함. 테스트 필요. 
			//selfMineralCount * 2 + 8 * PreBot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center);
			return Math.min(GameConstant.MAX_WORKER_COUNT, selfMineralCount * 2);
		}
	}
	
	/**
	 * BuildOrderItem : Vulture
	 * 
	 * 전략별 비율에 따른 FactoryUnitSelector에 의해 선택된 유닛을 생산한다.
	 */
	public static final class TrainVulture extends BuildAction {
		private FactoryUnitSelector selector;
		
		public TrainVulture(FactoryUnitSelector selector) {
			super(new BuildOrderItem(new MetaType(UnitType.Terran_Vulture), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, 0, true, -1));
			this.selector = selector;
		}

		@Override
		public boolean buildCondition() {
			return selector.selectFactoryUnit() == super.getBuildType();
		}
	}
	
	/**
	 * BuildOrderItem : TrainTank
	 * 
	 * 전략별 비율에 따른 FactoryUnitSelector에 의해 선택된 유닛을 생산한다.
	 */
	public static final class TrainTank extends BuildAction {
		private FactoryUnitSelector selector;
		
		public TrainTank(FactoryUnitSelector selector) {
			super(new BuildOrderItem(new MetaType(UnitType.Terran_Siege_Tank_Tank_Mode), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, 0, true, -1));
			this.selector = selector;
		}

		@Override
		public boolean buildCondition() {
			return selector.selectFactoryUnit() == super.getBuildType();
		}
	}
	
	/**
	 * BuildOrderItem : TrainGoliath
	 * 
	 * 전략별 비율에 따른 FactoryUnitSelector에 의해 선택된 유닛을 생산한다.
	 */
	public static final class TrainGoliath extends BuildAction {
		private FactoryUnitSelector selector;
		
		public TrainGoliath(FactoryUnitSelector selector) {
			super(new BuildOrderItem(new MetaType(UnitType.Terran_Goliath), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, 0, true, -1));
			this.selector = selector;
		}

		@Override
		public boolean buildCondition() {
			return selector.selectFactoryUnit() == super.getBuildType();
		}
	}

}
