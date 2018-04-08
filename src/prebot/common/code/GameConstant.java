package prebot.common.code;

public class GameConstant {
	
	public static final int IGNORE_ENEMY_UNITINFO_SECONDS = 15;
	
	public static final int UNIT_PRODUCE_HITPOINT = 80; // 유닛생산을 하지 않을 정도의 생산기지 hitpoint

	public static final int MAX_WORKER_COUNT = 65; // 최대로 생산할 수 있는 일꾼수
	
	/// MapGrid 에서 한 개 GridCell 의 size
	public static final int MAP_GRID_SIZE = 32;
	
	/// StarCraft 및 BWAPI 에서 1 Tile = 32 * 32 Point (Pixel) 입니다<br>
	/// Position 은 Point (Pixel) 단위이고, TilePosition 은 Tile 단위입니다 
	public static final int TILE_SIZE = 32;

	/// 각각의 Refinery 마다 투입할 일꾼 최대 숫자
	public static final int WORKERS_PER_REFINERY = 3;
	/// 건물과 건물간 띄울 최소한의 간격 - 일반적인 건물의 경우
	public static final int BUILDING_SPACING = 2;
	/// 건물과 건물간 띄울 최소한의 간격 - ResourceDepot 건물의 경우 (Nexus, Hatchery, Command Center)
	public static final int BUILDING_RESOUECE_DEPOT_SPACING = 0;
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기에
	public static final int BULDING_PYLON_EARLY_STAGE_SPACING = 4;
	/// 건물과 건물간 띄울 최소한의 간격 - Protoss_Pylon 건물의 경우 - 게임 초기 이후에
	public static final int BUILDING_PYLON_SPACING = 2;
	/// 건물과 건물간 띄울 최소한의 간격 - Terran_Supply_Depot 건물의 경우
	public static final int BUILDING_SUPPLYDEPOT_SPACING = 0;
	/// 건물과 건물간 띄울 최소한의 간격 - 방어 건물의 경우 (포톤캐논. 성큰콜로니. 스포어콜로니. 터렛. 벙커)
	public static final int BUILDING_DEFENSE_TOWER_SPACING = 0; 
}
