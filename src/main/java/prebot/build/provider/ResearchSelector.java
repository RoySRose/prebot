package prebot.build.provider;


import java.util.List;

import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.UnitFindRange;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.StrategyIdea;
import prebot.strategy.StrategyManager;
import prebot.strategy.constant.EnemyStrategyOptions;
import prebot.strategy.constant.EnemyStrategyOptions.UpgradeOrder;
import prebot.strategy.constant.StrategyConfig.EnemyStrategy;
import prebot.strategy.constant.StrategyConfig.EnemyStrategyException;
import prebot.build.provider.DefaultBuildableItem;

//연구해야될 UpgradeType 만
public class ResearchSelector implements Selector<MetaType>{
	BuildCondition buildCondition;
	MetaType metaType;
//    MetaType techType;
//    MetaType upgradeType;
    //BuildCondition buildCondition;

    public final MetaType getSelected(){
        return metaType;
    }

    public final void select(){
    	//metaType = new MetaType(UpgradeType.None);
   		//metaType = new MetaType(UpgradeType.None);
    	
	    	//if (Prebot.Broodwar.getFrameCount() % 43 == 0) {
    	metaType = new MetaType();
    	
		executeResearchChk();
			//}

    	
    	//FileUtils.appendTextToFile("log.txt", "\n ResearchSelector select return || Tech => " + metaType.getTechType() +" || Upgrade => " + metaType.getUpgradeType());

        //buildCondition = new BuildCondition();
        
    }
    
    public int currentResearched;
    
    public void executeResearchChk() {
//    	FileUtils.appendTextToFile("log.txt", "\n executeResearchChk || execute");

		boolean VS = (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Ion_Thrusters) == 1 ? true : false)
				|| (Prebot.Broodwar.self().isUpgrading(UpgradeType.Ion_Thrusters) ? true : false);
		boolean VM = (Prebot.Broodwar.self().hasResearched(TechType.Spider_Mines)) || (Prebot.Broodwar.self().isResearching(TechType.Spider_Mines));
		boolean TS = (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)) || (Prebot.Broodwar.self().isResearching(TechType.Tank_Siege_Mode));
		boolean GR = (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false)
				|| (Prebot.Broodwar.self().isUpgrading(UpgradeType.Charon_Boosters) ? true : false);

		
		
		if (VS && VM && TS && GR)
			return; // 4개 모두 완료이면

		currentResearched = 0;
		if (VS) {
			currentResearched++;
		}
		if (VM) {
			currentResearched++;
		}
		if (TS) {
			currentResearched++;
		}
		if (GR) {
			currentResearched++;
		}
		
//		UpgradeOrder order = StrategyIdea.currentStrategy.upgrade;
		
		List<Unit> canMachineShop = UnitUtils.getUnitList(UnitFindRange.COMPLETE, UnitType.Terran_Machine_Shop);
		
		boolean canResearch = false;
		
		int canMachineShopCnt = 0;
		
//		현재 큐에 들어있는 개발건수
		int QueueResearch = BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Ion_Thrusters)
							+ BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines)
							+ BuildManager.Instance().buildQueue.getItemCount(TechType.Tank_Siege_Mode)
							+ BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Charon_Boosters);
		
		for(Unit unit : canMachineShop) {
			if(unit.canUpgrade()) {
				canMachineShopCnt++;
				
			}
		}
		
//		비어있는 머신샵이 최소 큐에 들어있는 개발건 보다 많아야 선택
		if(canMachineShopCnt != 0 && QueueResearch < canMachineShopCnt) {
//			System.out.println("QueueResearch : " + QueueResearch + " / canMachineShopCnt : " + canMachineShopCnt);
			canResearch = true;
		}
		
		
		
		
//		canResearch = true;
		
		if(canResearch == true) {
		
			List<MetaType> upgradeOrder = StrategyIdea.upgrade;
			
			
			for(MetaType e : upgradeOrder) {
//				System.out.println("upgradeOrder chk==> " + e.getName());
				if(BuildManager.Instance().buildQueue.getItemCount(e) != 0){
					continue;
				}
				if(e.isUpgrade()) {
					if(Prebot.Broodwar.self().getUpgradeLevel(e.getUpgradeType()) == 1 
							|| Prebot.Broodwar.self().isUpgrading(e.getUpgradeType())
							|| BuildManager.Instance().buildQueue.getItemCount(e.getUpgradeType()) != 0
							) {
						continue;
					}else {
						System.out.println("upgradeOrder selected ==> " + metaType.getUpgradeType());
						metaType = e;
						break;
					}
				}
				if(e.isTech()) {
					if(Prebot.Broodwar.self().hasResearched(e.getTechType()) 
							|| Prebot.Broodwar.self().isResearching(e.getTechType())
							|| BuildManager.Instance().buildQueue.getItemCount(e.getTechType()) != 0
							){
						continue;
					}else {
						metaType = e;
						System.out.println("techOrder selected ==> " + metaType.getTechType());
						break;
					}
				}
				
			}
			
		}
		
		

//		int myFactoryUnitSupplyCount = UnitUtils.myFactoryUnitSupplyCount();
//		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Command_Center) < 2 && currentResearched >= 2 && myFactoryUnitSupplyCount < 32
//				&& !(Prebot.Broodwar.self().minerals() > 300 && Prebot.Broodwar.self().gas() > 250))
//			return;
//
//		MetaType vultureSpeed = new MetaType(UpgradeType.Ion_Thrusters);
//		MetaType vultureMine = new MetaType(TechType.Spider_Mines);
//		MetaType TankSiegeMode = new MetaType(TechType.Tank_Siege_Mode);
//		MetaType GoliathRange = new MetaType(UpgradeType.Charon_Boosters);
//
//		MetaType vsZerg[] = new MetaType[] { vultureMine, GoliathRange, TankSiegeMode, vultureSpeed };
//		boolean vsZergbool[] = new boolean[] { VM, GR, TS, VS };
//		MetaType vsZergHydra[] = new MetaType[] { vultureMine, TankSiegeMode, GoliathRange, vultureSpeed };
//		boolean vsZergHydrabool[] = new boolean[] { VM, TS, GR, VS };
//		MetaType vsZergLurker[] = new MetaType[] { TankSiegeMode, GoliathRange, vultureMine, vultureSpeed };
//		boolean vsZergLurkerbool[] = new boolean[] { TS, GR, VM, VS };
//		MetaType vsTerran[] = new MetaType[] { vultureMine, TankSiegeMode, vultureSpeed, GoliathRange };
//		boolean vsTerranbool[] = new boolean[] { VM, TS, VS, GR };
//		MetaType vsTerranBio[] = new MetaType[] { vultureSpeed, TankSiegeMode, vultureMine, GoliathRange };
//		boolean vsTerranBiobool[] = new boolean[] { VS, TS, VM, GR };
//		// MetaType vsProtoss[] = new MetaType[]{vultureMine, vultureSpeed, TankSiegeMode, GoliathRange};
//		// boolean vsProtossbool[] = new boolean[]{VM, VS, TS, GR};
//		MetaType vsProtoss[] = new MetaType[] { TankSiegeMode, vultureMine, vultureSpeed, GoliathRange };
//		boolean vsProtossbool[] = new boolean[] { TS, VM, VS, GR };
//		MetaType vsProtossZealot[] = new MetaType[] { vultureSpeed, vultureMine, TankSiegeMode, GoliathRange };
//		boolean vsProtossZealotbool[] = new boolean[] { VS, VM, TS, GR };
//		MetaType vsProtossDragoon[] = new MetaType[] { TankSiegeMode, vultureMine, vultureSpeed, GoliathRange };
//		boolean vsProtossDragoonbool[] = new boolean[] { TS, VM, VS, GR };
//		MetaType vsProtossDouble[] = new MetaType[] { vultureMine, TankSiegeMode, vultureSpeed, GoliathRange };
//		boolean vsProtossDoublebool[] = new boolean[] { VM, TS, VS, GR };
//		MetaType vsProtossBasic_DoublePhoto[] = new MetaType[] { TankSiegeMode, vultureSpeed, vultureMine, GoliathRange };
//		boolean vsProtossBasic_DoublePhotobool[] = new boolean[] { TS, VS, VM, GR };
//
//		MetaType[] Current = null;
//		boolean[] Currentbool = null;
//		boolean air = true;
//		boolean terranBio = false;
//
//		EnemyStrategy currentStrategy = StrategyManager.Instance().currentStrategy;
//		EnemyStrategyException currentStrategyException = StrategyManager.Instance().currentStrategyException;
//
//		EnemyStrategyException lastStrategyException = StrategyManager.Instance().lastStrategyException;
//
//		if (InformationManager.Instance().enemyRace == Race.Protoss) {
//			Current = vsProtoss;
//			Currentbool = vsProtossbool;
//			if (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH
//					|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_ZEALOTPUSH)) {
//				Current = vsProtossZealot;
//				Currentbool = vsProtossZealotbool;
//			}
//			if ((currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH
//					|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DRAGOONPUSH))
//					|| (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH
//							|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_PHOTONRUSH))) {
//				Current = vsProtossDragoon;
//				Currentbool = vsProtossDragoonbool;
//			}
//			if (currentStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS
//					|| (currentStrategyException == EnemyStrategyException.INIT && lastStrategyException == EnemyStrategyException.PROTOSSEXCEPTION_DOUBLENEXUS)) {
//				Current = vsProtossDouble;
//				Currentbool = vsProtossDoublebool;
//			}
//
//			if (currentStrategy == EnemyStrategy.PROTOSSBASIC_DOUBLEPHOTO) {
//				Current = vsProtossBasic_DoublePhoto;
//				Currentbool = vsProtossBasic_DoublePhotobool;
//			}
//
//			air = false;
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Protoss_Stargate || unit.getType() == UnitType.Protoss_Arbiter || unit.getType() == UnitType.Protoss_Carrier
//						|| unit.getType() == UnitType.Protoss_Corsair || unit.getType() == UnitType.Protoss_Scout || unit.getType() == UnitType.Protoss_Arbiter_Tribunal
//						|| unit.getType() == UnitType.Protoss_Fleet_Beacon || unit.getType() == UnitType.Protoss_Shuttle) {
//					air = true;
//				}
//			}
//		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
//			Current = vsTerran;
//			Currentbool = vsTerranbool;
//			if (currentStrategy == EnemyStrategy.TERRANBASIC_BIONIC) {
//				Current = vsTerranBio;
//				Currentbool = vsTerranBiobool;
//				terranBio = true;
//			}
//			air = false;
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Terran_Starport || unit.getType() == UnitType.Terran_Science_Facility || unit.getType() == UnitType.Terran_Dropship
//						|| unit.getType() == UnitType.Terran_Science_Vessel || unit.getType() == UnitType.Terran_Wraith || unit.getType() == UnitType.Terran_Battlecruiser
//						|| unit.getType() == UnitType.Terran_Physics_Lab || unit.getType() == UnitType.Terran_Control_Tower) {
//					air = true;
//				}
//			}
//		} else {
//			Current = vsZerg;
//			Currentbool = vsZergbool;
//			for (Unit unit : Prebot.Broodwar.enemy().getUnits()) {
//				if (unit.getType() == UnitType.Zerg_Mutalisk || unit.getType() == UnitType.Zerg_Lair || unit.getType() == UnitType.Zerg_Spire
//						|| unit.getType() == UnitType.Zerg_Scourge || unit.getType() == UnitType.Zerg_Guardian || unit.getType() == UnitType.Zerg_Devourer) {
//					air = true;
//				}
//			}
//			if (currentStrategy == EnemyStrategy.ZERGBASIC_HYDRAWAVE || currentStrategy == EnemyStrategy.ZERGBASIC_LINGHYDRA) {
//				Current = vsZergHydra;
//				Currentbool = vsZergHydrabool;
//			}
//
//			if (currentStrategyException == EnemyStrategyException.ZERGEXCEPTION_PREPARELURKER) {
//				Current = vsZergLurker;
//				Currentbool = vsZergLurkerbool;
//			}
//		}
//
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
//			
//			if (unit == null)
//				continue;
//
//			if (unit.getType() == UnitType.Terran_Machine_Shop && unit.isCompleted() && unit.canUpgrade()) {
//				FileUtils.appendTextToFile("log.txt", "\n executeResearchChk || Machine Shop is empty");
//				if (Currentbool == null)
//					return;
//				for (int i = 0; i < 4; i++) {
////					FileUtils.appendTextToFile("log.txt", "\n executeResearchChk || chk for Current || "+ Current[i].getName());
//					if (Currentbool[i] == true) {
//						continue;
//					} else {
//						if (i == 3 && Current[3].getUpgradeType() == UpgradeType.Charon_Boosters) {
//							if (!air && !(Prebot.Broodwar.self().minerals() > 200 && Prebot.Broodwar.self().gas() > 150)) {
//								continue;
//							}
//						}
//						if (terranBio && i == 2 && Current[2].getTechType() == TechType.Spider_Mines) {
//							if ((myFactoryUnitSupplyCount > 48 && Prebot.Broodwar.self().minerals() > 200 && Prebot.Broodwar.self().gas() > 150)
//									|| myFactoryUnitSupplyCount > 100) {
//
//							} else {
//								continue;
//							}
//						}
//						if (BuildManager.Instance().buildQueue.getItemCount(Current[i]) == 0) {
//							UpgradeType tempU = null;
//							FileUtils.appendTextToFile("log.txt", "\n executeResearchChk || buildQueue.getItemCount 0 || " + Current[i].getName());
//							if (Current[i].isUpgrade()) {
//								
//								boolean booster = false;
//								for (Unit unitcheck : Prebot.Broodwar.self().getUnits()) {
//									if (unitcheck.getType() == UnitType.Terran_Armory && unitcheck.isCompleted()) {
//										booster = true;
//									}
//								}
//								tempU = Current[i].getUpgradeType();
//								if (tempU == UpgradeType.Charon_Boosters && booster == false) {
//									return;
//								}
//							}
//							//if(Current[i].isUpgrade()) {
//								metaType = Current[i];
//							//}
////							FileUtils.appendTextToFile("log.txt", "\n executeResearchChk || select Research || " + metaType);
//							
//							/*if (currentResearched <= 2) {
//								//BuildManager.Instance().buildQueue.queueAsHighestPriority(Current[i], true);
//								DefaultBuildableItem.setBlocking(true);
//								DefaultBuildableItem.setHighPriority(true);
//								metaType =  Current[i];
//							} else {
//								BuildManager.Instance().buildQueue.queueAsLowestPriority(Current[i], false);
//								metaType =  Current[i];
//							}*/
//							
//						}
//						break;
//					}
//				}
//			}
//		}
	}
}
