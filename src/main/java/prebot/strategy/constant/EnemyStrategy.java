package prebot.strategy.constant;

import java.util.Arrays;
import java.util.List;

import bwapi.TechType;
import bwapi.UpgradeType;
import prebot.common.MetaType;

public class EnemyStrategy {
	
	public static enum MarineOption {
		NORMAL, STOP, MORE;
	}
	
	public static enum AddOnOption {
		IMMEDIATELY, VULTURE_FIRST;
	}
	
	public static enum ExpansionOption {
		ONE_FACTORY, TWO_FACTORY;
	}
	
	public static class FactoryRatio {
		public int vulture;
		public int tank;
		public int goliath;
		
		private FactoryRatio(int vulture, int tank, int goliath) {
			this.vulture = vulture;
			this.tank = tank;
			this.goliath = goliath;
		}
		private static FactoryRatio ratio(int vulture, int tank, int goliath) {
			return new FactoryRatio(vulture, tank, goliath);
		}
	}
	
	// 마인업-시즈업-속도업
	private static final List<MetaType> VM_TS_VS = Arrays.asList(new MetaType(TechType.Spider_Mines)
			, new MetaType(TechType.Tank_Siege_Mode)
			, new MetaType(UpgradeType.Ion_Thrusters));
	
	// 시즈업-마인업-속도업
	private static final List<MetaType> TS_VM_VS = Arrays.asList(new MetaType(TechType.Tank_Siege_Mode)
			, new MetaType(TechType.Spider_Mines)
			, new MetaType(UpgradeType.Ion_Thrusters));
	
	// 마인업-시즈업-속도업-골리앗업
	private static final List<MetaType> VM_TS_VS_GR = Arrays.asList(new MetaType(TechType.Spider_Mines)
			, new MetaType(TechType.Tank_Siege_Mode)
			, new MetaType(UpgradeType.Ion_Thrusters)
			, new MetaType(UpgradeType.Charon_Boosters));
	
	// 마인업-골리앗업-속도업-시즈업
	private static final List<MetaType> VM_GR_VS_TS = Arrays.asList(new MetaType(TechType.Spider_Mines)
			, new MetaType(UpgradeType.Charon_Boosters)
			, new MetaType(UpgradeType.Ion_Thrusters)
			, new MetaType(TechType.Tank_Siege_Mode));
	
	// 속도업-마인업-골리앗업-시즈업
	private static final List<MetaType> VS_VM_GR_TS = Arrays.asList(new MetaType(UpgradeType.Ion_Thrusters)
			, new MetaType(TechType.Spider_Mines)
			, new MetaType(UpgradeType.Charon_Boosters)
			, new MetaType(TechType.Tank_Siege_Mode));
	
	// 마인업-골리앗업-시즈업-속도업
	private static final List<MetaType> VM_GR_TS_VS = Arrays.asList(new MetaType(TechType.Spider_Mines),
			new MetaType(UpgradeType.Charon_Boosters),
			new MetaType(TechType.Tank_Siege_Mode),
			new MetaType(UpgradeType.Ion_Thrusters));
	
	public static enum Strategy {
		// TODO 1. 원팩멀티일때 앞마당심시티 (TBD) 서플+배럭+엔베
		// TODO 2. 엔지니어링베이, 터렛, 벙커 건설타이밍을 시간으로 준다.
		// TODO 3. 벙커건설이 끼어들어오면 마린이 있어야 한다. 없으면 생산한다.
		
		// [토스전 기본전략]
		// : 1팩 -> 마린6기 -> 애드온 -> 탱크 -> 마인업 -> 벌처 -> 앞마당전진(FD) -> 멀티 -> 시즈업
		
		// PHASE1 : 시작 ~ 코어완료 OR 일정시간 경과
		PROTOSS_INIT(1, 1, 0, VM_TS_VS,
				MarineOption.NORMAL, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), // INIT DEFAULT
		
		PROTOSS_ONEGATE_CORE(1, 1, 0, VM_TS_VS,
				MarineOption.NORMAL, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), //
		
		PROTOSS_TWOGATE(6, 1, 0, VM_TS_VS,
				MarineOption.STOP, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // 
		
		PROTOSS_DOUBLE(1, 1, 0, VM_TS_VS,
				MarineOption.STOP, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY), // camp=F_EXPANSION
		
		PROTOSS_FORGE(1, 1, 0, TS_VM_VS,
				MarineOption.STOP, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY), //
		// 넥서스가 소환되지 않는 경우 포톤러시를 의심해야 한다.
		
		PROTOSS_FORGE_DOUBLE(1, 1, 0, TS_VM_VS,
				MarineOption.STOP, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY), // camp=S_CHOKE, 공격실패시 F_EXPANSION
		
		
		// PHASE2 : PHASE1 종료 ~ PHASE2 에 대한 위험이 종료되는 시점 (camp가 F_EXPANSION으로 이동, 적 병력/다크, 아군 병력/터렛/컴셋 고려)
		// 원게이트 코어 정석빌드에서 연계되는 것에 대한 대비. 더블이나 투게이트 이후 연계는 이미 대비가 되어 있을 것이므로 아마 곧바로 종료.
		PROTOSS_MORE_GATE(1, 1, 0, TS_VM_VS,
				MarineOption.NORMAL, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), // camp=F_EXPANSION
		// + 위험종료 : PROTOSS_DEFAULT와 다르지 않으므로 바로종료 (TODO TBD: 정면이 위험하면 입구심시티)
		
		PROTOSS_TEMPLAR(1, 1, 0, VM_TS_VS,
				MarineOption.NORMAL, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), // camp=(다크타이밍이 안전할 경우에만) F_EXPANSION
		// + WATCHER : 마인매설(+본진, 앞마당), attack=S_CHOKE
		// + CHECKER : 할당량 감소(거의 할당하지 않음)
		// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 벌처 일정량 이상 보유.
		
		PROTOSS_ROBOTICS(5, 5, 2, VM_TS_VS,
				MarineOption.NORMAL, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), //
		// + WATCHER : 마인매설(+본진)
		// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 생산 완료.
		
		PROTOSS_STARGATE(5, 5, 2, VM_TS_VS,
				MarineOption.NORMAL, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), //
		// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 생산 완료.

		// PHASE3 - PHASE2 종료 ~
		PROTOSS_DEFAULT(1, 1, 0, VM_TS_VS),
		PROTOSS_PROTOSS_ARBITER(5, 5, 1, VM_TS_VS_GR),
		PROTOSS_PROTOSS_CARRIER(1, 1, 8, VM_TS_VS_GR),
		
		// [저그전 기본전략]
		// : 8배럭 -> 2scv정찰 -> 마린6기 -> 1팩 -> 벌처 -> 애드온 -> 마인업 -> 2팩 -> 멀티

		// PHASE1 : 시작 ~ 레어발견 OR 일정시간 경과
		ZERG_INIT(2, 1, 2, VM_GR_TS_VS
				, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // INIT DEFAULT, camp=S_CHOKE,
		
		ZERG_5DRONE(7, 1, 1, VS_VM_GR_TS
				, MarineOption.MORE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=BASE, 벙커 
		ZERG_9DRONE(2, 1, 2, VM_GR_TS_VS, MarineOption.MORE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_CHOKE, 벙커
		ZERG_9DRONE_GAS(2, 1, 2, VS_VM_GR_TS, MarineOption.MORE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // 마린=MORE, camp=F_CHOKE, 벙커
		ZERG_OVER_POOL(2, 1, 2, VM_GR_TS_VS, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_CHOKE
		ZERG_OVER_POOL_GAS(2, 1, 2, VS_VM_GR_TS, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_CHOKE
		ZERG_2HAT(2, 1, 2, VM_GR_TS_VS, MarineOption.MORE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=S_CHOKE, 벙커(공격)
		ZERG_2HAT_GAS(2, 1, 2, VM_GR_VS_TS, MarineOption.MORE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=S_CHOKE, 벙커(공격)

		// PHASE2 : PHASE1 종료 ~ PHASE2 에 대한 위험이 종료되는 시점 (camp가 F_EXPANSION으로 이동, 적 병력/다크, 아군 병력/터렛/컴셋 고려)
		ZERG_NO_LAIR_LING(3, 1, 0, VS_VM_GR_TS, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION, 벙커
		// + CHECKER : 할당량 감소(거의 할당하지 않음)
		// + 위험종료 : BASE근처에 적이 없음. 벙커 완성. 벌처 일정량 이상 보유.
		
		ZERG_NO_LAIR_HYDRA(1, 5, 0, VM_TS_VS_GR, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION, 벙커
		// + WATCHER : 마인매설(+촘촘하게)
		// + CHECKER : 할당량 감소(거의 할당하지 않음)
		// + 위험종료 : BASE근처에 적이 없음. 벙커 완성. 탱크 일정량 이상 보유.
		
		ZERG_HYDRA_TECH(1, 5, 1, VM_TS_VS_GR, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION
		// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 탱크 일정량 이상 보유.
		
		ZERG_SPIRE_TECH(1, 1, 15, VM_GR_VS_TS, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION
		// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 일정량 이상 보유.
		
		ZERG_MIXTED_TECH(1, 3, 5, VM_GR_VS_TS, MarineOption.NORMAL, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION
		// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 일정량 이상 보유.
		
		// PHASE3 : PHASE2 종료 ~
		ZERG_DEFAULT(0, 2, 10, VM_GR_TS_VS),
		ZERG_HYDRA_MANY(1, 5, 2, VM_TS_VS_GR),
		ZERG_MUTAL_MANY(1, 1, 10, VM_GR_TS_VS),
		ZERG_LURKER(1, 2, 1, VM_TS_VS_GR),
		ZERG_MIXED(1, 2, 6, VM_GR_TS_VS),
		ZERG_LING_MUTAL(1, 2, 4, VM_GR_TS_VS),
		ZERG_ULTRA(1, 2, 1, VM_TS_VS_GR);

		// [테란전 기본전략]
		// 1배럭 -> 커맨드 -> 마린2기 -> 가스 -> 1팩 -> 1스타 -> 시즈업 -> 3팩
		
		
		private Strategy(int vulture, int tank, int goliath, List<MetaType> upgrade) {
			this.ratio = FactoryRatio.ratio(vulture, tank, goliath);
			this.upgrade = upgrade;
			this.marineOption = MarineOption.NORMAL;
			this.addOnOption = AddOnOption.VULTURE_FIRST;
			this.expansionOption = ExpansionOption.TWO_FACTORY;
		}

		private Strategy(int vulture, int tank, int goliath, List<MetaType> upgrade, MarineOption marineOption, AddOnOption addOnOption, ExpansionOption expansionOption) {
			this.ratio = FactoryRatio.ratio(vulture, tank, goliath);
			this.upgrade = upgrade;
			this.marineOption = marineOption;
			this.addOnOption = addOnOption;
			this.expansionOption = expansionOption;
		}
		
		public FactoryRatio ratio;
		public List<MetaType> upgrade;
		public MarineOption marineOption;
		public AddOnOption addOnOption;
		public ExpansionOption expansionOption;
	}
}
