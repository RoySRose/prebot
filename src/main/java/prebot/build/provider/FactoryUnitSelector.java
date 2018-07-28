package prebot.build.provider;


import bwapi.UnitType;
import prebot.build.prebot1.BuildManager;
import prebot.common.main.Prebot;
import prebot.common.util.PlayerUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class FactoryUnitSelector implements Selector<UnitType>{

	public int vultureratio = 0;
	public int tankratio = 0;
	public int goliathratio = 0;
	public int wgt = 1;
	
	public int GetCurrentTot(UnitType checkunit) {
		return BuildManager.Instance().buildQueue.getItemCount(checkunit) + Prebot.Broodwar.self().allUnitCount(checkunit);
	}

	public int GetCurrentTotBlocked(UnitType checkunit) {
		int cnt = Prebot.Broodwar.self().allUnitCount(checkunit);
		return cnt;
	}
	
	public static UnitType chooseunit(int ratea, int rateb, int ratec, int wgt, int tota, int totb, int totc) {

		if (wgt < 1 || wgt > 3) {
			wgt = 1;
		}
		double tempa = 0;
		double tempb = 0;
		double tempc = 0;
		if (ratea == 0) {
			tempa = 99999999;
		} else {
			tempa = 1.0 / ratea * tota;
		}
		if (rateb == 0) {
			tempb = 99999999;
		} else {
			tempb = 1.0 / rateb * totb;
		}
		if (ratec == 0) {
			tempc = 99999999;
		} else {
			tempc = 1.0 / ratec * totc;
		}
		int num = least(tempa, tempb, tempc, wgt);
		if (num == 3) {// 1:벌쳐, 2:시즈, 3:골리앗
			return UnitType.Terran_Goliath;
		} else if (num == 2) {
			return UnitType.Terran_Siege_Tank_Tank_Mode;
		} else {
			return UnitType.Terran_Vulture;
		}
	}
	
	public static int least(double a, double b, double c, int checker) {

		int ret = 0;
		if (a > b) {
			if (b > c) {
				ret = 3; // a>b>c
			} else {
				ret = 2; // a>b, b>=c
			}
		} else {
			if (a > c) { // a<=b, a>c
				ret = 3;
			} else { // a<=b, a<=c
				ret = 1;
			}
		}
		if (ret == 1) {
			if (a == b && checker != 3) {
				ret = checker;
			} else if (a == c && checker != 2) {
				ret = checker;
			}
		} else if (ret == 2) {
			if (b == a && checker != 3) {
				ret = checker;
			} else if (b == c && checker != 1) {
				ret = checker;
			}
		} else if (ret == 3) {
			if (c == a && checker != 2) {
				ret = checker;
			} else if (c == b && checker != 1) {
				ret = checker;
			}
		}
		return ret;
	}
	
//	public void setCombatUnitRatio() {
//		vultureratio = 0;
//		tankratio = 0;
//		goliathratio = 0;
//		wgt = 1;
//
//		// config setting 가지고 오기
//		if (StrategyManager.Instance().currentStrategyException == EnemyStrategyException.INIT) {
//			vultureratio = StrategyIdea.currentStrategy.ratio.vulture;
//			tankratio = StrategyIdea.currentStrategy.ratio.tank;
//			goliathratio = StrategyIdea.currentStrategy.ratio.goliath;
//			wgt = 1;
//		} else {
//			vultureratio = StrategyManager.Instance().currentStrategyException.vultureRatio;
//			tankratio = StrategyManager.Instance().currentStrategyException.tankRatio;
//			goliathratio = StrategyManager.Instance().currentStrategyException.goliathRatio;
//			wgt = StrategyManager.Instance().currentStrategyException.weight;
//
//			if (vultureratio == 0 && tankratio == 0 && goliathratio == 0) {
//				vultureratio = StrategyManager.Instance().currentStrategy.vultureRatio;
//				tankratio = StrategyManager.Instance().currentStrategy.tankRatio;
//				goliathratio = StrategyManager.Instance().currentStrategy.goliathRatio;
//				wgt = StrategyManager.Instance().currentStrategy.weight;
//			}
//		}
//	}
	
	
	
	
	
	
//	original source ======================
	
    UnitType unitType;
    //BuildCondition buildCondition;

    public final UnitType getSelected(){
        return unitType;
    }

	public final void select() {
		unitType = UnitType.None;
		
		if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode) == 0
				&& BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath) == 0) {

			int totalVulture = UnitUtils.hasUnitOrWillBeCount(UnitType.Terran_Vulture);
			int totalTank = UnitUtils.hasUnitOrWillBeCount(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
			int totalGoliath = UnitUtils.hasUnitOrWillBeCount(UnitType.Terran_Vulture);

			vultureratio = StrategyIdea.factoryRatio.vulture;
			tankratio = StrategyIdea.factoryRatio.tank;
			goliathratio = StrategyIdea.factoryRatio.goliath;
			wgt = StrategyIdea.factoryRatio.weight;

			UnitType selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, totalVulture, totalTank, totalGoliath);
			if (PlayerUtils.enoughResource(selected.mineralPrice(), selected.gasPrice()) && Prebot.Broodwar.self().supplyUsed() <= 392) {
				unitType = selected;
			}
		}
	}
    
    
//	public final void select() {
//    	
////	    	setCombatUnitRatio();
//    		
//    		vultureratio = StrategyIdea.factoryRatio.vulture;
//			tankratio = StrategyIdea.factoryRatio.tank;
//			goliathratio = StrategyIdea.factoryRatio.goliath;
//			wgt = StrategyIdea.factoryRatio.weight;
//			
////			FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || ratio  || vultureratio : " + vultureratio + " / tankratio : " + tankratio + " / goliathratio : " + goliathratio);
//    		
//	        unitType = UnitType.None;
//	    	//unitType = null;
//	    	
//	    	int tot_vulture = GetCurrentTot(UnitType.Terran_Vulture);
//			int tot_tank = GetCurrentTot(UnitType.Terran_Siege_Tank_Tank_Mode) + GetCurrentTot(UnitType.Terran_Siege_Tank_Siege_Mode);
//			int tot_goliath = GetCurrentTot(UnitType.Terran_Goliath);
//	
//			UnitType selected = null;
////			int currentinbuildqueuecnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null)
////					+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode, null)
////					+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath, null);
//			
//			int currentinbuildqueuecnt = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture)
//					+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode)
//					+ BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath);
////			System.out.println("currentinbuildqueuecnt ==>> Vulture : " + BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture)
////					+" / Tank : " + BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Siege_Tank_Tank_Mode)
////					+" / Goliath : " +BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Goliath));
////			int currentinbuildqueuecnt_vulture = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Vulture, null);
//	
//			
//	//		FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || currentinbuildqueuecnt || " + currentinbuildqueuecnt);
//	
//			if (currentinbuildqueuecnt == 0) {
//				selected = chooseunit(vultureratio, tankratio, goliathratio, wgt, tot_vulture, tot_tank, tot_goliath);
//				
//	//			FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || now mineral : " + Prebot.Broodwar.self().minerals() + "now gas : " + Prebot.Broodwar.self().gas());
//	
//				if (PlayerUtils.enoughResource(selected.mineralPrice(), selected.gasPrice()) && Prebot.Broodwar.self().supplyUsed() <= 392) {
//					/*FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || selected unit || resource enough");
//					FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || selected unit ==> " + selected.toString());*/
//					//BuildManager.Instance().buildQueue.queueAsLowestPriority(selected, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
////					System.out.println("FactoryUnitSelector || selected unit || " + selected.toString());
////					FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || selected unit || " + selected.toString());
//					unitType = selected;
//				}
//			}
////			else {
////	//			현재 빌드큐에 팩토리 유닛이 들어가 있는데 벌쳐는 아닐경우, 자원이 가능하면 일단 벌쳐를 눌러준다.
////				if(currentinbuildqueuecnt_vulture < 1) {
////					
////					if (UnitType.Terran_Vulture.mineralPrice() <= Prebot.Broodwar.self().minerals() &&Prebot.Broodwar.self().supplyUsed() <= 392) {
////	//					FileUtils.appendTextToFile("log.txt", "\n FactoryUnitSelector || use odd resouce to vulture");
////						unitType = UnitType.Terran_Vulture;
////					}
////				}
////			}
//    }
}
