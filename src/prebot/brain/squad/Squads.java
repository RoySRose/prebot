package prebot.brain.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.micro.control.GoliathDefense;
import prebot.micro.control.TankDefense;
import prebot.micro.control.VultureChecker;
import prebot.micro.control.VultureDefense;
import prebot.micro.control.VultureWatcher;

public class Squads {
	
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
			for (Unit unit : totalUnitList) {
				if (unit.getType() == UnitType.Terran_Vulture) {
					vultureList.add(unit);
				} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					tankList.add(unit);
				} else if (unit.getType() == UnitType.Terran_Goliath) {
					goliathList.add(unit);
				}
			}
			
			vultureDefense.prepare(vultureList, enemyUnitInfoList);
			tankDefense.prepare(tankList, enemyUnitInfoList);
			goliathDefense.prepare(goliathList, enemyUnitInfoList);
			
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
			vultureWatcher.prepare(totalUnitList, enemyUnitInfoList);
			vultureWatcher.control();
		}
	}
	
	public static final class CheckerSquad extends Squad {
		private VultureChecker vultureWatcher = new VultureChecker();
		
		public CheckerSquad() {
			super(3, 100);
		}

		@Override
		public boolean want(Unit unit) {
			return unit.getType() == UnitType.Terran_Vulture;
		}

		@Override
		public void execute() {
			vultureWatcher.prepare(totalUnitList, enemyUnitInfoList);
			vultureWatcher.control();
		}
	}

}
