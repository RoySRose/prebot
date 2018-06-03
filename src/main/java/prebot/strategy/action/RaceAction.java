package prebot.strategy.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.common.constant.CommonCode;
import prebot.common.constant.CommonCode.EnemyUnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.UnitInfo;
import prebot.strategy.constant.StrategyConfig.EnemyBuild;

public abstract class RaceAction extends Action {

	protected EnemyBuild enemyBuildOfPhase = EnemyBuild.UNKNOWN; // TODO diplay
	protected List<UnitType> foundUnitTypes = new ArrayList<>(); // TODO diplay

	protected Map<UnitType, List<UnitInfo>> enemyUnitInfoMap = new HashMap<>(); // TODO display 유닛타입별 발견시점의 UnitInfo
	protected Map<Integer, Integer> buildStartSecMap = new HashMap<>(); // TODO display 유닛ID별 빌드시작 시작(건물)

	protected int enemyGasFoundSec = CommonCode.UNKNOWN;
	protected int enemyGasBuildSec = CommonCode.UNKNOWN;
	protected int enemyGasNotFoundSec = CommonCode.UNKNOWN;

	private Race race;
	private int phasEndSec;
	private boolean buildFoundFlag;

	public Race getRace() {
		return race;
	}

	public RaceAction(Race race, int phasEndSec) {
		super();
		this.race = race;
		this.phasEndSec = phasEndSec;
	}

	@Override
	public boolean exitCondition() {
		// 전략이 파악되었다.
		if (enemyBuildOfPhase != EnemyBuild.UNKNOWN) {
			StrategyIdea.enemyBuildPhase1 = enemyBuildOfPhase;
			return true;
		}

		// 일정시간이 지날 때까지 전략을 알지 못하였다.
		if (TimeUtils.before(phasEndSec)) {
			StrategyIdea.enemyBuildPhase1 = finalExpect();
			return true;
		}

		return false;
	}

	@Override
	public void action() {
		updateInfo();
		if (!buildFoundFlag) {
			buildFoundFlag = analyse();
		}
		expectBuild();
	}

	protected abstract boolean analyse();
	protected abstract void expectBuild();
	protected abstract EnemyBuild finalExpect();

	/// 유닛 발견 맵을 업데이트한다.
	protected void updateInfo() {
		for (UnitType unitType : foundUnitTypes) {
			List<UnitInfo> euiListFound = UnitUtils.getEnemyUnitInfoList(EnemyUnitFindRange.VISIBLE, unitType);
			if (euiListFound.isEmpty()) {
				continue;
			}

			List<UnitInfo> euiListSaved = enemyUnitInfoMap.get(unitType);
			if (euiListSaved == null || euiListSaved.isEmpty()) {
				euiListSaved = new ArrayList<>();
			}

			for (UnitInfo euiFound : euiListFound) {
				boolean containUnit = false;
				for (UnitInfo euiSaved : euiListSaved) {
					if (euiFound.getUnitID() == euiSaved.getUnitID()) {
						containUnit = true;
						break;
					}
				}
				if (!containUnit) {
					euiListSaved.add(euiFound);
					if (euiFound.getType().isBuilding()) {
						buildStartSecMap.put(euiFound.getUnitID(), TimeUtils.buildStartSeconds(euiFound.getUnit())); // 빌드시작한 시잔 (초). 이미 완성되어 알 수 없다면 UNKNOWN

						if (enemyGasFoundSec == CommonCode.UNKNOWN && unitType.isRefinery()) {
							enemyGasFoundSec = TimeUtils.elapsedSeconds();
							enemyGasBuildSec = TimeUtils.buildStartSeconds(euiFound.getUnit());
						}
					}
				}
			}
			enemyUnitInfoMap.put(unitType, euiListSaved);
		}

		if (enemyGasFoundSec == CommonCode.UNKNOWN) {
			BaseLocation enemyBase = InfoUtils.enemyBase();
			if (enemyBase != null) {
				List<Unit> geysers = enemyBase.getGeysers();
				if (!geysers.isEmpty()) {
					Unit enemyGas = geysers.get(0);
					if (Prebot.Broodwar.isVisible(enemyGas.getTilePosition())) {
						enemyGasNotFoundSec = TimeUtils.elapsedSeconds();
					}
				}
			}
		}
	}
}
