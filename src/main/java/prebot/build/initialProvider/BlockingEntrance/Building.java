package prebot.build.initialProvider.BlockingEntrance;

public enum Building {
    START(0),
    FIRST_SUPPLY(1),
    SECOND_SUPPLY(2),
    BARRACK(3),
    FACTORY(4),
    FACTORY2(5),
    BUNKER(6),
    ENTRANCE_TURRET1(7),
    ENTRANCE_TURRET2(8),
    SUPPLY_AREA(9)
    ;

    private final int value;

    Building(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
