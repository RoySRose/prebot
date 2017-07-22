package pre.util;

import java.util.Arrays;

import bwapi.Position;

public class KitingOption {
	
	public KitingOption() {
		cooltimeAlwaysAttack = true;
		unitedKiting = true;
		retreatToBase = false;
		saveThisUnit = false;
	}
	
	private boolean cooltimeAlwaysAttack; // cooltimeAlwaysAttack : true인 경우 쿨타임이 돌아왔을 때 항상 공격을 한다. 벌처, 레이스등의 견제의 경우 이 값을 true로 하면 안된다.
	private boolean unitedKiting; // unitedKiting : 회피지역의 risk를 계산시, united값이 true인 경우 자신의 유닛 분포상태를 안전한 지역으로 판단할지 결정한다.(해당값이 false이면 흩어지는 kiting을 한다.)
	private Position goalPosition; // goalPosition : 목표지점. 회피지점을 결정할때 risk가 같으면 목표지점을 고려한다.
	private Integer[] fleeAngle; // 회피각도
//	private int specialSquadType; // 0:일반, 1:watcher, 2:checker
	private boolean retreatToBase;
	private boolean saveThisUnit;
	
	public boolean isCooltimeAlwaysAttack() {
		return cooltimeAlwaysAttack;
	}
	public void setCooltimeAlwaysAttack(boolean cooltimeAlwaysAttack) {
		this.cooltimeAlwaysAttack = cooltimeAlwaysAttack;
	}
	public boolean isUnitedKiting() {
		return unitedKiting;
	}
	public void setUnitedKiting(boolean unitedKiting) {
		this.unitedKiting = unitedKiting;
	}
	public Position getGoalPosition() {
		return goalPosition;
	}
	public void setGoalPosition(Position goalPosition) {
		this.goalPosition = goalPosition;
	}
	public Integer[] getFleeAngle() {
		return fleeAngle;
	}
	public void setFleeAngle(Integer[] fleeAngle) {
		this.fleeAngle = fleeAngle;
	}
	public boolean isRetreatToBase() {
		return retreatToBase;
	}
	public void setRetreatToBase(boolean retreatToBase) {
		this.retreatToBase = retreatToBase;
	}
	public boolean isSaveThisUnit() {
		return saveThisUnit;
	}
	public void setSaveThisUnit(boolean saveThisUnit) {
		this.saveThisUnit = saveThisUnit;
	}
	@Override
	public String toString() {
		return "KitingOption [cooltimeAlwaysAttack=" + cooltimeAlwaysAttack + ", unitedKiting=" + unitedKiting
				+ ", goalPosition=" + goalPosition + ", fleeAngle=" + Arrays.toString(fleeAngle) + ", retreatToBase="
				+ retreatToBase + ", saveThisUnit=" + saveThisUnit + "]";
	}
}
