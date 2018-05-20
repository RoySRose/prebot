package prebot.micro.squad.impl;

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
import prebot.micro.control.VultureChecker;
import prebot.micro.squad.Squad;

public class CheckerSquad extends Squad {
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