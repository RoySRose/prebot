package prebot.strategy.analyse;

import bwapi.Race;
import bwapi.UnitType;
import prebot.common.constant.CommonCode;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.action.RaceAction;

public class ProtossBuildPhase2 extends RaceAction {

	private enum CluePhase2 {
	}
	
	private static final int PHASE2_END_TIME = TimeUtils.timeToFrames(8, 00); // ???

	private static final int CORE_BULD_COMPLETE_TIME = TimeUtils.timeToFrames(2, 40); // 정상빌드시 코어가 완성되는 시간 (일반적인 phase2 시작)
	
	private static final int THIRD_PYLON = TimeUtils.timeToFrames(4, 15);

	private static final int FAST_ADUN_TIME = TimeUtils.timeToFrames(2, 40); 
	private static final int FAST_TEMPLAR_ARCHIVE_TIME = TimeUtils.timeToFrames(3, 20);
//	private static final int FAST_DARK_TEMPLAR_START_TIME = TimeUtils.timeToFrames(4, 00);
	private static final int FAST_DARK_TEMPLAR_COMPELTE_TIME = TimeUtils.timeToFrames(4, 35);
	
	private static final int FAST_ROBOTICS_TIME = TimeUtils.timeToFrames(3, 40); // 일반적으로 로보틱스는 드라군을 찍고 올라가서 패스트다크보다 늦음
//	private static final int FAST_SHUTTLE_START_TIME = TimeUtils.timeToFrames(4, 35); // 셔틀 생산 시작
	private static final int FAST_SHUTTLE_COMPLETE_TIME = TimeUtils.timeToFrames(5, 15); // 셔틀 생산 완료
	
	private static final int FAST_ROBOTICS_SUPPORT_TIME = TimeUtils.timeToFrames(4, 50); // 로보틱스 서포트베이 또는 옵저버터리
//	private static final int FAST_REAVER_START_TIME = TimeUtils.timeToFrames(5, 15); // 리버 생산 시작
	private static final int FAST_REAVER_COMPLETE_TIME = TimeUtils.timeToFrames(6, 00); // 리버 생산 완료
	
	private static final int FAST_STARGATE_TIME = TimeUtils.timeToFrames(4, 50); // 스타게이트는 빠를 수가 없음
	private static final int FAST_SCOUT_START_TIME = TimeUtils.timeToFrames(5, 50);
	private static final int FAST_SCOUT_COMPLETE_TIME = TimeUtils.timeToFrames(6, 30);
	

	private static final int FIRST_EXPANSION = TimeUtils.timeToFrames(6, 30); // 원게이트에서 멀티
	private static final int FAST_THIRD_EXPANSION = TimeUtils.timeToFrames(7, 00); // 빠른 3번째 멀티
	
	public ProtossBuildPhase2() {
		super(Race.Protoss, 1, PHASE2_END_TIME);
	}
	
	private int coreBuildCompleteTime = CORE_BULD_COMPLETE_TIME;

	@Override
	protected void analyse() {
		if (unknownEnemyStrategy()) {
			analyseByCore();
		}
		if (unknownEnemyStrategy()) {
			analyseByAdun();
		}
		if (unknownEnemyStrategy()) {
			analyseByRobotics();
		}
		if (unknownEnemyStrategy()) {
			analyseStargate();
		}
		if (unknownEnemyStrategy()) {
			analyseAdunUnits();
		}
		if (unknownEnemyStrategy()) {
			analyseRoboticsUnits();
		}
		if (unknownEnemyStrategy()) {
			analyseStargateUnits();
		}
		if (unknownEnemyStrategy()) {
			analyseThirdExpansion();
		}
		if (unknownEnemyStrategy()) {
			analyseByNoBuilding();
		}
	}

	private void analyseByCore() {
//		coreBuildStartTime();
	}

//	private int coreBuildStartTime() {
//		FoundInfo coreFoundInfo = getFoundInfo(UnitType.Protoss_Cybernetics_Core);
//		int buildStartTime = buildStart(coreFoundInfo);
//		if (buildStartTime == CommonCode.UNKNOWN) {
//			if (StrategyIdea.strategyHistory.get(phaseIndex) == 
//				
//			}
//			
//		}
//		
//		return buildStartTime;
//	}

	private void analyseByAdun() {
	}

	private void analyseByRobotics() {
	}

	private void analyseStargate() {
	}
	
	private void analyseAdunUnits() {
	}
	
	private void analyseRoboticsUnits() {
	}

	private void analyseStargateUnits() {
	}
	
	private void analyseThirdExpansion() {
	}
	
	private void analyseByNoBuilding() {
	}
	
}
