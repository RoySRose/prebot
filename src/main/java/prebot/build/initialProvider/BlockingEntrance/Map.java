package prebot.build.initialProvider.BlockingEntrance;

public enum Map {
    OVERWATCH(1),
    CIRCUITBREAKER(2),
	FIGHTING_SPRIRITS(3);

    private final int value;

    Map(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
