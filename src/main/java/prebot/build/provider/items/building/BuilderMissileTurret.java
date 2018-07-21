package prebot.build.provider.items.building;

import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.build.prebot1.BuildManager;
import prebot.build.prebot1.ConstructionManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.common.MetaType;
import prebot.common.constant.CommonCode.PlayerRange;
import prebot.common.main.Prebot;
import prebot.common.util.InfoUtils;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.strategy.StrategyIdea;

public class BuilderMissileTurret extends DefaultBuildableItem {

	public BuilderMissileTurret(MetaType metaType) {
		super(metaType);
	}

	public final boolean buildCondition() {
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Engineering_Bay) == 0) {
			return false;
		}
		if (Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret) >= 10) {
			return false;
		}
		if (TimeUtils.before(StrategyIdea.turretBuildStartFrame)) {
			return false;
		}

		int buildQueueCount = BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Missile_Turret);
		int constructionCount = ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Missile_Turret, null);

		// 첫번째 터렛이 없고, 입막 터렛 위치가 지정되어있을경우
		if (!UnitUtils.myUnitDiscovered(UnitType.Terran_Missile_Turret) && BlockingEntrance.Instance().entrance_turret != TilePosition.None
				&& buildQueueCount + constructionCount == 0) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(BlockingEntrance.Instance().entrance_turret);
			return true;
		}

		
		BaseLocation myBase = InfoUtils.myBase();
		BaseLocation myFirstExpansion = InfoUtils.myFirstExpansion();
		Chokepoint myFirstChoke = InfoUtils.myFirstChoke();
		Chokepoint mySecondChoke = InfoUtils.mySecondChoke();

		int turretCount = Prebot.Broodwar.self().completedUnitCount(UnitType.Terran_Missile_Turret);

		// 미사일 터렛이 많을수록 더 넓은 지역을 커버하니 지을 수가 없게 되는것이 아닌지??
		// 베이스는 숫자를 (350 != 300) 일부러 다르게 한것인가? 터렛범위에 빌드/컨스트럭션 큐 범위
		if (noTurretNearPosition(myBase.getPosition(), 350, 300, turretCount)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(myBase.getPosition().toTilePosition());
			return true;
		}

		Position firstChokeMainHalf = new Position((myBase.getPosition().getX() + myFirstChoke.getX() * 2) / 3 - 60,
				(myBase.getPosition().getY() + myFirstChoke.getY() * 2) / 3 - 60);
		if (noTurretNearPosition(firstChokeMainHalf, 180, 180, turretCount)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(firstChokeMainHalf.toTilePosition());
			return true;
		}
		
		Position firstChokeExpHalf = new Position((myFirstExpansion.getPosition().getX() * 2 + myFirstChoke.getX()) / 3,
				(myFirstExpansion.getPosition().getY() * 2 + myFirstChoke.getY()) / 3);
		if (noTurretNearPosition(firstChokeMainHalf, 210, 150, turretCount)) {
			setHighPriority(true);
			setBlocking(true);
			setTilePosition(firstChokeExpHalf.toTilePosition());
			return true;
		}

		// TODO COMPLETE, ALL 테스트에 따른 변경여부 결정
		if (Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Command_Center) > 1) {
			if (noTurretNearPosition(mySecondChoke.getCenter(), 100, 100, turretCount)) {
				setHighPriority(true);
				setBlocking(true);
				setTilePosition(mySecondChoke.getCenter().toTilePosition());
				return true;
			}
		}
		
		return false;
	}

	private boolean noTurretNearPosition(Position centerPosition, int radius1, int radius2, int turretCount) {
		List<Unit> turretNearBase = UnitUtils.getUnitsInRadius(PlayerRange.ALL, centerPosition, radius1 + turretCount * 15, UnitType.Terran_Missile_Turret);
		if (!turretNearBase.isEmpty()) {
			return false;
		}

		int buildQueueCountNear = BuildManager.Instance().buildQueue.getItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius2);
		int constructionCountNear = ConstructionManager.Instance().getConstructionQueueItemCountNear(UnitType.Terran_Missile_Turret, centerPosition.toTilePosition(), radius2);
		return buildQueueCountNear + constructionCountNear == 0;
	}
}
