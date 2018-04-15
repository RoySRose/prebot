package prebot.brain;

import bwapi.Position;
import bwta.BaseLocation;
import prebot.brain.strategy.Strategy;

public class Idea {
	public Strategy strategy;

	public Position campPosition;
	public Position attackPosition;
	
	public BaseLocation expansionBaseLocation;
	
	public int checkerMaxCount = 0;
}
