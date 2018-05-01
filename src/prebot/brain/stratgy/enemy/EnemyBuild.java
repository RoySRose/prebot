package prebot.brain.stratgy.enemy;

import bwapi.Race;

public enum EnemyBuild {
	
	PLYON_BASE(Race.Protoss, 1, 10),
	PLYON_EXPANSION(Race.Protoss, 1, 10),
	PLYON_NOT_FOUND(Race.Protoss, 1, 10),
	
	GATE_GAS(Race.Protoss, 1, 40),
	TWO_GATE(Race.Protoss, 1, 40),
	FORGE(Race.Protoss, 1, 40),
	NOTHING(Race.Protoss, 1, 40),
	
	DOUBLE_NEX(Race.Protoss, 2, 00),
	GATE_CORE(Race.Protoss, 2, 30),
	
	ADUN(Race.Protoss, 3, 30),
	ROBOTICS(Race.Protoss, 3, 30),
	STARGATE(Race.Protoss, 3, 30);
	
	
	public Race race;
	public int miniutes;
	public int seconds;

	private EnemyBuild(Race race, int miniutes, int seconds) {
		this.race = race;
		this.miniutes = miniutes;
		this.seconds = seconds;
	}
}
