package prebot.brain.squad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.UnitUtils;
import prebot.micro.control.GoliathDefense;
import prebot.micro.control.TankDefense;
import prebot.micro.control.VultureChecker;
import prebot.micro.control.VultureDefense;
import prebot.micro.control.VultureWatcher;

public class DefaultSquads {
	
	public static final class IdleSquad extends Squad {
		public IdleSquad() {
			super(0, 0);
		}

		@Override
		public boolean want(Unit unit) {
			return true;
		}

		@Override
		public void execute() {
		}
	}
	
	
	public static final class MainDefenseSquad extends Squad {
		private VultureDefense vultureDefense = new VultureDefense();
		private TankDefense tankDefense = new TankDefense();
		private GoliathDefense goliathDefense = new GoliathDefense();
		
		public MainDefenseSquad() {
			super(1, 100);
		}

		@Override
		public boolean want(Unit unit) {
			return unit.getType() == UnitType.Terran_Vulture
					|| unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
					|| unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
					|| unit.getType() == UnitType.Terran_Goliath;
		}

		@Override
		public void execute() {
			List<Unit> vultureList = new ArrayList<>();
			List<Unit> tankList = new ArrayList<>();
			List<Unit> goliathList = new ArrayList<>();
			for (Unit unit : unitList) {
				if (unit.getType() == UnitType.Terran_Vulture) {
					vultureList.add(unit);
				} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					tankList.add(unit);
				} else if (unit.getType() == UnitType.Terran_Goliath) {
					goliathList.add(unit);
				}
			}
			
			vultureDefense.prepare(vultureList, euiList);
			tankDefense.prepare(tankList, euiList);
			goliathDefense.prepare(goliathList, euiList);
			
			vultureDefense.control();
			tankDefense.control();
			goliathDefense.control();
			
		}
	}
	
	public static final class WatcherSquad extends Squad {
		private VultureWatcher vultureWatcher = new VultureWatcher();
		
		public WatcherSquad() {
			super(2, 100);
		}

		@Override
		public boolean want(Unit unit) {
			return unit.getType() == UnitType.Terran_Vulture;
		}

		@Override
		public void execute() {
			if (!unitList.isEmpty()) {
				vultureWatcher.prepare(unitList, euiList);
				vultureWatcher.control();
			}
		}
	}
	
	public static final class CheckerSquad extends Squad {
		private VultureChecker vultureWatcher = new VultureChecker();

		private boolean recruitEnded = false;
		private Set<Integer> memberIdSet = new HashSet<>();
		
		public CheckerSquad() {
			super(3, 100);
		}

		@Override
		public boolean want(Unit unit) {
			if (unit.getType() != UnitType.Terran_Vulture) {
				return false;
			}
			
			if (!recruitEnded) {
				this.recruit();
				recruitEnded = true;
			}
			
			return memberIdSet.contains(unit.getID());
		}

		/// checker squad는 매 frame 1회 용감한 checker부대원을 모집한다.
		/// 스파이더마인을 많이 보유한 벌처의 우선순위가 높다.
		private void recruit() {
			memberIdSet.clear();
			for (Unit scv : unitList) {
				memberIdSet.add(scv.getID());
			}
			
			int openingCount = Idea.of().checkerMaxCount - unitList.size();
			if (openingCount <= 0) {
				return;
			}
			
			// 보유마인 개수별로 벌처 분류
			Map<Integer, List<Integer>> vultureIdByMineCount = new HashMap<>();
			for (Unit vulture : UnitUtils.getUnitList(UnitType.Terran_Vulture, UnitFindRange.COMPLETE)) {
				List<Integer> vultureList = vultureIdByMineCount.get(new Integer(vulture.getSpiderMineCount()));
				if (vultureList == null) {
					vultureList = new ArrayList<Integer>();
				}
				vultureList.add(vulture.getID());
				vultureIdByMineCount.put(new Integer(vulture.getSpiderMineCount()), vultureList);
			}
			// 마인이 많은 순서대로 벌처 할당
			for (int mineCount = 3; mineCount >= 0; mineCount--) {
				List<Integer> vultureIds = vultureIdByMineCount.get(new Integer(mineCount));
				for (Integer vultureId : vultureIds) {
					memberIdSet.add(vultureId);
					openingCount--;
					if (openingCount == 0) {
						break;
					}
				}
				if (openingCount == 0) {
					break;
				}
			}
		}

		@Override
		public void execute() {
			vultureWatcher.prepare(unitList, euiList);
			vultureWatcher.control();
			
			recruitEnded = false; // 다음 frame에는 다시 모집
		}
	}

}
