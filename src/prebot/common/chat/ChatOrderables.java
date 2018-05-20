package prebot.common.chat;

import prebot.common.main.Prebot;
import prebot.common.main.UXManager;

public class ChatOrderables {

	public static class AdjustGameSpeed extends ChatExecutable {
		public AdjustGameSpeed(char type) {
			super(type);
		}

		@Override
		public void execute(String option) {
			int optionInt = stringToInteger(option);
			Prebot.Game.setLocalSpeed(optionInt);
		}
	};

	public static class DiplayGameStatus extends ChatExecutable {
		public DiplayGameStatus(char type) {
			super(type);
		}

		@Override
		public void execute(String option) {
			int optionInt = stringToInteger(option);
			UXManager.Instance().setDisplayOption(optionInt);
		}
	};
	
	/** string을 int로 바꾼다. 숫자가 아닌 경우 default 0 */
	private static int stringToInteger(String stringValue) {
		try {
			return Integer.parseInt(stringValue);
		} catch (NumberFormatException nfe) {
			System.out.println("stringValue is not number.");
			return 0;
		}
	}
}
