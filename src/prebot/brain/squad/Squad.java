package prebot.brain.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.common.code.Code.CommonCode;
import prebot.common.util.UnitUtils;
import prebot.information.UnitInfo;

public abstract class Squad {
	
	public String getName() {
		return getClass().getName();
	}

	public int priority;
	public int squadAdditionalRadius;
	
	public List<Unit> totalUnitList = new ArrayList<>();
	public List<UnitInfo> enemyUnitInfoList = new ArrayList<>();
	// public Position goalPosition = Position.None;
	
	// private List<Unit> unitOldBies = new ArrayList<>();
	// private List<Unit> unitNewBies = new ArrayList<>();

	public Squad(int priority, int squadAdditionalRadius) {
		this.priority = priority;
		this.squadAdditionalRadius = squadAdditionalRadius;
	}

	public void addUnit(Unit unit) {
		totalUnitList.add(unit);
	}
	
	public void removeUnit(Unit unit) {
		// remove(Object), contains(Object)가 정상동작하는가? unitList.remove(unit); 등 추후 변경가능
		int idx = getIndexOfUnitList(unit);
		if (idx != CommonCode.INDEX_NOT_FOUND) {
			totalUnitList.remove(idx);
		}
	}

	public boolean hasUnit(Unit unit) {
		return getIndexOfUnitList(unit) != CommonCode.INDEX_NOT_FOUND;
	}
	
	private int getIndexOfUnitList(Unit unit) {
		for (int idx = 0; idx < totalUnitList.size(); idx++) {
			if (totalUnitList.get(idx).getID() == unit.getID()) {
				return idx;
			}
		}
		return CommonCode.INDEX_NOT_FOUND;
	}

	/// squad에 소속될수 있는 유닛인지 확인한다.
	public abstract boolean want(Unit unit);
	
	/// squad 갱신
	public void update() {
		removeInvalidUnits();
		findEnemies();
	}

	/// squad 실행
	public abstract void execute();

	/// 유효하지 않은 유닛(죽은 유닛 등)을 제거한다.
	private void removeInvalidUnits() {
		List<Unit> validUnits = new ArrayList<>();
		for (Unit unit : totalUnitList) {
			if (UnitUtils.isValidUnit(unit) && want(unit)) {
				validUnits.add(unit);
			}
		}
		totalUnitList = validUnits;
	}
	
	/// 적 탐색
	private void findEnemies() {
		enemyUnitInfoList.clear();
		for (Unit unit : totalUnitList) {
			UnitUtils.addEnemyUnitInfoNearBy(enemyUnitInfoList, unit.getPosition(), unit.getType().sightRange() + squadAdditionalRadius);
		}
	}

}
