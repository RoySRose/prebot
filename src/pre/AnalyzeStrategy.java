package pre;

import java.util.List;
import java.util.Set;

import bwapi.Color;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Region;
import pre.main.MyBotModule;
import pre.manager.InformationManager;
import pre.manager.StrategyManager;
import pre.manager.StrategyManager.Strategys;
import pre.manager.StrategyManager.StrategysException;
import pre.util.MicroUtils;
//import pre.RespondToStrategy;

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
	
	public void AnalyzeEnemyStrategyInit() {

		//최초 각 종족별 Basic 세팅
		//Exception 은기본 init 처리
		StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
		if (MyBotModule.Broodwar.enemy().getRace() == Race.Protoss) {
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic);
		}else if (MyBotModule.Broodwar.enemy().getRace() == Race.Terran) {
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic);
		}else{
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic);
		}

	}


	private void AnalyzeVsProtoss() {
		
		
		/*if(StrategyManager.Instance().getCurrentStrategyBasic().equals(StrategyManager.Strategys.protossBasic_Templer) && !StrategyManager.Strategys.values().equals(StrategyManager.Strategys.protossBasic_Carrier)){
			
			//String test = StrategyManager.Strategys.protossBasic_Templer;
			//캐리어와 템플러 대비 기본이 아니라면 일반 기본으로
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.protossBasic); // 이런식으로 기본 전략 세팅가능
		}*/
		if((InformationManager.Instance().getNumUnits(UnitType.Protoss_Templar_Archives, InformationManager.Instance().enemyPlayer) >= 1
				&& MyBotModule.Broodwar.getFrameCount() < 7000)
				// 약  5분 안쪽으로 템플러 아카이브를 발견하거나				
				||(MyBotModule.Broodwar.getFrameCount() > 5500 &&
							InformationManager.Instance().getNumUnits(UnitType.Protoss_Dragoon, InformationManager.Instance().enemyPlayer)
							+InformationManager.Instance().getNumUnits(UnitType.Protoss_Zealot, InformationManager.Instance().enemyPlayer) < 4)
				//약 4분이 넘어갈떄 질럿 + 드래군의 수가 4기 미만일경우(정찰이 안된경우나 마찬가지)
				//|| (InformationManager.Instance().getNumUnits(UnitType., InformationManager.Instance().enemyPlayer) >= 1)
				//다크에 공격당한다면
 
				){

				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Dark);
				//RespondToStrategy.instance().enemy_dark_templar = true;
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Academy, InformationManager.Instance().selfPlayer) >= 1){
					// 아카데미가 있다면 exit
					// 엔지니어링 베이까지 이니셜에 넣을계획
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
				}
		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Robotics_Facility, InformationManager.Instance().enemyPlayer) >= 1){
			//로보틱스를 발견하면, 터렛으로 본진대비
			
				//엔지니어링 베이가 있다면, 기본전략으로 대응하고, 본진에 터렛만 추가.
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Shuttle);
				
//				RespondToStrategy.instance().enemy_shuttle = true;
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, InformationManager.Instance().selfPlayer) >= 1){
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
				}
			if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle, InformationManager.Instance().enemyPlayer) >= 1){
				//셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
				//셔틀대비 골리앗 비율이 맞다면 넘어간다.
				//로직 추가할것. 생각중
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_ShuttleMix);
				if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer) / InformationManager.Instance().getNumUnits(UnitType.Protoss_Shuttle, InformationManager.Instance().enemyPlayer) >= 2){
					//셔틀 1대에 골리앗 2기정도 추가
					//완료되면 exit
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
				}
			}
			if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Robotics_Support_Bay, InformationManager.Instance().enemyPlayer) >= 1
				||InformationManager.Instance().getNumUnits(UnitType.Protoss_Reaver, InformationManager.Instance().enemyPlayer) >= 1){
				//셔틀을 발견한건 익셉션 처리로. 셔틀 대비 골리앗만 추가.
				//본진 방어가 되면 넘어간다. 로직 추가할것.
				//이건 exit 조건을 본진에 방어병력이 있는 지로 판단해야 하는데 애매해서 일단 넘겼음
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Reaver);
			}

		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer) >= 1){
			//스카웃은 큰 위협이 안되므로, 골리앗만 맞춰줄것
			
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_Scout);
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer) >= InformationManager.Instance().getNumUnits(UnitType.Protoss_Scout, InformationManager.Instance().enemyPlayer)){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}
			
		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) >= 8){
			//캐리어가 8기 이상일때부터 대비시작. 고스트 & 락다운 준비
			
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.protossException_CarrierMany);
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Ghost, InformationManager.Instance().selfPlayer) >= InformationManager.Instance().getNumUnits(UnitType.Protoss_Carrier, InformationManager.Instance().enemyPlayer) / 2){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}
		}
		
		//프로토스는 특히 베이직이 단순한데 질럿 드래군 하템 비율에 따라 맞춰주면 되고
		//다템이나 아비터 / 캐리어 올인에 대한 익셉션만 처리하면된다.c
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
		//치즈러시 대비
		int nongbong_cnt = 0;
		Unit enemy_bunker = null;
		bwta.BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		bwta.Region myRegion = base.getRegion();
		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
		if (enemyUnitsInRegion.size() >= 5){
			 for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
				 if(enemyUnitsInRegion.get(enemy).getType().equals("Terran_Marine")||enemyUnitsInRegion.get(enemy).getType().equals("Terran_SCV")){
					 nongbong_cnt ++;
				 }
				 //치즈러쉬 와서 벙커를 지으면 벙커부터 쳐준다.
				 //.... 어쩔까...... 마린은 왠지 도망갈것 같고..........
				 //벙커를 치는게 낫지 않을까 농봉이니까
				 if(enemyUnitsInRegion.get(enemy).getType() == UnitType.Terran_Bunker){
					 enemy_bunker = enemyUnitsInRegion.get(enemy);
				 }
			 }
		}
		
		//벌쳐가 없는 상태에서 상대 유닛이 내본진에 5기 이상있다면 농봉이다
		if(InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture) < 1
			&& nongbong_cnt >= 5){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_CheeseRush);
		}
		//벌쳐가 있거나 마린이 있다면 농봉없이 벙커짓고 막자.
		//단 치즈러쉬가 와서 벙커를 짓고 있다면 농봉상태 유지
		if((nongbong_cnt <= 4 && InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Vulture) > 0)
				||nongbong_cnt <= 4 && InformationManager.Instance().selfPlayer.completedUnitCount(UnitType.Terran_Marine) > 2){
			//인구수대비 해제된 상태에서 적 벙커가 없다면 농봉해제. 아니라면 유지.
			if(enemy_bunker == null){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}
		}
		
		//핵에 대한건 보류 조건이 까다롭다. 원하는 조건을 어찌 해야할지 모르겄네
		/*if(InformationManager.Instance().getNumUnits(UnitType.Terran_Nuclear_Silo, InformationManager.Instance().enemyPlayer) >= 1){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_NuClear);
		}*/
		
		if(InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith, InformationManager.Instance().enemyPlayer) >= 20){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.terranException_Wraith);
		}
		
		if(StrategyManager.StrategysException.values().equals(StrategyManager.StrategysException.Init)){
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks, InformationManager.Instance().enemyPlayer) >= 2){
				//2배럭이면 바이오닉
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_Bionic);
			}
			//팩이 배럭보다 많거나, 팩이 3개 이상일경우 기본으로 변경
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks, InformationManager.Instance().enemyPlayer) <= InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, InformationManager.Instance().enemyPlayer)
					||InformationManager.Instance().getNumUnits(UnitType.Terran_Barracks, InformationManager.Instance().enemyPlayer) >= 3){
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic);
			}
			
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Starport, InformationManager.Instance().enemyPlayer) == 1
				&& InformationManager.Instance().getNumUnits(UnitType.Terran_Factory, InformationManager.Instance().enemyPlayer) >= 1){
				//공중 유닛에 대한 대응
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_AirUnit);
//				RespondToStrategy.instance().enemy_wraith = true;
			}else if(InformationManager.Instance().getNumUnits(UnitType.Terran_Starport, InformationManager.Instance().enemyPlayer) >= 2){
				//스타포트 다수에 대한 대응
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.terranBasic_AirUnit);
//				RespondToStrategy.instance().enemy_wraith = true;
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
		int lurker_chk = 0;
		int ultra_chk = 0;
		
		
		int ling_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer);
		int hydra_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk, InformationManager.Instance().enemyPlayer);
		int lurker_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker, InformationManager.Instance().enemyPlayer)
				+ InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker_Egg, InformationManager.Instance().enemyPlayer);
		int mutal_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer);
		int ultra_cnt = InformationManager.Instance().getNumUnits(UnitType.Zerg_Ultralisk, InformationManager.Instance().enemyPlayer);;
		
		if(ling_cnt >= 1){
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
		
		
		//기본 저그 전술 세팅
		/*if(StrategyManager.Instance().getCurrentStrategyBasic() == StrategyManager.Strategys.zergBasic_HighTech){

			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HighTech);
		}else{
			System.out.println("zergBasic 으로 세팅");
			StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic);
		}
		System.out.println("zergBasic 으로 세팅");*/
		//익셉션은 init 기본
		//StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
		
		//start zergException_NongBong
		int nongbong_cnt = 0;
		int haveMarine = 0;
		int haveBunker = 0;
		
		Unit theBunker = null;
		Unit theCommand = null;
		BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
		Region myRegion = base.getRegion();
		List<Unit> enemyUnitsInRegion = MicroUtils.getUnitsInRegion(myRegion, InformationManager.Instance().enemyPlayer);
		if (enemyUnitsInRegion.size() >= 4){
			//내 본진에 저글링이 4마리 이상이고
			for (Unit unit : MyBotModule.Broodwar.getAllUnits()){
				if(unit.getType() == UnitType.Terran_Bunker){
					haveBunker ++;
					theBunker = unit;
				}
				
				if(unit.getType() == UnitType.Terran_Marine){
					haveMarine ++;
					if(haveBunker > 0 && haveMarine >0){
						theBunker.load(unit);
					}
				}
				
				if(unit.getType() == UnitType.Terran_Command_Center){
					theCommand = unit;
				}
				
			}
			//벙커가 없는 경우.
			if(haveBunker == 0){
				 for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
					 if(enemyUnitsInRegion.get(enemy).getType() == UnitType.Zerg_Zergling
							 && enemyUnitsInRegion.get(enemy).getDistance(theCommand) < 200){ 
						 nongbong_cnt ++;
					 }
				 }
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_NongBong);
			}
			// 벙커가 1개인데 마린이 2 미만인 경우
			if(haveBunker == 1 && haveMarine < 2){
				for(int enemy = 0; enemy < enemyUnitsInRegion.size(); enemy ++){
					 if(enemyUnitsInRegion.get(enemy).getType() == UnitType.Zerg_Zergling
							 && enemyUnitsInRegion.get(enemy).getDistance(theBunker) < 100){ 
						 NongbongScv.Instance().NongBong_At_Unit = enemyUnitsInRegion.get(enemy);
						 nongbong_cnt ++;
					 }
				 }
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_NongBong);
			}
			
		}
		if(StrategyManager.Instance().getCurrentStrategyException() ==  StrategyManager.StrategysException.zergException_NongBong  && nongbong_cnt < 3){
			//본진 저글링이 3미만 이라면 농봉을 안타고 init으로 exit
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
		}
		
		//end zergException_NongBong
		
		//start hydra exception
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den, InformationManager.Instance().enemyPlayer) >= 1){

			if(cntLair >= 1){
				//레어가 둘일리는 없지만 어쨌든 둘이라도
				//레어가 있고
				if( cntHatchery >= 1 ){
					//다른 해처리도 일반 적인 럴커 준비. 컴셋과 엔지니어링 베이가 있다면 이미 준비 끝.
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_PrepareLurker);
//					RespondToStrategy.instance().enemy_lurker = true;
					if(InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, InformationManager.Instance().selfPlayer) > 0
						&& InformationManager.Instance().getNumUnits(UnitType.Terran_Comsat_Station, InformationManager.Instance().selfPlayer) > 0){
						//컴셋스테이션O , 엔지니어링 베이 O 면 일단 럴커 대비는 된것으로.
						StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
					}
				}else{
					//1레어 라면 패스트 럴커로 인식. 마인업까지 포함되어야 한다.
					StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_FastLurker);
//					RespondToStrategy.instance().enemy_lurker = true;
					if(InformationManager.Instance().getNumUnits(UnitType.Terran_Engineering_Bay, InformationManager.Instance().selfPlayer) > 0
							&& InformationManager.Instance().getNumUnits(UnitType.Terran_Comsat_Station, InformationManager.Instance().selfPlayer) > 0
							&& MyBotModule.Broodwar.self().hasResearched(TechType.Spider_Mines)){
						//엔지니어링 베이 O
						//컴셋 스테이션 O
						//마인업그레이드 O
						StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);	
					}
				}
				
			}else{
				//레어가 없는 상태로 히드라덴이라면 히드라 웨이브로 일단 맞춘다.
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HydraWave);
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}

		}//end hydra exception
		
		//start guardian exception
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian, InformationManager.Instance().enemyPlayer) >= 1
			||InformationManager.Instance().getNumUnits(UnitType.Zerg_Greater_Spire, InformationManager.Instance().enemyPlayer) >= 1){
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_Guardian);

			//그레이터 스파이어 or 가디언을 발견할 경우
			//레이스가 round(가디언/3) or 5기를 만족한다면 exit
			
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith, InformationManager.Instance().selfPlayer) == 5
					||Math.ceil(InformationManager.Instance().getNumUnits(UnitType.Zerg_Guardian, InformationManager.Instance().enemyPlayer) /3) >= InformationManager.Instance().getNumUnits(UnitType.Terran_Wraith, InformationManager.Instance().selfPlayer) ){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
					
			}
		}
		
		if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Hive, InformationManager.Instance().enemyPlayer) >=1){
			// 하이브 발견. 하이테크 시에는 베슬 4기 이상 추가
			// 이걸로 디파일러 까지 배제가능
			StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.zergException_HighTech);
			
			//
			if(InformationManager.Instance().getNumUnits(UnitType.Terran_Science_Vessel, InformationManager.Instance().selfPlayer) > 3){
				StrategyManager.Instance().setCurrentStrategyException(StrategyManager.StrategysException.Init);
			}
		}
		

		//zerg basic 전략은 상위테크부터 배제해 나간다.
		if(ultra_cnt == 0){
			//울트라 없음
			if(mutal_cnt == 0){
				//뮤탈 없음
				if(hydra_cnt ==0){
					//히드라 없음
					//링까지 없는 경우는 없겠지만 어쨌든 기본
					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic);
				}else if(hydra_cnt !=0){
					//울트라 X 뮤탈 X 히드라 O
					if(ling_cnt ==0){
						//온리히드라
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HydraWave);
					}else if(ling_cnt !=0){
						if(ling_cnt >= 8){
							//링이 많으면 링+히드라
							StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingHydra);
						}else{
							//링이 적으면 히드라 웨이브
							StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HydraWave);
						}
					}
				}
				
			}else if(mutal_cnt != 0){
				//울트라 X 뮤탈 O
				if(hydra_cnt ==0){
					//히드라 X
					if(ling_cnt ==0){
						//링없으면 온리 뮤탈
						if(mutal_cnt < 15){
							//뮤탈 15보다 적으면 걍 뮤탈
							StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Mutal);
						}else{
							//많으면 온리뮤탈 대비
							StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_MutalMany);
						}
					}else if(ling_cnt !=0){
						if(mutal_cnt < 15){
							//링있으면 뮤탈링
							StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingMutal);
						}else{
							//많으면 온리뮤탈 대비
							StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_MutalMany);
						}
					}
					
				}else if(hydra_cnt !=0){
					//울트라 X 뮤탈 O 히드라 O
					if(ling_cnt ==0){
						//링없으면 히드라 뮤탈
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HydraMutal);
					}else if(ling_cnt !=0){
						//링까지 있을때
						//골리앗을 기본으로 갈거니까
						//종합세트를 lair / hive 단계로 나누었으면 좋겠음.
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_GiftSet);
						
					}
				}
			}
			
			
		}else if(ultra_cnt != 0){
			//울트라 O
			if(mutal_cnt == 0){
				//뮤탈 X
				if(hydra_cnt ==0){
					//히드라 X
					if(ling_cnt ==0){
						//링X
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
					}else if(ling_cnt !=0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingUltra);
					}
				}else if(hydra_cnt !=0){
					//히드라는 탱크비율 문제이므로 크게 상관없음
					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
					if(ling_cnt ==0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
					}else if(ling_cnt !=0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingUltra);
					}
				}
				
			}else if(mutal_cnt != 0){
				//울트라 빌드에는 일정수준 뮤탈에 대한 방어가 들어있어야 함
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
				if(hydra_cnt ==0){
					if(ling_cnt ==0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
					}else if(ling_cnt !=0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_GiftSet);
					}
					
				}else if(hydra_cnt !=0){
					if(ling_cnt ==0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_GiftSet);
					}else if(ling_cnt !=0){
						StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_GiftSet);
					}
				}
			}
		}
		
		
		/* 기존로직은일단 주석처리
			if(mutal_cnt >= 1){
				//|| InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire, InformationManager.Instance().enemyPlayer) >= 1){
				//뮤탈을 봤을경우   //건물을 보는것은 일단 보류. 
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Mutal);
				if(mutal_cnt >= 15){
					//뮤탈 올인 or 뮤탈쪽에 무게를 싣는 전략 대비
					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_MutalMany);
				}
			}
			
			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Ultralisk_Cavern, InformationManager.Instance().enemyPlayer) >=1
				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) < 10
				&& (InformationManager.Instance().getNumUnits(UnitType.Zerg_Mutalisk, InformationManager.Instance().enemyPlayer) <= InformationManager.Instance().getNumUnits(UnitType.Terran_Goliath, InformationManager.Instance().selfPlayer))){
				//울트라리스크 커번을 봤을때, 울트라가 나오고 나서 비율조정을 하게 되면 늦으므로, 미리 조정을 한다.
				//단, 히드라는 탱크가 많아도 괜찮지만, 뮤탈은 골리앗 비중이 적어지면 어려우므로,
				//1. 일정 뮤탈 숫자 이하일때, 비중을 변경하던지
				//2. 발키리를 몇기정도 생산하는걸로. 이건 고민해볼걸
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_Ultra);
				
				if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8){
					 //링+ 울트라. 울트라가 나온다면 이 전략일 확률이 크겠지.
					StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingUltra);
				}
				
			}
			
			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8
				&&InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk, InformationManager.Instance().enemyPlayer) >=5){
				// 링+히드라 조합. 링 >=8 & 히드라 >=5
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingHydra);
			}
			
			if(InformationManager.Instance().getNumUnits(UnitType.Zerg_Zergling, InformationManager.Instance().enemyPlayer) >=8
				&&
				(InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker, InformationManager.Instance().enemyPlayer) >=1
				||InformationManager.Instance().getNumUnits(UnitType.Zerg_Lurker_Egg, InformationManager.Instance().enemyPlayer) >=1)){
				// 링+럴커 조합. 럴커를 보거나 럴커에그를 보았을때
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingLurker);
			}
			
			if((ling_cnt >=8
				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Spire, InformationManager.Instance().enemyPlayer) >=1
				&& InformationManager.Instance().getNumUnits(UnitType.Zerg_Hydralisk_Den, InformationManager.Instance().enemyPlayer) ==0)
					|| ling_cnt >= 8 && mutal_cnt >= 5){
				// 링+뮤탈. 링 >=8 & 스파이어 & 히드라덴 없음
				// 또는 링 >=8 & 뮤탈 발견
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_LingMutal);
			}
			
			//아마 우리가 상대하게될 가장 많은 조합
			//저그전은 이것때문에 특히 컴셋이 중요한데
			//시즈 탱크와 시즈업은 히드라가 나오는 순간 찍어줘도 되기 때문이다.
			if(hydra_cnt >=8	&& mutal_cnt >=8){
				// 히드라+뮤탈. 히드라 >=8 & 뮤탈 >= 8
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_HydraMutal);
			}
			
			if(ling_chk + hydra_chk + mutal_chk + ultra_chk >= 3){
				StrategyManager.Instance().setCurrentStrategyBasic(StrategyManager.Strategys.zergBasic_GiftSet);
			}
	
			
		//};
		
		//exception 상태가 아닐때는 Init 으로
		//StrategyManager.Instance().setCurrentStrategyException("Init");
			if(MyBotModule.Broodwar.getFrameCount() % 48 == 0){
				System.out.println("Current Basic strategy ===>>>>  " +  StrategyManager.Instance().getCurrentStrategyBasic());
				
				System.out.println("Current Exception strategy ===>>>>  " +  StrategyManager.Instance().getCurrentStrategyException());
			}
			기존로직 주석처리 끝*/
		
		
	}
	
}