package prebot.chat;

public interface ChatOrderable {
	boolean expectable(String type);
	void perform(String option);
}
