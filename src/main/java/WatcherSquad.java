

import java.util.Collection;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;

public class WatcherSquad extends Squad {
	
//	private static final int MAX_REGROUP_POSITION_SIZE = 3;
	private StrategyCode.SmallFightPredict smallFightPredict = StrategyCode.SmallFightPredict.ATTACK;
	private int watcherFleeStartFrame = 0;

//	private Position otherWatcherPosition;
//	private Position[] regroupPositions = new Position[MAX_REGROUP_POSITION_SIZE];
//	private int regroupPositionIndex = 0;
//	private int otherWatcherPositionIndex = 0;
	
	public StrategyCode.SmallFightPredict getSmallFightPredict() {
		return smallFightPredict;
	}

	private WatcherControl vultureWatcher = new WatcherControl();
	
	public WatcherSquad() {
		super(MicroConfig.SquadInfo.WATCHER);
		setUnitType(UnitType.Terran_Vulture);
	}

	@Override
	public boolean want(Unit unit) {
		Squad squad = CombatManager.Instance().squadData.getSquad(unit);
		if (squad instanceof CheckerSquad || squad instanceof GuerillaSquad) {
			return false;
		}
		return true;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		return assignableUnitList;
	}

	@Override
	public void execute() {
		int watcherFleeSeconds = 8;
		if (InfoUtils.enemyRace() == Race.Terran) {
			watcherFleeSeconds = 5;
		}
		
		if (unitList.isEmpty()) {
			return;
		}
		
		euiList = MicroUtils.filterTargetInfos(euiList, TargetFilter.AIR_UNIT);
		Unit regroupLeader = UnitUtils.getClosestUnitToPosition(unitList, StrategyIdea.watcherPosition);
		
		if (StrategyIdea.initiated) {
			smallFightPredict = StrategyCode.SmallFightPredict.ATTACK;
		} else {
			if (TimeUtils.elapsedSeconds(watcherFleeStartFrame) <= watcherFleeSeconds) {
				smallFightPredict = StrategyCode.SmallFightPredict.BACK;
				
			} else if (PositionFinder.Instance().otherPositionTimeUp(regroupLeader)) {
				smallFightPredict = StrategyCode.SmallFightPredict.BACK;
				watcherFleeStartFrame = TimeUtils.elapsedFrames();
//				System.out.println("watcher flee - other position time up");
				
			} else {
				smallFightPredict = VultureFightPredictor.watcherPredictByUnitInfo(unitList, euiList);
				if (smallFightPredict == StrategyCode.SmallFightPredict.BACK) {
					watcherFleeStartFrame = TimeUtils.elapsedFrames();
//					System.out.println("watcher flee - enemy");
				}
			}
		}
		
		int saveUnitLevel = 1;
		if (StrategyIdea.mainSquadMode == MicroConfig.MainSquadMode.NO_MERCY) {
			smallFightPredict = StrategyCode.SmallFightPredict.ATTACK;
			saveUnitLevel = 0;
		} else if (smallFightPredict == StrategyCode.SmallFightPredict.OVERWHELM) {
			saveUnitLevel = 0;
		}
		
		if (smallFightPredict != StrategyCode.SmallFightPredict.BACK) {
			regroupLeader = null;
		}

		Position avoidBunkerPosition = null;
		if (TimeUtils.beforeTime(8, 0) && BlockingEntrance.Instance().bunker1 != null && !UnitUtils.myUnitDiscovered(UnitType.Terran_Bunker)) {
			avoidBunkerPosition = BlockingEntrance.Instance().bunker1.toPosition();
		}
		
		vultureWatcher.setSaveUnitLevel(saveUnitLevel);
		vultureWatcher.setRegroupLeader(regroupLeader);
		vultureWatcher.setAvoidBunkerPosition(avoidBunkerPosition);
		
		vultureWatcher.controlIfUnitExist(unitList, euiList);
	}


	/// 적 탐색
	@Override
	protected void findEnemies() {
		euiList.clear();
		
		Position goalPosition = StrategyIdea.watcherPosition;
		Unit closestUnit = UnitUtils.getClosestUnitToPosition(unitList, goalPosition);
		if (closestUnit != null) {
			if (StrategyIdea.initiated) {
				Set<UnitInfo> mainSquadEnemies = getMainSquadEnemies();
				if (mainSquadEnemies != null) {
					euiList.addAll(mainSquadEnemies);
				}
			}
			
			addEnemyUnitInfosInRadius(euiList, closestUnit.getPosition(), goalPosition, UnitType.Terran_Vulture.sightRange());
			
			for (Unit unit : unitList) {
				if (unit.getID() == closestUnit.getID() || unit.getDistance(closestUnit) < 500) {
					continue;
				}
				if (!TimeUtils.executeUnitRotation(unit, LagObserver.groupsize())) {
					continue;
				}
				addEnemyUnitInfosInRadius(euiList, unit.getPosition(), goalPosition, UnitType.Terran_Vulture.sightRange());
			}
			
			if (closestUnit.getDistance(goalPosition) < 500) {
				UnitUtils.addEnemyUnitInfosInRadius(TargetFilter.AIR_UNIT, euiList, goalPosition, UnitType.Terran_Vulture.sightRange(), true, false);
			}
		}
	}
	
	private void addEnemyUnitInfosInRadius(Collection<UnitInfo> euis, Position currentPosition, Position goalPosition, int radius) {
		Collection<UnitInfo> values = InfoUtils.enemyUnitInfoMap().values();
		
		for (UnitInfo eui : values) {
			if (euis.contains(eui)) {
				continue;
			}
			if (eui.getUnitID() == 0 && eui.getType() == UnitType.None) {
				continue;
			}
			if (TargetFilter.excludeByFilter(eui, TargetFilter.AIR_UNIT)) {
				continue;
			}
			if (UnitUtils.ignorableEnemyUnitInfo(eui)) {
				continue;
			}
			
			int weaponRange = 0; // radius 안의 공격범위가 닿는 적까지 포함
			if (eui.getType() == UnitType.Terran_Bunker) {
				weaponRange = MyBotModule.Broodwar.enemy().weaponMaxRange(UnitType.Terran_Marine.groundWeapon()) + 96;
			} else {
				if (eui.getType().groundWeapon() != WeaponType.None) {
					weaponRange = Math.max(weaponRange, MyBotModule.Broodwar.enemy().weaponMaxRange(eui.getType().groundWeapon()));
				}
			}
			
			if (eui.getLastPosition().getDistance(currentPosition) < radius + weaponRange) {
				
				boolean add = true;
				if (goalPosition != null) {
					double radian1 = MicroUtils.targetDirectionRadian(currentPosition, goalPosition);
					double radian2 = MicroUtils.targetDirectionRadian(currentPosition, eui.getLastPosition());
//					System.out.println(radian1 + " / " + radian2 + " = " + Math.abs(radian1 - radian2));
					if (Math.abs(radian1 - radian2) < 1.2) {
						add = true;
					}
				}
				
				if (add) {
					euis.add(eui);
				}
			}
		}
	}
}