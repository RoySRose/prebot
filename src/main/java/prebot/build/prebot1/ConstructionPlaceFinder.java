package prebot.build.prebot1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import prebot.build.constant.BuildConfig;
import prebot.build.initialProvider.BlockingEntrance.BlockingEntrance;
import prebot.common.main.Prebot;
import prebot.common.util.FileUtils;
import prebot.common.util.PositionUtils;
import prebot.strategy.InformationManager;
import prebot.strategy.MapSpecificInformation.GameMap;

/// 건설위치 탐색을 위한 class
public class ConstructionPlaceFinder {

	/// 건설위치 탐색 방법
	public enum ConstructionPlaceSearchMethod { 
		SpiralMethod,	///< 나선형으로 돌아가며 탐색
		SupplyDepotMethod, /// < 서플라이 디팟 메쏘드. 가로 세로를 서플라이 크기만큼 더해서 찾기
		NewMethod 		///< 예비
	};
	
	
	
	public int maxSupplyCntX = 3;
	public int maxSupplyCntY = 4;
	
	/// 건물 건설 예정 타일을 저장해놓기 위한 2차원 배열<br>
	/// TilePosition 단위이기 때문에 보통 128*128 사이즈가 된다<br>
	/// 참고로, 건물이 이미 지어진 타일은 저장하지 않는다
	private boolean[][] reserveMap = new boolean[128][128];
	
	/// BaseLocation 과 Mineral / Geyser 사이의 타일들을 담는 자료구조. 여기에는 Addon 이외에는 건물을 짓지 않도록 합니다	
	private Set<TilePosition> tilesToAvoid = new HashSet<TilePosition>();
	private Set<TilePosition> tilesToAvoidAbsolute = new HashSet<TilePosition>();
	//서플라이 짓는 지역
	private Set<TilePosition> tilesToAvoidSupply = new HashSet<TilePosition>();
	
	private static ConstructionPlaceFinder instance = new ConstructionPlaceFinder();
	
	private static boolean isInitialized = false;
	
	/// static singleton 객체를 리턴합니다
	public static ConstructionPlaceFinder Instance() {
		if (isInitialized == false) {
			instance.setTilesToAvoid();
			instance.setTilesToAvoidForFirstGas();
			isInitialized = true;
		}
		return instance;
	}

	/// seedPosition 및 seedPositionStrategy 파라메터를 활용해서 건물 건설 가능 위치를 탐색해서 리턴합니다<br>
	/// seedPosition 주위에서 가능한 곳을 선정하거나, seedPositionStrategy 에 따라 지형 분석결과 해당 지점 주위에서 가능한 곳을 선정합니다<br>
	/// seedPosition, seedPositionStrategy 을 입력하지 않으면, MainBaseLocation 주위에서 가능한 곳을 리턴합니다
	public final TilePosition getBuildLocationWithSeedPositionAndStrategy(UnitType buildingType, TilePosition seedPosition, BuildOrderItem.SeedPositionStrategy seedPositionStrategy)
	{
		TilePosition desiredPosition = TilePosition.None;

		// seedPosition 을 입력한 경우 그 근처에서 찾는다
		if (seedPosition != TilePosition.None  && seedPosition.isValid() )
		{
			FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy :: buildingType ::" + buildingType + " // seedPosition :: " + seedPosition.toString());
			//System.out.println("checking here");
			desiredPosition = getBuildLocationNear(buildingType, seedPosition, true, true);
			
		}
		// seedPosition 을 입력하지 않은 경우
		else {
			//System.out.println("전략포지션 입력 ==>> (" + seedPositionStrategy + ")" );
			Chokepoint tempChokePoint;
			BaseLocation tempBaseLocation;
			TilePosition tempTilePosition = null;
//			Region tempBaseRegion;
//			int vx, vy;
//			double d, t;
//			int bx, by;
			
			switch (seedPositionStrategy) {

			case MainBaseLocation:
				tempTilePosition = InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getTilePosition();
				desiredPosition = getBuildLocationNear(buildingType, tempTilePosition, true);
				
				if(desiredPosition == null){
					BuildManager.Instance().MainBaseLocationFull = true;
				}
				break;

			case FirstExpansionLocation:
				tempBaseLocation = InformationManager.Instance().getFirstExpansionLocation(Prebot.Broodwar.self());
				if (tempBaseLocation != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation.getTilePosition());
				}
				if(desiredPosition == null){
					BuildManager.Instance().FirstExpansionLocationFull   = true;
				}
				break;

			case FirstChokePoint:
				tempChokePoint = InformationManager.Instance().getFirstChokePoint(Prebot.Broodwar.self());
				if (tempChokePoint != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempChokePoint.getCenter().toTilePosition());
				}
				if(desiredPosition == null){
					BuildManager.Instance().FirstChokePointFull    = true;
				}
				break;

			case SecondChokePoint:
				tempChokePoint = InformationManager.Instance().getSecondChokePoint(Prebot.Broodwar.self());
				if (tempChokePoint != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempChokePoint.getCenter().toTilePosition());
				}
				if(desiredPosition == null){
					BuildManager.Instance().SecondChokePointFull     = true;
				}
				break;
				
			case NextExpansionPoint: //TODO NextSupplePoint 전에 중간포인트로 봐야하나?
				tempBaseLocation = InformationManager.Instance().getNextExpansionLocation();
				if (tempBaseLocation != null) {
					desiredPosition = getBuildLocationNear(buildingType, tempBaseLocation.getTilePosition());
				}else{
					desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getTilePosition());
				}
				break;
				
			case LastBuilingPoint: 
				tempTilePosition = InformationManager.Instance().getLastBuilingLocation();

				FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy :: case LastBuilingPoint ::" + buildingType + " // tempTilePosition :: " + tempTilePosition);
				if (tempTilePosition != null) {
					if(buildingType == UnitType.Terran_Supply_Depot){
						if(BuildManager.Instance().FisrtSupplePointFull == true){
							tempTilePosition = BlockingEntrance.Instance().getSupplyPosition(tempTilePosition);
							desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
						
							break;
						}
					}
				
					desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
				}
//				else{
//					desiredPosition = getBuildLocationNear(buildingType, InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition());
//				}
				break;
				
			case NextSupplePoint: 
				
				if(buildingType == UnitType.Terran_Supply_Depot){
					if(BuildManager.Instance().FisrtSupplePointFull != true){
						tempTilePosition = BlockingEntrance.Instance().getSupplyPosition();
						desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
					
						if(desiredPosition == null){
							BuildManager.Instance().FisrtSupplePointFull = true;
						}
						break;
					}
				}else{
					tempTilePosition = InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getTilePosition();
				}
				break;
			
			case getLastBuilingFinalLocation: //이놈이 마지막이니까.... NULL 일수가 없다.
			
				tempTilePosition = InformationManager.Instance().getLastBuilingFinalLocation();
				desiredPosition = getBuildLocationNear(buildingType, tempTilePosition);
			break;
					
			default:
				break;
			}
			
		}

		return desiredPosition;
	}

	/// desiredPosition 근처에서 건물 건설 가능 위치를 탐색해서 리턴합니다<br>
	/// desiredPosition 주위에서 가능한 곳을 찾아 반환합니다<br>
	/// desiredPosition 이 valid 한 곳이 아니라면, desiredPosition 를 MainBaseLocation 로 해서 주위를 찾는다<br>
	/// Returns a suitable TilePosition to build a given building type near specified TilePosition aroundTile.<br>
	/// Returns BWAPI::TilePositions::None, if suitable TilePosition is not exists (다른 유닛들이 자리에 있어서, Pylon, Creep, 건물지을 타일 공간이 전혀 없는 경우 등)
	
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, Boolean MethodFix)
	{
		return getBuildLocationNear(buildingType, desiredPosition, MethodFix, false);
	}
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition){
		return getBuildLocationNear(buildingType, desiredPosition, false);
	}
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, Boolean MethodFix, Boolean spaceZero)
	{
		//System.out.println("getBuildLocationNear 입력111 ==>> (" + desiredPosition.getX() + " , " +  desiredPosition.getY() + ")" );
		if (buildingType.isRefinery())
		{
			//std::cout << "getRefineryPositionNear "<< std::endl;

			return getRefineryPositionNear(desiredPosition);
		}

		if (desiredPosition == TilePosition.None || desiredPosition == TilePosition.Unknown || desiredPosition == TilePosition.Invalid || desiredPosition.isValid() == false)
		{
			desiredPosition = InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getTilePosition();
		}

		TilePosition testPosition = TilePosition.None;

		// TODO 과제 : 건설 위치 탐색 방법은 ConstructionPlaceSearchMethod::SpiralMethod 로 하는데, 더 좋은 방법은 생각해볼 과제이다
		int constructionPlaceSearchMethod = 0;
		
		if(buildingType == UnitType.Terran_Supply_Depot && MethodFix == false){
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal();
		}else{ 
			constructionPlaceSearchMethod = ConstructionPlaceSearchMethod.SpiralMethod.ordinal();	
		}
		
		// 일반적인 건물에 대해서는 건물 크기보다 Config::Macro::BuildingSpacing 칸 만큼 상하좌우로 더 넓게 여유공간을 두어서 빈 자리를 검색한다
		int buildingGapSpace = BuildConfig.buildingSpacing;

		// ResourceDepot (Nexus, Command Center, Hatchery),
		// Protoss_Pylon, Terran_Supply_Depot, 
		// Protoss_Photon_Cannon, Terran_Bunker, Terran_Missile_Turret, Zerg_Creep_Colony 는 다른 건물 바로 옆에 붙여 짓는 경우가 많으므로 
		// buildingGapSpace을 다른 Config 값으로 설정하도록 한다
		if (buildingType.isResourceDepot()) {
			buildingGapSpace = BuildConfig.buildingResourceDepotSpacing;
		}
//		if(buildingType == UnitType.Terran_Barracks){
//			buildingGapSpace = 0;
//		}
//		if(buildingType == UnitType.Terran_Factory){
//			buildingGapSpace = 0;
//		}
		if (buildingType == UnitType.Terran_Supply_Depot) {
			buildingGapSpace = BuildConfig.BUILDING_SUPPLY_DEPOT_SPACING;
			if(constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal()){
				buildingGapSpace=0;
			}
//			20180719. hkk. 저그전에 대비해서 첫서플과 두번째 서플은 건물들을 붙여지어야 함
			if( (desiredPosition.getX() == BlockingEntrance.Instance().first_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().first_supple.getY())
			|| (desiredPosition.getX() == BlockingEntrance.Instance().second_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().second_supple.getY())){
				buildingGapSpace = 0;
			}
			if(MethodFix == true){
				buildingGapSpace=1;
			}
			if(spaceZero == true){
				buildingGapSpace=0;
			}
		}
		
//		else if (buildingType == UnitType.Protoss_Photon_Cannon || buildingType == UnitType.Terran_Bunker 
//			|| buildingType == UnitType.Terran_Missile_Turret || buildingType == UnitType.Zerg_Creep_Colony) {
		else if (buildingType == UnitType.Terran_Missile_Turret) {
			buildingGapSpace = BuildConfig.BUILDING_DEFENSE_TOWER_SPACING;
//			FileUtils.appendTextToFile("log.txt", "\n SetBlockingTilePosition start ==>> ");
//			20180716. hkk. 입막 좌표의 터렛일 경우 서플라이에 붙여짓기.
			if(BlockingEntrance.Instance().entrance_turret1 != TilePosition.None) {
				if(desiredPosition.getX() == BlockingEntrance.Instance().entrance_turret1.getX() && desiredPosition.getY() == BlockingEntrance.Instance().entrance_turret1.getY()) {
//					FileUtils.appendTextToFile("log.txt","\\n entrance_turret : buildingGapSpace : " + buildingGapSpace);
					buildingGapSpace = 0; 
				}
			}
		}else if (buildingType == UnitType.Terran_Bunker) {
			buildingGapSpace = 0;
		}
		
//		20180728. hkk. 이니셜 빌드 지정건물들은 여백 0
		if( (desiredPosition.getX() == BlockingEntrance.Instance().first_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().first_supple.getY())
		|| (desiredPosition.getX() == BlockingEntrance.Instance().second_supple.getX() && desiredPosition.getY() == BlockingEntrance.Instance().second_supple.getY())
		|| (desiredPosition.getX() == BlockingEntrance.Instance().starport1.getX() && desiredPosition.getY() == BlockingEntrance.Instance().starport1.getY())
		|| (desiredPosition.getX() == BlockingEntrance.Instance().starport2.getX() && desiredPosition.getY() == BlockingEntrance.Instance().starport2.getY())
		|| (desiredPosition.getX() == BlockingEntrance.Instance().factory.getX() && desiredPosition.getY() == BlockingEntrance.Instance().factory.getY())
		|| (desiredPosition.getX() == BlockingEntrance.Instance().barrack.getX() && desiredPosition.getY() == BlockingEntrance.Instance().barrack.getY())){
			buildingGapSpace = 0;
		}
		
		if (buildingType == UnitType.Terran_Missile_Turret) {
			while (buildingGapSpace >= 0) {

				testPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);

				// std::cout << "ConstructionPlaceFinder testPosition " << testPosition.x << "," << testPosition.y << std::endl;

				if (testPosition != TilePosition.None && testPosition != TilePosition.Invalid)
					return testPosition;
						
				// 찾을 수 없다면, buildingGapSpace 값을 줄여서 다시 탐색한다
				// buildingGapSpace 값이 1이면 지상유닛이 못지나가는 경우가 많아  제외하도록 한다 
				// 4 -> 3 -> 2 -> 0 -> 탐색 종료
				//      3 -> 2 -> 0 -> 탐색 종료 
				//           1 -> 0 -> 탐색 종료
				if (buildingGapSpace > 2) {
					buildingGapSpace -= 1;
				}
				else if (buildingGapSpace == 2){
					buildingGapSpace = 0;
				}
				else if (buildingGapSpace == 1){
					buildingGapSpace = 0;
				}
				else {
					break;
				}
			}
		}else{
			FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear :: not turret go to getBuildLocationNear => " + buildingType + " : " + desiredPosition);
			testPosition = getBuildLocationNear(buildingType, desiredPosition, buildingGapSpace, constructionPlaceSearchMethod);
		}

		if (testPosition != TilePosition.None && testPosition != TilePosition.Invalid){
			return testPosition;
		}
		else{
			return TilePosition.None;
		}
	}

	/// 해당 buildingType 이 건설될 수 있는 위치를 desiredPosition 근처에서 탐색해서 탐색결과를 리턴합니다<br>
	/// buildingGapSpace를 반영해서 canBuildHereWithSpace 를 사용해서 체크<br>
	/// 못찾는다면 BWAPI::TilePositions::None 을 리턴합니다<br>
	/// TODO 과제 : 건물을 계획없이 지을수 있는 곳에 짓는 것을 계속 하다보면, 유닛이 건물 사이에 갇히는 경우가 발생할 수 있는데, 이를 방지하는 방법은 생각해볼 과제입니다
	public final TilePosition getBuildLocationNear(UnitType buildingType, TilePosition desiredPosition, int buildingGapSpace, int constructionPlaceSearchMethod)
	{
		//System.out.println("getBuildLocationNear 입력222 ==>> (" + desiredPosition.getX() + " , " +  desiredPosition.getY() + ")" );
		// std::cout << std::endl << "getBuildLocationNear " << buildingType.getName().c_str() << " " << desiredPosition.x << "," << desiredPosition.y 
		//	<< " gap " << buildingGapSpace << " method " << constructionPlaceSearchMethod << std::endl;

		//returns a valid build location near the desired tile position (x,y).
		TilePosition resultPosition = TilePosition.None;
		ConstructionTask b = new ConstructionTask(buildingType, desiredPosition);

		// maxRange 를 설정하지 않거나, maxRange 를 128으로 설정하면 지도 전체를 다 탐색하는데, 매우 느려질뿐만 아니라, 대부분의 경우 불필요한 탐색이 된다
		// maxRange 는 16 ~ 64가 적당하다
		int maxRange = 35; // maxRange = BWAPI::Broodwar->mapWidth()/4;
		boolean isPossiblePlace = false;
			
		
		FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear :: " + buildingType + " : " + desiredPosition);
		
		if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SpiralMethod.ordinal())
		{
			// desiredPosition 으로부터 시작해서 spiral 하게 탐색하는 방법
			// 처음에는 아래 방향 (0,1) -> 오른쪽으로(1,0) -> 위로(0,-1) -> 왼쪽으로(-1,0) -> 아래로(0,1) -> ..
			int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			int spiralMaxLength = 1;
			int numSteps = 0;
			boolean isFirstStep = true;

			int spiralDirectionX = 0;
			int spiralDirectionY = 1;
			while (spiralMaxLength < maxRange)
			{
				if (currentX >= 0 && currentX < Prebot.Broodwar.mapWidth() && currentY >= 0 && currentY < Prebot.Broodwar.mapHeight()) {

					FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear SpiralMethod:: " + buildingType + " : (" + currentX +" , " + currentY +")");
					isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, buildingGapSpace);

					if (isPossiblePlace) {
						resultPosition = new TilePosition(currentX, currentY);
						FileUtils.appendTextToFile("log.txt", "\n getBuildLocationNear isPossiblePlace true resultPosition:: " + buildingType + " : " + resultPosition);
						break;
					}
					//System.out.println(buildingType + " 은 여긴안돼 ==>>>> ("+currentX+"/"+currentY+")");
					
				}

				currentX = currentX + spiralDirectionX;
				currentY = currentY + spiralDirectionY;
				numSteps++;
				
				// 다른 방향으로 전환한다
				if (numSteps == spiralMaxLength)
				{
					numSteps = 0;

					if (!isFirstStep)
						spiralMaxLength++;

					isFirstStep = !isFirstStep;

					if (spiralDirectionX == 0)
					{
						spiralDirectionX = spiralDirectionY;
						spiralDirectionY = 0;
					}
					else
					{
						spiralDirectionY = -spiralDirectionX;
						spiralDirectionX = 0;
					}
				}
			}
//			if(resultPosition ==null){
//				System.out.println("chekcking resultPosition: " + currentX + ", "+ currentY);
//			}else{
//				System.out.println("chekcking resultPosition: " + resultPosition.toString());
//			}
		}
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.SupplyDepotMethod.ordinal()) {
			//서플라이 디팟 용 로직(4X4)
			// y축부터 시작. 최초 포지션에서 X축 + 3 넘어가면 Y축 +2
			int currentX = desiredPosition.getX();
			int currentY = desiredPosition.getY();
			//System.out.println("**************input TilePosition ==>>>>  (" + currentX + " / " + currentY + ")");
			int depostSizeX = 3;
			int depostSizeY = 2;

			//서킷브레이커만 4X4
			if (InformationManager.Instance().getMapSpecificInformation().getMap() == GameMap.CIRCUITBREAKER) {
				maxSupplyCntX = 4;
		    }
		
			/*for(int y_position = 0; y_position < maxSupplyCntY ; y_position ++){
				for(int x_position = 0; x_position < maxSupplyCntX ; x_position ++){
					if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

						isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

						if (isPossiblePlace) {
							resultPosition = new TilePosition(currentX, currentY);
							return resultPosition;
						}
						//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
					}
					currentX = currentX + depostSizeX;
					//currentY = currentY + spiralDirectionY;
				}
				//X축만 변경했을때 못찾을경우 Y축을 증가시키고 X축은 최초 포지션으로 설정
				currentX = desiredPosition.getX();
				currentY = currentY + depostSizeY;
			}*/
			for(int x_position= 0; x_position < maxSupplyCntX ; x_position ++){
				for(int y_position  = 0; y_position < maxSupplyCntY ; y_position ++){
					if (currentX >= 0 && currentX < Prebot.Broodwar.mapWidth() && currentY >= 0 && currentY < Prebot.Broodwar.mapHeight()) {

						isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, 0);

						if (isPossiblePlace) {
							resultPosition = new TilePosition(currentX, currentY);
							break;
						}
						//System.out.println("is impossible place ==> (" + currentX + " / " + currentY + ")");
					}
					
					currentY = currentY + depostSizeY;
					//currentY = currentY + spiralDirectionY;
				}
				if (isPossiblePlace) {
					break;
				}
				
				currentY = desiredPosition.getY();
				currentX = currentX + depostSizeX;
			}
			//System.out.println("supply position ==>>>>>>>>>>>  (" +currentX + " , " +currentY + ")");
			
			
			/*while (maxSupplyCnt < 4)
			{
				if (currentX >= 0 && currentX < MyBotModule.Broodwar.mapWidth() && currentY >= 0 && currentY < MyBotModule.Broodwar.mapHeight()) {

					isPossiblePlace = canBuildHereWithSpace(new TilePosition(currentX, currentY), b, buildingGapSpace);

					if (isPossiblePlace) {
						resultPosition = new TilePosition(currentX, currentY);
						break;
					}
				}

				currentX = currentX + spiralDirectionX;
				currentY = currentY + spiralDirectionY;
				numSteps++;
				
				// 다른 방향으로 전환한다
				if (numSteps == spiralMaxLength)
				{
					numSteps = 0;

					if (!isFirstStep)
						spiralMaxLength++;

					isFirstStep = !isFirstStep;

					if (spiralDirectionX == 0)
					{
						spiralDirectionX = spiralDirectionY;
						spiralDirectionY = 0;
					}
					else
					{
						spiralDirectionY = -spiralDirectionX;
						spiralDirectionX = 0;
					}
				}
			}*/
//			if(resultPosition ==null){
//				System.out.println("chekcking resultPosition2: " + currentX + ", "+ currentY);
//			}else{
//				System.out.println("chekcking resultPosition2: " + resultPosition.toString());
//			}
		}
		else if (constructionPlaceSearchMethod == ConstructionPlaceSearchMethod.NewMethod.ordinal()) {
		}

		return resultPosition;
	}

	/// 해당 위치에 건물 건설이 가능한지 여부를 buildingGapSpace 조건을 포함해서 판단하여 리턴합니다<br>
	/// Broodwar 의 canBuildHere, isBuildableTile, isReservedTile 를 체크합니다
	public final boolean canBuildHereWithSpace(TilePosition position, final ConstructionTask b, int buildingGapSpace)
	{
		//System.out.println("canBuildHereWithSpace 입력222 ==>> (" + position.getX() + " , " +  position.getY() + ") , buildingGapSpace ==>> " + buildingGapSpace );
		//if we can't build here, we of course can't build here with space
		FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace start :: "+ b.getType() + " // position :: " + position +" // buildingGapSpace :: " + buildingGapSpace);
		
		if (!canBuildHere(position, b))
		{
			FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace :: !canBuildHere "+ b.getType() + " // position :: " + position +" // buildingGapSpace :: " + buildingGapSpace);
			return false;
		}

		// height and width of the building
		int width = b.getType().tileWidth();
		int height = b.getType().tileHeight();

		// define the rectangle of the building spot
		// 건물 크기보다 상하좌우로 더 큰 사각형
		int startx;
		int starty;
		int endx;
		int endy;

		//buildingGapSpace = 0;@@@@@@
		
		boolean horizontalOnly = false;

		// Refinery 의 경우 GapSpace를 체크할 필요 없다
		if (b.getType().isRefinery())
		{
		}
		// Addon 타입의 건물일 경우에는, 그 Addon 건물 왼쪽에 whatBuilds 건물이 있는지를 체크한다
		if (b.getType().isAddon())
		{
			final UnitType builderType = b.getType().whatBuilds().first;

			TilePosition builderTile = new TilePosition(position.getX() - builderType.tileWidth(), position.getY() + 2 - builderType.tileHeight());

			startx = builderTile.getX() - buildingGapSpace;
			starty = builderTile.getY() - buildingGapSpace;
			endx = position.getX() + width + buildingGapSpace;
			endy = position.getY() + height + buildingGapSpace;

			// builderTile에 Lifted 건물이 아니고 whatBuilds 건물이 아닌 건물이 있는지 체크
			for (int i = 0; i <= builderType.tileWidth(); ++i)
			{
				for (int j = 0; j <= builderType.tileHeight(); ++j)
				{
					for (Unit unit : Prebot.Broodwar.getUnitsOnTile(builderTile.getX() + i, builderTile.getY() + j))
					{
						if ((unit.getType() != builderType) && (!unit.isLifted()))
						{
							return false;
						}
					}
				}
			}
		}
		else 
		{
			//make sure we leave space for add-ons. These types of units can have addon:
			if (b.getType() == UnitType.Terran_Command_Center ||
				b.getType() == UnitType.Terran_Factory ||
				b.getType() == UnitType.Terran_Starport ||
				b.getType() == UnitType.Terran_Science_Facility)
			{
				width += 3;
			}
	
			if( (position.getX() == BlockingEntrance.Instance().first_supple.getX() && position.getY() == BlockingEntrance.Instance().first_supple.getY())
				|| (position.getX() == BlockingEntrance.Instance().second_supple.getX() && position.getY() == BlockingEntrance.Instance().second_supple.getY())
				|| (position.getX() == BlockingEntrance.Instance().starport1.getX() && position.getY() == BlockingEntrance.Instance().starport1.getY())
				|| (position.getX() == BlockingEntrance.Instance().starport2.getX() && position.getY() == BlockingEntrance.Instance().starport2.getY())
				|| (position.getX() == BlockingEntrance.Instance().factory.getX() && position.getY() == BlockingEntrance.Instance().factory.getY())
				|| (position.getX() == BlockingEntrance.Instance().barrack.getX() && position.getY() == BlockingEntrance.Instance().barrack.getY())){
				
//				FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace chk initial point:: buildingType "+ b.getType() + " // position :: " + position  +" // buildingGapSpace :: " + buildingGapSpace);
				width = 0;
				height = 0;
				buildingGapSpace = 0;
			}


			// 상하좌우에 buildingGapSpace 만큼 간격을 띄운다
			if (horizontalOnly == false)
			{
				startx = position.getX() - buildingGapSpace;
				starty = position.getY() - buildingGapSpace;
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height + buildingGapSpace;
			}
			// 좌우로만 buildingGapSpace 만큼 간격을 띄운다
			else {
				startx = position.getX() - buildingGapSpace;
				starty = position.getY();
				endx = position.getX() + width + buildingGapSpace;
				endy = position.getY() + height;
			}

			// 테란종족 건물의 경우 다른 건물의 Addon 공간을 확보해주기 위해, 왼쪽 2칸은 반드시 GapSpace가 되도록 한다
			/*if (b.getType().getRace() == Race.Terran) {
				if (buildingGapSpace < 2) {
					startx = position.getX() - 2;
					endx = position.getX() + width + buildingGapSpace;
				}
			}*/

			
			
			// 건물이 차지할 공간 뿐 아니라 주위의 buildingGapSpace 공간까지 다 비어있는지, 건설가능한 타일인지, 예약되어있는것은 아닌지, TilesToAvoid 에 해당하지 않는지 체크
			for (int x = startx; x < endx; x++)
			{
				for (int y = starty; y < endy; y++)
				{
					// if we can't build here, or space is reserved, we can't build here
					if (isBuildableTile(b, x, y) == false)
					{
						return false;
					}

					if (isReservedTile(x, y)) {
						return false;
					}

					// ResourceDepot / Addon 건물이 아닌 일반 건물의 경우, BaseLocation 과 Geyser 사이 타일 (TilesToAvoid) 에는 건물을 짓지 않는다
//					20180719. hkk. 저그전 대비 배럭과 서플 가스 주변에 붙여짓기 필요
					if (b.getType().isResourceDepot() == false && b.getType().isAddon() == false
							&& b.getType() != UnitType.Terran_Bunker && b.getType() != UnitType.Terran_Missile_Turret
							&& b.getType() != UnitType.Terran_Barracks && b.getType() != UnitType.Terran_Supply_Depot) {
						if (isTilesToAvoid(x, y)) {
						}
					}
					//서플라이 지역은 서플라이 외에는 지을수 없다.
					if (b.getType() != UnitType.Terran_Supply_Depot) {
						if (isTilesToAvoidSupply(x, y)) {
						}
					}
					
					if (isTilesToAvoidAbsolute(x, y)) {
					}
					
				}
			}
		}

		// if this rectangle doesn't fit on the map we can't build here
		if (startx < 0 || starty < 0 || endx > Prebot.Broodwar.mapWidth() || endx < position.getX() + width || endy > Prebot.Broodwar.mapHeight())
		{
			return false;
		}

		FileUtils.appendTextToFile("log.txt", "\n canBuildHereWithSpace return true; :: "+ b.getType() + " // position :: " + position +" // buildingGapSpace :: " + buildingGapSpace);
		return true;
	}

	/// 해당 위치에 건물 건설이 가능한지 여부를 리턴합니다 <br>
	/// Broodwar 의 canBuildHere 및 _reserveMap 와 isOverlapsWithBaseLocation 을 체크
	public final boolean canBuildHere(TilePosition position, final ConstructionTask b)
	{
		/*if (!b.type.isRefinery() && !InformationManager::Instance().tileContainsUnit(position))
		{
		return false;
		}*/
		
		// This function checks for creep, power, and resource distance requirements in addition to the tiles' buildability and possible units obstructing the build location.
//		if (!MyBotModule.Broodwar.canBuildHere(position, b.getType(), b.getConstructionWorker()))
		if (!Prebot.Broodwar.canBuildHere(position, b.getType()))
		{
			return false;
		}
		
		// check the reserve map
		for (int x = position.getX() ; x < position.getX() + b.getType().tileWidth(); x++)
		{
			for (int y = position.getY() ; y < position.getY() + b.getType().tileHeight(); y++)
			{
				//if (reserveMap.get(x).get(y))
				if (reserveMap[x][y])
				{
					FileUtils.appendTextToFile("log.txt", "\n canBuildHere :: reserveMap :: (" + x + " , " + y + ")");
					return false;
				}
			}
		}

		// if it overlaps a base location return false
		// ResourceDepot 건물이 아닌 다른 건물은 BaseLocation 위치에 짓지 못하도록 한다
		if (isOverlapsWithBaseLocation(position, b.getType()))
		{
			//System.out.println("here isOverlapsWithBaseLocation ============");
			return false;
		}

		return true;
	}

	/// seedPosition 근처에서 Refinery 건물 건설 가능 위치를 탐색해서 리턴합니다 <br>
	/// 지도상의 여러 가스 광산 (Resource_Vespene_Geyser) 중 예약되어있지 않은 곳(isReservedTile), 다른 섬이 아닌 곳, 이미 Refinery 가 지어져있지않은 곳 중<br> 
	/// seedPosition 과 가장 가까운 곳을 리턴합니다
	public final TilePosition getRefineryPositionNear(TilePosition seedPosition)
	{
		if (seedPosition == TilePosition.None || seedPosition == TilePosition.Unknown || seedPosition == TilePosition.Invalid || seedPosition.isValid() == false)
		{
			seedPosition = InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getTilePosition();
		}
		
		TilePosition closestGeyser = TilePosition.None;
		double minGeyserDistanceFromSeedPosition = 100000000;

		//TODO BASICBOT 1.1 버젼의 가스 처리다.. 확인해 봐야함.
//		for (Unit geyser : MyBotModule.Broodwar.getStaticGeysers())
//		{
//			// geyser->getPosition() 을 하면, Unknown 으로 나올 수 있다.
//			// 반드시 geyser->getInitialPosition() 을 사용해야 한다
//			Position geyserPos = geyser.getInitialPosition();
//			TilePosition geyserTilePos = geyser.getInitialTilePosition();
//
//			// 이미 예약되어있는가
//			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
//				continue;
//			}
//
//			// geyser->getType() 을 하면, Unknown 이거나, Resource_Vespene_Geyser 이거나, Terran_Refinery 와 같이 건물명이 나오고, 
//			// 건물이 파괴되어도 자동으로 Resource_Vespene_Geyser 로 돌아가지 않는다
//			// geyser 위치에 있는 유닛들에 대해 isRefinery() 로 체크를 해봐야 한다
//
//			// seedPosition 으로부터 16 TILE_SIZE 거리 이내에 있는가
//			// Fighting Spirit 맵처럼 seedPosition 으로부터 동일한 거리 내에 geyser 가 여러개 있을 수 있는 경우 Refinery 건물을 짓기 위해서는 seedPosition 을 정확하게 입력해야 한다
//			double thisDistance = geyserTilePos.getDistance(seedPosition);
//			
//			if (thisDistance <= 16 && thisDistance < minGeyserDistanceFromSeedPosition)
//			{
//				minGeyserDistanceFromSeedPosition = thisDistance;
//				closestGeyser = geyser.getInitialTilePosition();
//			}
//		}
		
		// 전체 geyser 중에서 seedPosition 으로부터 16 TILE_SIZE 거리 이내에 있는 것을 찾는다
		for (Unit geyser : Prebot.Broodwar.getStaticGeysers())
		{
			// geyser->getPosition() 을 하면, Unknown 으로 나올 수 있다.
			// 반드시 geyser->getInitialPosition() 을 사용해야 한다

			Position geyserPos = geyser.getInitialPosition();
			TilePosition geyserTilePos = geyser.getInitialTilePosition();

			//std::cout << " geyserTilePos " << geyserTilePos.x << "," << geyserTilePos.y << std::endl;

			// 이미 예약되어있는가
			if (isReservedTile(geyserTilePos.getX(), geyserTilePos.getY())) {
				continue;
			}

			// if it is not connected fron seedPosition, it is located in another island
			if (!BWTA.isConnected(seedPosition, geyserTilePos))
			{
				continue;
			}

			// 이미 지어져 있는가
			boolean refineryAlreadyBuilt = false;
			List<Unit> alreadyBuiltUnits = Prebot.Broodwar.getUnitsInRadius(geyserPos, 4 * BuildConfig.TILE_SIZE);
			for (Unit u : alreadyBuiltUnits) {
				if (u.getType().isRefinery() && u.exists()) {
					refineryAlreadyBuilt = true;
				}
			}

			//std::cout << " geyser TilePos is not reserved, is connected, is not refineryAlreadyBuilt" << std::endl;

			if (refineryAlreadyBuilt == false)
			{
				//double thisDistance = BWTA.getGroundDistance(geyserPos.toTilePosition(), seedPosition);

				double thisDistance = PositionUtils.getGroundDistance(geyserPos, seedPosition.toPosition());
				
				if (thisDistance < minGeyserDistanceFromSeedPosition)
				{
					//std::cout << " selected " << std::endl;

					minGeyserDistanceFromSeedPosition = thisDistance;
					closestGeyser = geyser.getInitialTilePosition();
				}
			}
		}
		return closestGeyser;
	}

	/// 해당 위치가 BaseLocation 과 겹치는지 여부를 리턴합니다<br>
	/// BaseLocation 에는 ResourceDepot 건물만 건설하고, 다른 건물은 건설하지 않기 위함입니다
	public final boolean isOverlapsWithBaseLocation(TilePosition tile, UnitType type)
	{
		// if it's a resource depot we don't care if it overlaps
		if (type.isResourceDepot() || type == UnitType.Terran_Barracks  || type == UnitType.Terran_Bunker)
		{
			return false;
		}

		// dimensions of the proposed location
		int tx1 = tile.getX();
		int ty1 = tile.getY();
		int tx2 = tx1 + type.tileWidth();
		int ty2 = ty1 + type.tileHeight();

		// for each base location
		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// dimensions of the base location
			int bx1 = base.getTilePosition().getX();
			int by1 = base.getTilePosition().getY();
			int bx2 = bx1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileWidth();
			int by2 = by1 + InformationManager.Instance().getBasicResourceDepotBuildingType().tileHeight();

			// conditions for non-overlap are easy
			boolean noOverlap = (tx2 < bx1) || (tx1 > bx2) || (ty2 < by1) || (ty1 > by2);

			// if the reverse is true, return true
			if (!noOverlap)
			{
				return true;
			}
		}

		// otherwise there is no overlap
		return false;
	}

	/// 건물 건설 가능 타일인지 여부를 리턴합니다
	public final boolean isBuildableTile(final ConstructionTask b, int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			return false;
		}

		// 맵 데이터 뿐만 아니라 빌딩 데이터를 모두 고려해서 isBuildable 체크
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (Prebot.Broodwar.isBuildable(x, y, true) == false)
		{
			return false;
		}

		// constructionWorker 이외의 다른 유닛이 있으면 false를 리턴한다
		for (Unit unit : Prebot.Broodwar.getUnitsOnTile(x, y))
		{
			if ((b.getConstructionWorker() != null) && (unit != b.getConstructionWorker()))
			{
				return false;
			}
		}

		return true;
	}

	/// 건물 건설 예정 타일로 예약해서, 다른 건물을 중복해서 짓지 않도록 합니다
	public void reserveTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, true);
				// C++ : reserveMap[x][y] = true;
			}
		}*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, true);
				reserveMap[x][y] = true;
				// C++ : reserveMap[x][y] = true;
			}
		}
	}
	
	/// 건물 건설 예정 타일로 예약했던 것을 해제합니다
	public void freeTiles(TilePosition position, int width, int height)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				reserveMap.get(x).set(y, false);
				// C++ : reserveMap[x][y] = false;
			}
		}*/
		if(position == TilePosition.None) {
			return;
		}
		
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;

		for (int x = position.getX(); x < position.getX() + width && x < rwidth; x++)
		{
			for (int y = position.getY() ; y < position.getY() + height && y < rheight; y++)
			{
				//reserveMap.get(x).set(y, false);
				reserveMap[x][y] = false;
				// C++ : reserveMap[x][y] = false;
			}
		}
	}

	// 건물 건설 예약되어있는 타일인지 체크
	public final boolean isReservedTile(int x, int y)
	{
		/*int rwidth = reserveMap.size();
		int rheight = reserveMap.get(0).size();
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap.get(x).get(y);*/
		int rwidth = reserveMap.length;
		int rheight = reserveMap[0].length;
		if (x < 0 || y < 0 || x >= rwidth || y >= rheight)
		{
			return false;
		}

		return reserveMap[x][y];
	}

	/// reserveMap을 리턴합니다
	public boolean[][] getReserveMap() {
		return reserveMap;
	}

	/// (x, y) 가 BaseLocation 과 Mineral / Geyser 사이의 타일에 해당하는지 여부를 리턴합니다
	public final boolean isTilesToAvoid(int x, int y)
	{
		if(new TilePosition(x,y) == BlockingEntrance.Instance().first_supple
		|| new TilePosition(x,y) == BlockingEntrance.Instance().second_supple
		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport1
		|| new TilePosition(x,y) == BlockingEntrance.Instance().starport2
		|| new TilePosition(x,y) == BlockingEntrance.Instance().factory
		|| new TilePosition(x,y) == BlockingEntrance.Instance().barrack) {
			FileUtils.appendTextToFile("log.txt", "\n isTilesToAvoid free pass initial");
			return true;
		}
		
		for (TilePosition t : tilesToAvoid) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}
	public final boolean isTilesToAvoidAbsolute(int x, int y)
	{
		for (TilePosition t : tilesToAvoidAbsolute) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}
	
	/// (x, y) 가 서플라이 지역이라면 지을수 없다.
	public final boolean isTilesToAvoidSupply(int x, int y)
	{
		for (TilePosition t : tilesToAvoidSupply) {
			if (t.getX() == x && t.getY() == y) {
				return true;
			}
		}

		return false;
	}

	/// BaseLocation 과 Mineral / Geyser 사이의 타일들을 찾아 _tilesToAvoid 에 저장합니다<br>
	/// BaseLocation 과 Geyser 사이, ResourceDepot 건물과 Mineral 사이 공간으로 건물 건설 장소를 정하면<br> 
	/// 일꾼 유닛들이 장애물이 되어서 건설 시작되기까지 시간이 오래걸리고, 지어진 건물이 장애물이 되어서 자원 채취 속도도 느려지기 때문에, 이 공간은 건물을 짓지 않는 공간으로 두기 위함입니다
	public void setTilesToAvoid()
	{
		// ResourceDepot 건물의 width = 4 타일, height = 3 타일
		// Geyser 의            width = 4 타일, height = 2 타일
		// Mineral 의           width = 2 타일, height = 1 타일

		for (BaseLocation base : BWTA.getBaseLocations())
		{
			// Island 일 경우 건물 지을 공간이 절대적으로 좁기 때문에 건물 안짓는 공간을 두지 않는다
			if (base.isIsland()) continue;
			if (BWTA.isConnected(base.getTilePosition(), InformationManager.Instance().getMainBaseLocation(Prebot.Broodwar.self()).getTilePosition()) == false) continue;

			// dimensions of the base location
			int bx0 = base.getTilePosition().getX();
			int by0 = base.getTilePosition().getY();
			int bx4 = base.getTilePosition().getX() + 4;
			int by3 = base.getTilePosition().getY() + 3;

			// BaseLocation 과 Geyser 사이의 타일을 BWTA::getShortestPath 를 사용해서 구한 후 _tilesToAvoid 에 추가
			for (Unit geyser : base.getGeysers())
			{
				TilePosition closeGeyserPosition = geyser.getInitialTilePosition();

				// dimensions of the closest geyser
				int gx0 = closeGeyserPosition.getX();
				int gy0 = closeGeyserPosition.getY();
				int gx4 = closeGeyserPosition.getX() + 4;
				int gy2 = closeGeyserPosition.getY() + 2;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = gx0; k < gx4; k++) {
							for (int l = gy0; l < gy2; l++) {
								List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, l));
								for (TilePosition t : tileList) {
									tilesToAvoid.add(t);									
								}
							}
						}
					}
				}

				/*
				// Geyser 가 Base Location 의 어느방향에 있는가에 따라 최소한의 타일만 판단해서 tilesToAvoid 에 추가하는 방법도 있다
				//
				//    11시방향   12시방향  1시방향
				//
				//     9시방향             3시방향
				//
				//     7시방향    6시방향  5시방향
				int whichPosition = 0;

				// dimensions of the tilesToAvoid
				int vx0 = 0;
				int vx1 = 0;
				int vy0 = 0;
				int vy1 = 0;

				// 11시 방향
				if (gx0 < bx0 && gy0 < by0) {
					vx0 = gx0 + 1; // Geyser 의 중앙
					vy0 = gy0;     // Geyser 의 상단
					vx1 = bx0 + 3; // ResourceDepot 의 중앙
					vy1 = by0;     // ResourceDepot의 상단
				}
				// 9시 방향
				else if (gx0 < bx0 && gy0 <= by3) {
					vx0 = gx4; // Geyser 의 오른쪽끝
					vy0 = gy0; // Geyser 의 상단
					vx1 = bx0; // ResourceDepot 의 왼쪽끝
					vy1 = gy2; // Geyser 의 하단 
				}
				// 7시 방향
				else if (gx0 < bx0 && gy2 > by3) {
					vx0 = gx0 + 1; // Geyser 의 상단 중앙
					vy0 = by3;     // ResourceDepot 의 하단
					vx1 = bx0 + 3; // ResourceDepot 의 하단 중앙
					vy1 = gy0;     // Geyser 의 상단
				}
				// 6시 방향
				else if (gx0 < bx4 && gy0 > by3) {
					vx0 = bx0 + 1; // ResourceDepot 의 하단 중앙
					vy0 = by3;     // ResourceDepot 의 하단 
					vx1 = gx0 + 3; // Geyser 의 상단 중앙
					vy1 = gy0;     // Geyser 의 상단
				}
				// 12시 방향
				else if (gx0 < bx4 && gy0 < by0) {
					vx0 = gx0;     // Geyser 의 하단 왼쪽끝
					vy0 = gy2; 
					vx1 = gx0 + 3; // Geyser 의 중앙
					vy1 = by0;     // ResourceDepot 의 상단
				}
				// 1시 방향
				else if (gx0 > bx0 && gy0 < by0) {
					vx0 = bx0 + 2; // ResourceDepot 의 상단 중앙
					vy0 = gy0 + 1; // Geyser 의 하단
					vx1 = gx0 + 2; // Geyser 의 중앙
					vy1 = by0 + 1; // ResourceDepot 의 상단
				}
				// 5시 방향
				else if (gx0 > bx0 && gy0 >= by3) {
					vx0 = bx0 + 2; // ResourceDepot 의 하단 중앙
					vy0 = by0 + 2; // ResourceDepot 의 하단
					vx1 = gx0 + 2; // Geyser 의 중앙
					vy1 = gy0 + 1; // Geyser 의 하단
				}
				// 3시 방향
				else if (gx0 > bx0 && gy0 >= by0) {
					vx0 = bx4; // ResourceDepot 의 오른쪽끝
					vy0 = gy0; // Geyser 의 상단
					vx1 = gx0; // Geyser 의 왼쪽 끝
					vy1 = gy2; // Geyser 의 하단
				}

				for (int i = vx0; i < vx1; i++) {
					for (int j = vy0; j < vy1; j++) {
						_tilesToAvoid.insert(BWAPI::TilePosition(i, j));
					}
				}
				*/

			}

			// BaseLocation 과 Mineral 사이의 타일을 BWTA::getShortestPath 를 사용해서 구한 후 _tilesToAvoid 에 추가
			for (Unit mineral : base.getMinerals())
			{
				TilePosition closeMineralPosition = mineral.getInitialTilePosition();

				// dimensions of the closest mineral
				int mx0 = closeMineralPosition.getX();
				int my0 = closeMineralPosition.getY();
				int mx2 = mx0 + 2;
				int my1 = my0 + 1;

				for (int i = bx0; i < bx4; i++) {
					for (int j = by0; j < by3; j++) {
						for (int k = mx0; k < mx2; k++) {
							List<TilePosition> tileList = (List<TilePosition>) BWTA.getShortestPath(new TilePosition(i, j), new TilePosition(k, my0));
							for (TilePosition t : tileList) {
								tilesToAvoid.add(t);								
							}
						}
					}
				}
				if(InformationManager.Instance().enemyRace != Race.Protoss) {
					
					int fromx = mineral.getTilePosition().getX()-2;
					int fromy = mineral.getTilePosition().getY()-2;
					
					for (int x = fromx; x > 0 && x < fromx + 6 && x < Prebot.Broodwar.mapWidth(); x++)
				        {
				            for (int y = fromy ; y > 0 && y < fromy + 6 && y < Prebot.Broodwar.mapHeight(); y++)
				            {
							TilePosition temp = new TilePosition(x,y);
							tilesToAvoid.add(temp);
						}
					}
				}
			}
		}
	}
	public void setTilesToAvoidSupply() {
		
//		System.out.println("map name of setTilesToAvoidSupply ==>> " + InformationManager.Instance().getMapSpecificInformation().getMap());
		
		if(InformationManager.Instance().getMapSpecificInformation().getMap() != GameMap.UNKNOWN) {
			
//			System.out.println("setTilesToAvoidSupply map is not UNKNOWN");
		
			int supply_x = BlockingEntrance.Instance().getSupplyPosition().getX();
			int supply_y = BlockingEntrance.Instance().getSupplyPosition().getY();
			
			for(int x = 0; x < 9 ; x++){
				for(int y = 0; y < 8 ; y++){
					TilePosition t = new TilePosition(supply_x+x,supply_y+y);
					tilesToAvoidSupply.add(t);
					//System.out.println("supply region ==>>>>  ("+t.getX()+","+t.getY()+")");
				}
			}
		}
	}

    public void setTilesToAvoidForFirstGas()
	{
		Unit firstgas = InformationManager.Instance().getMyfirstGas();
	
		int fromx = firstgas.getTilePosition().getX()-2;
		int fromy = firstgas.getTilePosition().getY()-2;
		
		for (int x = fromx; x > 0 && x < fromx + 8 && x < Prebot.Broodwar.mapWidth(); x++)
	        {
	            for (int y = fromy ; y > 0 && y < fromy + 6 && y < Prebot.Broodwar.mapHeight(); y++)
	            {
	            	if(fromx < x && x < fromx+5 && fromy < y && y < fromy+3){
						continue;
					}
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoid.add(temp);
			}
		}
	}
	/// BaseLocation 과 Mineral / Geyser 사이의 타일들의 목록을 리턴합니다		
	public Set<TilePosition> getTilesToAvoid() {
		return tilesToAvoid;
	}

	public Set<TilePosition> getTilesToAvoidAbsolute() {
		return tilesToAvoidAbsolute;
	}
	
	public void setTilesToAvoidFac(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()-1;
		int fromy = unit.getTilePosition().getY()-1;
		
		/*if(fromx<0){
			fromx=0;
		}
		if(fromy<0){
			fromy =0;
		}*/
		
		for (int x = fromx; x > 0 && x < fromx + 8 && x < Prebot.Broodwar.mapWidth(); x++)
	        {
	            for (int y = fromy ; y > 0 && y < fromy + 5 && y < Prebot.Broodwar.mapHeight(); y++)
	            {
				if((x==fromx + 6 || x==fromx + 7) && y == fromy){
					continue;
				}
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidAbsolute.add(temp);
			}
		}
	}
	
	public void setTilesToAvoidAddon(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()+4;
		int fromy = unit.getTilePosition().getY()+1;
		
		for (int x = fromx; x < fromx + 2 && x < Prebot.Broodwar.mapWidth(); x++)
		{
			//팩토리 외 건물은 위아래가 비어있을 필요가 없음
			for (int y = fromy ; y < fromy +  2&& y < Prebot.Broodwar.mapHeight(); y++)
			{
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidAbsolute.add(temp);
			}
		}
	}
	
	public void setTilesToAvoidCCAddon(Unit unit) {
		
		int fromx = unit.getTilePosition().getX()+4;
		int fromy = unit.getTilePosition().getY();
		
		for (int x = fromx; x < fromx + 3 && x < Prebot.Broodwar.mapWidth(); x++)
		{
			//팩토리 외 건물은 위아래가 비어있을 필요가 없음
			for (int y = fromy ; y < fromy + 3 && y < Prebot.Broodwar.mapHeight(); y++)
			{
				TilePosition temp = new TilePosition(x,y);
				tilesToAvoidAbsolute.add(temp);
			}
		}
	}
}