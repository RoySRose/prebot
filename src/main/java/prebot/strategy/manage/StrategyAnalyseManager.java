package prebot.strategy.manage;

import java.util.ArrayList;
import java.util.List;

import bwapi.Race;
import bwapi.TilePosition;
import prebot.common.constant.CommonCode;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.strategy.StrategyIdea;
import prebot.strategy.analyse.ProtossStrategist;
import prebot.strategy.analyse.Strategist;
import prebot.strategy.analyse.TerranStrategist;
import prebot.strategy.analyse.UnitAnalyser;
import prebot.strategy.analyse.ZergStrategist;
import prebot.strategy.analyse.protoss.AdunAnalyser;
import prebot.strategy.analyse.protoss.AssimilatorAnalyser;
import prebot.strategy.analyse.protoss.CannonAnalyser;
import prebot.strategy.analyse.protoss.CoreAnalyser;
import prebot.strategy.analyse.protoss.ForgeAnalyser;
import prebot.strategy.analyse.protoss.GateAnalyser;
import prebot.strategy.analyse.protoss.NexsusAnalyser;
import prebot.strategy.analyse.protoss.ObservatoryAnalyser;
import prebot.strategy.analyse.protoss.RoboticsAnalyser;
import prebot.strategy.analyse.protoss.RoboticsSupportAnalyser;
import prebot.strategy.analyse.protoss.StargateAnalyser;
import prebot.strategy.analyse.protoss.TemplarArchAnalyser;
import prebot.strategy.analyse.protoss.ZealotAnalyser;
import prebot.strategy.analyse.terran.AcademyAnalyser;
import prebot.strategy.analyse.terran.BarracksAnalyser;
import prebot.strategy.analyse.terran.CommandCenterAnalyser;
import prebot.strategy.analyse.terran.FactoryAnalyser;
import prebot.strategy.analyse.terran.RefineryAnalyser;
import prebot.strategy.analyse.terran.StarportAnalyser;
import prebot.strategy.analyse.zerg.ExtractorAnalyser;
import prebot.strategy.analyse.zerg.HatcheryAnalyser;
import prebot.strategy.analyse.zerg.HydraDenAnalyser;
import prebot.strategy.analyse.zerg.LairAnalyser;
import prebot.strategy.analyse.zerg.OverloadAnalyser;
import prebot.strategy.analyse.zerg.SpawningPoolAnalyser;
import prebot.strategy.analyse.zerg.SpireAnalyser;
import prebot.strategy.analyse.zerg.ZerglingAnalyser;
import prebot.strategy.constant.EnemyStrategy;

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
	
	public void update() {
		updateVisitFrame();
		EnemyBuildTimer.Instance().update();
		
		for (UnitAnalyser analyser : analysers) {
			analyser.upateFoundInfo();
			analyser.analyse();
		}

		if (strategist != null) {
			EnemyStrategy strategyToApply = strategist.strategyToApply();
			
			if (strategyToApply != EnemyStrategy.UNKNOWN && StrategyIdea.currentStrategy != strategyToApply) {
//				Prebot.Broodwar.printf(UxColor.CHAR_WHITE + "ENEMY STRATEY : " + strategyToApply.name());
				StrategyIdea.strategyHistory.add(StrategyIdea.currentStrategy);
				StrategyIdea.currentStrategy = strategyToApply;
			}
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
			analysers.add(new ZealotAnalyser());
			
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
			
			strategist = new ZergStrategist();
			
		} else if (race == Race.Terran) {
			analysers.add(new AcademyAnalyser());
			analysers.add(new BarracksAnalyser());
			analysers.add(new CommandCenterAnalyser());
			analysers.add(new FactoryAnalyser());
			analysers.add(new RefineryAnalyser());
			analysers.add(new StarportAnalyser());
			
			strategist = new TerranStrategist();
		}
	}
	
	/// 유닛 발견 맵을 업데이트한다.
	private void updateVisitFrame() {
		if (InfoUtils.enemyBase() == null) {
			return;
		}
		
		TilePosition enemyBaseTile = InfoUtils.enemyBase().getTilePosition();
		TilePosition enemyGasTile = InfoUtils.enemyBaseGas().getTilePosition();
		TilePosition enemyFirstExpansionTile = InfoUtils.enemyFirstExpansion().getTilePosition();
		
		if (Prebot.Broodwar.isVisible(enemyBaseTile)) {
			lastCheckFrameBase = TimeUtils.elapsedFrames();
		}
		if (Prebot.Broodwar.isVisible(enemyGasTile)) {
			lastCheckFrameGas = TimeUtils.elapsedFrames();
		}
		if (Prebot.Broodwar.isVisible(enemyFirstExpansionTile)) {
			lastCheckFrameFirstExpansion = TimeUtils.elapsedFrames();
		}
	}
	
	public int lastCheckFrame(LastCheckLocation lastCheckLocation) {
		if (lastCheckLocation == LastCheckLocation.BASE) {
			return StrategyAnalyseManager.Instance().lastCheckFrameBase;
		} else if (lastCheckLocation == LastCheckLocation.FIRST_EXPANSION) {
			return StrategyAnalyseManager.Instance().lastCheckFrameFirstExpansion;
		} else if (lastCheckLocation == LastCheckLocation.GAS) {
			return StrategyAnalyseManager.Instance().lastCheckFrameGas;
		} else {
			return CommonCode.NONE;
		}
	}

}
