package prebot.common.debug.chat.impl;

import prebot.common.debug.UXManager;
import prebot.common.debug.chat.ChatExecuter;

public class UxOptionChanger extends ChatExecuter {
	public UxOptionChanger(char type) {
		super(type);
	}

	@Override
	public void execute(String option) {
		int optionInt = stringToInteger(option);
		UXManager.Instance().setUxOption(optionInt);
	}
};