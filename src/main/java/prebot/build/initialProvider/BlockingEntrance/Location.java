package prebot.build.initialProvider.BlockingEntrance;

public enum Location {
    START(0),
    One(1),
    Five(2),
    Seven(3),
    Eleven(4);

    private final int value;

    Location(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
