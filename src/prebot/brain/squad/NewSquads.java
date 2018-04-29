package prebot.brain.squad;

import java.util.HashSet;
import java.util.Set;

import bwapi.Unit;
import bwapi.UnitType;
import bwta.Chokepoint;
import prebot.brain.Idea;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;
import prebot.main.manager.InformationManager;
import prebot.main.manager.WorkerManager;
import prebot.micro.control.ScvScout;

public class NewSquads {

	public static final class ScvScoutSquad extends Squad {
		private ScvScout scvScout = new ScvScout();

		private boolean recruitEnded = false;
		private Set<Integer> memberIdSet = new HashSet<>();

		public ScvScoutSquad() {
			super(2, 100);
		}

		@Override
		public boolean want(Unit unit) {
			if (unit.getType() != UnitType.Terran_SCV) {
				return false;
			}
			if (!recruitEnded) {
				this.recruit();
				recruitEnded = true;
			}

			return memberIdSet.contains(unit.getID());
		}

		private void recruit() {
			memberIdSet.clear();
			for (Unit scv : unitList) {
				memberIdSet.add(scv.getID());
			}
			
			int openingCount = Idea.of().scvScoutMaxCount - unitList.size();
			if (openingCount <= 0) {
				return;
			}

			Chokepoint firstChoke = InformationManager.Instance().getFirstChokePoint(Prebot.Game.self());
			Unit scvNearFirstChoke = UnitUtils.getClosestMineralWorkerToPosition(WorkerManager.Instance().getWorkerData().getWorkers(), firstChoke.getCenter());
			if (scvNearFirstChoke != null) {
				memberIdSet.add(scvNearFirstChoke.getID());
			}
		}

		@Override
		public void execute() {
			scvScout.prepare(unitList, euiList);
			scvScout.control();

			recruitEnded = false; // 다음 frame에는 다시 모집
		}
	}
}
