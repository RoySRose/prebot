package prebot.common.debug.chat;

import java.util.ArrayList;
import java.util.List;

import prebot.common.debug.chat.impl.GameSpeedAdjuster;
import prebot.common.debug.chat.impl.StrategeChanger;
import prebot.common.debug.chat.impl.UxOptionChanger;

public class ChatBot {

	private static List<ChatExecuter> chatExecuters = new ArrayList<>();

	public static void addChatExecuter(ChatExecuter chatExecuter) {
		chatExecuters.add(chatExecuter);
	}

	/*****************************************************************************
	 * 설명
	 * 
	 * 1) 게임스피드 조절	: s + 숫자	ex) s0=매우빠름, s24=bot경기스피드, s42=fastest
	 * 2) UX 타입 변경	: d + 숫자	ex) d1=?, d0=prebot1 display
	 * 3) 전략 변경		: $ + 전략명	ex) $TERRAN INIT
	 * 
	 *****************************************************************************/
	static {
		chatExecuters.add(new GameSpeedAdjuster('s'));
		chatExecuters.add(new UxOptionChanger('d'));
		chatExecuters.add(new StrategeChanger('$'));
	}

	/**
	 * 채팅 명령을 인식하여 수행한다.<br/>
	 * 
	 * @param command
	 */
	public static void operateChatBot(String command) {
		if (command == null || command.length() < 2) {
			return;
		}

		char type = command.charAt(0);
		String option = command.substring(1);

		for (ChatExecuter executer : chatExecuters) {
			if (executer.isExecuteCharacter(type)) {
				executer.execute(option);
				break;
			}
		}
	}

	/**
	 * @param text
	 * @return
	 */
	public static void reply(String text) {
	}

}
