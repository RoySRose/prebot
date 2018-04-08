package prebot.brain.squad;

import bwapi.Unit;
import bwta.BaseLocation;
import prebot.common.util.CommandUtils;
import prebot.main.PreBot;
import prebot.manager.InformationManager;

public class Squads {
	
	public static final class IdleSquad extends Squad {

		public IdleSquad() {
			super("IDLE_SQUAD", 0);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean want(Unit unit) {
			return true;
		}

		@Override
		public boolean execute() {
			return false;
		}
	}
	
	
	public static final class MainSquad extends Squad {

		public MainSquad() {
			super("MAIN_SQUAD", 1);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean want(Unit unit) {
			return !unit.getType().isWorker();
		}

		@Override
		public boolean execute() {
			BaseLocation base = InformationManager.Instance().getMainBaseLocation(PreBot.Broodwar.self());
			for (Unit unit : unitList) {
				CommandUtils.attackMove(unit, base.getPosition());
			}
			return true;
		}
		
	}

}
