package prebot.chat;

public abstract class ChatExecutable {
	private char type;
	public ChatExecutable(char ch) {
		this.type = Character.toLowerCase(ch);
	}
	
	public boolean collectType(char ch) {
		return this.type == Character.toLowerCase(ch);
	}
	
	abstract void execute(String option);
}
