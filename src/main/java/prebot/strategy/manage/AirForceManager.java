package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;

public class AirForceManager {
	
	public static class StrikeLevel {
		public static final int CRITICAL_SPOT = 3; // 때리면 죽는 곳 공격 (터렛건설중인 SCV, 아모리 건설중인 SCV, 엔지니어링베이 건설중인 SCV)
		public static final int SORE_SPOT = 2; // 때리면 아픈 곳 공격 (커맨드센터건설중인 SCV, 팩토리 건설중인 SCV, 뭔가 건설중인 SCV, 체력이 적은 SCV, 가까운 SCV, 탱크)
		public static final int POSSIBLE_SPOT = 1; // 때릴 수 있는 곳 공격 (벌처, 건물 등 잡히는 대로)
	}

	public static final int AIR_FORCE_TEAM_MERGE_DISTANCE = 80;
	public static final int AIR_FORCE_SAFE_DISTANCE = 80; // 안전 실제 터렛 사정거리보다 추가로 확보하는 거리 
	public static final int AIR_FORCE_TARGET_MIDDLE_POSITION_SIZE = 1;
	

	public static int airForceTargetPositionSize = 0; // 최소값=3 (enemyBase, enemyExpansionBase, middlePosition1)
	
	private static AirForceManager instance = new AirForceManager();
	private int strikeLevelStartFrame = CommonCode.NONE;

	/// static singleton 객체를 리턴합니다
	public static AirForceManager Instance() {
		return instance;
	}
	
	private int strikeLevel = StrikeLevel.CRITICAL_SPOT;
	private boolean disabledAutoAdjustment = false;
	
	public int getStrikeLevel() {
		return strikeLevel;
	}

	public void setStrikeLevel(int strikeLevel) {
		this.strikeLevel = strikeLevel;
	}

	public boolean isDisabledAutoAdjustment() {
		return disabledAutoAdjustment;
	}

	public void setDisabledAutoAdjustment(boolean disabledAutoAdjustment) {
		this.disabledAutoAdjustment = disabledAutoAdjustment;
	}

	private BaseLocation firstBase = null; // 시작 공격 베이스
	private BaseLocation secondBase = null; // 다음 공격 베이스

	private List<Position> targetPositions = new ArrayList<>(); // 타깃포지션
	private Map<Integer, AirForceTeam> airForceTeamMap = new HashMap<>(); // key : wraith ID
	private int totalAchievement = 0;
	
	public List<Position> getTargetPositions() {
		return targetPositions;
	}
	
	public Map<Integer, AirForceTeam> getAirForceTeamMap() {
		return airForceTeamMap;
	}

	public void update() {
		BaseLocation enemyBase = InfoUtils.enemyBase();
		BaseLocation enemyFirstExpansion = InfoUtils.enemyFirstExpansion();
		if (enemyBase == null || enemyFirstExpansion == null) {
			return;
		}
		if (strikeLevelStartFrame == CommonCode.NONE) {
			strikeLevelStartFrame = TimeUtils.elapsedFrames();
		}
		
		setTargetPosition();
		changeAirForceTeamTargetPosition();
		adjustStrikeLevel();
	}

	private void setTargetPosition() {
		boolean resetTargetPosition = false;
		if (firstBase == null || secondBase == null || targetPositions.isEmpty()) {
			resetTargetPosition =  true;
		} else {
			boolean enemyBaseFirstCase = InfoUtils.enemyBase().equals(firstBase) && InfoUtils.enemyFirstExpansion().equals(secondBase);
			boolean enemyExpansionFirstCase = InfoUtils.enemyFirstExpansion().equals(firstBase) && InfoUtils.enemyBase().equals(secondBase);
			resetTargetPosition = !enemyBaseFirstCase && !enemyExpansionFirstCase;
		}
		
		if (resetTargetPosition) {
			firstBase = secondBase = null;
			targetPositions.clear();

			/// 첫번째 공격 base, 마지막 공격 base를 지정하고, 중간 포지션을 설정한다.
			double airDistanceToBase = InfoUtils.myBase().getAirDistance(InfoUtils.enemyBase());
			double airDistanceToExpansion = InfoUtils.myBase().getAirDistance(InfoUtils.enemyFirstExpansion());
			
			boolean expansionFirst = false;
			if (airDistanceToBase < airDistanceToExpansion) {
				expansionFirst = false;
				
				firstBase = InfoUtils.enemyBase(); // 본진먼저
				secondBase = InfoUtils.enemyFirstExpansion();
			} else {
				expansionFirst = true;
				
				firstBase = InfoUtils.enemyFirstExpansion(); // 앞마당먼저
				secondBase = InfoUtils.enemyBase();
			}
			Position mineralPosition = null;
			List<Unit> staticMinerals = InfoUtils.enemyFirstExpansion().getStaticMinerals();
			if (staticMinerals != null) {
				Position mineralCenterPosition = UnitUtils.centerPositionOfUnit(staticMinerals);
				Position firstExpansionPosition = InfoUtils.enemyFirstExpansion().getPosition();
				
				int vectorX = (int) ((mineralCenterPosition.getX() - firstExpansionPosition.getX()) / 3.0);
				int vectorY = (int) ((mineralCenterPosition.getY() - firstExpansionPosition.getY()) / 3.0);
				mineralPosition = new Position(mineralCenterPosition.getX() + vectorX, mineralCenterPosition.getY() + vectorY);
			}

			int vectorX = secondBase.getPosition().getX() - firstBase.getPosition().getX();
			int vectorY = secondBase.getPosition().getY() - firstBase.getPosition().getY();

			double vectorXSegment = vectorX / (AIR_FORCE_TARGET_MIDDLE_POSITION_SIZE + 1);
			double vectorYSegment = vectorY / (AIR_FORCE_TARGET_MIDDLE_POSITION_SIZE + 1);

			if (expansionFirst && mineralPosition != null) {
				targetPositions.add(mineralPosition);
			}
			
			targetPositions.add(firstBase.getPosition());
			for (int index = 0; index < AIR_FORCE_TARGET_MIDDLE_POSITION_SIZE; index++) {
				int resultX = firstBase.getPosition().getX() + (int) (vectorXSegment * (index + 1));
				int resultY = firstBase.getPosition().getY() + (int) (vectorYSegment * (index + 1));

				targetPositions.add(new Position(resultX, resultY));
			}
			targetPositions.add(secondBase.getPosition());
			
			if (!expansionFirst && mineralPosition != null) {
				targetPositions.add(mineralPosition);
			}
			
			AirForceManager.airForceTargetPositionSize = targetPositions.size();
		}
	}

	private void changeAirForceTeamTargetPosition() {
		for (AirForceTeam airForceTeam : airForceTeamMap.values()) {
			Position targetPosition = airForceTeam.getTargetPosition();
			if (targetPosition == null) {
				continue;
			}

			List<UnitInfo> enemyDefTowerList = UnitUtils.getEnemyUnitInfosInRadiusForAir(targetPosition, 20, UnitUtils.enemyAirDefenseUnitType());
			if (enemyDefTowerList.isEmpty()) {
				continue;
			}
			airForceTeam.changeTargetIndex();
		}
	}

	private void adjustStrikeLevel() {
		if (disabledAutoAdjustment) {
			return;
		}
		
		boolean levelDown = false;
		boolean levelUp = false;
		if (totalAchievement > 0) {
			strikeLevelStartFrame = TimeUtils.elapsedFrames();
		}
		
		if (strikeLevel == StrikeLevel.CRITICAL_SPOT) {
			if (InfoUtils.enemyRace() == Race.Terran) {
				if (TimeUtils.elapsedFrames(strikeLevelStartFrame) > 1 * TimeUtils.MINUTE) { // 레이쓰가 활동한지 일정시간 지남
					levelDown = true;
				} else if (!StrategyIdea.currentStrategy.buildTimeMap.isMechanic()) { // 메카닉이 아님
					levelDown = true;
				} else if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Terran_Goliath, UnitType.Terran_Armory)) { // 골리앗 발견, 완성된 아모리 발견
					levelDown = true;
				}
			} else if (InfoUtils.enemyRace() == Race.Zerg) {
				if (TimeUtils.elapsedFrames(strikeLevelStartFrame) > 1 * TimeUtils.MINUTE) { // 레이쓰가 활동한지 일정시간 지남
					levelDown = true;
				} else if (UnitUtils.enemyCompleteUnitDiscovered(UnitType.Zerg_Hydralisk)) { // 히드라 발견
					levelDown = true;
				}
			} else if (InfoUtils.enemyRace() == Race.Protoss) {
				levelDown = true;
			}
			
		} else if (strikeLevel == StrikeLevel.SORE_SPOT) {
			// TODO 레이쓰가 일정 수 파괴되었을 때로 할지 고민
			if (totalAchievement <= 50) {
				levelDown = true;
			} else if (TimeUtils.elapsedFrames(strikeLevelStartFrame) > 20 * TimeUtils.SECOND) {
				levelDown = true;
			}
			
		} else if (strikeLevel == StrikeLevel.POSSIBLE_SPOT) {
			if (totalAchievement >= 100) {
				levelUp = true;
			}
		}
		
		if (levelDown) {
			strikeLevel--;
			strikeLevelStartFrame = TimeUtils.elapsedFrames();
		} else if (levelUp) {
			strikeLevel++;
			strikeLevelStartFrame = TimeUtils.elapsedFrames();
		}
		
	}

	/// update air force team
	public void updateAirForceTeam(List<Unit> wraithList) {
		/// new team
		for (Unit wraith : wraithList) {
			AirForceTeam teamOfWraith = airForTeamOfWraith(wraith.getID());
			if (teamOfWraith == null) {
				airForceTeamMap.put(wraith.getID(), new AirForceTeam(wraith));
			}
		}
		
		// 리더의 위치를 비교하여 합칠 그룹인지 체크한다.
		Map<Integer, Integer> airForceTeamMergeMap = new HashMap<>(); // key:merge될 그룹 leaderID, value:merge할 그룹 leaderID
		Set<AirForceTeam> airForceTeamSet = new HashSet<>(airForceTeamMap.values());
		for (AirForceTeam airForceTeam : airForceTeamSet) {
			for (AirForceTeam compareForceUnit : airForceTeamSet) {
				Unit airForceLeader = airForceTeam.leaderUnit;
				Unit compareForceLeader = compareForceUnit.leaderUnit;
				if (airForceLeader.getDistance(compareForceLeader) <= AIR_FORCE_TEAM_MERGE_DISTANCE) {
					airForceTeamMergeMap.put(compareForceLeader.getID(), airForceLeader.getID());
				}
			}
		}

		// 합쳐지는 팀 레이쓰의 airForceTeamMap을 재설정한다.
		for (Unit wraith : wraithList) {
			Integer fromForceUnitLeaderId = airForceTeamMap.get(wraith.getID()).leaderUnit.getID();
			Integer toForceUnitLeaderId = airForceTeamMergeMap.get(fromForceUnitLeaderId);
			if (toForceUnitLeaderId != null) {
				airForceTeamMap.put(wraith.getID(), airForceTeamMap.get(toForceUnitLeaderId));
			}
		}
		
		// team 멤버 세팅
		airForceTeamSet = new HashSet<>(airForceTeamMap.values());
		for (AirForceTeam airForceTeam : airForceTeamSet) {
			airForceTeam.memberList.clear();
		}
		List<Integer> excludedWraithList = new ArrayList<>(); // 삭제된 레이쓰
		for (Integer wraithId : airForceTeamMap.keySet()) {
			Unit wraith = Prebot.Broodwar.getUnit(wraithId);
			if (UnitUtils.isCompleteValidUnit(wraith)) {
				AirForceTeam airForceTeam = airForceTeamMap.get(wraith.getID());
				airForceTeam.memberList.add(wraith);
			} else {
				excludedWraithList.add(wraithId);
			}
		}
		for (Integer wraithId : excludedWraithList) {
			airForceTeamMap.remove(wraithId);
		}
		
		airForceTeamSet = new HashSet<>(airForceTeamMap.values());
		totalAchievement = 0;
		for (AirForceTeam airForceTeam : airForceTeamSet) {
			// leader 교체
			Unit newLeader = UnitUtils.getClosestUnitToPosition(airForceTeam.memberList, airForceTeam.getTargetPosition());
			airForceTeam.leaderUnit = newLeader;
			
			// achievement
			int achievement = airForceTeam.achievement();
			totalAchievement += achievement;
		}
	}

	public boolean isLeader(int wraithID) {
		return airForceTeamMap.get(wraithID).leaderUnit.getID() == wraithID;
	}

	public AirForceTeam airForTeamOfWraith(int wraithID) {
		return airForceTeamMap.get(wraithID);
	}
}
