package prebot.brain.squad;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.main.manager.WorkerManager;

public class NewSquads {

	public static final class ScvScoutSquad extends Squad {
		public ScvScoutSquad() {
			super(2, 100);
		}

		@Override
		public boolean want(Unit unit) {
			if (unit.getType() != UnitType.Terran_SCV) {
				return false;
			}
			return WorkerManager.Instance().isScoutWorker(unit);
		}

		@Override
		public void execute() {
		}
	}
}
