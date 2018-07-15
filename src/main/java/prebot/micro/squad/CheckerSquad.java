package prebot.micro.squad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.factory.VultureControl;
import prebot.strategy.StrategyIdea;
import prebot.strategy.manage.VultureTravelManager;

public class CheckerSquad extends Squad {
	private VultureControl vultureControl = new VultureControl();

	public CheckerSquad() {
		super(SquadInfo.CHECKER);
	}

	@Override
	public boolean want(Unit unit) {
		return unit.getType() == UnitType.Terran_Vulture
				&& !VultureTravelManager.Instance().checkerRetired(unit.getID());
	}

	/// checker squad는 매 frame 1회 용감한 checker부대원을 모집한다.
	/// 스파이더마인을 많이 보유한 벌처의 우선순위가 높다.
	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		int openingCount = StrategyIdea.checkerMaxNumber - unitList.size();
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
			List<Unit> vultureList = vultureIdByMineCount.getOrDefault(new Integer(mineCount), new ArrayList<Unit>());
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
		vultureControl.control(unitList, euiList);
	}

	@Override
	public void findEnemies() {
		euiList.clear();
		for (Unit unit : unitList) {
			if (!VultureTravelManager.Instance().checkerIgnoreModeEnabled(unit.getID())) {
				UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.CHECKER.squadRadius);
			}
		}
	}
}