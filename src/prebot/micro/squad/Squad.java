package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import prebot.brain.information.UnitInfo;
import prebot.common.code.Code.CommonCode;
import prebot.common.util.UnitUtils;

public abstract class Squad {
	
	public String getName() {
		return getClass().getSimpleName();
	}

	private int priority;
	private int squadRadius;
	private boolean activated;
	
	public List<Unit> unitList = new ArrayList<>();
	public List<UnitInfo> euiList = new ArrayList<>();
	// public Position goalPosition = Position.None;
	
	// private List<Unit> unitOldBies = new ArrayList<>();
	// private List<Unit> unitNewBies = new ArrayList<>();

	public Squad(int priority, int squadRadius) {
		this.priority = priority;
		this.squadRadius = squadRadius;
		this.activated = false;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
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

	/// squad에 소속될수 있는 유닛인지 확인한다.
	public abstract boolean want(Unit unit);
	
	/// squad 실행
	public abstract void execute();

	/// 유효하지 않은 유닛(죽은 유닛 등)을 제거한다.
	public void removeInvalidUnits() {
		List<Unit> validUnits = new ArrayList<>();
		for (Unit unit : unitList) {
			if (UnitUtils.isValidUnit(unit) && want(unit)) {
				validUnits.add(unit);
			}
		}
		unitList = validUnits;
	}
	
	/// 적 탐색
	public void findEnemies() {
		euiList.clear();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfoNearBy(euiList, unit.getPosition(), unit.getType().sightRange() + squadRadius);
		}
	}

}
