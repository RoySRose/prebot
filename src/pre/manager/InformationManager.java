package pre.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import pre.MapGrid;
import pre.UnitData;
import pre.UnitInfo;
import pre.main.MyBotModule;
import pre.util.MapSpecificInformation;
import pre.util.MapSpecificInformation.MAP;

/// 게임 상황정보 중 일부를 자체 자료구조 및 변수들에 저장하고 업데이트하는 class<br>
/// 현재 게임 상황정보는 BWAPI::Broodwar 를 조회하여 파악할 수 있지만, 과거 게임 상황정보는 BWAPI::Broodwar 를 통해 조회가 불가능하기 때문에 InformationManager에서 별도 관리하도록 합니다<br>
/// 또한, BWAPI::Broodwar 나 BWTA 등을 통해 조회할 수 있는 정보이지만 전처리 / 별도 관리하는 것이 유용한 것도 InformationManager에서 별도 관리하도록 합니다
public class InformationManager {
	private static InformationManager instance = new InformationManager();

	public Player selfPlayer;		///< 아군 Player		
	public Player enemyPlayer;		///< 적군 Player		
	public Race selfRace;			///< 아군 Player의 종족
	public Race enemyRace;			///< 적군 Player의 종족  

	private boolean isReceivingEveryMultiInfo;

	/// 해당 Player의 주요 건물들이 있는 BaseLocation. <br>
	/// 처음에는 StartLocation 으로 지정. mainBaseLocation 내 모든 건물이 파괴될 경우 재지정<br>
	/// 건물 여부를 기준으로 파악하기 때문에 부적절하게 판단할수도 있습니다 
	private Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();

	/// 해당 Player의 mainBaseLocation 이 변경되었는가 (firstChokePoint, secondChokePoint, firstExpansionLocation 를 재지정 했는가)
	private Map<Player, Boolean> mainBaseLocationChanged = new HashMap<Player, Boolean>();

	/// 해당 Player가 점령하고 있는 Region 이 있는 BaseLocation<br>
	/// 건물 여부를 기준으로 파악하기 때문에 부적절하게 판단할수도 있습니다 
	private Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();

	/// 해당 Player가 점령하고 있는 Region<br>
	/// 건물 여부를 기준으로 파악하기 때문에 부적절하게 판단할수도 있습니다 
	private Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();

	/// 해당 Player의 mainBaseLocation 에서 가장 가까운 ChokePoint
	private Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	/// 해당 Player의 mainBaseLocation 에서 가장 가까운 BaseLocation
	private Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	/// 해당 Player의 mainBaseLocation 에서 두번째로 가까운 (firstChokePoint가 아닌) ChokePoint<br>
	/// 게임 맵에 따라서, secondChokePoint 는 일반 상식과 다른 지점이 될 수도 있습니다
	private Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();
	
	// 나머지 멀티 location (가까운 순으로 sorting)
	private Map<Player, List<BaseLocation>> otherExpansionLocations = new HashMap<Player, List<BaseLocation>>();
	
	/// 센터 진출로
	private Map<Player, Position> readyToAttackPosition = new HashMap<Player, Position>();
	
	private MapSpecificInformation mapSpecificInformation = null;

	/// Player - UnitData(각 Unit 과 그 Unit의 UnitInfo 를 Map 형태로 저장하는 자료구조) 를 저장하는 자료구조 객체
	private Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();

	/// static singleton 객체를 리턴합니다
	public static InformationManager Instance() {
		return instance;
	}

	public InformationManager() {
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		isReceivingEveryMultiInfo = false;

		unitData.put(selfPlayer, new UnitData());
		unitData.put(enemyPlayer, new UnitData());
		
		occupiedBaseLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		occupiedRegions.put(selfPlayer, new HashSet());
		occupiedRegions.put(enemyPlayer, new HashSet());

		mainBaseLocations.put(selfPlayer, BWTA.getStartLocation(MyBotModule.Broodwar.self()));
		mainBaseLocationChanged.put(selfPlayer, new Boolean(true));

		occupiedBaseLocations.get(selfPlayer).add(mainBaseLocations.get(selfPlayer));
		if (mainBaseLocations.get(selfPlayer) != null) {
			updateOccupiedRegions(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()),
				MyBotModule.Broodwar.self());
		}

		mainBaseLocations.put(enemyPlayer, null);
		mainBaseLocationChanged.put(enemyPlayer, new Boolean(false));
		
		firstChokePoint.put(selfPlayer, null);
		firstChokePoint.put(enemyPlayer, null);
		firstExpansionLocation.put(selfPlayer, null);
		firstExpansionLocation.put(enemyPlayer, null);
		secondChokePoint.put(selfPlayer, null);
		secondChokePoint.put(enemyPlayer, null);

		otherExpansionLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		otherExpansionLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		
		readyToAttackPosition.put(selfPlayer, null);
		readyToAttackPosition.put(enemyPlayer, null);

		updateMapSpecificInformation();
		updateChokePointAndExpansionLocation();
	}

	/// Unit 및 BaseLocation, ChokePoint 등에 대한 정보를 업데이트합니다
	public void update() {
		updateUnitsInfo();
		// occupiedBaseLocation 이나 occupiedRegion 은 거의 안바뀌므로 자주 안해도 된다
		if (MyBotModule.Broodwar.getFrameCount() % 120 == 0) {
			updateBaseLocationInfo();
			setEveryMultiInfo();
		}
	}

	/// 전체 unit 의 정보를 업데이트 합니다 (UnitType, lastPosition, HitPoint 등)
	public void updateUnitsInfo() {
		// update our units info
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			updateUnitInfo(unit);
		}
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			updateUnitInfo(unit);
		}

		// remove bad enemy units
		if (unitData.get(enemyPlayer) != null) {
			unitData.get(enemyPlayer).removeBadUnits();
		}
		if (unitData.get(selfPlayer) != null) {
			unitData.get(selfPlayer).removeBadUnits();
		}
	}

	/// 해당 unit 의 정보를 업데이트 합니다 (UnitType, lastPosition, HitPoint 등)
	public void updateUnitInfo(Unit unit) {
		try {
			if (!(unit.getPlayer() == selfPlayer || unit.getPlayer() == enemyPlayer)) {
				return;
			}

			if (enemyRace == Race.Unknown && unit.getPlayer() == enemyPlayer) {
				enemyRace = unit.getType().getRace();
			}
			unitData.get(unit.getPlayer()).updateUnitInfo(unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitShow(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitHide(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitCreate(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitComplete(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitMorph(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitRenegade(Unit unit) { 
		updateUnitInfo(unit); 
	}
	
	/// Unit 에 대한 정보를 업데이트합니다 <br>
	/// 유닛이 파괴/사망한 경우, 해당 유닛 정보를 삭제합니다
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}

		unitData.get(unit.getPlayer()).removeUnit(unit);
	}


	/// 해당 Player (아군 or 적군) 의 position 주위의 유닛 목록을 unitInfo 에 저장합니다		 
	public void getNearbyForce(List<UnitInfo> unitInfo, Position p, Player player, int radius) {
		Iterator<Integer> it = getUnitData(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for each unit we know about for that player
		// for (final Unit kv :
		// getUnitData(player).getUnits().keySet().iterator()){
		while (it.hasNext()) {
			final UnitInfo ui = getUnitData(player).getUnitAndUnitInfoMap().get(it.next());

			// if it's a combat unit we care about
			// and it's finished!
			if (isCombatUnitType(ui.getType()) && ui.isCompleted()) {
				// determine its attack range
				int range = 0;
				if (ui.getType().groundWeapon() != WeaponType.None) {
					range = ui.getType().groundWeapon().maxRange() + 40;
				}

				// if it can attack into the radius we care about
				if (ui.getLastPosition().getDistance(p) <= (radius + range)) {
					// add it to the vector
					// C++ : unitInfo.push_back(ui);
					unitInfo.add(ui);
				}
			} else if (ui.getType().isDetector() && ui.getLastPosition().getDistance(p) <= (radius + 250)) {
				// add it to the vector
				// C++ : unitInfo.push_back(ui);
				unitInfo.add(ui);
			}
		}
	}
	
	/// 해당 Player (아군 or 적군) 의 position 주위의 유닛 목록을 unitInfo 에 저장합니다		 
	public List<UnitInfo> getNearbyForce(Position p, Player player, int radius) {
		List<UnitInfo> unitInfo = new ArrayList<>();
		Iterator<Integer> it = getUnitData(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for each unit we know about for that player
		// for (final Unit kv :
		// getUnitData(player).getUnits().keySet().iterator()){
		while (it.hasNext()) {
			final UnitInfo ui = getUnitData(player).getUnitAndUnitInfoMap().get(it.next());

			// if it's a combat unit we care about
			// and it's finished!
			if (isCombatUnitType(ui.getType()) && ui.isCompleted()) {
				// determine its attack range
				int range = 0;
				if (ui.getType().groundWeapon() != WeaponType.None) {
					range = ui.getType().groundWeapon().maxRange() + 40;
				}

				// if it can attack into the radius we care about
				if (ui.getLastPosition().getDistance(p) <= (radius + range)) {
					// add it to the vector
					// C++ : unitInfo.push_back(ui);
					unitInfo.add(ui);
				}
			} else if (ui.getType().isDetector() && ui.getLastPosition().getDistance(p) <= (radius + 250)) {
				// add it to the vector
				// C++ : unitInfo.push_back(ui);
				unitInfo.add(ui);
			}
		}
		return unitInfo;
	}

	/// 해당 Player (아군 or 적군) 의 해당 UnitType 유닛 숫자를 리턴합니다 (훈련/건설 중인 유닛 숫자까지 포함)
	public int getNumUnits(UnitType t, Player player) {
		return getUnitData(player).getNumUnits(t.toString());
	}

	/// 해당 Player (아군 or 적군) 의 모든 유닛 통계 UnitData 을 리턴합니다		 
	public final UnitData getUnitData(Player player) {
		return unitData.get(player);
	}

	public void updateBaseLocationInfo() {
		if (occupiedRegions.get(selfPlayer) != null) {
			occupiedRegions.get(selfPlayer).clear();
		}
		if (occupiedRegions.get(enemyPlayer) != null) {
			occupiedRegions.get(enemyPlayer).clear();
		}
		if (occupiedBaseLocations.get(selfPlayer) != null) {
			occupiedBaseLocations.get(selfPlayer).clear();
		}
		if (occupiedBaseLocations.get(enemyPlayer) != null) {
			occupiedBaseLocations.get(enemyPlayer).clear();
		}

		// enemy 의 startLocation을 아직 모르는 경우
		if (mainBaseLocations.get(enemyPlayer) == null) {
			// how many start locations have we explored
			int exploredStartLocations = 0;
			boolean enemyStartLocationFound = false;

			// an unexplored base location holder
			BaseLocation unexplored = null;

			for (BaseLocation startLocation : BWTA.getStartLocations()) {
				if (existsPlayerBuildingInRegion(BWTA.getRegion(startLocation.getTilePosition()), enemyPlayer)) {
					if (enemyStartLocationFound == false) {
						enemyStartLocationFound = true;
						mainBaseLocations.put(enemyPlayer, startLocation);
						mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));
					}
				}

				if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {
					// if it's explored, increment
					exploredStartLocations++;
				} else {
					// otherwise set it as unexplored base
					unexplored = startLocation;
				}
			}

			// if we've explored every start location except one, it's the enemy
			if (!enemyStartLocationFound && exploredStartLocations == ((int) BWTA.getStartLocations().size() - 1)) {
				enemyStartLocationFound = true;
				mainBaseLocations.put(enemyPlayer, unexplored);
				mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));				
				// C++ : _occupiedBaseLocations[_enemy].push_back(unexplored);
				if(occupiedBaseLocations.get(enemyPlayer) == null)
				{
					occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>()); 
				}
				occupiedBaseLocations.get(enemyPlayer).add(unexplored);
			}
		}

		// update occupied base location
		// 어떤 Base Location 에는 아군 건물, 적군 건물 모두 혼재해있어서 동시에 여러 Player 가 Occupy 하고
		// 있는 것으로 판정될 수 있다
		for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
			if (hasBuildingAroundBaseLocation(baseLocation, enemyPlayer)) {
				// C++ : _occupiedBaseLocations[_enemy].push_back(baseLocation);
				occupiedBaseLocations.get(enemyPlayer).add(baseLocation);
			}

			if (hasBuildingAroundBaseLocation(baseLocation, selfPlayer)) {
				// C++ : _occupiedBaseLocations[_self].push_back(baseLocation);
				occupiedBaseLocations.get(selfPlayer).add(baseLocation);
			}
		}

		if (mainBaseLocations.get(enemyPlayer) != null) {
			
			// 적 MainBaseLocation 업데이트 로직 버그 수정
			// 적군의 빠른 앞마당 건물 건설 + 아군의 가장 마지막 정찰 방문의 경우, 
			// enemy의 mainBaseLocations를 방문안한 상태에서는 건물이 하나도 없다고 판단하여 mainBaseLocation 을 변경하는 현상이 발생해서
			// enemy의 mainBaseLocations을 실제 방문했었던 적이 한번은 있어야 한다라는 조건 추가.  
			if (MyBotModule.Broodwar.isExplored(mainBaseLocations.get(enemyPlayer).getTilePosition())) {
		
				if (existsPlayerBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(enemyPlayer).getTilePosition()), enemyPlayer) == false) {
					for (BaseLocation loaction : occupiedBaseLocations.get(enemyPlayer)) {
						if (existsPlayerBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()),enemyPlayer)) {
							mainBaseLocations.put(enemyPlayer, loaction);
							mainBaseLocationChanged.put(enemyPlayer, new Boolean(true));				
							break;
						}
					}
				}
			}
		}

		// self의 mainBaseLocations에 대해, 그곳에 있는 건물이 모두 파괴된 경우
		// _occupiedBaseLocations 중에서 _mainBaseLocations 를 선정한다
		if (mainBaseLocations.get(selfPlayer) != null) {
			if (existsPlayerBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()), selfPlayer) == false) {
				for (BaseLocation location : occupiedBaseLocations.get(selfPlayer)) {
					if (existsPlayerBuildingInRegion(BWTA.getRegion(location.getTilePosition()), selfPlayer)) {
						mainBaseLocations.put(selfPlayer, location);
						mainBaseLocationChanged.put(selfPlayer, new Boolean(true));				
						break;
					}
				}
			}
		}

		Iterator<Integer> it = null;
		if (unitData.get(enemyPlayer) != null) {
			it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();

			// for each enemy building unit we know about
			// for (const auto & kv : unitData.get(enemy).getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.enemy());
				}
			}
		}

		if (unitData.get(selfPlayer) != null) {
			it = unitData.get(selfPlayer).getUnitAndUnitInfoMap().keySet().iterator();

			// for each of our building units
			// for (const auto & kv : _unitData[_self].getUnits())
			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(selfPlayer).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.self());
				}
			}
		}

		updateChokePointAndExpansionLocation();
	}
	
	public List<UnitInfo> getEnemyBuildingUnitsNear(Unit myunit, int radius, boolean canAttack, boolean ground, boolean air)
	{
		List<UnitInfo> units = new ArrayList<>();
		
		Iterator<Integer> it = null;
		it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui != null){
				if(myunit.getDistance(ui.getLastPosition()) > radius){
					continue;
				}
				if(ui.getType().isBuilding()){
					if (canAttack != true){
						units.add(ui);
					}else{
						if(ground){
							if(ui.getType().groundWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
						if(air){
							if(ui.getType().airWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
					}
				}
			}
		}
		
		return units;
	}
	
	public List<UnitInfo> getEnemyBuildingUnitsNear(Position myunit, int radius, boolean canAttack, boolean ground, boolean air)
	{
		List<UnitInfo> units = new ArrayList<>();
		
		Iterator<Integer> it = null;
		it = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().keySet().iterator();
		
		
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(enemyPlayer).getUnitAndUnitInfoMap().get(it.next());
			if(ui != null){
				if(myunit.getDistance(ui.getLastPosition()) > radius){
					continue;
				}
				if(ui.getType().isBuilding()){
					if (canAttack != true){
						units.add(ui);
					}else{
						if(ground){
							if(ui.getType().groundWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
						if(air){
							if(ui.getType().airWeapon() != WeaponType.None){
								units.add(ui);
							}
						}
					}
				}
			}
		}
		
		return units;
	}
	
	

	public void setEveryMultiInfo() {
		
		if (mainBaseLocations.get(selfPlayer) != null && mainBaseLocations.get(enemyPlayer) != null) {
			BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);
			for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
			{
				if (!BWTA.isConnected(targetBaseLocation.getTilePosition(), sourceBaseLocation.getTilePosition())) continue;
				if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
				if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
				//적군 베이스도 아닐때
				if (hasBuildingAroundBaseLocation(targetBaseLocation,enemyPlayer,10) == true) continue;
				
//				occupiedBaseLocations.
				
//				System.out.print("targetBaseLocationX : " + targetBaseLocation.getTilePosition().getX());
//				System.out.println(", targetBaseLocationY : " + targetBaseLocation.getTilePosition().getY());
//				
//				System.out.println("getduration: "+ MapGrid.Instance().getCellLastVisitDuration(targetBaseLocation.getPosition()));
				if (MapGrid.Instance().getCellLastVisitDuration(targetBaseLocation.getPosition()) > 8000)
				{
					isReceivingEveryMultiInfo = false;
//					System.out.println("isReceivingEveryMultiInfo1: " + isReceivingEveryMultiInfo);
					return;
				}
			}
			isReceivingEveryMultiInfo = true;
//			System.out.println("isReceivingEveryMultiInfo2: " + isReceivingEveryMultiInfo);
		}else{
			isReceivingEveryMultiInfo = false;
//			System.out.println("isReceivingEveryMultiInfo3: " + isReceivingEveryMultiInfo);
		}
	}
	public BaseLocation getNextExpansionLocation() {
		
		BaseLocation res = null;
		
//		long startTime = 0;
//		startTime = System.currentTimeMillis();
	
		if (mainBaseLocations.get(selfPlayer) != null && firstExpansionLocation.get(selfPlayer) != null && mainBaseLocations.get(enemyPlayer) != null) {
			BaseLocation sourceBaseLocation = firstExpansionLocation.get(selfPlayer);
			BaseLocation enemyBaseLocation = mainBaseLocations.get(enemyPlayer);
			
//			long currentTime = System.currentTimeMillis();
//			System.out.println("###start" + " : " + (currentTime - startTime) + " millisec");
//			startTime = currentTime;
//			int num = 1;
			
			double tempDistance;
			double sourceDistance;
			double closestDistance = 1000000000;
			
			for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
			{
				if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
				if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
				if (firstExpansionLocation.get(enemyPlayer) != null){
					if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(enemyPlayer).getTilePosition())) continue;
				}
				if (targetBaseLocation.getTilePosition().equals(firstExpansionLocation.get(selfPlayer).getTilePosition())) continue;
				if (hasBuildingAroundBaseLocation(targetBaseLocation,selfPlayer,6) == true) continue;
				if (hasBuildingAroundBaseLocation(targetBaseLocation,enemyPlayer,6) == true) continue;
				
//				currentTime = System.currentTimeMillis();
//				System.out.println("###" + num + " : " + (currentTime - startTime) + " millisec");
//				startTime = currentTime;
				
//				sourceDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), targetBaseLocation.getTilePosition());
//				tempDistance = sourceDistance - BWTA.getGroundDistance(enemyBaseLocation.getTilePosition(), targetBaseLocation.getTilePosition());
				sourceDistance = sourceBaseLocation.getGroundDistance(targetBaseLocation);
				tempDistance = sourceDistance - enemyBaseLocation.getGroundDistance(targetBaseLocation);
				
				if (tempDistance < closestDistance && sourceDistance > 0) {
					closestDistance = tempDistance;
					res = targetBaseLocation;
				}
//				currentTime = System.currentTimeMillis();
//				System.out.println("###" + num + " : " + (currentTime - startTime) + " millisec");
//				startTime = currentTime;
//				num++;
			}
			
//			currentTime = System.currentTimeMillis();
//			System.out.println("###end" + " : " + (currentTime - startTime) + " millisec");
//			startTime = currentTime;			
		}
		return res;
	}
	
	public void updateChokePointAndExpansionLocation() {
		if (mainBaseLocationChanged.get(selfPlayer).booleanValue() == true) {
		
			if (mainBaseLocations.get(selfPlayer) != null) {
				BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);
	
				firstChokePoint.put(selfPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));
							
				double tempDistance;
				double closestDistance = 1000000000;
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(selfPlayer).getTilePosition())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), targetBaseLocation.getTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						firstExpansionLocation.put(selfPlayer, targetBaseLocation);
					}
				}
	
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					if ( chokepoint.getCenter().equals(firstChokePoint.get(selfPlayer).getCenter())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						secondChokePoint.put(selfPlayer, chokepoint);
						this.updateReadyToAttackPosition(selfPlayer, chokepoint);
					}
				}
				this.updateOtherExpansionLocation(sourceBaseLocation);
			}
			mainBaseLocationChanged.put(selfPlayer, new Boolean(false));
		}
		
		if (mainBaseLocationChanged.get(enemyPlayer).booleanValue() == true) {

	
			if (mainBaseLocations.get(enemyPlayer) != null) {
				BaseLocation sourceBaseLocation = mainBaseLocations.get(enemyPlayer);
	
				firstChokePoint.put(enemyPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));
				
				double tempDistance;
				double closestDistance = 1000000000;
				for (BaseLocation targetBaseLocation : BWTA.getBaseLocations())
				{
					if (targetBaseLocation.getTilePosition().equals(mainBaseLocations.get(enemyPlayer).getTilePosition())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), targetBaseLocation.getTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						firstExpansionLocation.put(enemyPlayer, targetBaseLocation);
					}
				}
	
				closestDistance = 1000000000;
				for(Chokepoint chokepoint : BWTA.getChokepoints() ) {
					if ( chokepoint.getCenter().equals(firstChokePoint.get(enemyPlayer).getCenter())) continue;
	
					tempDistance = BWTA.getGroundDistance(sourceBaseLocation.getTilePosition(), chokepoint.getCenter().toTilePosition());
					if (tempDistance < closestDistance && tempDistance > 0) {
						closestDistance = tempDistance;
						secondChokePoint.put(enemyPlayer, chokepoint);
						this.updateReadyToAttackPosition(enemyPlayer, chokepoint);
					}
				}
				this.updateOtherExpansionLocation(sourceBaseLocation);
			}
			mainBaseLocationChanged.put(enemyPlayer, new Boolean(false));
		}
	}
	
	class BaseDistance {
		BaseDistance(BaseLocation base, double distance) {
			this.base = base;
			this.distance = distance;
		}
		BaseLocation base;
		double distance;
	}
	
	public void updateOtherExpansionLocation(BaseLocation baseLocation) {
		
		final BaseLocation myBase = mainBaseLocations.get(selfPlayer);
		final BaseLocation myFirstExpansion = firstExpansionLocation.get(selfPlayer);
		
		final BaseLocation enemyBase = mainBaseLocations.get(enemyPlayer);
		final BaseLocation enemyFirstExpansion = firstExpansionLocation.get(enemyPlayer);
		
		if (myBase == null || myFirstExpansion == null || enemyBase == null || enemyFirstExpansion == null) {
			return;
		}

		otherExpansionLocations.get(selfPlayer).clear();
		otherExpansionLocations.get(enemyPlayer).clear();
		
		int islandCnt = 0;
		int mainBaseCnt = 0;
		for (BaseLocation base : BWTA.getBaseLocations()) {
			if (base.isIsland()) {
				islandCnt++;
				continue;
			}
			// BaseLocation을 equal로 비교하면 오류가 있을 수 있다.
			if (base.getPosition().equals(myBase.getPosition()) || base.getPosition().equals(myFirstExpansion.getPosition())
					|| base.getPosition().equals(enemyBase.getPosition()) || base.getPosition().equals(enemyFirstExpansion.getPosition())) {
				mainBaseCnt++;
				continue;
			}
			if (base.minerals() < 1000) {
				continue;
			}
			otherExpansionLocations.get(selfPlayer).add(base);
			otherExpansionLocations.get(enemyPlayer).add(base);
		}
		System.out.println("updateOtherExpansionLocation -> " + islandCnt + " / " + mainBaseCnt);
		
		Collections.sort(otherExpansionLocations.get(selfPlayer), new Comparator<BaseLocation>() {
			@Override public int compare(BaseLocation base1, BaseLocation base2) {
				BaseLocation srcBase = myFirstExpansion;
				return (int) (srcBase.getGroundDistance(base1) - srcBase.getGroundDistance(base2));
			}
		});
		Collections.sort(otherExpansionLocations.get(enemyPlayer), new Comparator<BaseLocation>() {
			@Override public int compare(BaseLocation base1, BaseLocation base2) {
				BaseLocation srcBase = enemyFirstExpansion;
				return (int) (srcBase.getGroundDistance(base1) - srcBase.getGroundDistance(base2));
			}
		});
	}
	
	public void updateReadyToAttackPosition(Player player, Chokepoint chokepoint) {
		int approachDist = 300;
		
		Position secondChokePosition = chokepoint.getCenter();
		Position center = new Position(2048, 2048); // 128x128 맵의 센터

		int x = center.getX() - secondChokePosition.getX();
		int y = center.getY() - secondChokePosition.getY();
	    double radian = Math.atan2(y, x);

	    while (approachDist > 0) {
		    Position approachVector = new Position((int)(approachDist * Math.cos(radian)), (int)(approachDist * Math.sin(radian)));
		    Position position = new Position (secondChokePosition.getX() + approachVector.getX(), secondChokePosition.getY() + approachVector.getY());
		    if (position.isValid() && BWTA.getRegion(position) != null
		    		&& MyBotModule.Broodwar.isWalkable(position.getX() / 8, position.getY() / 8)) {
				readyToAttackPosition.put(player, position);
				break;
		    }
		    approachDist += 10;
	    }
	}
	

	public void updateOccupiedRegions(Region region, Player player) {
		// if the region is valid (flying buildings may be in null regions)
		if (region != null) {
			// add it to the list of occupied regions
			if (occupiedRegions.get(player) == null) {
				occupiedRegions.put(player, new HashSet<Region>());
			}
			occupiedRegions.get(player).add(region);
		}
	}

	/// 해당 BaseLocation 에 player의 건물이 존재하는지 리턴합니다
	/// @param baseLocation 대상 BaseLocation
	/// @param player 아군 / 적군
	/// @param radius TilePosition 단위
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player, int radius) {

		// invalid regions aren't considered the same, but they will both be null
		if (baseLocation == null) {
			return false;
		}
		// 반지름 10 (TilePosition 단위) 이면 거의 화면 가득이다
		if(radius > 10){
			radius = 10;
		}
		
		if (unitData.get(player) != null) {
			Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

			while (it.hasNext()) {
				final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
				if (ui.getType().isBuilding()) {
					TilePosition buildingPosition = ui.getLastPosition().toTilePosition();

//					System.out.print("buildingPositionX : " + buildingPosition.getX());
//					System.out.println(", buildingPositionY : " + buildingPosition.getY());
//					System.out.print("baseLocationX : " + baseLocation.getTilePosition().getX());
//					System.out.println(", baseLocationY : " + baseLocation.getTilePosition().getY());
					
					if (buildingPosition.getX() >= baseLocation.getTilePosition().getX() - radius
							&& buildingPosition.getX() <= baseLocation.getTilePosition().getX() + radius
							&& buildingPosition.getY() >= baseLocation.getTilePosition().getY() - radius
							&& buildingPosition.getY() <= baseLocation.getTilePosition().getY() + radius) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/// 해당 BaseLocation 주위 10타일 반경 내에 player의 건물이 존재하는지 리턴합니다
	/// @param baseLocation 대상 BaseLocation
	/// @param player 아군 / 적군
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player) {
		return hasBuildingAroundBaseLocation(baseLocation, player, 10);
	}

	/// 해당 Region 에 해당 Player의 건물이 존재하는지 리턴합니다
	public boolean existsPlayerBuildingInRegion(Region region, Player player) {
		// invalid regions aren't considered the same, but they will both be null
		if (region == null || player == null) {
			return false;
		}

		Iterator<Integer> it = unitData.get(player).getUnitAndUnitInfoMap().keySet().iterator();

		// for (const auto & kv : unitData.get(self).getUnits())
		while (it.hasNext()) {
			final UnitInfo ui = unitData.get(player).getUnitAndUnitInfoMap().get(it.next());
			if (ui.getType().isBuilding() ) {
				
				// Terran 종족의 Lifted 건물의 경우, BWTA.getRegion 결과가 null 이다
				if (BWTA.getRegion(ui.getLastPosition()) == null) continue;

				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}
		}
		return false;
	}

	/// 해당 Player (아군 or 적군) 의 모든 유닛 목록 (가장 최근값) UnitAndUnitInfoMap 을 리턴합니다<br>	 
	/// 파악된 정보만을 리턴하기 때문에 적군의 정보는 틀린 값일 수 있습니다
	public final Map<Integer, UnitInfo> getUnitAndUnitInfoMap(Player player) {
		return getUnitData(player).getUnitAndUnitInfoMap();
	}

	/// 해당 Player (아군 or 적군) 가 건물을 건설해서 점령한 Region 목록을 리턴합니다
	public Set<Region> getOccupiedRegions(Player player) {
		return occupiedRegions.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 건물을 건설해서 점령한 BaseLocation 목록을 리턴합니다		 
	public List<BaseLocation> getOccupiedBaseLocations(Player player) {
		return occupiedBaseLocations.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 을 리턴합니다		 
	public BaseLocation getMainBaseLocation(Player player) {
		return mainBaseLocations.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 가장 가까운 ChokePoint 를 리턴합니다		 
	public Chokepoint getFirstChokePoint(Player player) {
		return firstChokePoint.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 가장 가까운 Expansion BaseLocation 를 리턴합니다		 
	public BaseLocation getFirstExpansionLocation(Player player) {
		return firstExpansionLocation.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 두번째로 가까운 ChokePoint 를 리턴합니다<br>		 
	/// 게임 맵에 따라서, secondChokePoint 는 일반 상식과 다른 지점이 될 수도 있습니다
	public Chokepoint getSecondChokePoint(Player player) {
		return secondChokePoint.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation과 First Expansion을 제외한 BaseLocation을 가까운 순으로 정렬하여 리턴합니다		 
	public List<BaseLocation> getOtherExpansionLocations(Player player) {
		return otherExpansionLocations.get(player);
	}

	/// 센터 진출로 포지션을 리턴한다. 헌터에서 썼다가 결과는 책임못진다. insaneojw
	public Position getReadyToAttackPosition(Player player) {
		return readyToAttackPosition.get(player);
	}

	//모든 멀티가 확인된 상태인지 확인
	public boolean isReceivingEveryMultiInfo() {
		return isReceivingEveryMultiInfo;
	}
	
	//점령한 베이스 개수 확인
	public int getOccupiedBaseLocationsCnt(Player player) {
		return occupiedBaseLocations.get(player).size();
	}
	
	
	/// 해당 UnitType 이 전투 유닛인지 리턴합니다
	public final boolean isCombatUnitType(UnitType type) {
		if (type == UnitType.Zerg_Lurker /* || type == UnitType.Protoss_Dark_Templar*/) {
			return false;
		}

		// check for various types of combat units
		if (type.canAttack() || type == UnitType.Terran_Medic || type == UnitType.Protoss_Observer
				|| type == UnitType.Terran_Bunker) {
			return true;
		}

		return false;
	}
	
	// 해당 종족의 UnitType 중 Basic Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getBasicCombatUnitType() {
		return getBasicCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getBasicCombatUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Zealot;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Marine;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Zergling;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Advanced Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getAdvancedCombatUnitType() {
		return getAdvancedCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getAdvancedCombatUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Dragoon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Medic;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Hydralisk;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 을 생산하기 위해 건설해야하는 UnitType을 리턴합니다
	public UnitType getBasicCombatBuildingType() {
		return getBasicCombatBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 을 생산하기 위해 건설해야하는 UnitType을 리턴합니다
	public UnitType getBasicCombatBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Gateway;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Barracks;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Hatchery;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Observer 에 해당하는 UnitType을 리턴합니다
	public UnitType getObserverUnitType() {
		return getObserverUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Observer 에 해당하는 UnitType을 리턴합니다
	public UnitType getObserverUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Observer;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Science_Vessel;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Overlord;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 ResourceDepot 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicResourceDepotBuildingType() {
		return getBasicResourceDepotBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 ResourceDepot 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicResourceDepotBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Nexus;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Command_Center;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hatchery;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Refinery 기능을 하는 UnitType을 리턴합니다
	public UnitType getRefineryBuildingType() {
		return getRefineryBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Refinery 기능을 하는 UnitType을 리턴합니다
	public UnitType getRefineryBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Assimilator;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Refinery;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Extractor;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Worker 에 해당하는 UnitType을 리턴합니다
	public UnitType getWorkerType() {
		return getWorkerType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Worker 에 해당하는 UnitType을 리턴합니다
	public UnitType getWorkerType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Probe;
		} else if (race == Race.Terran) {
			return UnitType.Terran_SCV;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Drone;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 SupplyProvider 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicSupplyProviderUnitType() {
		return getBasicSupplyProviderUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 SupplyProvider 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicSupplyProviderUnitType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Pylon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Supply_Depot;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Overlord;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Basic Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicDefenseBuildingType() {
		return getBasicDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicDefenseBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Pylon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Bunker;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Creep_Colony;
//		} else {
//			return UnitType.None;
//		}
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedDefenseBuildingType() {
		return getAdvancedDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedDefenseBuildingType(Race race) {
//		if (race == Race.Protoss) {
//			return UnitType.Protoss_Photon_Cannon;
//		} else if (race == Race.Terran) {
			return UnitType.Terran_Missile_Turret;
//		} else if (race == Race.Zerg) {
//			return UnitType.Zerg_Sunken_Colony;
//		} else {
//			return UnitType.None;
//		}
	}
	
	public void updateMapSpecificInformation() {
		List<BaseLocation> startingBase = new ArrayList<>();
		MAP candiMapByPosition = null;
		for (BaseLocation base : BWTA.getStartLocations()) {
			if (base.isStartLocation()) {
				startingBase.add(base);
			}
		}
		
		// position으로 map 판단
		final int posFighting[][] = new int[][]{{288, 3760}, {288, 240}, {3808, 272}, {3808, 3792}};
		final int posLost[][] = new int[][]{{288, 2832}, {928, 3824}, {3808, 912}, {1888, 240}};
		if (startingBase.size() == 8) {
			candiMapByPosition = MAP.TheHunters;
		} else if (startingBase.size() == 4) {
			Position basePos = mainBaseLocations.get(selfPlayer).getPosition();
			for (int[] pos : posFighting) {
				if (basePos.equals(new Position(pos[0], pos[1]))) {
					candiMapByPosition = MAP.FightingSpririts;
					break;
				}
			}
			if (candiMapByPosition == null) {
				for (int[] pos : posLost) {
					if (basePos.equals(new Position(pos[0], pos[1]))) {
						candiMapByPosition = MAP.LostTemple;
						break;
					}
				}
			}
		} else {
			candiMapByPosition = MAP.Unknown;
		}
		
		// name으로 map 판단
		MAP candiMapByName = null;
		String mapName = MyBotModule.Broodwar.mapFileName().toUpperCase();
		if (mapName.matches(".*HUNT.*")) {
			candiMapByName = MAP.TheHunters;
		} else if (mapName.matches(".*LOST.*") || mapName.matches(".*TEMPLE.*")) {
			candiMapByName = MAP.LostTemple;
		} else if (mapName.matches(".*FIGHT.*") || mapName.matches(".*SPIRIT.*")) {
			candiMapByName = MAP.FightingSpririts;
		} else {
			candiMapByName = MAP.Unknown;
		}

		// 최종 결정
		MAP mapDecision = MAP.LostTemple;
		if (candiMapByPosition == candiMapByName) {
			mapDecision = candiMapByPosition;
			System.out.println("map : " + candiMapByPosition + "(100%)");
		} else {
			if (candiMapByPosition != MAP.Unknown) {
				mapDecision = candiMapByPosition;
				System.out.println("map : " + mapDecision + "(mapByName is -> " + candiMapByName + ")");
			} else if (candiMapByName != MAP.Unknown) {
				mapDecision = candiMapByName;
				System.out.println("map : " + mapDecision + "(mapByPosition is -> " + candiMapByPosition + ")");
			}
		}

		MapSpecificInformation tempMapInfo = new MapSpecificInformation();
		tempMapInfo.setMap(mapDecision);
		tempMapInfo.setStartingBaseLocation(startingBase);
		
		mapSpecificInformation = tempMapInfo;
	}
	
	public MapSpecificInformation getMapSpecificInformation() {
		return mapSpecificInformation;
	}
}