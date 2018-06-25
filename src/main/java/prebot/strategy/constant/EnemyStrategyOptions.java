package prebot.strategy.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode;
import prebot.common.util.TimeUtils;

public class EnemyStrategyOptions {
	
	public static class FactoryRatio {
		public int vulture;
		public int tank;
		public int goliath;
		
		private FactoryRatio(int vulture, int tank, int goliath) {
			this.vulture = vulture;
			this.tank = tank;
			this.goliath = goliath;
		}
		public static FactoryRatio ratio(int vulture, int tank, int goliath) {
			return new FactoryRatio(vulture, tank, goliath);
		}
		@Override
		public String toString() {
			return "UNIT RATIO : vulture=" + vulture + ", tank=" + tank + ", goliath=" + goliath;
		}
	}
	
	public static class MarineCount {
		public static final int NO_MARINE = 0;
		public static final int TWO_MARINE = 2;
		public static final int FOUR_MARINE = 4;
		public static final int SIX_MARINE = 6;
		public static final int EIGHT_MARINE = 8;
	}
	
	public static enum AddOnOption {
		IMMEDIATELY, VULTURE_FIRST;
	}
	
	public static enum ExpansionOption {
		ONE_FACTORY, TWO_FACTORY, TWO_STARPORT;
	}

	public static class UpgradeOrder {
		// 시즈업
		public static final List<MetaType> TS = Arrays.asList(new MetaType(TechType.Tank_Siege_Mode));
		
		// 마인업-시즈업-속도업
		public static final List<MetaType> VM_TS_VS = Arrays.asList(new MetaType(TechType.Spider_Mines)
				, new MetaType(TechType.Tank_Siege_Mode)
				, new MetaType(UpgradeType.Ion_Thrusters));
		
		// 시즈업-마인업-속도업
		public static final List<MetaType> TS_VM_VS = Arrays.asList(new MetaType(TechType.Tank_Siege_Mode)
				, new MetaType(TechType.Spider_Mines)
				, new MetaType(UpgradeType.Ion_Thrusters));
		
		// 속도업-시즈업-마인업
		public static final List<MetaType> VS_TS_VM = Arrays.asList(new MetaType(UpgradeType.Ion_Thrusters)
				, new MetaType(TechType.Tank_Siege_Mode)
				, new MetaType(TechType.Spider_Mines));
		
		// 마인업-시즈업-속도업-골리앗업
		public static final List<MetaType> VM_TS_VS_GR = Arrays.asList(new MetaType(TechType.Spider_Mines)
				, new MetaType(TechType.Tank_Siege_Mode)
				, new MetaType(UpgradeType.Ion_Thrusters)
				, new MetaType(UpgradeType.Charon_Boosters));
		
		// 마인업-골리앗업-속도업-시즈업
		public static final List<MetaType> VM_GR_VS_TS = Arrays.asList(new MetaType(TechType.Spider_Mines)
				, new MetaType(UpgradeType.Charon_Boosters)
				, new MetaType(UpgradeType.Ion_Thrusters)
				, new MetaType(TechType.Tank_Siege_Mode));
		
		// 시즈업-마인업-속도업-골리앗업
		public static final List<MetaType> TS_VM_VS_GR = Arrays.asList(new MetaType(TechType.Tank_Siege_Mode)
				, new MetaType(TechType.Spider_Mines)
				, new MetaType(UpgradeType.Ion_Thrusters)
				, new MetaType(UpgradeType.Charon_Boosters));
		
		// 속도업-마인업-골리앗업-시즈업
		public static final List<MetaType> VS_VM_GR_TS = Arrays.asList(new MetaType(UpgradeType.Ion_Thrusters)
				, new MetaType(TechType.Spider_Mines)
				, new MetaType(UpgradeType.Charon_Boosters)
				, new MetaType(TechType.Tank_Siege_Mode));
		
		// 마인업-골리앗업-시즈업-속도업
		public static final List<MetaType> VM_GR_TS_VS = Arrays.asList(new MetaType(TechType.Spider_Mines),
				new MetaType(UpgradeType.Charon_Boosters),
				new MetaType(TechType.Tank_Siege_Mode),
				new MetaType(UpgradeType.Ion_Thrusters));
	}
	
	public static class DefaultTimeMap {
		private Map<UnitType, List<Integer>> buildingTimer = new HashMap<>();
		private Map<TechType, Integer> techTimer = new HashMap<>();
		private Map<UpgradeType, Integer> upgradeTimer = new HashMap<>();
		
		public DefaultTimeMap putAll(DefaultTimeMap defaultTimeMap) {
			buildingTimer.putAll(defaultTimeMap.buildingTimer);
			techTimer.putAll(defaultTimeMap.techTimer);
			upgradeTimer.putAll(defaultTimeMap.upgradeTimer);
			return this;
		}
		public DefaultTimeMap put(UnitType unitType, int minutes, int seconds) {
			int defaultTime = TimeUtils.timeToFrames(minutes, seconds);
			List<Integer> defaultTimeList = buildingTimer.get(unitType);
			if (defaultTimeList == null) {
				defaultTimeList = new ArrayList<>();
			}
			defaultTimeList.add(defaultTime);
			buildingTimer.put(unitType, defaultTimeList);
			return this;
		}
		public DefaultTimeMap put(TechType techType, int minutes, int seconds) {
			int defaultTime = TimeUtils.timeToFrames(minutes, seconds);
			techTimer.put(techType, defaultTime);
			return this;
		}
		public DefaultTimeMap put(UpgradeType upgradeType, int minutes, int seconds) {
			int defaultTime = TimeUtils.timeToFrames(minutes, seconds);
			upgradeTimer.put(upgradeType, defaultTime);
			return this;
		}
		public int time(UnitType unitType) {
			return timeOfIndex(unitType, 0);
		}
		public int time(UnitType unitType, int margin) {
			return timeOfIndex(unitType, 0) + margin * TimeUtils.SECOND;
		}
		public int timeOfIndex(UnitType unitType, int index) {
			List<Integer> defaultTimeList = buildingTimer.get(unitType);
			if (defaultTimeList == null || defaultTimeList.size() <= index) {
				return CommonCode.UNKNOWN;
			}
			return defaultTimeList.get(index);
		}
		public int timeOfIndex(UnitType unitType, int index, int margin) {
			return timeOfIndex(unitType, index) + margin * TimeUtils.SECOND;
		}
		public int time(TechType techType) {
			Integer defaultTime = techTimer.get(techType);
			return defaultTime != null ? defaultTime : CommonCode.UNKNOWN;
		}
		public int time(TechType techType, int margin) {
			return time(techType) + margin * TimeUtils.SECOND;
		}
		public int time(UpgradeType upgradeType) {
			Integer defaultTime = upgradeTimer.get(upgradeType);
			return defaultTime != null ? defaultTime : CommonCode.UNKNOWN;
		}
		public int time(UpgradeType upgradeType, int margin) {
			return time(upgradeType) + margin * TimeUtils.SECOND;
		}
		@Override
		public String toString() {
			return "DefaultTimeMap [buildingTimer=" + buildingTimer + ", techTimer=" + techTimer + ", upgradeTimer=" + upgradeTimer + "]";
		}
	}
}
