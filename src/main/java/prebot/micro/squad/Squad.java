package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import prebot.common.constant.CommonCode;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.strategy.UnitInfo;

public abstract class Squad {
	private int squadExecutedFrame;
	
	public boolean squadExecuted() {
		return squadExecutedFrame == TimeUtils.elapsedFrames();
	}

	private String squadName;
	private int priority;
	private int squadRadius;

	public List<Unit> unitList = new ArrayList<>();
	public List<UnitInfo> euiList = new ArrayList<>();
	// public Position goalPosition = Position.None;

	// private List<Unit> unitOldBies = new ArrayList<>();
	// private List<Unit> unitNewBies = new ArrayList<>();

	public Squad(SquadInfo squadInfo) {
		this.squadName = squadInfo.squadName;
		this.priority = squadInfo.priority;
		this.squadRadius = squadInfo.squadRadius;
	}

	public Squad(SquadInfo squadInfo, Position targetPosition) {
		this.squadName = squadInfo.squadName + targetPosition.toString();
		this.priority = squadInfo.priority;
		this.squadRadius = squadInfo.squadRadius;
	}

	public String getSquadName() {
		return squadName;
	}

	public int getPriority() {
		return priority;
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

	public void update() {
	}

	public abstract boolean want(Unit unit);

	/// 스쿼드 업데이트
	public abstract List<Unit> recruit(List<Unit> assignableUnitList);

	/// squad 실행
	public void findEnemiesAndExecuteSquad() {
		if (squadExecutedFrame == TimeUtils.elapsedFrames()) {
//			System.out.println("ALREADY EXECUTED SQUAD - " + squadName);
			return;
		}
		findEnemies();
		execute();
		squadExecutedFrame = TimeUtils.elapsedFrames();
	}
	
	public abstract void execute();

	/// 유효하지 않은 유닛(죽은 유닛 등)을 리턴
	public List<Unit> invalidUnitList() {
		List<Unit> invalidUnitList = new ArrayList<>();
		for (Unit unit : unitList) {
			if (!UnitUtils.isCompleteValidUnit(unit) || !want(unit)) {
				invalidUnitList.add(unit);
			}
		}
		return invalidUnitList;
	}

	/// 적 탐색
	protected void findEnemies() {
		euiList.clear();
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, unit.getPosition(), unit.getType().sightRange() + squadRadius);
		}
	}

}
