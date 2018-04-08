package prebot.brain.action;

public abstract class Action {
	public Action next;

	public boolean actionCondition() { return true; }
	public abstract void doAction();
	
	public boolean exitCondition() { return false; }
	public abstract void finalize();
}