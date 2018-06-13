package prebot.micro;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.strategy.StrategyManager;


public class MineralManager {

	private static MineralManager instance = new MineralManager();
	
	/// static singleton 객체를 리턴합니다
	public static MineralManager Instance() {
		return instance;
	}
	//
	int walkData[][] = new int[128*4][128*4];
	Position CCtrick = null; //path finding trick with CC
	Unit ourCC = null;
	Unit closestMineral = null;
	public ArrayList<Minerals> minerals = new ArrayList<Minerals>();
		  
		
}

