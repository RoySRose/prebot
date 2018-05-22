package prebot.micro.squad.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.common.code.Code.SquadName;
import prebot.micro.control.VultureChecker;
import prebot.micro.squad.Squad;

public class CheckerSquad extends Squad {
	private VultureChecker vultureWatcher = new VultureChecker();

	public CheckerSquad(int priority, int squadRadius) {
		super(SquadName.CHECKER, priority, squadRadius);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Vulture;
	}

	/// checker squad는 매 frame 1회 용감한 checker부대원을 모집한다.
	/// 스파이더마인을 많이 보유한 벌처의 우선순위가 높다.
	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		int openingCount = Idea.of().checkerMaxCount - unitList.size();
		if (openingCount <= 0) {
			return Collections.emptyList();
		}
		
		// 보유마인 개수별로 벌처 분류
		Map<Integer, List<Unit>> vultureIdByMineCount = new HashMap<>();
		for (Unit vulture : assignableUnitList) {
			List<Unit> vultureList = vultureIdByMineCount.get(new Integer(vulture.getSpiderMineCount()));
			if (vultureList == null) {
				vultureList = new ArrayList<Unit>();
			}
			vultureList.add(vulture);
			vultureIdByMineCount.put(new Integer(vulture.getSpiderMineCount()), vultureList);
		}

		// 마인이 많은 순서대로 벌처 할당
		List<Unit> recruitList = new ArrayList<>();
		for (int mineCount = 3; mineCount >= 0; mineCount--) {
			List<Unit> vultureList = vultureIdByMineCount.get(new Integer(mineCount));
			for (Unit vulture : vultureList) {
				recruitList.add(vulture);
				openingCount--;
				if (openingCount == 0) {
					return recruitList;
				}
			}
		}
		return recruitList;
	}

	@Override
	public void execute() {
		vultureWatcher.prepare(unitList, euiList);
		vultureWatcher.control();
	}
}