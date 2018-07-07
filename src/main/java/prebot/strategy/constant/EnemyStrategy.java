package prebot.strategy.constant;

import java.util.List;

import prebot.common.MetaType;
import prebot.strategy.constant.EnemyStrategyOptions.AddOnOption;
import prebot.strategy.constant.EnemyStrategyOptions.BuildTimeMap;
import prebot.strategy.constant.EnemyStrategyOptions.ExpansionOption;
import prebot.strategy.constant.EnemyStrategyOptions.FactoryRatio;
import prebot.strategy.constant.EnemyStrategyOptions.MarineCount;
import prebot.strategy.constant.EnemyStrategyOptions.UpgradeOrder;

public enum EnemyStrategy {

	// TODO 1. 원팩멀티일때 앞마당심시티 (TBD) 서플+배럭+엔베
	// TODO 2. 엔지니어링베이, 터렛, 벙커 건설타이밍을 시간으로 준다.
	// TODO 3. 벙커건설이 끼어들어오면 마린이 있어야 한다. 없으면 생산한다.
	
	// [토스전 기본전략 : 원팩 FD]
	// : (입구막기) 1팩 -> 마린6기 -> 애드온 -> 탱크 -> 마인업 -> 벌처 -> 앞마당전진(FD) -> 멀티 -> 시즈업
	
	// PHASE1 : 시작 ~ 코어완료 OR 일정시간 경과
	PROTOSS_INIT(1, 1, 0, UpgradeOrder.VM_TS_VS
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_1GATE_CORE()),
	
	PROTOSS_1GATE_CORE(PROTOSS_INIT
			, TimeMapForProtoss.PROTOSS_1GATE_CORE()),
	
	PROTOSS_2GATE(6, 1, 0, UpgradeOrder.VM_TS_VS //
			, MarineCount.ONE_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_2GATE()),
	
	PROTOSS_2GATE_CENTER(PROTOSS_2GATE),
	
	PROTOSS_DOUBLE(1, 1, 0, UpgradeOrder.VM_TS_VS // camp=F_EXPANSION
			, MarineCount.ONE_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_DOUBLE()),
	
	PROTOSS_FORGE_DEFENSE(1, 1, 0, UpgradeOrder.TS_VM_VS
			, MarineCount.ONE_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_FORGE_DEFENSE()), //
	
	PROTOSS_FORGE_CANNON_RUSH(1, 1, 0, UpgradeOrder.TS_VM_VS
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_FORGE_CANNON_RUSH()),
	
	PROTOSS_FORGE_DOUBLE(1, 1, 0, UpgradeOrder.TS_VM_VS // camp=S_CHOKE, 공격실패시 F_EXPANSION
			, MarineCount.ONE_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_FORGE_DOUBLE()),
	
	PROTOSS_GATE_DOUBLE(1, 1, 0, UpgradeOrder.TS_VM_VS, // camp=F_EXPANSION, 공격실패시 F_EXPANSION
			MarineCount.ONE_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_GATE_DOUBLE()),
	
	// PHASE2 : PHASE1 종료 ~ PHASE2 에 대한 위험이 종료되는 시점 (camp가 F_EXPANSION으로 이동, 적 병력/다크, 아군 병력/터렛/컴셋 고려)
	PROTOSS_FAST_DRAGOON(1, 2, 0, UpgradeOrder.TS_VM_VS // camp=F_EXPANSION
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_FAST_DRAGOON()),
	// + 위험종료 : PROTOSS_DEFAULT와 다르지 않으므로 바로종료 (TODO TBD: 정면이 위험하면 입구심시티)
	
	PROTOSS_FAST_DARK(1, 2, 0, UpgradeOrder.VM_TS_VS // camp=(다크타이밍이 안전할 경우에만) F_EXPANSION
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_FAST_DARK()),
	// + WATCHER : 마인매설(+본진, 앞마당), attack=S_CHOKE
	// + CHECKER : 할당량 감소(거의 할당하지 않음)
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 벌처 일정량 이상 보유.
	
	PROTOSS_DARK_DROP(1, 2, 0, UpgradeOrder.VM_TS_VS // camp=(다크타이밍이 안전할 경우에만) F_EXPANSION
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_DARK_DROP()),
	
	PROTOSS_ROBOTICS_REAVER(5, 5, 2, UpgradeOrder.VM_TS_VS //
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_ROBOTICS_REAVER()),
	
	PROTOSS_ROBOTICS_OB_DRAGOON(5, 5, 2, UpgradeOrder.VM_TS_VS //
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_ROBOTICS_OB_DRAGOON()),
	// + WATCHER : 마인매설(+본진)
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 생산 완료.
	
	PROTOSS_HARDCORE_ZEALOT(1, 1, 0, UpgradeOrder.VM_VS_TS // camp=F_EXPANSION
			, MarineCount.ONE_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_FAST_DRAGOON()),
	
	PROTOSS_STARGATE(1, 1, 0, UpgradeOrder.VM_VS_TS // camp=F_EXPANSION
			, MarineCount.FOUR_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_STARGATE()),
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 생산 완료.
	
	PROTOSS_DOUBLE_GROUND(1, 1, 0, UpgradeOrder.VM_VS_TS // camp=F_EXPANSION
			, MarineCount.ONE_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY
			, TimeMapForProtoss.PROTOSS_DOUBLE_GROUND()),
	
	PROTOSS_DOUBLE_CARRIER(1, 1, 0, UpgradeOrder.VM_VS_TS // camp=F_EXPANSION
			, MarineCount.ONE_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY
			, TimeMapForProtoss.PROTOSS_DOUBLE_CARRIER()),
	

	// PHASE3 - PHASE2 종료 ~
	PROTOSS_GROUND(1, 1, 0, UpgradeOrder.VM_TS_VS),
	PROTOSS_PROTOSS_AIR1(5, 5, 1, UpgradeOrder.VM_TS_VS_GR),
	PROTOSS_PROTOSS_AIR2(1, 5, 5, UpgradeOrder.VM_TS_VS_GR),
	PROTOSS_PROTOSS_AIR3(1, 1, 8, UpgradeOrder.VM_TS_VS_GR),
	
	// [저그전 기본전략 : 8배럭벙커링후 2팩토리 메카닉]
	// : 8배럭 -> 2scv정찰 -> 마린6기 -> 1팩 -> 벌처 -> 애드온 -> 마인업 -> 2팩 -> 멀티

	// PHASE1 : 시작 ~ 레어발견 OR 일정시간 경과
	ZERG_INIT(2, 1, 2, UpgradeOrder.VM_GR_TS_VS // INIT DEFAULT, camp=S_CHOKE,
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_9DRONE()),
	
	ZERG_5DRONE(7, 1, 1, UpgradeOrder.VS_VM_GR_TS // camp=BASE, 벙커
			, MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_5DRONE()),
	
	ZERG_9DRONE(2, 1, 2, UpgradeOrder.VM_GR_TS_VS // camp=F_CHOKE, 벙커
			, MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_9DRONE()),
	
	ZERG_9DRONE_GAS(2, 1, 2, UpgradeOrder.VS_VM_GR_TS // 마린=MORE, camp=F_CHOKE, 벙커
			, MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_9DRONE_GAS()),
	
	ZERG_9DRONE_GAS_DOUBLE(2, 1, 2, UpgradeOrder.VS_VM_GR_TS // 마린=MORE, camp=F_CHOKE, 벙커
			, MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_9DRONE_GAS_DOUBLE()),
	
	ZERG_OVERPOOL(2, 1, 2, UpgradeOrder.VM_GR_TS_VS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_OVERPOOL()),
	
	ZERG_OVERPOOL_GAS(2, 1, 2, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_OVERPOOL_GAS()),
	
	ZERG_OVERPOOL_GAS_DOUBLE(2, 1, 2, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_OVERPOOL_GAS_DOUBLE()),
	
	ZERG_2HAT_GAS(2, 1, 2, UpgradeOrder.VM_GR_VS_TS // camp=S_CHOKE, 벙커(공격)
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_2HAT_GAS()),
	
	ZERG_TWIN_HAT(ZERG_2HAT_GAS),
	
	ZERG_3HAT(2, 1, 2, UpgradeOrder.VM_GR_TS_VS // camp=S_CHOKE, 벙커(공격)
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForZerg.ZERG_3HAT()),

	// PHASE2 : PHASE1 종료 ~ PHASE2 에 대한 위험이 종료되는 시점 (camp가 F_EXPANSION으로 이동, 적 병력/다크, 아군 병력/터렛/컴셋 고려)
	// PHASE2 : 시작 ~ 레어발견 OR 일정시간 경과
	ZERG_FAST_MUTAL(1, 0, 3, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY, new BuildTimeMap()),
	
	ZERG_FAST_LURKER(2, 1, 1, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY, new BuildTimeMap()),
	
	ZERG_NO_LAIR_LING(3, 1, 1, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY, new BuildTimeMap()),
	
	ZERG_NO_LAIR_HYDRA(1, 2, 0, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY, new BuildTimeMap()),
	
	ZERG_LAIR_MIXED(1, 2, 0, UpgradeOrder.VS_VM_GR_TS // camp=F_CHOKE
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY, new BuildTimeMap()),
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 일정량 이상 보유.
	
	// PHASE3 : PHASE2 종료 ~
	ZERG_GROUND3(1, 5, 1, UpgradeOrder.VM_TS_VS_GR),
	ZERG_GROUND2(1, 5, 2, UpgradeOrder.VM_TS_VS_GR),
	ZERG_GROUND1(1, 5, 3, UpgradeOrder.VM_TS_VS_GR),
	ZERG_MIXED(1, 5, 5, UpgradeOrder.VM_GR_TS_VS),
	ZERG_AIR1(1, 2, 6, UpgradeOrder.VM_GR_TS_VS),
	ZERG_AIR2(1, 1, 10, UpgradeOrder.VM_GR_TS_VS),

	// [테란전 기본전략 : 2스타포트 클로킹 레이쓰]
	// : 1팩 -> 마린2기 -> 1벌처 (SCV죽이고) -> 2스타 -> 1스타애드온 클로킹개발 -> 레이스 몇개에서 출발할지는 TBD -> 팩토리애드온 -> 커맨드 -> 탱크
	// * 레이쓰는 빌드중인 건물 SCV를 최우선으로 공격한다. 특히 아머리, 엔베, 터렛, 아케데미
	
	// PHASE1 : 시작 ~ 팩토리 또는 아카데미 발견 OR 일정시간 경과
	TERRAN_INIT(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_MECHANIC()),
	
	TERRAN_MECHANIC(TERRAN_INIT
			, TimeMapForTerran.TERRAN_MECHANIC()),
	
	TERRAN_2BARRACKS(2, 1, 0, UpgradeOrder.VS_TS_VM
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForTerran.TERRAN_2BARRACKS()),
	
	TERRAN_BBS(2, 1, 0, UpgradeOrder.VS_TS_VM
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForTerran.TERRAN_BBS()),
	
	TERRAN_1BARRACKS_DOUBLE(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_1BARRACKS_DOUBLE()),
	
	TERRAN_NO_BARRACKS_DOUBLE(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.NO_BARRACKS_DOUBLE()),

	// PHASE2 : PHASE1 종료 ~ ?
	TERRAN_1FAC_DOUBLE(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_1FAC_DOUBLE()),
	
	TERRAN_1FAC_DOUBLE_1STAR(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_1FAC_DOUBLE_1STAR()),
	
	TERRAN_1FAC_DOUBLE_ARMORY(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_1FAC_DOUBLE_ARMORY()),
	
	TERRAN_2FAC(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_2FAC()),
	
	TERRAN_1FAC_1STAR(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_1FAC_1STAR()),
	
	TERRAN_2STAR(1, 1, 0, UpgradeOrder.TS
			, MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT
			, TimeMapForTerran.TERRAN_2STAR()),
	
	TERRAN_BIONIC(2, 1, 0, UpgradeOrder.VS_TS_VM
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForTerran.TERRAN_BIONIC()),
	
	TERRAN_2BARRACKS_1FAC(1, 1, 0, UpgradeOrder.VS_TS_VM
			, MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY
			, TimeMapForTerran.TERRAN_2BARRACKS_1FAC()),
	
	TERRAN_DOUBLE_BIONIC(TERRAN_2STAR), //
	
	TERRAN_DOUBLE_MECHANIC(TERRAN_2STAR), //

	// PHASE3 : PHASE2 종료 ~
	TERRAN_MECHANIC_VULTURE_TANK(0, 8, 1, UpgradeOrder.TS_VM_VS_GR),
	TERRAN_MECHANIC_GOLIATH_TANK(0, 4, 1, UpgradeOrder.TS_VM_VS_GR),
	TERRAN_MECHANIC_WRAITH_TANK(0, 1, 0, UpgradeOrder.TS_VM_VS_GR),
//	TERRAN_MECHANIC_BATTLE_TANK(0, 4, 1, UpgradeOrder.TS_VM_VS_GR),

	UNKNOWN(ZERG_INIT);
	
	private EnemyStrategy(EnemyStrategy strategy) {
		this.ratio = strategy.ratio;
		this.upgrade = strategy.upgrade;
		this.marineCount = strategy.marineCount;
		this.addOnOption = strategy.addOnOption;
		this.expansionOption = strategy.expansionOption;
		this.buildTimeMap = strategy.buildTimeMap;
	}
	
	private EnemyStrategy(EnemyStrategy strategy, BuildTimeMap addTimeMap) {
		this.ratio = strategy.ratio;
		this.upgrade = strategy.upgrade;
		this.marineCount = strategy.marineCount;
		this.addOnOption = strategy.addOnOption;
		this.expansionOption = strategy.expansionOption;
		this.buildTimeMap = strategy.buildTimeMap.putAll(addTimeMap);
	}
	
	private EnemyStrategy(int vulture, int tank, int goliath, List<MetaType> upgrade) {
		this.ratio = FactoryRatio.ratio(vulture, tank, goliath);
		this.upgrade = upgrade;
		this.marineCount = MarineCount.FOUR_MARINE;
		this.addOnOption = AddOnOption.VULTURE_FIRST;
		this.expansionOption = ExpansionOption.TWO_FACTORY;
		this.buildTimeMap = new BuildTimeMap();
	}

	private EnemyStrategy(int vulture, int tank, int goliath, List<MetaType> upgrade, int marineCount, AddOnOption addOnOption, ExpansionOption expansionOption, BuildTimeMap defaultTimeMap) {
		this.ratio = FactoryRatio.ratio(vulture, tank, goliath);
		this.upgrade = upgrade;
		this.marineCount = marineCount;
		this.addOnOption = addOnOption;
		this.expansionOption = expansionOption;
		this.buildTimeMap = defaultTimeMap;
	}
	
	public FactoryRatio ratio;
	public List<MetaType> upgrade;
	public int marineCount;
	public AddOnOption addOnOption;
	public ExpansionOption expansionOption;
	public BuildTimeMap buildTimeMap;
	
	public String toString() {
		StringBuilder sb = new StringBuilder().append("[").append(name()).append("]").append("\n").append(ratio.toString()).append("\n");
		for (MetaType metaType : upgrade) {
			if (metaType.isUnit()) {
				sb.append(metaType.getUnitType()).append(" / ");
			} else if (metaType.isTech()) {
				sb.append(metaType.getTechType()).append(" / ");
			} else if (metaType.isUpgrade()) {
				sb.append(metaType.getUpgradeType()).append(" / ");
			}
		}
		sb.append("\n").append("MARINE COUNT=").append(marineCount).append("\n").append(addOnOption).append("\n").append(expansionOption).append("");
		sb.append("\n\n").append(buildTimeMap);
		return sb.toString();
	}
}
