package prebot.common.debug.chat.impl;

import prebot.common.debug.chat.ChatExecuter;
import prebot.common.main.Prebot;

public class GameSpeedAdjuster extends ChatExecuter {
	public GameSpeedAdjuster(char type) {
		super(type);
	}

	@Override
	public void execute(String option) {
		int optionInt = stringToInteger(option);
		Prebot.Broodwar.setLocalSpeed(optionInt);
	}
};