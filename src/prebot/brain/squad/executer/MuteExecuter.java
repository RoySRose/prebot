package prebot.brain.squad.executer;

@Deprecated
public class MuteExecuter { //implements SquadExecutable, ChatOrderable {

//	private static final String ATT = "att";
//	private static final String DEF = "def";
//	private static final String KIT = "kit";
//	private static final String AGG = "agg";
//	
//	private static final String BASE = "base";
//	private static final String EXP = "exp";
//	
//	private static final String LEFT = "left";
//	private static final String RIGHT = "right";
//
//	@Override
//	public boolean requires(Unit unit) {
//		return unit.getType() == UnitType.Zerg_Mutalisk;
//	}
//
//	@Override
//	public void execute(List<Unit> muteList) {
//		List<UnitInfo> euiList = this.getNearEnemyInfo(muteList);
//
//		// this.changeMission(muteList, euiList);
//		this.microControl(muteList, euiList);
//	}
//
//	private void microControl(List<Unit> muteList, List<UnitInfo> euiList) {
//		if (!PositionUtils.isValidPosition(targetPosition)) {
//			return;
//		}
//
//		FleeOption fOption = new FleeOption(targetPosition, true, freeAngles);
//		KitingOption kOption = new KitingOption(fOption, false);
//
//		CalcTargetScore calcTargetScore = new CalcTargetScore(1, 1, 1, 1);
//		DecisionMaker decisionMaker = new DecisionMaker(calcTargetScore);
//
//		for (Unit mute : muteList) {
//			Decision decision = decisionMaker.makeDecisionForFlyer(mute, euiList, targetPosition, kOption, aggressivity);
//			decision.perform();
//		}
//	}
//
//	@Override
//	public boolean expectable(String type) {
//		return CommandType.MUTE.TYPE.equals(type);
//	}
//
//	@Override
//	public void perform(String option) {
//		if (option == null || option.isEmpty()) {
//			return;
//		}
//		String optionWhat = option.substring(0, 2);
//		String optionHow = option.substring(3);
//		this.changeControlInformation(optionWhat, optionHow);
//	}
//
//	private void changeControlInformation(String optionWhat, String optionHow) {
//
//		if (ATT.equals(optionWhat)) {
//			changeTargetPosition(PreBot.Broodwar.enemy(), optionHow);
//
//		} else if (DEF.equals(optionWhat)) {
//			changeTargetPosition(PreBot.Broodwar.self(), optionHow);
//
//		} else if (KIT.equals(optionWhat)) {
//			changeFreeAngle(optionHow);
//
//		} else if (AGG.equals(optionWhat)) { // 카이팅 aggressivity 변경
//			changeAggressivity(optionHow);
//		}
//
//	}
//
//	private void changeTargetPosition(Player playerOfPosition, String optionHow) {
//		if (BASE.equals(optionHow)) {
//			targetPosition = PlayerUtils.getBasePosition(playerOfPosition);
//		} else if (EXP.equals(optionHow)) {
//			targetPosition = PlayerUtils.getFirstExpansionPosition(playerOfPosition);
//		} else {
//			targetPosition = PlayerUtils.getTargetPosition(playerOfPosition);
//		}
//	}
//
//	private void changeFreeAngle(String optionHow) {
//		if (LEFT.equals(optionHow)) {
//			freeAngles = Angles.WIDE;
//		} else if (RIGHT.equals(optionHow)) {
//			freeAngles = Angles.WIDE;
//		} else {
//			freeAngles = Angles.WIDE;
//		}
//	}
//
//	private void changeAggressivity(String optionHow) {
//		if ("0".equals(optionHow)) {
//			aggressivity = 0;
//		}
//	}
//
//	public void MuteOrder() {
//	}
//
//	private Position targetPosition = Position.None;
//	public int[] freeAngles = Angles.WIDE;
//	public int aggressivity = 0;
//
//	private List<UnitInfo> getNearEnemyInfo(List<Unit> muteList) {
//		List<UnitInfo> euiList = new ArrayList<>();
//		for (Unit unit : muteList) {
//			UnitUtils.getEuiList(euiList, unit.getPosition(), 500);
//		}
//		return euiList;
//	}

	// private static final int RETREAT_MINIMUM_DAMAGE = 30;
	// private static final int DAMAGE_MEMORY_SIZE = 10 * FRAME.SEC;

	// private enum MISSION {
	// Defense, AttackUnit, AttackBase;
	// }

	// private MISSION mission = MISSION.Defense;
	// private int switchedFrame = 0;
	//
	// private int[] muteHitPointReduced = new int[DAMAGE_MEMORY_SIZE];
	// private int[] enemyHitPointReduced = new int[DAMAGE_MEMORY_SIZE];

	// private MicroForMutalisk microForMutalisk = new MicroForMutalisk();

	// private void updateDamageOnFrame(List<Unit> muteList, List<UnitInfo> euiList) {
	// int totalHitPointsReduced = 0;
	// UnitData unitData = InformationManager.Instance().getUnitData(MyBotModule.Broodwar.self());
	// for (Unit unit : muteList) {
	// UnitInfo ui = unitData.getUnitAndUnitInfoMap().get(unit.getID());
	// totalHitPointsReduced += ui.hitPointsReduced;
	// }
	// muteHitPointReduced[MyBotModule.Broodwar.getFrameCount() % DAMAGE_MEMORY_SIZE] = totalHitPointsReduced;
	//
	// int enemyTotalShieldsReduced = 0;
	// int enemyTotalHitPointsReduced = 0;
	// for (UnitInfo eui : euiList) {
	// enemyTotalShieldsReduced += eui.shieldsReduced;
	// enemyTotalHitPointsReduced += eui.hitPointsReduced;
	// }
	// enemyHitPointReduced[MyBotModule.Broodwar.getFrameCount() % DAMAGE_MEMORY_SIZE] = enemyTotalShieldsReduced + enemyTotalHitPointsReduced;
	// }
	//
	// private void changeMission(List<Unit> muteList, List<UnitInfo> euiList) {
	// if (PositionUtils.targetPosition(MyBotModule.Broodwar.enemy()) == null) {
	// mission = MISSION.Defense;
	// return;
	// }
	//
	// this.updateDamageOnFrame(muteList, euiList);
	// int secForTime = 0, secForDamage = 0; //TODO , damageRatio = 0;
	// switch (positionType) {
	// case MyBase:
	// secForTime = 5; secForDamage = 0; break;
	// case EnemyBase:
	// secForTime = 30; secForDamage = 4; break;
	// }
	//
	// // 1. 시간 초과 : secForTime(초) 이상이 지나갔다면 positionType을 변경
	// if (MyBotModule.Broodwar.getFrameCount() - switchedFrame >= secForTime * FRAME.SEC) {
	// return true;
	// }
	//
	// // 2. 데미지 초과 : secForDamage(초) 동안 누적된 데미지를 통한 positionType 변경(후퇴)
	// int sumOfMuteHitPointReduced = 0, sumOfEnemyHitPointReduced = 0;
	// for (int before = 0; before < secForDamage * FRAME.SEC; before++) {
	// int index = (MyBotModule.Broodwar.getFrameCount() - before) % DAMAGE_MEMORY_SIZE;
	// sumOfMuteHitPointReduced += muteHitPointReduced[index];
	// sumOfEnemyHitPointReduced += enemyHitPointReduced[index];
	// }
	//
	// // 데미지 손해
	// if (sumOfMuteHitPointReduced > RETREAT_MINIMUM_DAMAGE && sumOfMuteHitPointReduced > sumOfEnemyHitPointReduced) {
	// return true;
	// }
	// }

	// private void updateTargetPositionType() {
	// switch (positionType) {
	// case MyBase:
	// this.positionType = TargetPositionType.EnemyBase;
	// this.switchedFrame = MyBotModule.Broodwar.getFrameCount();
	// break;
	//
	// case EnemyBase:
	// this.positionType = TargetPositionType.MyBase;
	// this.switchedFrame = MyBotModule.Broodwar.getFrameCount();
	// break;
	// }
	// }
}
