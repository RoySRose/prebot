package prebot.brain.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.code.Code.CommonCode;
import prebot.common.util.UnitUtils;

public abstract class Squad {

	public final String squadName;
	public final int priority;
	public List<Unit> unitList = new ArrayList<>();
	public List<UnitType> squadUnitTypeList = new ArrayList<>(); /// squad 포함가능 유닛타입
	
	// private List<Unit> unitOldBies = new ArrayList<>();
	// private List<Unit> unitNewBies = new ArrayList<>();

	public Squad(String squadName, int priority) {
		this.squadName = squadName;
		this.priority = priority;
	}

	public void addUnit(Unit unit) {
		unitList.add(unit);
	}
	
	public void removeUnit(Unit unit) {
		// remove(Object), contains(Object)가 정상동작하는가? unitList.remove(unit); 등 추후 변경가능
		int idx = getIndexOfUnitList(unit);
		if (idx != CommonCode.INDEX_NOT_FOUND) {
			unitList.remove(idx);
		}
	}

	public boolean hasUnit(Unit unit) {
		return getIndexOfUnitList(unit) != CommonCode.INDEX_NOT_FOUND;
	}
	
	private int getIndexOfUnitList(Unit unit) {
		for (int idx = 0; idx < unitList.size(); idx++) {
			if (unitList.get(idx).getID() == unit.getID()) {
				return idx;
			}
		}
		return CommonCode.INDEX_NOT_FOUND;
	}

	/// squad 갱신
	public void update() {
		removeInvalidUnits();
	}

	/// squad에 소속될수 있는 유닛인지 확인한다.
	public abstract boolean want(Unit unit);

	/// squad 실행
	public abstract boolean execute();

	/// 유효하지 않은 유닛(죽은 유닛 등)을 제거한다.
	private void removeInvalidUnits() {
		List<Unit> validUnits = new ArrayList<>();
		for (Unit unit : unitList) {
			if (UnitUtils.isValidUnit(unit) && want(unit)) {
				validUnits.add(unit);
			}
		}
		unitList = validUnits;
	}

}
