package prebot.brain.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.information.UnitInfo;
import prebot.main.manager.InformationManager;
import prebot.main.manager.WorkerManager;
import prebot.micro.control.TestControl;

public class Squads {
	
	public static final class IdleSquad extends Squad {
		public IdleSquad() {
			super("IDLE_SQUAD", 0);
		}

		@Override
		public boolean want(Unit unit) {
			return true;
		}

		@Override
		public void findEnemies() {
		}

		@Override
		public void execute() {
		}
	}
	
	
	public static final class MainSquad extends Squad {
		private TestControl testControl = new TestControl();
		
		public MainSquad() {
			super("MAIN_SQUAD", 1);
		}

		@Override
		public boolean want(Unit unit) {
			return !unit.getType().isWorker();
		}

		@Override
		public void findEnemies() {
			List<UnitInfo> enemyUnitInfoList = new ArrayList<>();
			for (Unit unit : unitList) {
//				InformationManager.Instance().getNearbyForce(enemyUnitInfoList, unit.getPosition(), InformationManager.Instance().enemyPlayer, 300);
			}
		}

		@Override
		public void execute() {
			testControl.prepare(goalPosition, unitList, enemyUnitInfoList);
			
			for (Unit unit : unitList) {
				testControl.control(unit);
			}
		}
	}
	
	public static final class ScvScoutSquad extends Squad {
		public ScvScoutSquad() {
			super("SCOUT_SQUAD", 2);
		}

		@Override
		public boolean want(Unit unit) {
			if (unit.getType() != UnitType.Terran_SCV) {
				return false;
			}
			return WorkerManager.Instance().isScoutWorker(unit);
		}

		@Override
		public void findEnemies() {
		}

		@Override
		public void execute() {
		}
	}

}
