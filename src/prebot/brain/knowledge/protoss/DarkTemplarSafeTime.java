package prebot.brain.knowledge.protoss;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.knowledge.Knowledge;
import prebot.brain.strategy.GeneralStrategy;
import prebot.brain.stratgy.enemy.EnemyBuild;
import prebot.common.code.Code.CommonCode;
import prebot.common.code.Code.EnemyUnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.information.UnitInfo;
import prebot.main.Prebot;

public class DarkTemplarSafeTime extends Knowledge {
	
	private List<UnitType> buildingTypeList = new ArrayList<>();
	private int darkTemplarSafeTime = 5 * TimeUtils.MINUTE; // 5분
	private int darkTemplarFoundTime = CommonCode.NONE;
	
	public DarkTemplarSafeTime() {
		buildingTypeList.add(UnitType.Protoss_Templar_Archives);
		buildingTypeList.add(UnitType.Protoss_Citadel_of_Adun);
		buildingTypeList.add(UnitType.Protoss_Cybernetics_Core);
	}

	@Override
	protected boolean notOccured() {
		if (Prebot.Game.enemy().getRace() == Race.Terran || Prebot.Game.enemy().getRace() == Race.Zerg)
			return true;
		
		return Idea.of().strategy != null && Idea.of().strategy instanceof GeneralStrategy;
	}

	@Override
	protected boolean occured() {
		// 다크템플러를 발견 (occured)
		if (UnitUtils.getEnemyUnitInfoCount(UnitType.Protoss_Dark_Templar, EnemyUnitFindRange.ALL) > 0) {
			darkTemplarFoundTime = TimeUtils.elapsedFrames();
			return true;
			
		} else {
			// 건설중인 템플러아카이브, 코어, 아둔 발견에 따른 다크템플러 시간 판단
			Unit building = null;
			for (UnitType buildingType : buildingTypeList) {
				building = minimumBuildTimeBuilding(buildingType);
				
				// 현재 건설중인 건물의 남은 build시간 + 다크템플러를 생산하기 추가되어야 하는 건물 build시간 + 다크템플러 build시간
				if (building != null && !building.isCompleted()) {
					int safeTime = TimeUtils.elapsedFrames() + UnitUtils.remainingBuildTimeByHitPoint(building) + UnitType.Protoss_Dark_Templar.buildTime();
					int additionalBuildingsBuildTime = 0;
					if (buildingType == UnitType.Protoss_Cybernetics_Core) {
						additionalBuildingsBuildTime = UnitType.Protoss_Citadel_of_Adun.buildTime() + UnitType.Protoss_Templar_Archives.buildTime();
					} else if (buildingType == UnitType.Protoss_Citadel_of_Adun) {
						additionalBuildingsBuildTime = UnitType.Protoss_Templar_Archives.buildTime();
					} else if (buildingType == UnitType.Protoss_Templar_Archives) {
						additionalBuildingsBuildTime = 0;
					}
					darkTemplarSafeTime = safeTime + additionalBuildingsBuildTime;
					break;
				}
			}
			
			// 중복되어 건물되는 건물에 의해 재계산되지 않도록 처리
			if (building != null) {
				if (building.getType() == UnitType.Protoss_Cybernetics_Core) {
					buildingTypeList.remove(UnitType.Protoss_Cybernetics_Core);
				} else if (building.getType() == UnitType.Protoss_Citadel_of_Adun) {
					buildingTypeList.remove(UnitType.Protoss_Citadel_of_Adun);
					buildingTypeList.remove(UnitType.Protoss_Cybernetics_Core);
				} else if (building.getType() == UnitType.Protoss_Templar_Archives) {
					buildingTypeList.remove(UnitType.Protoss_Templar_Archives);
					buildingTypeList.remove(UnitType.Protoss_Citadel_of_Adun);
					buildingTypeList.remove(UnitType.Protoss_Cybernetics_Core);
				}
			}
			
			// 건설중인 건물을 발견 못하였을 때, 전략에 따른 다크템플러 시간 판단
			if (buildingTypeList.contains(UnitType.Protoss_Cybernetics_Core)) {
				// 투게이트 전략에 따른 처리
				if (Idea.of().enemyBuildList.contains(EnemyBuild.TWO_GATE)) {
					darkTemplarSafeTime = Math.max(darkTemplarSafeTime, 5 * TimeUtils.MINUTE + 30 * TimeUtils.SECOND);
				}
				// 선포지 전략에 따른 처리
				else if (Idea.of().enemyBuildList.contains(EnemyBuild.FORGE)) {
					darkTemplarSafeTime = Math.max(darkTemplarSafeTime, 5 * TimeUtils.MINUTE + 30 * TimeUtils.SECOND);
				}
				// 더블넥 전략에 따른 처리
				else if (Idea.of().enemyBuildList.contains(EnemyBuild.DOUBLE_NEX)) {
					darkTemplarSafeTime = Math.max(darkTemplarSafeTime, 5 * TimeUtils.MINUTE + 60 * TimeUtils.SECOND);
				}
			}
			Idea.of().darkTemplarSafeTime = darkTemplarSafeTime; // Idea 정보 입력
			return false;
		}
	}

	@Override
	protected boolean foundCertainProof() {
		return darkTemplarFoundTime >= darkTemplarSafeTime;
	}

	@Override
	protected boolean foundCertainDisproof() {
		return darkTemplarFoundTime < darkTemplarSafeTime;
	}


	/** buildingType의 적 건물 중 buildTime이 최소로 남은 유닛을 리턴 */
	private Unit minimumBuildTimeBuilding(UnitType buildingType) {
		int minimumRemainBuildTime = CommonCode.INT_MAX;
		Unit minimumRemainBuildTimeBuilding = null;
		List<UnitInfo> buildingInfos = UnitUtils.getEnemyUnitInfoList(buildingType, EnemyUnitFindRange.VISIBLE);
		for (UnitInfo buildingInfo : buildingInfos) {
			Unit building = buildingInfo.getUnit();
			if (building.getRemainingBuildTime() < minimumRemainBuildTime) {
				minimumRemainBuildTime = building.getRemainingBuildTime();
				minimumRemainBuildTimeBuilding = building;
			}
		}
		return minimumRemainBuildTimeBuilding;
	}

}
