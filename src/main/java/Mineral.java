

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


public class Mineral {

	private static Mineral instance = new Mineral();
	
	/// static singleton 객체를 리턴합니다
	public static Mineral Instance() {
		return instance;
	}
	//
	int walkData[][] = new int[128*4][128*4];
	Position CCtrick = null; //path finding trick with CC
	Unit ourCC = null;
	Unit closestMineral = null;
	ArrayList<Mineral> Minerals = new ArrayList<Mineral>();
	int MinToCC = 0;
	int CCToMin = 0;
	int MinX = 0;
	int MinY = 0;
	int ID = 0;
	Unit miner = null;
	int SCVcount = 0;
	Unit mineralTrick = null;//mineral which allows for mineral trick to speed up SCV
	Position posTrick = null;//Position to allow path finding trick
	String Facing = ""; //0 = scv enters left, 1 = scv enters up
		  
		
}