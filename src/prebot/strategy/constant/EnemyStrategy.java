package prebot.strategy.constant;

public class EnemyStrategy {
	public static enum Strategy { 
		ZERGBASIC
		,ZERGBASIC_HYDRAWAVE
		,ZERGBASIC_GIFTSET
		,ZERGBASIC_HYDRAMUTAL
		,ZERGBASIC_LINGHYDRA
		,ZERGBASIC_LINGLURKER
		,ZERGBASIC_LINGMUTAL
		,ZERGBASIC_LINGULTRA
		,ZERGBASIC_MUTAL
		,ZERGBASIC_MUTALMANY
		,ZERGBASIC_ULTRA
		,PROTOSSBASIC
		,PROTOSSBASIC_CARRIER
		,PROTOSSBASIC_DOUBLEPHOTO
		,TERRANBASIC
		,TERRANBASIC_BIONIC
		,TERRANBASIC_MECHANIC
		,TERRANBASIC_MECHANICWITHWRAITH
		,TERRANBASIC_MECHANICAFTER
		,TERRANBASIC_BATTLECRUISER	
		,ATTACKISLAND
//		,TERRANBASIC_REVERSERUSH
		} //기본 전략 나열
	public static enum StrategyException { 
		ZERGEXCEPTION_FASTLURKER
		,ZERGEXCEPTION_GUARDIAN
		,ZERGEXCEPTION_NONGBONG
		,ZERGEXCEPTION_ONLYLING
		,ZERGEXCEPTION_PREPARELURKER
		,ZERGEXCEPTION_REVERSERUSH
		,ZERGEXCEPTION_HIGHTECH
		,PROTOSSEXCEPTION_CARRIERMANY
		,PROTOSSEXCEPTION_DARK
		,PROTOSSEXCEPTION_REAVER
		,PROTOSSEXCEPTION_SCOUT
		,PROTOSSEXCEPTION_SHUTTLE
		,PROTOSSEXCEPTION_SHUTTLEMIX
		,PROTOSSEXCEPTION_READYTOZEALOT
		,PROTOSSEXCEPTION_ZEALOTPUSH
		,PROTOSSEXCEPTION_READYTODRAGOON
		,PROTOSSEXCEPTION_DRAGOONPUSH
		,PROTOSSEXCEPTION_PHOTONRUSH
		,PROTOSSEXCEPTION_DOUBLENEXUS
		,PROTOSSEXCEPTION_ARBITER
		,TERRANEXCEPTION_CHEESERUSH
		,TERRANEXCEPTION_NUCLEAR
		,TERRANEXCEPTION_WRAITHCLOAK
		,INIT} //예외 전략 나열, 예외가 아닐때는 무조건 INIT 으로
	
}
