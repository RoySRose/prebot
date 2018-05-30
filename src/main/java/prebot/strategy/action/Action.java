package prebot.strategy.action;

public abstract class Action {
	public abstract boolean exitCondition();
	public abstract void action();
}
