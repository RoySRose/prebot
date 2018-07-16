package prebot.micro.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.Region;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.constant.CommonCode.RegionType;
import prebot.common.util.InfoUtils;
import prebot.common.util.UnitUtils;
import prebot.micro.SquadData;
import prebot.micro.WorkerManager;
import prebot.micro.WorkerData.WorkerJob;
import prebot.micro.constant.MicroConfig.SquadInfo;
import prebot.micro.control.GundamControl;
import prebot.micro.control.MarineControl;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyCode.EnemyUnitStatus;

public class EarlyDefenseSquad extends Squad {

	private GundamControl gundamControl = new GundamControl();
	private MarineControl marineControl = new MarineControl();
	
	private static final int REACT_RADIUS = 50;

	public EarlyDefenseSquad() {
		super(SquadInfo.EARLY_DEFENSE);
	}

	@Override
	public boolean want(Unit unit) {
		if (unit.getType() == UnitType.Terran_Marine) {
			return true;
		}
		if (unit.getType() == UnitType.Terran_SCV) {
			List<Unit> enemyUnitsInRegion = UnitUtils.getUnitsInRegion(RegionType.MY_BASE, PlayerRange.ENEMY);
			if (!enemyUnitsInRegion.isEmpty()) {
				return unit.getHitPoints() > 16;
			}
		}
		return false;
	}

	@Override
	public List<Unit> recruit(List<Unit> assignableUnitList) {
		List<Unit> marineList = new ArrayList<>();
		List<Unit> scvList = new ArrayList<>();
		
		for (Unit unit : assignableUnitList) {
			if (unit.getType() == UnitType.Terran_Marine) {
				marineList.add(unit);
			} else if (unit.getType() == UnitType.Terran_SCV && WorkerManager.Instance().getWorkerData().getWorkerJob(unit) != WorkerJob.Scout) {
				scvList.add(unit);
			}
		}
		
		if (StrategyIdea.enemyUnitStatus == EnemyUnitStatus.IN_MY_REGION && !InformationManager.Instance().isBlockingEnterance()) {
			return defenseForScvAndMarine(marineList, scvList, euiList);
		} else {
			return marineList;
		}
	}

	private List<Unit> defenseForScvAndMarine(List<Unit> marineList, List<Unit> scvList, List<UnitInfo> euiList) {
		// TODO 포톤캐넌, 파일런, 벙커 등 전략 대응. 반응거리(REACT_RADIUS) 더 길게 처리. 건물 당 SCV 몇기를 동원할지 등 처리
		List<Unit> enemyInSightList = new ArrayList<>();
		for (UnitInfo eui : euiList) {
			Unit enemy = UnitUtils.unitInSight(eui);
			if (enemy != null) {
				enemyInSightList.add(enemy);
			}
		}
		// 메인베이스와 가장 가까운 적 유닛이, 아군유닛의 REACT_RADIUS 내로 들어왔으면 유닛 할당
		Unit closeEnemyUnit = UnitUtils.getClosestUnitToPosition(enemyInSightList, InfoUtils.myBase().getPosition());
		if (closeEnemyUnit == null) {
			return marineList;
		}
		List<Unit> myUnitList = UnitUtils.getUnitsInRadius(PlayerRange.SELF, closeEnemyUnit.getPosition(), REACT_RADIUS); 
		if (myUnitList.isEmpty() && !marineList.isEmpty()) {
			return marineList;
		}
		
		// 얼마나 SCV 동원이 필요한지 체크
		double scvCountForDefense = scvCountForDefense(enemyInSightList);
		
		/*유닛이 줄었을때 필요일꾼 만큼만 스쿼드 유지 나머지는 idle*/
		while(marineList.size() + unitList.size() > scvCountForDefense){
			SquadData squadData = new SquadData();
			Unit defenseScv = UnitUtils.getFarthestCombatWorkerToPosition(unitList, closeEnemyUnit.getPosition());
			if (defenseScv == null) {
				break;
			}
			squadData.excludeUnitFromSquad(defenseScv);
			unitList.remove(defenseScv);
		}
		
		List<Unit> recruitScvList = new ArrayList<>();
		while (unitList.size() + marineList.size()+ recruitScvList.size()< scvCountForDefense) {
			Unit defenseScv = UnitUtils.getClosestMineralWorkerToPosition(scvList, closeEnemyUnit.getPosition());
			if (defenseScv == null) {
				break;
			}
			recruitScvList.add(defenseScv);
			scvList.remove(defenseScv);
		}
		
		return recruitScvList;
	}

	private double scvCountForDefense(List<Unit> enemyInSightList) {
		Region campRegion = BWTA.getRegion(StrategyIdea.campPosition);
		Unit bunker = marineControl.getCompleteBunker(campRegion);
		
		double scvCountForDefense = 0.0;
		for (Unit enemy : enemyInSightList) {
			if (UnitUtils.isValidUnit(enemy)) {
				if (bunker == null) {
					if (enemy.getType() == UnitType.Protoss_Probe || enemy.getType() == UnitType.Zerg_Drone) {
						scvCountForDefense += 1.0;
					} else if (enemy.getType() == UnitType.Terran_SCV) {
						scvCountForDefense += 1.5;
					} else if (enemy.getType() == UnitType.Protoss_Zealot) {
						scvCountForDefense += 4;
					} else if (enemy.getType() == UnitType.Zerg_Zergling) {
						scvCountForDefense += 2;
					}  else if (enemy.getType() == UnitType.Terran_Marine) {
						scvCountForDefense += 2;
					} else if (enemy.getType().isBuilding()) {
						scvCountForDefense += 3;
					} 
				}else  if(bunker.getLoadedUnits().size() > 0){
					if (enemy.getType() == UnitType.Protoss_Probe || enemy.getType() == UnitType.Zerg_Drone) {
						scvCountForDefense += 1.0;
					} else if (enemy.getType() == UnitType.Terran_SCV) {
						scvCountForDefense += 1.0;
					} else if (enemy.getType() == UnitType.Protoss_Zealot) {
						scvCountForDefense += 2;
					} else if (enemy.getType() == UnitType.Zerg_Zergling) {
						scvCountForDefense += 1;
					}  else if (enemy.getType() == UnitType.Terran_Marine) {
						scvCountForDefense += 1;
					} else if (enemy.getType().isBuilding()) {
						scvCountForDefense += 3;
					} 
				}
			}
		}
		return scvCountForDefense;
	}
	
	@Override
	public void findEnemies() {
		euiList.clear();
		
		List<UnitInfo> enemyUnitsInRegion = InfoUtils.euiListInMyRegion(InfoUtils.myBase().getRegion());
		if (enemyUnitsInRegion.size() >= 1) {
			for (UnitInfo enemy : enemyUnitsInRegion) {
					euiList.add(enemy);
			}
		}
		for (Unit unit : unitList) {
			UnitUtils.addEnemyUnitInfosInRadiusForGround(euiList, unit.getPosition(), unit.getType().sightRange() + SquadInfo.EARLY_DEFENSE.squadRadius);
		}
	}

	@Override
	public void execute() {
		Map<UnitType, List<Unit>> unitListMap = UnitUtils.makeUnitListMap(unitList);
		List<Unit> scvList = unitListMap.getOrDefault(UnitType.Terran_SCV, new ArrayList<Unit>());
		List<Unit> marineList = unitListMap.getOrDefault(UnitType.Terran_Marine, new ArrayList<Unit>());
		
		marineControl.control(marineList, euiList);
		gundamControl.control(scvList, euiList);
	}

}
