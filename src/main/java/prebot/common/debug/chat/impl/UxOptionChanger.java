package prebot.common.debug.chat.impl;

import prebot.common.debug.chat.ChatExecuter;
import prebot.common.main.PreBotUXManager;

public class UxOptionChanger extends ChatExecuter {
	public UxOptionChanger(char type) {
		super(type);
	}

	@Override
	public void execute(String option) {
		int optionInt = stringToInteger(option);
		PreBotUXManager.Instance().setUxOption(optionInt);
	}
};