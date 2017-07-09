package pre;

import java.util.List;
import java.util.Set;

import bwapi.Color;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.manager.StrategyManager;
import pre.manager.StrategyManager.Strategys;
import pre.manager.StrategyManager.StrategysException;
import pre.util.MicroUtils;

/// 봇 프로그램 설정
public class AnalyzeStrategy {

	private static AnalyzeStrategy instance = new AnalyzeStrategy();
	
	public static AnalyzeStrategy Instance() {
		return instance;
	}
	

	public void AnalyzeEnemyStrategy() {

		//최대한 로직 막 타지 않게 상대 종족별로 나누어서 진행 
//		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
//			AnalyzeVsProtoss();
//		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
//			AnalyzeVsTerran();
//		}else{
			AnalyzeVsZerg();
//		}

	}


	private void AnalyzeVsProtoss() {
		
		
		//조건에 따라...
		//basic 의 기본은 상대 종족별로 다른거 같고.
		if(!StrategyManager.Strategys.values().equals(StrategyManager.Strategys.protossBasic_Templer) && !StrategyManager.Strategys.values().equals(StrategyManager.Strategys.protossBasic_Carrier)){
			//캐리어와 템플러 대비 기본이 아니라면 일반 기본으로
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic); // 이런식으로 기본 전략 세팅가능
		}
		StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
		
		if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Robotics_Facility, InformationManager.Instance().enemyPlayer) >= 1){
			//로보틱스를 발견하면, 터렛으로 본진대비
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, InformationManager.Instance().selfPlayer) == 0){
				//엔지니어링 베이가 있다면, 기본전략으로 대응하고, 본진에 터렛만 추가.
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Shuttle);
			}
			if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle, InformationManager.Instance().enemyPlayer) >= 1){
				//셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
				//셔틀대비 골리앗 비율이 맞다면 넘어간다.
				//로직 추가할것. 생각중
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_ShuttleMix);
			}
			if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Robotics_Support_Bay, InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Protoss_Reaver, InformationManager.Instance().enemyPlayer) >= 1){
				//셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
				//본진 방어가 되면 넘어간다. 로직 추가할것.
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Reaver);
			}

		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer) >= 1){
			//스카웃은 큰 위협이 안되므로, 골리앗만 맞춰줄것
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer) < InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer)){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Scout);	
			}
			
		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) >= 8){
			//캐리어가 8기 이상일때부터 대비시작. 고스트 & 락다운 준비
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Ghost, InformationManager.Instance().selfPlayer) < InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) / 2){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_CarrierMany);	
			}
		}
		
		if(StrategyManager.StrategysException.values().equals(StrategyManager.StrategysException.Init)){
			//익셉션이 아니라면 기본전략 체크
			if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Templar_Archives, InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Protoss_High_Templar, InformationManager.Instance().enemyPlayer) >= 1){
				if(!StrategyManager.StrategysException.values().equals(StrategyManager.Strategys.protossBasic_Carrier)){
					//캐리어 대비 로직에 템플러 대비로직이 포함되어있으므로(사이언스베슬, EMP)
					//캐리어 대비중인경우는 전략을 바꾸지 않는다.
					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic_Templer);
				}
			}
			
			if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Stargate, InformationManager.Instance().enemyPlayer) >= 1
				//캐리어 대비 전략
				&& InformationManager.Instance().getNumUnits(UnitType.Protoss_Fleet_Beacon, InformationManager.Instance().enemyPlayer) >= 1){
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic_Carrier);
			}
		}

		
	}
	
	private void AnalyzeVsTerran() {
		StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic);
		StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
		
		
		//치즈러시 대비
		int nongbong_cnt = 0;
		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		bwta.Region myRegion = base.getRegion();
		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
		if (enemyUnitsInRegion.size() >= 5){
			 for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
				 if(enemyUnitsInRegion.get(enemy).getType().equals("Terra_Marine")||enemyUnitsInRegion.get(enemy).getType().equals("Terran_SCV")){
					 nongbong_cnt ++;
				 }
			 }
		}
		
		if(InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Factory) < 1
			&& nongbong_cnt >= 5){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_CheeseRush);
		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Terran_Nuclear_Silo, InformationManager.Instance().enemyPlayer) >= 1){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_NuClear);
		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith, InformationManager.Instance().enemyPlayer) >= 20){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_Wraith);
		}
		
		if(StrategyManager.StrategysException.values().equals(StrategyManager.StrategysException.Init)){
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Starport, InformationManager.Instance().enemyPlayer) == 1
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, InformationManager.Instance().enemyPlayer) >= 1){
				//공중 유닛에 대한 대응
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_AirUnit);
			}else if(InformationManager.Instance().getNumUnits(UnitType.Terran_Starport, InformationManager.Instance().enemyPlayer) >= 2){
				//스타포트 다수에 대한 대응
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_AirUnit);	
			}
			
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Dropship, InformationManager.Instance().enemyPlayer) >= 3){
				//드랍쉽에 대한 대응
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_DropShip);
			}
			
			if(
				(InformationManager.Instance().getNumUnits(UnitType.Terran_Science_Facility, InformationManager.Instance().enemyPlayer) >= 1
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Physics_Lab, InformationManager.Instance().enemyPlayer) >= 1)
				|| InformationManager.Instance().getNumUnits(UnitType.Terran_Battlecruiser, InformationManager.Instance().enemyPlayer) >= 1){
				//(사이언스퍼실리티 & 피직스 랩 발견) or 배틀크루저 발견 
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_BattleCruiser);
			}
			
		
		}

		//exception 상태가 아닐때는 Init 으로
		//StrategyManager.Instance().setCurrentStrategyException("Init");
		
	}
	
	private void AnalyzeVsZerg() {
		int cntHatchery = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hatchery, InformationManager.Instance().enemyPlayer);
		int cntLair = InformationManager.Instance().getNumUnits(UnitType.Zerg_Lair, InformationManager.Instance().enemyPlayer);
		int cntHive = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hive, InformationManager.Instance().enemyPlayer);
		
		int ling_chk = 0;
		int hydra_chk = 0;
		int mutal_chk = 0;
		int ultra_chk = 0;
		
		System.out.println("Analyze start");
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >= 1){
			ling_chk  = 1;
		}
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk, InformationManager.Instance().enemyPlayer) >= 1
			||InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker, InformationManager.Instance().enemyPlayer) >= 1){
			hydra_chk  = 1;
		}
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) >= 1
			||InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian, InformationManager.Instance().enemyPlayer) >= 1){
			mutal_chk  = 1;
		}
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Ultralisk, InformationManager.Instance().enemyPlayer) >= 1){
			ultra_chk  = 1;
		}
		
		
		StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic);
		System.out.println("Analyze start");
		
//		//basic
//		if(저그 히드라 조건 충족하면){
//			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.히드라);
//		}else if(저그 뮤탈 조건 충족하면){
//			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.뮤탈);
//		}else if(저그 러커 조건 충족하면){
//			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.러커);
//		}else{
//			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.베이직);
//		}
//		
//		if(StrategyManager.StrategysException.Init 이면){
//			in 조건에 해당되는거
//			if(저그 농봉 조건 충족하면){
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.농봉);
//			}else if(저그 ??? 충족하면){
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.???);
//			}
//		}
//		
//		if(StrategyManager.StrategysException.Init 이 아니면){
//			if(저그 농봉 앗웃 조건 충족하면){
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.Init);
//			}else if(저그 ??? 아웃 조건 충족하면){
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.Init);
//			}
//			
//		}
		
		//기본 저그 전술 세팅
//		if(StrategyManager.Strategys.values().equals(StrategyManager.Strategys.zergBasic_HighTech)){
//			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HighTech);
//		}else{
//			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic);
//		}
//		//익셉션은 init 기본
//		StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
//		
//		//start zergException_NongBong
//		int nongbong_cnt = 0;
//		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
//		bwta.Region myRegion = base.getRegion();
//		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
//		if (enemyUnitsInRegion.size() >= 4){
//			 for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
//				 if(enemyUnitsInRegion.get(enemy).getType().equals("Zerg_Zergling")){
//					 nongbong_cnt ++;
//				 }
//			 }
//		}
//		//저글링이 4마리 이상
//		if(nongbong_cnt >= 4) {
//			//벙커가 없는 경우.
//			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Bunker , InformationManager.Instance().selfPlayer) == 0){
//				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_NongBong);
//			}
//			// 벙커가 1개인데 마린이 2 미만인 경우
//			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Bunker , InformationManager.Instance().selfPlayer) == 1 
//				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Marine , InformationManager.Instance().selfPlayer) < 2){
//				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_NongBong);
//			}
//		}
//		//end zergException_NongBong
//		
//		//start hydra exception
//		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den, InformationManager.Instance().enemyPlayer) >= 1){
//
//			if(cntLair >= 1){
//				//레어가 있고
//				if( cntHatchery >= 1 ){
//					//다른 해처리도 일번 적인 럴커 준비. 컴셋과 엔지니어링 베이가 있다면 이미 준비 끝.
//					if(InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, InformationManager.Instance().selfPlayer) == 0
//						&& InformationManager.Instance().getNumUnits(UnitType.Terran_Comsat_Station, InformationManager.Instance().selfPlayer) == 0){
//						StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_PrepareLurker);
//					}
//				}else{
//					//1레어 라면 패스트 럴커로 인식. 마인업까지 포함되어야 한다.
//					if(InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, InformationManager.Instance().selfPlayer) == 0
//						&& InformationManager.Instance().getNumUnits(UnitType.Terran_Comsat_Station, InformationManager.Instance().selfPlayer) == 0){
//						//마인업 완료에 대한 조건 추가해야함.
//						StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_FastLurker);
//					}
//				}
//				
//			}else{
//				//생산건물 >= 3 && 스파이어가 없다면.
//				if((cntHatchery + cntLair) >= 3 && InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire, InformationManager.Instance().enemyPlayer) == 0){
//					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_HydraWave);
//				}
//			}
//
//		}//end hydra exception
		
		//start guardian exception
//		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian, InformationManager.Instance().enemyPlayer) >= 1
//			||InformationManager.Instance().getNumUnits(UnitType.Zerg_Greater_Spire, InformationManager.Instance().enemyPlayer) >= 1){
//			//그레이터 스파이어 or 가디언을 발견할 경우
//			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith, InformationManager.Instance().selfPlayer) < 5){
//				//레이쓰 비율은 아직 미정. 기본적으로 5기까지만 뽑는걸로
//				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_Guardian);	
//			}
//		}
//		

		
//		
//		
//	
//		//exception strategy 가 init 이라면. basic startegy 체크
//		if(StrategyManager.StrategysException.values().equals("init")){
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) < 15
//				|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire, InformationManager.Instance().enemyPlayer) >= 1){
//				//스파이어를 보거나 뮤탈을 봤을경우
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Mutal);
//				if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) >= 15){
//					//뮤탈 올인 or 뮤탈쪽에 무게를 싣는 전략 대비
//					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_MutalMany);
//				}
//			}
//			
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Ultralisk_Cavern, InformationManager.Instance().enemyPlayer) >=1
//				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) < 10
//				&& (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) <= InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer))){
//				//울트라리스크 커번을 봤을때, 울트라가 나오고 나서 비율조정을 하게 되면 늦으므로, 미리 조정을 한다.
//				//단, 히드라는 탱크가 많아도 괜찮지만, 뮤탈은 골리앗 비중이 적어지면 어려우므로,
//				//1. 일정 뮤탈 숫자 이하일때, 비중을 변경하던지
//				//2. 발키리를 몇기정도 생산하는걸로. 이건 고민해볼걸
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
//				
//				if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8){
//					 //링+ 울트라. 울트라가 나온다면 이 전략일 확률이 크겠지.
//					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingUltra);
//				}
//				
//			}
//			
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8
//				&&InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk, InformationManager.Instance().enemyPlayer) >=5){
//				// 링+히드라 조합. 링 >=8 & 히드라 >=5
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingHydra);
//			}
//			
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8
//				&&
//				(InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker, InformationManager.Instance().enemyPlayer) >=1
//				||InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker_Egg, InformationManager.Instance().enemyPlayer) >=1)){
//				// 링+럴커 조합. 럴커를 보거나 럴커에그를 보았을때
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingLurker);
//			}
//			
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8
//				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire, InformationManager.Instance().enemyPlayer) >=1
//				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den, InformationManager.Instance().enemyPlayer) ==0){
//				// 링+뮤탈. 링 >=8 & 스파이어 & 히드라덴 없음
//				// 또는 링 >=8 & 뮤탈 발견
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingLurker);
//			}
//			
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk, InformationManager.Instance().enemyPlayer) >=8
//				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) >=8){
//				// 히드라+뮤탈. 히드라 >=8 & 뮤탈 >= 8
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HydraMutal);
//			}
//			
//			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hive, InformationManager.Instance().enemyPlayer) >=1){
//				// 하이브 발견. 하이테크 기본
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HighTech);
//			}
//			
//			if(ling_chk + hydra_chk + mutal_chk + ultra_chk >= 3){
//				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_GiftSet);
//			}
//	
//			
//		}
		
		//exception 상태가 아닐때는 Init 으로
		//StrategyManager.Instance().setCurrentStrategyException("Init");
		
	}
	
	
}