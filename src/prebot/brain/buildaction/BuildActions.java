package prebot.brain.buildaction;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.UnitUtils;
import prebot.main.PreBot;
import prebot.manager.build.BuildOrderItem;
import prebot.manager.build.MetaType;

public class BuildActions {

	/**
	 * BuildOrderItem : 서플라이 디팟
	 * 
	 * 1. 서플라이수를 200를 채웠다면 더이상 지을 필요 없음
	 * 2. 부족한 서플라이 수가 0이하라면 지을 필요 없음
	 *    : 부족한 서플라이 수 = 사용중서플라이수 + 여유서플라이수 - 현재서플라이수
	 *    : 여유서플라이수는 팩토리 대비 조정된다 -> 추가설명 필요
	 * 3. 건설 중인 서플라이까지 고려했을 때 부족하지 않으면 지을 필요 없음.
	 */
	public static final class BuildSupplyDepot extends BuildAction {

		public BuildSupplyDepot() {
			super(new BuildOrderItem(new MetaType(UnitType.Terran_Supply_Depot), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, 0, false, -1));
		}

		@Override
		public boolean buildCondition() {
			// 서플라이수를 200를 채웠다면 더이상 지을 필요 없음
			if (PreBot.Broodwar.self().supplyTotal() > 400) {
				return false;
			}

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼
			// 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int commandCenterCount = UnitUtils.getUnitCount(UnitType.Terran_Command_Center, UnitFindRange.COMPLETE);
			int factoryCount = UnitUtils.getUnitCount(UnitType.Terran_Factory, UnitFindRange.COMPLETE);

			int factoryOperatingCount = 0;
			for (Unit factory : UnitUtils.getUnitList(UnitType.Terran_Factory, UnitFindRange.COMPLETE)) {
				if (factory.isTraining()) {
					factoryOperatingCount++;
				}
			}

			// TODO 아래 계산식 최적화 필요. 일단 의미를 알기 어려워서 코드만으로 효과를 예상하기가 힘들다.
			int supplyMargin = 5;
			if (commandCenterCount == 1) {// TODO "이거 현재는 faccnt cccnt 기준 안 먹는다. 기준 다시 잡아야됨." 라고 주석으로 쓰여있었음.
				if (factoryCount > 0) {
					supplyMargin = 6 + 4 * factoryCount + factoryOperatingCount * 2;
				}
			} else {
				supplyMargin = 11 + 4 * factoryCount + factoryOperatingCount * 2;
			}

			// 2. 부족한 서플라이 수가 0이하라면 지을 필요 없음
			int currentSupplyShortage = PreBot.Broodwar.self().supplyUsed() + supplyMargin - PreBot.Broodwar.self().supplyTotal();
			if (currentSupplyShortage <= 0) {
				return false;
			}

			// 3. 건설 중인 서플라이까지 고려했을 때 부족하지 않으면 지을 필요 없음.
			int supplyDepotInQueue = UnitUtils.getUnitCount(UnitType.Terran_Supply_Depot, UnitFindRange.CONSTRUCTION_QUEUE);
			if (currentSupplyShortage <= supplyDepotInQueue * UnitType.Terran_Supply_Depot.supplyProvided()) {
				return false;
			}

			return true;
		}

	}
	
}
