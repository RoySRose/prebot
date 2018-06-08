package prebot.strategy.constant;

import java.util.List;

import prebot.common.MetaType;
import prebot.strategy.constant.EnemyStrategyOptions.AddOnOption;
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
	PROTOSS_INIT(1, 1, 0, UpgradeOrder.VM_TS_VS,
			MarineCount.SIX_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), // INIT DEFAULT
	
	PROTOSS_1GATE_CORE(PROTOSS_INIT), //
	
	PROTOSS_2GATE(6, 1, 0, UpgradeOrder.VM_TS_VS,
			MarineCount.NO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // 
	
	PROTOSS_DOUBLE(1, 1, 0, UpgradeOrder.VM_TS_VS,
			MarineCount.NO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY), // camp=F_EXPANSION
	
	PROTOSS_FORGE(1, 1, 0, UpgradeOrder.TS_VM_VS,
			MarineCount.NO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.ONE_FACTORY), //
	// 넥서스가 소환되지 않는 경우 포톤러시를 의심해야 한다.
	
	PROTOSS_FORGE_DOUBLE(1, 1, 0, UpgradeOrder.TS_VM_VS,
			MarineCount.NO_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.TWO_FACTORY), // camp=S_CHOKE, 공격실패시 F_EXPANSION
	
	
	// PHASE2 : PHASE1 종료 ~ PHASE2 에 대한 위험이 종료되는 시점 (camp가 F_EXPANSION으로 이동, 적 병력/다크, 아군 병력/터렛/컴셋 고려)
	// 원게이트 코어 정석빌드에서 연계되는 것에 대한 대비. 더블이나 투게이트 이후 연계는 이미 대비가 되어 있을 것이므로 아마 곧바로 종료.
	PROTOSS_TEMPLAR(PROTOSS_INIT), // camp=(다크타이밍이 안전할 경우에만) F_EXPANSION
	// + WATCHER : 마인매설(+본진, 앞마당), attack=S_CHOKE
	// + CHECKER : 할당량 감소(거의 할당하지 않음)
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 벌처 일정량 이상 보유.
	
	PROTOSS_MORE_GATE(1, 1, 0, UpgradeOrder.TS_VM_VS,
			MarineCount.SIX_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), // camp=F_EXPANSION
	// + 위험종료 : PROTOSS_DEFAULT와 다르지 않으므로 바로종료 (TODO TBD: 정면이 위험하면 입구심시티)
	
	PROTOSS_ROBOTICS(5, 5, 2, UpgradeOrder.VM_TS_VS,
			MarineCount.SIX_MARINE, AddOnOption.IMMEDIATELY, ExpansionOption.ONE_FACTORY), //
	// + WATCHER : 마인매설(+본진)
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 생산 완료.
	
	PROTOSS_STARGATE(PROTOSS_ROBOTICS), //
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 생산 완료.

	// PHASE3 - PHASE2 종료 ~
	PROTOSS_DEFAULT(1, 1, 0, UpgradeOrder.VM_TS_VS),
	PROTOSS_PROTOSS_ARBITER(5, 5, 1, UpgradeOrder.VM_TS_VS_GR),
	PROTOSS_PROTOSS_CARRIER(1, 1, 8, UpgradeOrder.VM_TS_VS_GR),
	
	// [저그전 기본전략 : 8배럭벙커링후 2팩토리 메카닉]
	// : 8배럭 -> 2scv정찰 -> 마린6기 -> 1팩 -> 벌처 -> 애드온 -> 마인업 -> 2팩 -> 멀티

	// PHASE1 : 시작 ~ 레어발견 OR 일정시간 경과
	ZERG_INIT(2, 1, 2, UpgradeOrder.VM_GR_TS_VS
			, MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // INIT DEFAULT, camp=S_CHOKE,
	
	ZERG_5DRONE(7, 1, 1, UpgradeOrder.VS_VM_GR_TS
			, MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=BASE, 벙커
	
	ZERG_9DRONE(2, 1, 2, UpgradeOrder.VM_GR_TS_VS,
			MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_CHOKE, 벙커
	
	ZERG_9DRONE_GAS(2, 1, 2, UpgradeOrder.VS_VM_GR_TS,
			MarineCount.EIGHT_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // 마린=MORE, camp=F_CHOKE, 벙커
	
	ZERG_OVERPOOL(2, 1, 2, UpgradeOrder.VM_GR_TS_VS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_CHOKE
	
	ZERG_OVERPOOL_GAS(2, 1, 2, UpgradeOrder.VS_VM_GR_TS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_CHOKE
	
	ZERG_2HAT(2, 1, 2, UpgradeOrder.VM_GR_TS_VS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=S_CHOKE, 벙커(공격)
	
	ZERG_2HAT_GAS(2, 1, 2, UpgradeOrder.VM_GR_VS_TS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=S_CHOKE, 벙커(공격)

	// PHASE2 : PHASE1 종료 ~ PHASE2 에 대한 위험이 종료되는 시점 (camp가 F_EXPANSION으로 이동, 적 병력/다크, 아군 병력/터렛/컴셋 고려)
	ZERG_NO_LAIR_LING(3, 1, 0, UpgradeOrder.VS_VM_GR_TS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION, 벙커
	// + CHECKER : 할당량 감소(거의 할당하지 않음)
	// + 위험종료 : BASE근처에 적이 없음. 벙커 완성. 벌처 일정량 이상 보유. 
	
	ZERG_NO_LAIR_HYDRA(1, 5, 0, UpgradeOrder.VM_TS_VS_GR,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION, 벙커
	// + WATCHER : 마인매설(+촘촘하게)
	// + CHECKER : 할당량 감소(거의 할당하지 않음)
	// + 위험종료 : BASE근처에 적이 없음. 벙커 완성. 탱크 일정량 이상 보유.
	
	ZERG_LAIR_HYDRA_TECH(1, 5, 2, UpgradeOrder.VM_TS_VS_GR,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION
	
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 탱크 일정량 이상 보유.
	
	ZERG_LAIR_SPIRE_TECH(1, 1, 15, UpgradeOrder.VM_GR_VS_TS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 일정량 이상 보유.
	
	ZERG_LAIR_MIXTED_TECH(1, 3, 5, UpgradeOrder.VM_GR_VS_TS,
			MarineCount.SIX_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY), // camp=F_EXPANSION
	// + 위험종료 : BASE근처에 적이 없음. 포지션별 터렛 완성. 골리앗 일정량 이상 보유.
	
	// PHASE3 : PHASE2 종료 ~
	ZERG_DEFAULT(0, 2, 10, UpgradeOrder.VM_GR_TS_VS),
	ZERG_HYDRA_MANY(1, 5, 2, UpgradeOrder.VM_TS_VS_GR),
	ZERG_MUTAL_MANY(1, 1, 10, UpgradeOrder.VM_GR_TS_VS),
	ZERG_LURKER(1, 2, 1, UpgradeOrder.VM_TS_VS_GR),
	ZERG_MIXED(1, 2, 6, UpgradeOrder.VM_GR_TS_VS),
	ZERG_LING_MUTAL(1, 2, 4, UpgradeOrder.VM_GR_TS_VS),
	ZERG_ULTRA(1, 2, 1, UpgradeOrder.VM_TS_VS_GR),

	// [테란전 기본전략 : 2스타포트 클로킹 레이쓰]
	// : 1팩 -> 마린2기 -> 1벌처 (SCV죽이고) -> 2스타 -> 1스타애드온 클로킹개발 -> 레이스 몇개에서 출발할지는 TBD -> 팩토리애드온 -> 커맨드 -> 탱크
	// * 레이쓰는 빌드중인 건물 SCV를 최우선으로 공격한다. 특히 아머리, 엔베, 터렛, 아케데미
	
	// PHASE1 : 시작 ~ 팩토리 또는 아카데미 발견 OR 일정시간 경과
	TERRAN_INIT(1, 1, 0, UpgradeOrder.TS,
			MarineCount.TWO_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_STARPORT),
	
	TERRAN_NO_BARRACKS(TERRAN_INIT),
	
	TERRAN_1BARRACKS(TERRAN_INIT),
	
	TERRAN_1BARRACKS_GAS(TERRAN_INIT),
	
	TERRAN_2BARRACKS(2, 1, 0, UpgradeOrder.VS_TS_VM,
			MarineCount.FOUR_MARINE, AddOnOption.VULTURE_FIRST, ExpansionOption.TWO_FACTORY),
	
	TERRAN_DOUBLE(TERRAN_INIT),
	
	TERRAN_1BARRACKS_DOUBLE(TERRAN_INIT),

	// PHASE2 : PHASE1 종료 ~ ?
	TERRAN_BIONIC(TERRAN_2BARRACKS),
	TERRAN_MECHANIC(TERRAN_INIT),
	TERRAN_1FAC_DOUBLE(TERRAN_INIT),
	TERRAN_2FAC(TERRAN_INIT),
	TERRAN_1FAC_1STAR(TERRAN_INIT),
	TERRAN_2STAR(TERRAN_INIT),

	// PHASE3 : PHASE2 종료 ~
	TERRAN_MECHANIC_DEFAULT(0, 5, 1, UpgradeOrder.TS_VM_VS_GR),
	TERRAN_MECHANIC_VULTURE_TANK(TERRAN_MECHANIC_DEFAULT),
	TERRAN_MECHANIC_GOLIATH_TANK(TERRAN_MECHANIC_DEFAULT),
	TERRAN_MECHANIC_WRAITH_TANK(0, 3, 1, UpgradeOrder.TS_VM_VS_GR),
	TERRAN_MECHANIC_BATTLE(0, 2, 1, UpgradeOrder.TS_VM_VS_GR);
	
	
	private EnemyStrategy(EnemyStrategy strategy) {
		this.ratio = strategy.ratio;
		this.upgrade = strategy.upgrade;
		this.marineCount = strategy.marineCount;
		this.addOnOption = strategy.addOnOption;
		this.expansionOption = strategy.expansionOption;
	}
	
	private EnemyStrategy(int vulture, int tank, int goliath, List<MetaType> upgrade) {
		this.ratio = FactoryRatio.ratio(vulture, tank, goliath);
		this.upgrade = upgrade;
		this.marineCount = MarineCount.SIX_MARINE;
		this.addOnOption = AddOnOption.VULTURE_FIRST;
		this.expansionOption = ExpansionOption.TWO_FACTORY;
	}

	private EnemyStrategy(int vulture, int tank, int goliath, List<MetaType> upgrade, int marineCount, AddOnOption addOnOption, ExpansionOption expansionOption) {
		this.ratio = FactoryRatio.ratio(vulture, tank, goliath);
		this.upgrade = upgrade;
		this.marineCount = marineCount;
		this.addOnOption = addOnOption;
		this.expansionOption = expansionOption;
	}
	
	public FactoryRatio ratio;
	public List<MetaType> upgrade;
	public int marineCount;
	public AddOnOption addOnOption;
	public ExpansionOption expansionOption;
	
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
		sb.append("\n").append("MARINE COUNT=").append(marineCount).append("\n").append(addOnOption).append("\n").append(expansionOption).append("\n");
		return sb.toString();
	}
}
