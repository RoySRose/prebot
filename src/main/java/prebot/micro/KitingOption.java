package prebot.micro;

public class KitingOption {
	
	public enum CoolTimeAttack {
		KEEP_SAFE_DISTANCE(false), COOLTIME_ALWAYS(true), COOLTIME_ALWAYS_IN_RANGE(true);
		

		private CoolTimeAttack(boolean coolTimeAlwaysAttack) {
			this.coolTimeAlwaysAttack = coolTimeAlwaysAttack;
		}
		
		public boolean coolTimeAlwaysAttack;
	}

	public FleeOption fOption;
	public CoolTimeAttack cooltimeAlwaysAttack;

	public KitingOption(FleeOption fOption, CoolTimeAttack cooltimeAlwaysAttack) {
		this.fOption = fOption;
		this.cooltimeAlwaysAttack = cooltimeAlwaysAttack;
	}
	
	public static KitingOption defaultOption() {
		return new KitingOption(FleeOption.defaultOption(), KitingOption.CoolTimeAttack.COOLTIME_ALWAYS);
	}
}
