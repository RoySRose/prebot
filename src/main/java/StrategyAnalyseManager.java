

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.TilePosition;

public class StrategyAnalyseManager {
	
	public enum LastCheckLocation {
		BASE, FIRST_EXPANSION, GAS
	}
	
	private static StrategyAnalyseManager instance = new StrategyAnalyseManager();
	
	public static StrategyAnalyseManager Instance() {
		return instance;
	}
	
	private List<UnitAnalyser> analysers = new ArrayList<>();
	private Strategist strategist = null;

	public int lastCheckFrameBase = 0;
	public int lastCheckFrameGas = 0;
	public int lastCheckFrameFirstExpansion = 0;

	public int getPhase() {
		if (strategist != null) {
			return strategist.getPhase();
		} else {
			return CommonCode.UNKNOWN;
		}
	}
	
	public void update() {
		if (strategist != null && strategist.getPhase() < 3) {
			updateVisitFrame();
			EnemyBuildTimer.Instance().update();
			
			for (UnitAnalyser analyser : analysers) {
				analyser.upateFoundInfo();
				analyser.analyse();
			}
		}

		EnemyStrategy strategyToApply;
		if (strategist != null) {
			strategyToApply = strategist.strategyToApply();
		} else {
			strategyToApply = EnemyStrategy.ZERG_INIT;
		}

		if (!StrategyChanger.stopStrategiestForDebugging) {
			if (strategyToApply != EnemyStrategy.UNKNOWN && StrategyIdea.currentStrategy != strategyToApply) {
//				MyBotModule.Broodwar.printf(UxColor.CHAR_WHITE + "ENEMY STRATEY : " + strategyToApply.name());
				StrategyIdea.strategyHistory.add(StrategyIdea.currentStrategy);
				StrategyIdea.currentStrategy = strategyToApply;
				this.applyDetailValue(strategyToApply);
			}
		}
		
		if (InfoUtils.enemyRace() == Race.Protoss && UnitUtils.myFactoryUnitSupplyCount() >= 3 * 3) {
			StrategyIdea.marineCount = 0;
		}
	}

	private void applyDetailValue(EnemyStrategy currentStrategy) {
		StrategyIdea.factoryRatio = currentStrategy.factoryRatio;
		StrategyIdea.upgrade = currentStrategy.upgrade;
		StrategyIdea.marineCount = currentStrategy.marineCount;

		// addOn option
		if (currentStrategy.addOnOption != null) {
			StrategyIdea.addOnOption = currentStrategy.addOnOption;
		}
		
		// air unit count
		if (currentStrategy.expansionOption != null) {
			StrategyIdea.expansionOption = currentStrategy.expansionOption;
			if (currentStrategy.expansionOption == EnemyStrategyOptions.ExpansionOption.TWO_STARPORT) {
				StrategyIdea.wraithCount = 4;
				StrategyIdea.valkyrieCount = 0;
			} else if (currentStrategy.expansionOption == EnemyStrategyOptions.ExpansionOption.ONE_STARPORT) {
				StrategyIdea.wraithCount = 0;
				StrategyIdea.valkyrieCount = 2;
			} else if (currentStrategy.expansionOption == EnemyStrategyOptions.ExpansionOption.ONE_FACTORY || currentStrategy.expansionOption == EnemyStrategyOptions.ExpansionOption.TWO_FACTORY) {
				StrategyIdea.wraithCount = 0;
				StrategyIdea.valkyrieCount = 0;
			}
		}
		if (currentStrategy.buildTimeMap != null) {
			StrategyIdea.buildTimeMap = currentStrategy.buildTimeMap;
		}
	}

	public void setUp(Race race) {
		if (race == Race.Protoss) {
			analysers.add(new AdunAnalyser());
			analysers.add(new AssimilatorAnalyser());
			analysers.add(new CannonAnalyser());
			analysers.add(new CoreAnalyser());
			analysers.add(new ForgeAnalyser());
			analysers.add(new GateAnalyser());
			analysers.add(new NexsusAnalyser());
			analysers.add(new ObservatoryAnalyser());
			analysers.add(new RoboticsAnalyser());
			analysers.add(new RoboticsSupportAnalyser());
			analysers.add(new StargateAnalyser());
			analysers.add(new TemplarArchAnalyser());
			analysers.add(new FleetBeaconAnalyser());
			
			analysers.add(new ZealotAnalyser());
			analysers.add(new DragoonAnalyser());
			analysers.add(new DarkTemplarAnalyser());
			analysers.add(new ShuttleAnalyser());
			analysers.add(new ReaverAnalyser());
			analysers.add(new ObserverAnalyser());
			
			strategist = new ProtossStrategist();
			
		} else if (race == Race.Zerg) {
			analysers.add(new ExtractorAnalyser());
			analysers.add(new HatcheryAnalyser());
			analysers.add(new HydraDenAnalyser());
			analysers.add(new LairAnalyser());
			analysers.add(new OverloadAnalyser());
			analysers.add(new SpawningPoolAnalyser());
			analysers.add(new SpireAnalyser());

			analysers.add(new ZerglingAnalyser());
			analysers.add(new HydraliskAnalyser());
			analysers.add(new LurkerAnalyser());
			analysers.add(new MutaliskAnalyser());
			
			strategist = new ZergStrategist();
			
		} else if (race == Race.Terran) {
			analysers.add(new AcademyAnalyser());
			analysers.add(new BarracksAnalyser());
			analysers.add(new CommandCenterAnalyser());
			analysers.add(new FactoryAnalyser());
			analysers.add(new RefineryAnalyser());
			analysers.add(new StarportAnalyser());

			analysers.add(new MarineAnalyser());
			analysers.add(new MedicAnalyser());
			analysers.add(new FirebatAnalyser());
			analysers.add(new VultureAnalyser());
			analysers.add(new TankAnalyser());
			analysers.add(new GoliathAnalyser());
			analysers.add(new WraithAnalyser());
			analysers.add(new DropshipAnalyser());
			
			strategist = new TerranStrategist();
		}
	}
	
	/// 유닛 발견 맵을 업데이트한다.
	private void updateVisitFrame() {
		if (InfoUtils.enemyBase() == null || InfoUtils.enemyBaseGas() == null) {
			return;
		}
		
		TilePosition enemyBaseTile = InfoUtils.enemyBase().getTilePosition();
		TilePosition enemyGasTile = InfoUtils.enemyBaseGas().getTilePosition();
		TilePosition enemyFirstExpansionTile = InfoUtils.enemyFirstExpansion().getTilePosition();
		
		if (MyBotModule.Broodwar.isVisible(enemyBaseTile)) {
			lastCheckFrameBase = TimeUtils.elapsedFrames();
		}
		if (MyBotModule.Broodwar.isVisible(enemyGasTile)) {
			lastCheckFrameGas = TimeUtils.elapsedFrames();
		}
		if (MyBotModule.Broodwar.isVisible(enemyFirstExpansionTile)) {
			lastCheckFrameFirstExpansion = TimeUtils.elapsedFrames();
		}
	}
	
	public int lastCheckFrame(StrategyAnalyseManager.LastCheckLocation lastCheckLocation) {
		if (lastCheckLocation == StrategyAnalyseManager.LastCheckLocation.BASE) {
			return StrategyAnalyseManager.Instance().lastCheckFrameBase;
		} else if (lastCheckLocation == StrategyAnalyseManager.LastCheckLocation.FIRST_EXPANSION) {
			return StrategyAnalyseManager.Instance().lastCheckFrameFirstExpansion;
		} else if (lastCheckLocation == StrategyAnalyseManager.LastCheckLocation.GAS) {
			return StrategyAnalyseManager.Instance().lastCheckFrameGas;
		} else {
			return CommonCode.NONE;
		}
	}

}
