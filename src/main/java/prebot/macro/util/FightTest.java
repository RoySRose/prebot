package prebot.macro.util;


import bwapi.UnitType;
import prebot.macro.Decision;

import java.util.HashMap;
import java.util.Map;

public class FightTest {

    ScoreBoard scoreBoard = new ScoreBoard();
    Map<UnitType, Integer> myUnit;
    Map<UnitType, Integer> enemyUnit;

    public FightTest(Map<UnitType,Integer> myUnit, Map<UnitType,Integer> enemyUnit) {
        this.myUnit = myUnit;
        this.enemyUnit = enemyUnit;
    }

    private int calculateForce( Map<UnitType, Integer> myUnit ) {

        int point=0;

        for (Map.Entry<UnitType, Integer> enemyUnit : myUnit.entrySet()){
            point += (enemyUnit.getValue() * scoreBoard.getPoint(enemyUnit.getKey()));
        }

        return point;
    }

    private String makeDecision(int myForcePoint, int enemyForcePoint) {

        //상대가 다크나, 마인이 있는데 공격 판단하면 안된다. 어덯게 할지?
        if(myForcePoint > enemyForcePoint){
            return "Win";
        }else{
            return "Lost";
        }
    }
    public void fight(){

        int myForcePoint = calculateForce(myUnit);
        int enemyForcePoint = calculateForce(enemyUnit);

        System.out.println("myForcePoint: " +myForcePoint);
        System.out.println("enemyForcePoint: " +enemyForcePoint);
        System.out.println(makeDecision(myForcePoint, enemyForcePoint));
    }

    public void list() {

        for (Map.Entry<UnitType, Integer> myUnit : myUnit.entrySet()) {
            System.out.println(myUnit.getKey().getClass().getName() + ": " + myUnit.getValue());
        }
        for (Map.Entry<UnitType, Integer> enemyUnit : enemyUnit.entrySet()) {
            System.out.println(enemyUnit.getKey().toString() + ": " + enemyUnit.getValue());
        }

    }
}