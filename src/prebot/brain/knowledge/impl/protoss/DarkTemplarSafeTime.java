package prebot.brain.knowledge.impl.protoss;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.brain.Idea;
import prebot.brain.information.UnitInfo;
import prebot.brain.knowledge.Knowledge;
import prebot.brain.stratgy.enemy.EnemyBuild;
import prebot.build.temp.strategy.GeneralStrategy;
import prebot.common.code.Code.CommonCode;
import prebot.common.code.Code.EnemyUnitFindRange;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;

public class DarkTemplarSafeTime extends Knowledge {
	
	private List<UnitType> hintBuildingTypeList = new ArrayList<>();
	private int darkTemplarSafeTime = 5 * TimeUtils.MINUTE; // 5분
	private int darkTemplarFoundTime = CommonCode.NONE;
	
	public DarkTemplarSafeTime() {
		hintBuildingTypeList.add(UnitType.Protoss_Templar_Archives);
		hintBuildingTypeList.add(UnitType.Protoss_Citadel_of_Adun);
		hintBuildingTypeList.add(UnitType.Protoss_Cybernetics_Core);
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
			int safeTime = safeTimeByIncompleteEnemyBuilding();
			Idea.of().darkTemplarSafeFrame = Math.max(safeTime, Idea.of().darkTemplarSafeFrame);
			 
			Idea.of().darkTemplarSafeFrame = safeTimeByEnemyStrategy();
			
			return false;
		}
	}

	private int safeTimeByIncompleteEnemyBuilding() {
		// 건설중인 템플러아카이브, 코어, 아둔 발견에 따른 다크템플러 시간 판단
		Unit visibleBuilding = null;
		for (UnitType buildingType : hintBuildingTypeList) {
			visibleBuilding = visibleBuilding(buildingType);
			if (visibleBuilding != null) { // 중복 건설되는 건물에 재계산되지 않도록 처리
				break;
			}
		}
		
		if (visibleBuilding != null) {
			if (visibleBuilding.getType() == UnitType.Protoss_Cybernetics_Core) {
				hintBuildingTypeList.remove(UnitType.Protoss_Cybernetics_Core);
			} else if (visibleBuilding.getType() == UnitType.Protoss_Citadel_of_Adun) {
				hintBuildingTypeList.remove(UnitType.Protoss_Citadel_of_Adun);
				hintBuildingTypeList.remove(UnitType.Protoss_Cybernetics_Core);
			} else if (visibleBuilding.getType() == UnitType.Protoss_Templar_Archives) {
				hintBuildingTypeList.remove(UnitType.Protoss_Templar_Archives);
				hintBuildingTypeList.remove(UnitType.Protoss_Citadel_of_Adun);
				hintBuildingTypeList.remove(UnitType.Protoss_Cybernetics_Core);
			}
	
			// 현재 건설중인 건물의 남은 build시간 + 다크템플러를 생산하기 추가되어야 하는 건물 build시간 + 다크템플러 build시간
			if (!visibleBuilding.isCompleted()) {
				int additionalBuildingsBuildTime = 0;
				if (visibleBuilding.getType() == UnitType.Protoss_Cybernetics_Core) {
					additionalBuildingsBuildTime = UnitType.Protoss_Citadel_of_Adun.buildTime() + UnitType.Protoss_Templar_Archives.buildTime();
				} else if (visibleBuilding.getType() == UnitType.Protoss_Citadel_of_Adun) {
					additionalBuildingsBuildTime = UnitType.Protoss_Templar_Archives.buildTime();
				} else if (visibleBuilding.getType() == UnitType.Protoss_Templar_Archives) {
					additionalBuildingsBuildTime = 0;
				}
				return TimeUtils.elapsedFrames() + additionalBuildingsBuildTime + UnitUtils.remainingBuildTimeByHitPoint(visibleBuilding) + UnitType.Protoss_Dark_Templar.buildTime();
			}
		}
		return 0;
	}

	private int safeTimeByEnemyStrategy() {
		if (!hintBuildingTypeList.contains(UnitType.Protoss_Cybernetics_Core)) {
			return 0;
		}

		// 건설중인 건물을 발견 못하였을 때, 전략에 따른 다크템플러 시간 판단
		
		// 투게이트 전략에 따른 처리
		if (Idea.of().enemyBuildList.contains(EnemyBuild.TWO_GATE)) {
			return 5 * TimeUtils.MINUTE + 30 * TimeUtils.SECOND;
		}
		// 선포지 전략에 따른 처리
		else if (Idea.of().enemyBuildList.contains(EnemyBuild.FORGE)) {
			return 5 * TimeUtils.MINUTE + 30 * TimeUtils.SECOND;
		}
		// 더블넥 전략에 따른 처리
		else if (Idea.of().enemyBuildList.contains(EnemyBuild.DOUBLE_NEX)) {
			return 6 * TimeUtils.MINUTE;
		} else {
			return 0;
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
	private Unit visibleBuilding(UnitType buildingType) {
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
