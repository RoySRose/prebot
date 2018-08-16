//package prebot.macro.util;
//
//
//import bwapi.UnitType;
//import org.junit.Assert;
//import org.junit.Test;
//import prebot.macro.Decision;
//
//import java.sql.SQLOutput;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//public class ScoreBoardTest {
//
//    List<FightTest> fightList= new ArrayList<>();
//    Map<UnitType, Integer> myUnit = new HashMap<>();
//    Map<UnitType, Integer> enemyUnit = new HashMap<>();
//    @Test
//    public void winTest() throws Exception {
//
//        addFight();
//
//        for(FightTest fightTest : fightList){
//            fightTest.fight();
//            //fightTest.list();
//        }
//    }
//
//    private void addFight() {
//
//        myUnit.clear();
//        enemyUnit.clear();
//        myUnit.put(UnitType.Terran_Vulture, 2);
//        myUnit.put(UnitType.Terran_Goliath, 1);
//        myUnit.put(UnitType.Terran_Siege_Tank_Tank_Mode, 1);
//
//        enemyUnit.put(UnitType.Protoss_Zealot, 2);
//        enemyUnit.put(UnitType.Protoss_Dragoon, 2);
//
//        fightList.add(new FightTest(myUnit, enemyUnit));
//    }
//}