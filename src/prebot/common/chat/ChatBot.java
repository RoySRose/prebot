package prebot.common.chat;

import java.util.ArrayList;
import java.util.List;

import prebot.common.chat.ChatOrderables.AdjustGameSpeed;
import prebot.common.chat.ChatOrderables.DiplayGameStatus;

/**
 * @author insaneojw
 * 
 * 채팅에 대한 명령을 수행한다.<br/>
 * 채팅에 대한 명령처리기를 추가할 수 있다.
 */
public class ChatBot {

	private static List<ChatExecutable> chatOrderableList = new ArrayList<>();

	/**
	 * 명령처리기 추가
	 * 
	 * @param chatOrderable
	 */
	public static void addChatOrderable(ChatExecutable chatOrderable) {
		chatOrderableList.add(chatOrderable);
	}

	static {
		// add default chatOrderable object
		chatOrderableList.add(new AdjustGameSpeed('s'));
		chatOrderableList.add(new DiplayGameStatus('d'));
	}

	/**
	 * 채팅 명령을 인식하여 수행한다.<br/>
	 * TODO 명령 학습이 가능하도록 개발 필요
	 * 
	 * @param command
	 */
	public static void operateChatBot(String command) {
		if (command == null || command.length() < 2) {
			return;
		}

		char type = command.charAt(0);
		String option = command.substring(1);

		for (ChatExecutable chatOrderable : chatOrderableList) {
			if (chatOrderable.collectType(type)) {
				chatOrderable.execute(option);
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
