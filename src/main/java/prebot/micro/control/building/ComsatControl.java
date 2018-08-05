package prebot.micro.control.building;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bwapi.Order;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BaseLocation;
import prebot.common.MapGrid;
import prebot.common.MapGrid.GridCell;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.InfoUtils;
import prebot.common.util.MicroUtils;
import prebot.common.util.PositionUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.control.Control;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap.Feature;
import prebot.strategy.constant.StrategyConfig;

public class ComsatControl extends Control {
	private int scanEnemySquadFrame = 10 * TimeUtils.MINUTE;

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		
		// 상대 클록 유닛
		Position scanPosition = scanPositionForInvisibleEnemy(euiList);
		if (PositionUtils.isValidPosition(scanPosition)) {
			if (!MapGrid.Instance().scanIsActiveAt(scanPosition)) {
				Unit comsatMaxEnergy = null;
				int maxEnergy = 50;
				for (Unit comsat : unitList) {
//					if (TimeUtils.elapsedFrames(comsat.getLastCommandFrame()) < 5 * TimeUtils.SECOND) {
//						continue;
//					}
					
					if (comsat.getEnergy() >= maxEnergy && comsat.canUseTech(TechType.Scanner_Sweep, scanPosition)) {
						maxEnergy = comsat.getEnergy();
						comsatMaxEnergy = comsat;
					}
				}
				if (comsatMaxEnergy != null) {
					GridCell cell = MapGrid.Instance().getCell(scanPosition);
					int timeLastScan = cell.getTimeLastScan() + StrategyConfig.SCAN_DURATION;
					
					System.out.println("timeLastScan : " + scanPosition + " / " + cell.getCenter() + " / " + timeLastScan + " / " + StrategyConfig.SCAN_DURATION);
					System.out.println("frames : " + TimeUtils.elapsedFrames());
					
					MapGrid.Instance().scanAtPosition(scanPosition);
					CommandUtils.useTechPosition(comsatMaxEnergy, TechType.Scanner_Sweep, scanPosition);
					return;
				}
			}
		}


		if (TimeUtils.executeRotation(0, 48)) {
			return;
		}
		
		Unit comsatToUse = null;
		int usableEnergy = 75;
		if (UnitUtils.invisibleEnemyDiscovered() || StrategyIdea.currentStrategy.buildTimeMap.featureEnabled(Feature.DETECT_IMPORTANT)) {
			usableEnergy = 150;
		}
		
		if (usableEnergy < 180 && TimeUtils.afterTime(13, 0)) {
			usableEnergy = 180;
			
		} else if (TimeUtils.afterTime(10, 0)) {
			usableEnergy = 130;
			
		} else if (TimeUtils.afterTime(7, 0)) {
			usableEnergy = 80;
		}
		
		for (Unit comsatStation : unitList) {
			if (TimeUtils.elapsedFrames(comsatStation.getLastCommandFrame()) < 5 * TimeUtils.SECOND) {
				continue;
			}
			
			if (comsatStation.getEnergy() >= usableEnergy) {
				comsatToUse = comsatStation;
				usableEnergy = comsatStation.getEnergy();
			}
		}
		if (comsatToUse != null) {
			Position scanPositionForObservation = getScanPositionForObservation();
//			System.out.println("################################################ SCCCCCCCCCCCCCCCCCCCCAN -> " + scanPositionForObservation);
			
			if (PositionUtils.isValidPosition(scanPositionForObservation)) {
				MapGrid.Instance().scanAtPosition(scanPositionForObservation);
				CommandUtils.useTechPosition(comsatToUse, TechType.Scanner_Sweep, scanPositionForObservation);
			}
		}
		
	}

	/// 클로킹 유닛용 스캔 포지션
	private Position scanPositionForInvisibleEnemy(Collection<UnitInfo> euiList) {
		for (UnitInfo eui : euiList) {
			Unit enemyUnit = eui.getUnit();
			if (!UnitUtils.isValidUnit(enemyUnit) || !enemyUnit.isVisible()) {
				continue;
			}
			if (enemyUnit.isDetected() && enemyUnit.getOrder() != Order.Burrowing) {
				continue;
			}
			
			// 주위에 베슬이 있는지 확인하고 베슬이 여기로 오는 로직인지도 확인한 후에 오게 되면 패스 아니면 스캔으로 넘어간다
			List<Unit> nearVessel = UnitUtils.getUnitsInRadius(PlayerRange.SELF, enemyUnit.getPosition(), UnitType.Terran_Science_Vessel.sightRange() * 2, UnitType.Terran_Science_Vessel);
			if (nearVessel != null) {
				Unit neareasetVessel = UnitUtils.getClosestUnitToPositionNotStunned(nearVessel, enemyUnit.getPosition());
				if (neareasetVessel != null) {
					List<Unit> nearAllies = UnitUtils.getUnitsInRadius(PlayerRange.SELF, neareasetVessel.getPosition(), UnitType.Terran_Science_Vessel.sightRange());
					if (nearAllies != null && nearAllies.size() > 2) {
						continue;// 베슬이 올것으로 예상됨
					}
				}
			}
			
			List<Unit> myAttackUnits = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture,
					UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath, UnitType.Terran_Wraith);
			
			Race enemyRace = InfoUtils.enemyRace();
			int myAttackUnitInWeaponRangeCount = 0;
			for (Unit myAttackUnit : myAttackUnits) {
				WeaponType weaponType = MicroUtils.getWeapon(myAttackUnit, enemyUnit);
				if (weaponType == WeaponType.None) {
					continue;
				}

				int weaponMaxRange = Prebot.Broodwar.self().weaponMaxRange(weaponType);
				int weaponRangeMargin = 15; // 쉽게 스캔을 사용해 공격할 수 있도록 두는 여유값(조절필요)
				if (!enemyUnit.isMoving()) {
					weaponRangeMargin += 15;
				}
				int enemyUnitDistance = myAttackUnit.getDistance(enemyUnit);
				if (enemyUnitDistance < weaponMaxRange + weaponRangeMargin) {
					myAttackUnitInWeaponRangeCount++;

					if (enemyRace == Race.Protoss) {
						if (myAttackUnitInWeaponRangeCount >= 3) {
							return enemyUnit.getPosition();
						}
					} else if (enemyRace == Race.Terran) {
						if (myAttackUnitInWeaponRangeCount >= 2) {
							return enemyUnit.getPosition();
						}
					} else if (enemyRace == Race.Zerg) {
						if (myAttackUnitInWeaponRangeCount >= 5 || myAttackUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || myAttackUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
							return enemyUnit.getPosition();
						}
					}
				}
			}
		}
		return null;
	}

	/// 정찰용 스캔 포지션
	private Position getScanPositionForObservation() {
		// find place
		List<TilePosition> scanTilePositionCandidate = new ArrayList<TilePosition>();
		if (InfoUtils.enemyBase() != null) {
			scanTilePositionCandidate.add(InfoUtils.enemyBase().getTilePosition());
			if (InfoUtils.enemyRace() == Race.Protoss || InfoUtils.enemyRace() == Race.Terran) {
				scanTilePositionCandidate.add(InfoUtils.enemyFirstChoke().getCenter().toTilePosition());
			}
		}
		if (InfoUtils.enemyFirstExpansion() != null) {
			scanTilePositionCandidate.add(InfoUtils.enemyFirstExpansion().getTilePosition());
			if (InfoUtils.enemyRace() == Race.Protoss || InfoUtils.enemyRace() == Race.Terran) {
				scanTilePositionCandidate.add(InfoUtils.enemySecondChoke().getCenter().toTilePosition());
			}
		}
		
		if (TimeUtils.afterTime(14, 0)) {
			if (InformationManager.Instance().getIslandBaseLocations() != null) {
				for (BaseLocation islands : InformationManager.Instance().getIslandBaseLocations()) {
					scanTilePositionCandidate.add(islands.getTilePosition());
				}
			}
		}

		Position oldestCheckPosition = null;
		int oldestLastCheckTime = CommonCode.INT_MAX;
		for (TilePosition scanTilePosition : scanTilePositionCandidate) {
			if (Prebot.Broodwar.isVisible(scanTilePosition)) {
				continue;
			}
			Position scanPosotion = scanTilePosition.toPosition();
			GridCell cell = MapGrid.Instance().getCell(scanPosotion);
			if (cell == null) {
				continue;
			}
			
			int lastScanTime = TimeUtils.elapsedFrames(cell.getTimeLastScan());
			int lastVisitTime = TimeUtils.elapsedFrames(cell.getTimeLastVisited());
			int lastCheckTime = Math.min(lastScanTime, lastVisitTime);
			
			if (lastCheckTime < oldestLastCheckTime) {
				oldestCheckPosition = scanPosotion;
				oldestLastCheckTime = lastCheckTime;
			}
		}
		
		if (StrategyIdea.totalEnemyCneterPosition != null && scanEnemySquadFrame < oldestLastCheckTime) {
			if (!Prebot.Broodwar.isVisible(StrategyIdea.totalEnemyCneterPosition.toTilePosition())) {
				scanEnemySquadFrame = TimeUtils.elapsedFrames();
				return StrategyIdea.totalEnemyCneterPosition;	
			}
		}
		
		return oldestCheckPosition;
	}
	
}