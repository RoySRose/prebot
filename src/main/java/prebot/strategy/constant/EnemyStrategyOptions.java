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
		public static final int ONE_MARINE = 0;
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

		// 마인업-속도업-시즈업
		public static final List<MetaType> VM_VS_TS = Arrays.asList(new MetaType(TechType.Spider_Mines)
				, new MetaType(UpgradeType.Ion_Thrusters)
				, new MetaType(TechType.Tank_Siege_Mode));
		
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
	
	public static class BuildTimeMap {
		private boolean isDouble = false;
		private boolean isMechanic = false;
		
		private Map<UnitType, List<Integer>> buildingTime = new HashMap<>();
		private Map<TechType, Integer> techTime = new HashMap<>();
		private Map<UpgradeType, Integer> upgradeTime = new HashMap<>();
		
		public BuildTimeMap setDouble() {
			this.isDouble = true;
			return this;
		}
		public BuildTimeMap setMechanic() {
			this.isMechanic = true;
			return this;
		}
		public BuildTimeMap put(UnitType unitType, int minutes, int seconds) {
			int defaultTime = TimeUtils.timeToFrames(minutes, seconds);
			List<Integer> defaultTimeList = buildingTime.get(unitType);
			if (defaultTimeList == null) {
				defaultTimeList = new ArrayList<>();
			}
			defaultTimeList.add(defaultTime);
			buildingTime.put(unitType, defaultTimeList);
			return this;
		}
		public BuildTimeMap put(TechType techType, int minutes, int seconds) {
			int defaultTime = TimeUtils.timeToFrames(minutes, seconds);
			techTime.put(techType, defaultTime);
			return this;
		}
		public BuildTimeMap put(UpgradeType upgradeType, int minutes, int seconds) {
			int defaultTime = TimeUtils.timeToFrames(minutes, seconds);
			upgradeTime.put(upgradeType, defaultTime);
			return this;
		}
		public BuildTimeMap putAll(BuildTimeMap defaultTimeMap) {
			buildingTime.putAll(defaultTimeMap.buildingTime);
			techTime.putAll(defaultTimeMap.techTime);
			upgradeTime.putAll(defaultTimeMap.upgradeTime);
			return this;
		}
		public int frame(UnitType unitType) {
			return frameOfIndex(unitType, 0);
		}
		public int frame(UnitType unitType, int margin) {
			return frameOfIndex(unitType, 0) + margin * TimeUtils.SECOND;
		}
		public int frameOfIndex(UnitType unitType, int index) {
			List<Integer> defaultTimeList = buildingTime.get(unitType);
			if (defaultTimeList == null || defaultTimeList.size() <= index) {
				return CommonCode.UNKNOWN;
			}
			return defaultTimeList.get(index);
		}
		public int frameOfIndex(UnitType unitType, int index, int margin) {
			return frameOfIndex(unitType, index) + margin * TimeUtils.SECOND;
		}
		public int frame(TechType techType) {
			Integer defaultTime = techTime.get(techType);
			return defaultTime != null ? defaultTime : CommonCode.UNKNOWN;
		}
		public int frame(TechType techType, int margin) {
			return frame(techType) + margin * TimeUtils.SECOND;
		}
		public int frame(UpgradeType upgradeType) {
			Integer defaultTime = upgradeTime.get(upgradeType);
			return defaultTime != null ? defaultTime : CommonCode.UNKNOWN;
		}
		public int frame(UpgradeType upgradeType, int margin) {
			return frame(upgradeType) + margin * TimeUtils.SECOND;
		}
		public boolean isDouble() {
			return isDouble;
		}
		public boolean isMechanic() {
			return isMechanic;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (UnitType unitType : buildingTime.keySet()) {
				List<Integer> times = buildingTime.get(unitType);
				List<String> timeStrings = new ArrayList<>();
				for (int time : times) {
					timeStrings.add(TimeUtils.framesToTimeString(time));
				}
				sb.append(unitType).append(timeStrings).append("\n");
			}
			for (TechType techType : techTime.keySet()) {
				Integer time = techTime.get(techType);
				sb.append(techType).append(TimeUtils.framesToTimeString(time)).append("\n");
			}
			for (UpgradeType upgradeType : upgradeTime.keySet()) {
				Integer time = upgradeTime.get(upgradeType);
				sb.append(upgradeType).append(TimeUtils.framesToTimeString(time)).append("\n");
			}
			
			return "[DefaultTimeMap]\n" + sb.toString();
		}
	}
}
