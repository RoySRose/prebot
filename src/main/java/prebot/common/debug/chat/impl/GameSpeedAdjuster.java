package prebot.common.debug.chat.impl;

import prebot.common.debug.chat.ChatExecuter;
import prebot.common.main.MyBotModule;

public class GameSpeedAdjuster extends ChatExecuter {
	public GameSpeedAdjuster(char type) {
		super(type);
	}

	@Override
	public void execute(String option) {
		int optionInt = stringToInteger(option);
		MyBotModule.Broodwar.setLocalSpeed(optionInt);
	}
};