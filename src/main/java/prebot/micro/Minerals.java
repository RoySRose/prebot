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


public class Minerals{
	int MinX = 0;
	int MinY = 0;
	int ID = 0;
	
	public Unit mineral = null;

	public Unit mineralTrick = null;//mineral which allows for mineral trick to speed up SCV
	public Position posTrick = null;//Position to allow path finding trick
	public String Facing = ""; //0 = scv enters left, 1 = scv enters up
	public Boolean possibleTrick = false; 
}
