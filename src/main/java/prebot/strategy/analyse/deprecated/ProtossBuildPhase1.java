package prebot.strategy.analyse.deprecated;

import prebot.strategy.action.RaceAction;

public class ProtossBuildPhase1 extends RaceAction {

//	private enum CluePhase1 {
//		NO_GAS, LATE_GAS, FAST_GAS, NOT_DOUBLE_NEXSUS, FAST_NEXSUS,
//		FAST_FORGE_IN_BASE, FAST_FORGE_IN_EXPANSION,
//		NOT_FOUND_GATE, ONE_GATE, TWO_GATE, MAYBE_GATE_IN_BASE,
//		NOT_FOUND_FAST_FORGE, FAST_CANNON_IN_BASE, FAST_CANNON_IN_EXPANSION, FAST_CANNON_SOMEWHERE
//	}
//
//	private static final int PHASE1_END_TIME = TimeUtils.timeToFrames(3, 30);
//
//	public ProtossBuildPhase1() {
//		super(Race.Protoss, 0, PHASE1_END_TIME);
//	}
//
//	/// 유닛 발견 맵을 분석한 정보
//	@Override
//	protected void analyse() {
//		if (unknownEnemyStrategy()) {
//			analyseByNexus();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByCore();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByGate();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByForge();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByAssimilator();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByCannon();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByZealot();
//		}
//		if (unknownEnemyStrategy()) {
//			analyseByNoBuilding();
//		}
//	}
//
//	private void analyseByGate() {
//		FoundInfo gateFoundInfo = getFoundInfo(UnitType.Protoss_Gateway);
//		// 2게이트웨이 가스통 여부
//		
//		if (!gateFoundInfo.euiList.isEmpty()) {
//			if (gateFoundInfo.euiList.size() >= 2) { // 게이트 웨이 2개 이상
//				removeClue(CluePhase1.NOT_FOUND_GATE);
//				removeClue(CluePhase1.ONE_GATE);
//				addClue(CluePhase1.TWO_GATE);
//				// 2게이트 빌드시작 시간
//				int firstGateStart = defaultBuildStartCompelteJustBefore(gateFoundInfo.euiList.get(0));
//				int secondGateStart = defaultBuildStartCompelteJustBefore(gateFoundInfo.euiList.get(1));
//				
//				if (firstGateStart < EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 1, 20)
//						&& secondGateStart < EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 1, 20)) {
//					setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "fast 2 gateway");
//				} else {
//					if (containsAll(CluePhase1.LATE_GAS)) {
//						setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "2 gateway, late gas");
//						
//					} else if (containsAll(CluePhase1.NO_GAS)) {
//						setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "2 gateway, no gas");
//					}
//				}
//			
//			} else if (gateFoundInfo.euiList.size() == 1) { // 게이트 웨이 1개
//				removeClue(CluePhase1.NOT_FOUND_GATE);
//				addClue(CluePhase1.ONE_GATE);
//			}
//		} else {
//			if (lastCheckFrame(LastCheckType.BASE) > EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 0, 20)) {
//				addClue(CluePhase1.NOT_FOUND_GATE);
//			}
//		}
//	}
//	
//	private void analyseByForge() {
//		FoundInfo forgeInExpansion = getFoundInfo(UnitType.Protoss_Forge, RegionType.ENEMY_FIRST_EXPANSION);
//		if (!forgeInExpansion.euiList.isEmpty()) {
//			int forgeStartFrame = defaultBuildStartCompelteJustBefore(forgeInExpansion);
//			if (forgeStartFrame < EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Forge, 15)) {
//				addClue(CluePhase1.FAST_FORGE_IN_EXPANSION);
//			}
//		} else {
//			FoundInfo forge = getFoundInfo(UnitType.Protoss_Forge);
//			if (!forge.euiList.isEmpty()) {
//				int forgeStartFrame = defaultBuildStartCompelteJustBefore(forge);
//				if (forgeStartFrame < EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Forge, 15)) {
//					addClue(CluePhase1.FAST_FORGE_IN_BASE);
//				}
//			} else {
//				if (lastCheckFrame(LastCheckType.GAS) < EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Forge, 15)) {
//					addClue(CluePhase1.NOT_FOUND_FAST_FORGE);
//				}
//			}
//		}
//	}
//	
//	private void analyseByAssimilator() {
//		FoundInfo gasFoundInfo = getFoundInfo(UnitType.Protoss_Assimilator);
//		if (!gasFoundInfo.euiList.isEmpty()) {
//			int gasStartFrame = defaultBuildStartCompelteJustBefore(gasFoundInfo);
//			if (gasStartFrame < EnemyStrategy.PROTOSS_1GATE_CORE.defaultTimeMap.time(UnitType.Protoss_Assimilator, 25)) {
//				removeClue(CluePhase1.NO_GAS);
//			} else {
//				addClue(CluePhase1.LATE_GAS);
//			}
//		} else {
//			if (lastCheckFrame(LastCheckType.GAS) < EnemyStrategy.PROTOSS_1GATE_CORE.defaultTimeMap.time(UnitType.Protoss_Assimilator, 25)) {
//				addClue(CluePhase1.NO_GAS);
//			}
//		}
//	}
//
//	private void analyseByCannon() {
//		FoundInfo cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon, RegionType.ENEMY_FIRST_EXPANSION);
//		if (!cannonFoundInfo.euiList.isEmpty()) {
//			int cannonStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
//			if (cannonStartFrame < EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Photon_Cannon, 15)) {
//				addClue(CluePhase1.FAST_CANNON_IN_EXPANSION);
//			}
//		} else {
//			cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon, RegionType.ENEMY_BASE);
//			if (!cannonFoundInfo.euiList.isEmpty()) {
//				int cannonStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
//				if (cannonStartFrame < EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Photon_Cannon, 15)) {
//					addClue(CluePhase1.FAST_CANNON_IN_BASE);
//				}
//			} else {
//				cannonFoundInfo = getFoundInfo(UnitType.Protoss_Photon_Cannon);
//				if (!cannonFoundInfo.euiList.isEmpty()) {
//					int cannonStartFrame = defaultBuildStartCompelteJustBefore(cannonFoundInfo);
//					if (cannonStartFrame < EnemyStrategy.PROTOSS_FORGE_DOUBLE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Photon_Cannon, 15)) {
//						addClue(CluePhase1.FAST_CANNON_SOMEWHERE);
//					}
//				}
//			}
//		}
//	}
//
//	private void analyseByZealot() {
//		FoundInfo zealotFoundInfo = getFoundInfo(UnitType.Protoss_Zealot);
//		if (!zealotFoundInfo.euiList.isEmpty()) {
//			int threeZealotFrame = EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 1, 20) + UnitType.Protoss_Zealot.buildTime();
//			int threeZealotInMyRegionFrame = threeZealotFrame + baseToBaseFrame(UnitType.Protoss_Zealot);
//			if (euiCountBefore(zealotFoundInfo.euiList, threeZealotFrame) >= 3) {
//				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "3 zealots in enemy base");
//			} else if (euiCountBeforeWhere(zealotFoundInfo.euiList, threeZealotInMyRegionFrame, RegionType.ENEMY_FIRST_EXPANSION, RegionType.ETC, RegionType.MY_BASE) >= 3) {
//				setEnemyStrategy(EnemyStrategy.PROTOSS_2GATE, "3 zealots");
//			}
//		}
//		
//		FoundInfo zealotInBase = getFoundInfo(UnitType.Protoss_Zealot, RegionType.ENEMY_BASE);
//		if (!zealotFoundInfo.euiList.isEmpty()) {
//			int oneZealotFrame = EnemyStrategy.PROTOSS_2GATE.defaultTimeMap.timeOfIndex(UnitType.Protoss_Gateway, 0, 20) + UnitType.Protoss_Zealot.buildTime();
//			if (euiCountBefore(zealotInBase.euiList, oneZealotFrame) >= 1) {
//				addClue(CluePhase1.MAYBE_GATE_IN_BASE);
//			}
//		}
//	}
//	
//	private void analyseByNoBuilding() {
//		
//		EnemyStrategy enemyStrategyExpect = EnemyStrategy.UNKNOWN;
//		if (containsAll(CluePhase1.FAST_NEXSUS) && !containsAny(CluePhase1.FAST_GAS)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_GATE_DOUBLE;
//
//		} else if (containsAll(CluePhase1.ONE_GATE, CluePhase1.LATE_GAS) && !containsAny(CluePhase1.FAST_GAS)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_2GATE;
//
//		} else if (containsAll(CluePhase1.ONE_GATE, CluePhase1.NO_GAS) && !containsAny(CluePhase1.FAST_GAS)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_2GATE;
//
//		} else if (containsAll(CluePhase1.NOT_FOUND_GATE, CluePhase1.NOT_FOUND_FAST_FORGE, CluePhase1.NO_GAS) && !containsAny(CluePhase1.MAYBE_GATE_IN_BASE, CluePhase1.FAST_GAS)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_2GATE_CENTER;
//
//		} else if (containsAll(CluePhase1.FAST_FORGE_IN_EXPANSION, CluePhase1.NO_GAS) && !containsAny(CluePhase1.NOT_DOUBLE_NEXSUS, CluePhase1.FAST_GAS)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DOUBLE;
//
//		} else if (containsAll(CluePhase1.FAST_FORGE_IN_BASE)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DEFENSE;
//			
//		} else if (containsAll(CluePhase1.FAST_CANNON_IN_EXPANSION, CluePhase1.NO_GAS) && !containsAny(CluePhase1.NOT_DOUBLE_NEXSUS, CluePhase1.FAST_GAS)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DOUBLE;
//			
//		} else if (containsAll(CluePhase1.FAST_CANNON_IN_BASE)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_DEFENSE;
//			
//		} else if (containsAll(CluePhase1.FAST_CANNON_SOMEWHERE)) {
//			enemyStrategyExpect = EnemyStrategy.PROTOSS_FORGE_CANNON_RUSH;
//		}
//		
//		setEnemyStrategyExpect(enemyStrategyExpect);
//	}
	
}
