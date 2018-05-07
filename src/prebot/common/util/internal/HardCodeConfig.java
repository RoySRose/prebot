package prebot.common.util.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.TilePosition;
import bwta.BWTA;
import bwta.BaseLocation;
import prebot.brain.Info;
import prebot.common.code.Code.GameMap;

public class HardCodeConfig {

	private static List<HardCodeInfo> mapInfoList = new ArrayList<>();
	private static Map<Position, HardCodeInfo> hardCodeInfoMap = new HashMap<>();

	public static Map<Position, HardCodeInfo> getHardCodeInfoMap() {
		if (hardCodeInfoMap.isEmpty() && Info.of().gameMap != null) {
			for (BaseLocation base : BWTA.getStartLocations()) {
				for (HardCodeInfo mapInfo : mapInfoList) {
					if (mapInfo.gameMap == Info.of().gameMap && base.getPosition().equals(mapInfo.basePosition)) {
						hardCodeInfoMap.put(base.getPosition(), mapInfo);
					}
				}
			}
		}
		return hardCodeInfoMap;
	}

	static {
		// FIGHTING_SPRIRITS
		{
			HardCodeInfo clock1 = new HardCodeInfo();
			clock1.gameMap = GameMap.FIGHTING_SPRIRITS;
			clock1.basePosition = new Position(3808, 272);
			clock1.description = "투혼 1시";
			clock1.firstDepotTile = new TilePosition(117, 9);
			clock1.secondDepotTile = new TilePosition(126, 1);
			clock1.firstBarracksTile = new TilePosition(111, 4);
			clock1.firstFactoryTile = new TilePosition(111, 1);
			clock1.secondFactoryTile = new TilePosition(111, 4);

			HardCodeInfo clock5 = new HardCodeInfo();
			clock5.gameMap = GameMap.FIGHTING_SPRIRITS;
			clock5.basePosition = new Position(3808, 3792);
			clock5.description = "투혼 5시";
			clock5.firstDepotTile = new TilePosition(118, 110);
			clock5.secondDepotTile = new TilePosition(125, 124);
			clock5.firstBarracksTile = new TilePosition(124, 110);
			clock5.firstFactoryTile = new TilePosition(109, 113);
			clock5.firstFactoryTile = new TilePosition(109, 116);

			HardCodeInfo clock7 = new HardCodeInfo();
			clock7.gameMap = GameMap.FIGHTING_SPRIRITS;
			clock7.basePosition = new Position(288, 3760);
			clock7.description = "투혼 7시";
			clock7.firstDepotTile = new TilePosition(12, 112);
			clock7.secondDepotTile = new TilePosition(0, 124);
			clock7.firstBarracksTile = new TilePosition(15, 114);
			clock7.firstFactoryTile = new TilePosition(11, 106);
			clock7.secondFactoryTile = new TilePosition(11, 103);

			HardCodeInfo clock11 = new HardCodeInfo();
			clock11.gameMap = GameMap.FIGHTING_SPRIRITS;
			clock11.basePosition = new Position(288, 240);
			clock11.description = "투혼 11시";
			clock11.firstDepotTile = new TilePosition(8, 12);
			clock11.secondDepotTile = new TilePosition(0, 0);
			clock11.firstBarracksTile = new TilePosition(12, 12);
			clock11.firstFactoryTile = new TilePosition(0, 14);
			clock11.secondFactoryTile = new TilePosition(2, 18);

			mapInfoList.add(clock1);
			mapInfoList.add(clock5);
			mapInfoList.add(clock7);
			mapInfoList.add(clock11);
		}

		// ???
		{
		}
	}
}
