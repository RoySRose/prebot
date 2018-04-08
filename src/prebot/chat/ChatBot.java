package prebot.chat;

import java.util.ArrayList;
import java.util.List;

import prebot.main.PreBot;
import prebot.manager.UXManager;

/**
 * @author insaneojw
 * 
 * 채팅에 대한 명령을 수행한다.<br/>
 * 채팅에 대한 명령처리기를 추가할 수 있다.
 */
public class ChatBot {

	private static List<ChatOrderable> chatOrderableList = new ArrayList<>();

	/**
	 * 명령처리기 추가
	 * 
	 * @param chatOrderable
	 */
	public static void addChatOrderable(ChatOrderable chatOrderable) {
		chatOrderableList.add(chatOrderable);
	}

	static {
		ChatOrderable adjustGameSpeed = new ChatOrderable() {
			@Override
			public boolean expectable(String type) {
				return CommandType.SPEED.TYPE.equals(type);
			}

			@Override
			public void perform(String option) {
				try {
					PreBot.Broodwar.setLocalSpeed(Integer.parseInt(option));
				} catch (NumberFormatException nfe) {
					System.out.println("game speed level must be an integer");
				}
			}
		};

		ChatOrderable diplayGameStatus = new ChatOrderable() {
			@Override
			public boolean expectable(String type) {
				return CommandType.DISPLAY.TYPE.equals(type);
			}

			@Override
			public void perform(String option) {
				UXManager.Instance().displayOption = option;
			}
		};

		// add default chatOrderable object
		chatOrderableList.add(adjustGameSpeed);
		chatOrderableList.add(diplayGameStatus);
	}

	private static final String COMMAND_DIVIDER = " ";

	/**
	 * 채팅 명령을 인식하여 수행한다.<br/>
	 * TODO 명령 학습이 가능하도록 개발 필요
	 * 
	 * @param command
	 */
	public static void operateChatBot(String command) {
		if (command == null || command.isEmpty()) {
			return;
		}

		String type = command;
		String option = "";

		if (command.contains(COMMAND_DIVIDER)) {
			type = command.split(COMMAND_DIVIDER)[0];
			option = command.split(COMMAND_DIVIDER)[1];
		}

		for (ChatOrderable chatOrderable : chatOrderableList) {
			if (chatOrderable.expectable(type)) {
				chatOrderable.perform(option);
				break;
			}
		}

	}

	/**
	 * TODO 상대방의 대화, 도발 등에 대응한다.(재미로)
	 *
	 * @param text
	 * @return
	 */
	public static void reply(String text) {
		// 이기고 있을때, 지고있을때 등을 고려하여 채팅 메시지에 대해 응답한다.
	}

}
