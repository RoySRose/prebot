package prebot.brain.action;

public class ActionVariables {
	/// WorkerManager.getAdjustedWorkersPerRefinery에서 가스일꾼을 N기로 전략적인 설정 가능하다.
	/// gasAdjustment == true 인 경우 default 가스조절 설정을 따른다.
	public static boolean gasAdjustment = false;
	public static int gasAdjustmentWorkerCount = 0;
}