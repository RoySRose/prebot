package pre;

import java.util.Set;

import bwapi.Color;
import bwapi.Race;
import bwapi.UnitType;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

	private static AnalyzeStrategy instance = new AnalyzeStrategy();
	
	public static AnalyzeStrategy Instance() {
		return instance;
	}
	
	//@@@@@@ 상대 input 에 따른 확인 함수- enum 이 익숙하면 enum 쓰는 것도 좋지만 그렇지 않다면 그냥 
	//한 쪽에다가 주석 처리 아래와 같이 해 놓고 
	//1 : 기본 전략
	//2 : 4,5드론 저글링 scv 러쉬 방어 전략
	//이런식으로 적어두고 int 형 쓰는게 훨씬 편할 수도 있음.
	public enum Strategy { 
		BasicStrategy,				// 기본 전략
		scvzerglingDF,				// 4,5드론 저글링 scv 러쉬 방어 전략
		
		temp1,			// 
		temp2,		// 
		temp3,			// 
		temp4		// 
	};

	
	//@@@@@@ 상대 input 에 따른 확인 함수 
	public static int AnalyzeInfo(){
		
		int res = 0;
		//@@@@@@ 예를 들어 유닛이 보였다
		// if(wraith 가 보였다){
			 
		 	//res = 1; //기본전략 유지 
		
		//}else{
			//res = 2; // 4,5드론 저글링 scv 러쉬 방어 전략
		//}
		return res;
	}
	
}