package prebot.manager.combat;

public class KitingOption {

	public FleeOption fOption;
	public boolean cooltimeAlwaysAttack;

	public KitingOption(FleeOption fOption, boolean cooltimeAlwaysAttack) {
		this.fOption = fOption;
		this.cooltimeAlwaysAttack = cooltimeAlwaysAttack;
	}
	
	public static KitingOption defaultOption() {
		return new KitingOption(FleeOption.defaultOption(), true);
	}
}
