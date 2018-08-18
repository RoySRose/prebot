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

public class ComsatControl extends Control {
	private int scanUsedFrame = 0;
	private int scanEnemySquadFrame = 10 * TimeUtils.MINUTE;

	@Override
	public void control(Collection<Unit> unitList, Collection<UnitInfo> euiList) {
		if (TimeUtils.elapsedFrames(scanUsedFrame) < 4 * TimeUtils.SECOND) {
			return;
		}
		if (!TimeUtils.executeRotation(0, 24)) {
			return;
		}
		
		// 상대 클록 유닛
		Position scanPosition = scanPositionForInvisibleEnemy(euiList);
		if (PositionUtils.isValidPosition(scanPosition)) {
			if (!MapGrid.Instance().scanIsActiveAt(scanPosition)) {
				Unit comsatMaxEnergy = null;
				int maxEnergy = 50;
				for (Unit comsat : unitList) {
					if (comsat.getEnergy() >= maxEnergy && comsat.canUseTech(TechType.Scanner_Sweep, scanPosition)) {
						maxEnergy = comsat.getEnergy();
						comsatMaxEnergy = comsat;
					}
				}
				if (comsatMaxEnergy != null) {
					MapGrid.Instance().scanAtPosition(scanPosition);
					comsatMaxEnergy.useTech(TechType.Scanner_Sweep, scanPosition);
					scanUsedFrame = TimeUtils.elapsedFrames();
					System.out.println("scan for invisible. position=" + scanPosition + ", time=" + TimeUtils.framesToTimeString(scanUsedFrame));
					return;
				}
			}
		}
		
		Unit comsatToUse = null;
		int usableEnergy = 75;
		
		if (TimeUtils.afterTime(16, 0)) {
			usableEnergy = 195;
		} else if (TimeUtils.afterTime(13, 0)) {
			usableEnergy = 180;
		} else {
			if (UnitUtils.invisibleEnemyDiscovered() || StrategyIdea.buildTimeMap.featureEnabled(Feature.DETECT_IMPORTANT)) {
				usableEnergy = 150;
			} else if (TimeUtils.afterTime(10, 0)) {
				usableEnergy = 130;
			} else if (TimeUtils.afterTime(7, 0)) {
				usableEnergy = 80;
			}
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

			if (PositionUtils.isValidPosition(scanPositionForObservation)) {
				MapGrid.Instance().scanAtPosition(scanPositionForObservation);
				comsatToUse.useTech(TechType.Scanner_Sweep, scanPositionForObservation);
				scanUsedFrame = TimeUtils.elapsedFrames();
				System.out.println("scan for search. position=" + scanPosition + ", time=" + TimeUtils.framesToTimeString(scanUsedFrame));
			}
		}
		
	}

	/// 클로킹 유닛용 스캔 포지션
	private Position scanPositionForInvisibleEnemy(Collection<UnitInfo> euiList) {
		for (UnitInfo eui : euiList) {
			Unit enemyUnit = eui.getUnit();
			if (!UnitUtils.isValidUnit(enemyUnit) && eui.getType() != UnitType.Terran_Vulture_Spider_Mine) {
				continue;
			}
			if (!enemyUnit.isVisible() && eui.getType() != UnitType.Terran_Vulture_Spider_Mine) {
				continue;
			}
			if (enemyUnit.isDetected() && enemyUnit.getOrder() != Order.Burrowing) {
				continue;
			}
			// 주위에 베슬이 있는지 확인하고 베슬이 여기로 오는 로직인지도 확인한 후에 오게 되면 패스 아니면 스캔으로 넘어간다
			List<Unit> nearVessel = UnitUtils.getUnitsInRadius(PlayerRange.SELF, eui.getLastPosition(), UnitType.Terran_Science_Vessel.sightRange() * 2, UnitType.Terran_Science_Vessel);
			if (nearVessel != null) {
				Unit neareasetVessel = UnitUtils.getClosestUnitToPositionNotStunned(nearVessel, eui.getLastPosition());
				if (neareasetVessel != null) {
					List<Unit> nearAllies = UnitUtils.getUnitsInRadius(PlayerRange.SELF, neareasetVessel.getPosition(), UnitType.Terran_Science_Vessel.sightRange());
					if (nearAllies != null && nearAllies.size() > 2) {
						continue;// 베슬이 올것으로 예상됨
					}
				}
			}
			
			List<Unit> myAttackUnits = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Vulture,
					UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Goliath);
			
			Race enemyRace = InfoUtils.enemyRace();
			int myAttackUnitInWeaponRangeCount = 0;
			for (Unit myAttackUnit : myAttackUnits) {
				WeaponType weaponType = MicroUtils.getWeapon(myAttackUnit.getType(), eui.getType());
				if (weaponType == WeaponType.None) {
					continue;
				}

				int weaponMaxRange = Prebot.Broodwar.self().weaponMaxRange(weaponType);
				int weaponRangeMargin = 15; // 쉽게 스캔을 사용해 공격할 수 있도록 두는 여유값(조절필요)
				if (!enemyUnit.isMoving()) {
					weaponRangeMargin += 10;
				}
				int enemyUnitDistance = myAttackUnit.getDistance(eui.getLastPosition());
				
//				System.out.println("1: " + enemyUnitDistance);
//				System.out.println("2: " + (weaponMaxRange + weaponRangeMargin));
				if (enemyUnitDistance < weaponMaxRange + weaponRangeMargin) {
					myAttackUnitInWeaponRangeCount++;

					if (enemyRace == Race.Protoss) {
						if (myAttackUnitInWeaponRangeCount >= 3) {
							return eui.getLastPosition();
						}
					} else if (enemyRace == Race.Terran) {
						if (myAttackUnitInWeaponRangeCount >= 2) {
							return eui.getLastPosition();
						}
					} else if (enemyRace == Race.Zerg) {
						if (myAttackUnitInWeaponRangeCount >= 5 || myAttackUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || myAttackUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
							return eui.getLastPosition();
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
		
		if (StrategyIdea.totalEnemyCneterPosition != null
				&& StrategyIdea.mainSquadLeaderPosition != null
				&& scanEnemySquadFrame < oldestLastCheckTime) {
			
			double radian = MicroUtils.targetDirectionRadian(StrategyIdea.mainSquadLeaderPosition, StrategyIdea.totalEnemyCneterPosition);
			Position squadFrontPosition = MicroUtils.getMovePosition(StrategyIdea.mainSquadLeaderPosition, radian, 700).makeValid();
			
//			if (!Prebot.Broodwar.isVisible(squadFrontPosition.toTilePosition())) {}
			scanEnemySquadFrame = TimeUtils.elapsedFrames();
			return squadFrontPosition;	
		}
		
		return oldestCheckPosition;
	}
	
}