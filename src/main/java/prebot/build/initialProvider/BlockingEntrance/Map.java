package prebot.build.initialProvider.BlockingEntrance;

public enum Map {
    A(1),
    B(2);

    private final int value;

    Map(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
