package prebot.strategy.constant;

public class StrategyConfig {

	// 전략 0 zergBasic
	// 전략 1 protossBasic
	// 전략 2 terranBasic //0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
	public static final int[] VULTURE_RATIO = new int[] { 0, 1, 0, 1, 1, 2, 1, 5, 1, 0, 5, 5, 1, 3, 0, 10, 5, 0, 0, 5, 0 }; // 기본전략 벌쳐 비, 예 vultureratio[0] 은 BasicvsZerg 에서의 비율
	public static final int[] TANK_RATIO = new int[] { 2, 5, 2, 5, 4, 4, 2, 6, 1, 1, 6, 5, 2, 3, 2, 5, 8, 7, 9, 1, 0 }; // 기본전략 탱크 비
	public static final int[] GOLIATH_RATIO = new int[] { 10, 2, 10, 6, 3, 4, 10, 2, 9, 12, 1, 0, 8, 0, 0, 0, 1, 1, 1, 1, 1 }; // 기본전략 골리앗 비
	public static final int[] WGT = new int[] { 1, 2, 1, 3, 1, 1, 1, 1, 3, 3, 1, 2, 3, 1, 1, 1, 2, 3, 3, 2, 3 }; // 기본전략 우선순위 1벌쳐, 2탱크, 3골리앗

	// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3
	public static final int[] VULTURE_RATIO_EXCEPTION = new int[] { 1, 0, 0, 7, 0, 1, 0, 9, 9, 8, 7, 7, 10, 6, 6, 1, 1, 0, 0, 0, 0, 0, 0, 0 }; // 예외전략 벌쳐 비, 예 vultureratio[0] 은
	public static final int[] TANK_RATIO_EXCEPTION = new int[] { 4, 2, 2, 3, 0, 3, 0, 3, 3, 3, 3, 3, 2, 1, 1, 3, 4, 0, 0, 0, 0, 0, 0, 0 }; // 예외전략 탱크 비
	public static final int[] GOLIATH_RATIO_EXCEPTION = new int[] { 1, 10, 10, 2, 0, 3, 0, 0, 0, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // 예외전략 골리앗 비
	public static final int[] WGT_EXCEPTION = new int[] { 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0 }; // 예외전략 우선순위 1벌쳐, 2탱크, 3골리앗

	// public static final int[] vultureratioexception = new int[] {6,0 ,0 ,7,0 ,6,2,9,9,8,7,7,10,8,3,1,0,0,0,0,0,0,0,0}; //예외전략 벌쳐 비, 예 vultureratio[0] 은
	// public static final int[] tankratioexception = new int[] {0,2 ,2 ,3,2 ,3,2,3,3,3,3,3,2 ,2,3,1,0,0,0,0,0,0,0,0}; //예외전략 탱크 비
	// public static final int[] goliathratioexception = new int[] {6,10,10,2,10,3,8,0,0,1,2,2,0 ,2,7,1,0,0,0,0,0,0,0,0}; //예외전략 골리앗 비
	// public static final int[] wgtexception = new int[] {1,1 ,1 ,1,1 ,1,1,1,1,1,1,1,1 ,1,1,1,0,0,0,0,0,0,0,0}; //예외전략 우선순위 1벌쳐, 2탱크, 3골리앗

	// 0 zergBasic
	// ,zergBasic_HydraWave
	// ,zergBasic_GiftSet
	// ,zergBasic_HydraMutal
	// ,zergBasic_LingHydra
	// 5 ,zergBasic_LingLurker
	// ,zergBasic_LingMutal
	// ,zergBasic_LingUltra
	// ,zergBasic_Mutal
	// ,zergBasic_MutalMany
	// 10 ,zergBasic_Ultra
	// ,protossBasic
	// ,protossBasic_Carrier
	// ,protossBasic_DoublePhoto
	// 14,terranBasic
	// ,terranBasic_Bionic
	// ,terranBasic_Mechanic
	// ,terranBasic_MechanicWithWraith
	// ,terranBasic_MechanicAfter
	// ,terranBasic_BattleCruiser
	// 20,AttackIsland

	// 0 zergException_FastLurker
	// ,zergException_Guardian
	// ,zergException_NongBong
	// ,zergException_OnLyLing
	// ,zergException_PrepareLurker
	// 5 ,zergException_ReverseRush
	// ,zergException_HighTech
	// ,protossException_CarrierMany
	// ,protossException_Dark
	// ,protossException_Reaver
	// 10 ,protossException_Scout
	// ,protossException_Shuttle
	// ,protossException_ShuttleMix
	// ,protossException_ReadyToZealot
	// ,protossException_ZealotPush
	// 15 ,protossException_ReadyToDragoon
	// ,protossException_DragoonPush
	// ,protossException_PhotonRush
	// ,protossException_DoubleNexus
	// ,protossException_Arbiter
	// 20 ,terranException_CheeseRush
	// ,terranException_NuClear
	// ,terranException_Wraith
	// ,Init
}
