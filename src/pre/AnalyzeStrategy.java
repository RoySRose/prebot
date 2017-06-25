package pre;

import java.util.Set;

import bwapi.Color;
import bwapi.Race;
import bwapi.UnitType;
import pre.main.MyBotModule;
import pre.manager.StrategyManager;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

	private static AnalyzeStrategy instance = new AnalyzeStrategy();
	
	public static AnalyzeStrategy Instance() {
		return instance;
	}
	

	public void AnalyzeEnemyStrategy() {

		//최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행 
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			AnalyzeVsProtoss();
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			AnalyzeVsTerran();
		}else{
			AnalyzeVsZerg();
		}

	}


	private void AnalyzeVsProtoss() {
		
		
		//조건에 따라...
		//basic 의 기본은 상대 종족별로 다른거 같고.
		StrategyManager.Instance().setCurrentStrategyBasic("BaiscVsProtoss"); // 이런식으로 기본 전략 세팅가능
		
		//exception 상태가 아닐때는 Init 으로
		//StrategyManager.Instance().setCurrentStrategyException("Init"); // 이런식으로 exception 도 세팅 가능
		//@@@@@@ 예를 들어 유닛이 보였다
		// if(wraith 가 보였다){
			 
		 	//res = 1; //기본전략 유지 
		
		//}else{
			//res = 2; // 4,5드론 저글링 scv 러쉬 방어 전략
		//}
		
	}
	
	private void AnalyzeVsTerran() {
		StrategyManager.Instance().setCurrentStrategyBasic("BaiscVsTerran");
		
		//exception 상태가 아닐때는 Init 으로
		//StrategyManager.Instance().setCurrentStrategyException("Init");
		
	}
	
	private void AnalyzeVsZerg() {
		StrategyManager.Instance().setCurrentStrategyBasic("BaiscVsZerg");
		
		//exception 상태가 아닐때는 Init 으로
		//StrategyManager.Instance().setCurrentStrategyException("Init");
		
	}
	
	
}