package prebot.micro.squad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.debug.BigWatch;
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
	protected UnitType[] unitTypes;

	public Set<Unit> unitList = new HashSet<>();
	public Set<UnitInfo> euiList = new HashSet<>();

	// private List<Unit> unitOldBies = new ArrayList<>();
	// private List<Unit> unitNewBies = new ArrayList<>();

	public Squad(SquadInfo squadInfo) {
		this.squadName = squadInfo.squadName;
	}

	public Squad(SquadInfo squadInfo, Position targetPosition) {
		this.squadName = squadInfo.squadName + targetPosition.toString();
	}
	
	public void setUnitType(UnitType... unitTypes) {
		this.unitTypes = unitTypes;
	}

	public UnitType[] getUnitTypes() {
		return unitTypes;
	}

	public String getSquadName() {
		return squadName;
	}

	public boolean hasUnit(Unit unit) {
		return unitList.contains(unit);
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
		BigWatch.start("findEnemies - " + squadName);
		findEnemies();
		BigWatch.record("findEnemies - " + squadName);
		
		if (!squadExecuted()) {
			BigWatch.start("squadExecution - " + squadName);
		}
		execute();
		if (!squadExecuted()) {
			BigWatch.record("squadExecution - " + squadName);
		}
		
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
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, unit.getPosition(), unit.getType().sightRange());
		}
	}

}
