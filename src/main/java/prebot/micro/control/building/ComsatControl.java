package prebot.micro.control.building;

import java.util.ArrayList;
import java.util.List;

import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.MapGrid;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.control.Control;
import prebot.strategy.AnalyzeStrategy;
import prebot.strategy.InformationManager;
import prebot.strategy.RespondToStrategy;
import prebot.strategy.UnitInfo;

public class ComsatControl extends Control {

	@Override
	public void control(List<Unit> unitList, List<UnitInfo> euiList) {
		// 상대 클록 유닛
		for (UnitInfo unitInfo : euiList) {
			Unit unit = unitInfo.getUnit();
			if (unit.isVisible() && (!unit.isDetected() || unit.getOrder() == Order.Burrowing)
					&& unit.getPosition().isValid() && unit.isFlying() == false) {
				if (InformationManager.Instance().enemyRace == Race.Protoss) {
					if (unit.isFlying() && RespondToStrategy.Instance().enemy_arbiter == false) {
						continue;
					}
				}
				// 주위에 베슬이 있는지 확인하고 베슬이 여기로 오는 로직인지도 확인한 후에 오게 되면 패스 아니면 스캔으로
				// 넘어간다
				List<Unit> nearvessel = UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
						UnitType.Terran_Science_Vessel.sightRange() * 2, UnitType.Terran_Science_Vessel);
				if (nearvessel != null) {
					Unit neareasetvessel = null;
					int closestDistToVessel = 100000;
					for (Unit vessel : nearvessel) {
						if (vessel.isStasised() || vessel.isLockedDown()) {
							continue;
						}
						int tempdist = unit.getDistance(vessel);
						if (tempdist < closestDistToVessel) {
							neareasetvessel = vessel;
							closestDistToVessel = tempdist;
						}
					}
					if (neareasetvessel != null) {
						List<Unit> nearallies = UnitUtils.getUnitsInRadius(PlayerRange.ALL,
								neareasetvessel.getPosition(), UnitType.Terran_Science_Vessel.sightRange());
						if (nearallies != null && nearallies.size() > 2) {
							break;// 베슬이 올것으로 예상됨
						}
					}
				}

				if (InformationManager.Instance().enemyRace == Race.Protoss) {
					List<Unit> myUnits = UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Vulture.groundWeapon().maxRange() + 2, UnitType.Terran_Vulture);
					myUnits.addAll(UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Goliath.groundWeapon().maxRange() + 2, UnitType.Terran_Goliath));
					myUnits.addAll(UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 2,
							UnitType.Terran_Siege_Tank_Siege_Mode));
					myUnits.addAll(UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange() + 2,
							UnitType.Terran_Siege_Tank_Tank_Mode));
					int faccnt = 0;
					for (Unit facunit : myUnits) {
						if (facunit.getType() == UnitType.Terran_Vulture || facunit.getType() == UnitType.Terran_Goliath
								|| facunit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
								|| facunit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
							faccnt++;
						}
					}
					if (faccnt > 2) {
						smartScan(unit.getPosition(), unitList);
						return;
					}
				} else if (InformationManager.Instance().enemyRace == Race.Terran) {
					List<Unit> myUnits = UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Wraith.sightRange(), UnitType.Terran_Goliath);
					if (myUnits.size() > 1) {
						smartScan(unit.getPosition(), unitList);
						return;
					}
				} else {
					List<Unit> myUnits = UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Vulture.groundWeapon().maxRange() + 2, UnitType.Terran_Vulture);
					myUnits.addAll(UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Goliath.groundWeapon().maxRange() + 2, UnitType.Terran_Goliath));
					myUnits.addAll(UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Siege_Tank_Siege_Mode.groundWeapon().maxRange() + 2,
							UnitType.Terran_Siege_Tank_Siege_Mode));
					myUnits.addAll(UnitUtils.getUnitsInRadius(PlayerRange.SELF, unit.getPosition(),
							UnitType.Terran_Siege_Tank_Tank_Mode.groundWeapon().maxRange() + 2,
							UnitType.Terran_Siege_Tank_Tank_Mode));

					int faccnt = 0;
					for (Unit facunit : myUnits) {
						if (facunit.getType() == UnitType.Terran_Vulture
								|| facunit.getType() == UnitType.Terran_Goliath) {
							faccnt++;
						}
						if (facunit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
								|| facunit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
							faccnt = 10;
						}
					}
					if (faccnt > 4) {
						smartScan(unit.getPosition(), unitList);
						return;
					}
				}
			}
		}

		Unit comsat = null;

		// 저그전 특이사항
		if (InformationManager.Instance().enemyRace == Race.Zerg) {

			int energy = 50;
			if (RespondToStrategy.Instance().enemy_lurker == true) {
				energy = 150;
			}

			for (Unit unit : unitList) {
				if (unit.getType() == UnitType.Terran_Comsat_Station && unit.getEnergy() > energy) {
					comsat = unit;
				}
			}
			if (comsat != null) {

				Player enemyPlayer = Prebot.Broodwar.enemy();
				// find place
				List<TilePosition> scanArea = new ArrayList<TilePosition>();

				if (InformationManager.Instance().getMainBaseLocation(enemyPlayer) != null) {
					scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
				}
				if (InformationManager.Instance().getFirstExpansionLocation(enemyPlayer) != null) {
					scanArea.add(
							InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
				}

				TilePosition target = null;
				int scantime = 1000000;
				if (scanArea.size() > 0) {
					for (TilePosition scans : scanArea) {
						if (Prebot.Broodwar.isVisible(scans)) {
							continue;
						}
						int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
						if (scantime > tempscantime) {
							target = scans;
							scantime = tempscantime;
						}
					}
				}
				if (target != null) {
					MapGrid.Instance().scanAtPosition(target.toPosition());
					comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
				}

			}
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) { // 폴토전
																				// 특이사항

			int energy = 50;
			if (RespondToStrategy.Instance().enemy_dark_templar == true
					|| RespondToStrategy.Instance().enemy_arbiter == true) {
				energy = 150;
			}

			for (Unit unit : unitList) {
				if (unit.getType() == UnitType.Terran_Comsat_Station && unit.getEnergy() > energy) {
					comsat = unit;
				}
			}
			if (comsat != null) {

				Player enemyPlayer = Prebot.Broodwar.enemy();
				// find place
				List<TilePosition> scanArea = new ArrayList<TilePosition>();

				if (InformationManager.Instance().getMainBaseLocation(enemyPlayer) != null) {
					scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
				}
				if (InformationManager.Instance().getFirstExpansionLocation(enemyPlayer) != null) {
					scanArea.add(
							InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
				}
				if (InformationManager.Instance().getMainBaseLocation(enemyPlayer) != null) {
					scanArea.add(
							InformationManager.Instance().getFirstChokePoint(enemyPlayer).getCenter().toTilePosition());
				}
				if (InformationManager.Instance().getFirstExpansionLocation(enemyPlayer) != null) {
					scanArea.add(InformationManager.Instance().getSecondChokePoint(enemyPlayer).getCenter()
							.toTilePosition());
				}
				if (Prebot.Broodwar.getFrameCount() > 20000) {
					if (InformationManager.Instance().getIslandBaseLocations() != null) {
						for (BaseLocation islands : InformationManager.Instance().getIslandBaseLocations()) {
							scanArea.add(islands.getTilePosition());
						}
					}
				}
				TilePosition target = null;
				int scantime = 1000000;
				if (scanArea.size() > 0) {
					for (TilePosition scans : scanArea) {
						if (Prebot.Broodwar.isVisible(scans)) {
							continue;
						}
						int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
						if (scantime > tempscantime) {
							target = scans;
							scantime = tempscantime;
						}
					}
				}
				if (target != null) {
					MapGrid.Instance().scanAtPosition(target.toPosition());
					comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
				}

			}
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {// 테란
																			// 특이사항

			int energy = 50;
			if (RespondToStrategy.Instance().enemy_wraith == true) {
				energy = 150;
			}

			for (Unit unit : unitList) {
				if (unit.getType() == UnitType.Terran_Comsat_Station && unit.getEnergy() > energy) {
					comsat = unit;
				}
			}
			if (comsat != null) {

				Player enemyPlayer = Prebot.Broodwar.enemy();
				// find place
				List<TilePosition> scanArea = new ArrayList<TilePosition>();

				if (InformationManager.Instance().getMainBaseLocation(enemyPlayer) != null) {
					scanArea.add(InformationManager.Instance().getMainBaseLocation(enemyPlayer).getTilePosition());
				}
				if (InformationManager.Instance().getFirstExpansionLocation(enemyPlayer) != null) {
					scanArea.add(
							InformationManager.Instance().getFirstExpansionLocation(enemyPlayer).getTilePosition());
				}
				if (InformationManager.Instance().getMainBaseLocation(enemyPlayer) != null) {
					scanArea.add(
							InformationManager.Instance().getFirstChokePoint(enemyPlayer).getCenter().toTilePosition());
				}
				if (InformationManager.Instance().getFirstExpansionLocation(enemyPlayer) != null) {
					scanArea.add(InformationManager.Instance().getSecondChokePoint(enemyPlayer).getCenter()
							.toTilePosition());
				}
				if (Prebot.Broodwar.getFrameCount() > 20000) {
					if (InformationManager.Instance().getIslandBaseLocations() != null) {
						for (BaseLocation islands : InformationManager.Instance().getIslandBaseLocations()) {
							scanArea.add(islands.getTilePosition());
						}
					}
				}
				TilePosition target = null;
				int scantime = 1000000;
				if (scanArea.size() > 0) {
					for (TilePosition scans : scanArea) {
						if (Prebot.Broodwar.isVisible(scans)) {
							continue;
						}
						int tempscantime = MapGrid.Instance().getCell(scans.toPosition()).getTimeLastScan();
						if (scantime > tempscantime) {
							target = scans;
							scantime = tempscantime;
						}
					}
				}
				if (target != null) {
					MapGrid.Instance().scanAtPosition(target.toPosition());
					comsat.useTech(TechType.Scanner_Sweep, target.toPosition());
				}
			}
		}
		AnalyzeStrategy.Instance().update();

	}

	public static boolean smartScan(Position targetPosition, List<Unit> unitList) {
		// if (targetPosition.isValid()) {
		// MyBotModule.Broodwar.sendText("SmartScan : bad position");
		// return false;
		// }
		if (MapGrid.Instance().scanIsActiveAt(targetPosition)) {
			// MyBotModule.Broodwar.sendText("SmartScan : last scan still on");
			return false;
		}

		// Choose the comsat with the highest energy.
		// If we're not terran, we're unlikely to have any comsats....
		int maxEnergy = 49; // anything greater is enough energy for a scan
		Unit comsat = null;
		for (Unit unit : unitList) {
			if (unit.getType() == UnitType.Terran_Comsat_Station && unit.getEnergy() > maxEnergy
					&& unit.canUseTech(TechType.Scanner_Sweep, targetPosition)) {
				maxEnergy = unit.getEnergy();
				comsat = unit;
			}
		}

		if (comsat != null) {
			MapGrid.Instance().scanAtPosition(targetPosition);
			return comsat.useTech(TechType.Scanner_Sweep, targetPosition);
		}

		return false;
	}



}