package prebot.build.initialProvider.BlockingEntrance;

public enum Building {
    START(0),
    FIRST_SUPPLY(1),
    SECOND_SUPPLY(2),
    BARRACK(3),
    FACTORY(4),
    FACTORY2(5),
    BUNKER(6)
    ;

    private final int value;

    Building(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
