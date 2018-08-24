
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;


/// 빌드(건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 순차적으로 실행하기 위해 빌드 큐를 관리하고, 빌드 큐에 있는 명령을 하나씩 실행하는 class<br>
/// 빌드 명령 중 건물 건설 명령은 ConstructionManager로 전달합니다
/// @see ConstructionManager
public class BuildManager extends GameManager {

	/// BuildOrderItem 들의 목록을 저장하는 buildQueue 
	public BuildOrderQueue buildQueue = new BuildOrderQueue();

	private static BuildManager instance = new BuildManager();

	public Boolean mainBaseLocationFull;
    public Boolean secondStartLocationFull;
	public Boolean firstChokePointFull;
	public Boolean firstExpansionLocationFull;
	public Boolean secondChokePointFull;
	public Boolean fisrtSupplePointFull;
	
	private BuildManagerFailureProtector failureProtector = new BuildManagerFailureProtector();

//	public boolean tank = false;
	
	/// static singleton 객체를 리턴합니다
	public static BuildManager Instance() {
		return instance;
	}
	
	public BuildManager() {
		mainBaseLocationFull = false;
        secondStartLocationFull = false;
		firstChokePointFull = false;
		firstExpansionLocationFull = false;
		secondChokePointFull = false;
		fisrtSupplePointFull = false;
		
	}

	/// buildQueue 에 대해 Dead lock 이 있으면 제거하고, 가장 우선순위가 높은 BuildOrderItem 를 실행되도록 시도합니다
	public void update() {
//		System.out.println("buildManager LagObserver :: " + LagObserver.managerRotationSize());

		if (!TimeUtils.executeRotation(LagObserver.managerExecuteRotation(LagObserver.MANAGER4, 0), LagObserver.managerRotationSize())) {
			return;
		}
	
		if (buildQueue.isEmpty()) {
			return;
		}

//		//FileUtils.appendTextToFile("log.txt", "\n frame count debug BuildManager Start :: " + System.currentTimeMillis());
//		System.out.println("frame count debug ==>> " + Prebot.Broodwar.getFrameCount());
		
		// Dead Lock 중에 앞선 건물이 없을 경우 추가한다.
		checkBuildOrderQueueDeadlockAndInsert();
		// Dead Lock 을 체크해서 제거한다
		
		checkBuildOrderQueueDeadlockAndAndFixIt();
		
		// Dead Lock 제거후 Empty 될 수 있다
		if (buildQueue.isEmpty()) {
			return;
		}

		// the current item to be used
		BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();
		
		//test용. 날릴것. hkk
//		tank = false;
//		if(currentItem.metaType.isUnit()) {
//			if(currentItem.metaType.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
//				System.out.println("=================== Siege Tank of BuildManager ====== producer ==> " + currentItem.producerID);
//				tank = true;
//			}
//		}
		//여기까지 날릴것
		

//		System.out.println("current HighestPriorityItem is " + currentItem.metaType.getName());

		// while there is still something left in the buildQueue
		while (!buildQueue.isEmpty()) {
			if (failureProtector.isSuspended(currentItem.metaType)) {
				if (!buildQueue.canSkipCurrentItem()) {
					break;
				}
				
				buildQueue.skipCurrentItem();
				currentItem = buildQueue.getItem();
				continue;
			}
			
			boolean isOkToRemoveQueue = true;

			// seedPosition 을 도출한다
			Position seedPosition = null;
			if (currentItem.seedLocation != TilePosition.None && currentItem.seedLocation != TilePosition.Invalid 
					&& currentItem.seedLocation != TilePosition.Unknown && currentItem.seedLocation.isValid()) {				
				seedPosition = currentItem.seedLocation.toPosition();
			}
			else {
				seedPosition = getSeedPositionFromSeedLocationStrategy(currentItem.seedLocationStrategy);
			}

			// this is the unit which can produce the currentItem
			Unit producer = getProducer(currentItem.metaType, seedPosition, currentItem.producerID);


			// Temporary Barrack lift
			if(currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == UnitType.Terran_Marine){
				//System.out.println("marine checked");
				if (InformationManager.Instance().firstBarrack == null) {
                    //System.out.println("marin out");
                	break;
                }
                if (InformationManager.Instance().firstBarrack != null && InformationManager.Instance().barrackStart + 24*3 > System.currentTimeMillis()) {
                    //System.out.println("marin wait");
                	break;
                }
            }
			/*
			 * if (currentItem.metaType.isUnit() &&
			 * currentItem.metaType.getUnitType().isBuilding()) { if (producer
			 * != null) { System.out.println("Build " +
			 * currentItem.metaType.getName() + " producer : " +
			 * producer.getType() + " ID : " + producer.getID()); } else {
			 * System.out.println("Build " + currentItem.metaType.getName() +
			 * " producer null"); } }
			 */

			boolean canMake = false;

			// 건물을 만들수 있는 유닛(일꾼)이나, 유닛을 만들수 있는 유닛(건물 or 유닛)이 있으면
			if (producer != null) {

				// check to see if we can make it right now
				// 지금 해당 유닛을 건설/생산 할 수 있는지에 대해 자원, 서플라이, 테크 트리, producer 만을 갖고
				// 판단한다
				//test용. 날릴것. hkk
//				if(tank) {
//					System.out.println("producer ==>  " + producer.getType() + " : " + producer.getID());
//				}
				
				canMake = canMakeNow(producer, currentItem.metaType);

				//test용. 날릴것. hkk
//				if(tank) {
//					System.out.println("canMake ===>>> " + canMake);
//				}
				
				
				/*
				 * if (currentItem.metaType.isUnit() &&
				 * currentItem.metaType.getUnitType().isBuilding() ) { std::cout
				 * + "Build " + currentItem.metaType.getName() +
				 * " canMakeNow : " + canMake + std::endl; }
				 */
			}
			

			// if we can make the current item, create it
			if (producer != null && canMake == true) {
				MetaType t = currentItem.metaType;

				if (t.isUnit()) {
					if (t.getUnitType().isBuilding()) {

						// 테란 Addon 건물의 경우 (Addon 건물을 지을수 있는지는 getProducer 함수에서 이미 체크완료)
						// 모건물이 Addon 건물 짓기 전에는 canBuildAddon = true,
						// isConstructing = false, canCommand = true 이다가
						// Addon 건물을 짓기 시작하면 canBuildAddon = false,
						// isConstructing = true, canCommand = true 가 되고 (Addon
						// 건물 건설 취소는 가능하나 Train 등 커맨드는 불가능)
						// 완성되면 canBuildAddon = false, isConstructing = false 가
						// 된다
						if (t.getUnitType().isAddon()) {

							producer.buildAddon(t.getUnitType());
							// 테란 Addon 건물의 경우 정상적으로 buildAddon 명령을 내려도 SCV가 모건물
							// 근처에 있을 때 한동안 buildAddon 명령이 취소되는 경우가 있어서
							// 모건물이 isConstructing = true 상태로 바뀐 것을 확인한 후
							// buildQueue 에서 제거해야한다
							if (producer.isConstructing() == false) {
								isOkToRemoveQueue = false;
							}
							

						}
						// 그외 대부분 건물의 경우
						else {
							// ConstructionPlaceFinder 를 통해 건설 가능 위치 desiredPosition 를 알아내서 ConstructionManager 의 ConstructionTask Queue에 추가를 해서 desiredPosition 에 건설을 하게 한다.
							// ConstructionManager 가 건설 도중에 해당 위치에 건설이 어려워지면 다시 ConstructionPlaceFinder 를 통해 건설 가능 위치를 desiredPosition 주위에서 찾을 것이다
//							//FileUtils.appendTextToFile("log.txt", "\n getDesiredPosition before :: buildManager :: " + System.currentTimeMillis()+ " :: " + t.getUnitType() + " :: "+ currentItem.seedLocation + " :: " + currentItem.seedLocationStrategy);
							TilePosition desiredPosition = getDesiredPosition(t.getUnitType(), currentItem.seedLocation,currentItem.seedLocationStrategy);

							if (desiredPosition != TilePosition.None) {
//								System.out.println("desiredPosition is not null :: " + t.getUnitType() + " :: " + desiredPosition);
//								//FileUtils.appendTextToFile("log.txt", "desiredPosition is not null :: " + System.currentTimeMillis());
								ConstructionManager.Instance().addConstructionTask(t.getUnitType(), desiredPosition);
							} else {
								// 건물 가능 위치가 없는 경우는, Protoss_Pylon 가 없거나, Creep 이 없거나, Refinery 가 이미 다 지어져있거나, 정말 지을 공간이 주위에 없는 경우인데,
								// 대부분의 경우 Pylon 이나 Hatchery가 지어지고 있는 중이므로, 다음 frame 에 건물 지을 공간을 다시 탐색하도록 한다.
								if (currentItem.seedLocation != null)
								if (desiredPosition != null)
								
								if(t.getUnitType() == UnitType.Terran_Supply_Depot || t.getUnitType() == UnitType.Terran_Academy || t.getUnitType() == UnitType.Terran_Armory) {
									desiredPosition = getDesiredPosition(t.getUnitType(), TilePosition.None, BuildOrderItem.SeedPositionStrategy.NextSupplePoint);
								}else {
									desiredPosition = getDesiredPosition(t.getUnitType(), TilePosition.None, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
								}
								
								if (desiredPosition != TilePosition.None) {
									ConstructionManager.Instance().addConstructionTask(t.getUnitType(), desiredPosition);
								}else {
									failureProtector.update(currentItem.metaType);
									isOkToRemoveQueue = true;
								}
							}
						}
					}
					// 지상유닛 / 공중유닛의 경우
					else {
						producer.train(t.getUnitType());
//						if(producer.isTraining() == false){
//							isOkToRemoveQueue = false;
//						}
					}
				}
				// if we're dealing with a tech research
				else if (t.isTech()) {
					producer.research(t.getTechType());
				} else if (t.isUpgrade()) {
					producer.upgrade(t.getUpgradeType());
					if(t.getUpgradeType() == UpgradeType.Terran_Vehicle_Weapons) {
						BuildQueueProvider.Instance().startUpgrade(t.getUpgradeType());
					}
				}
				// remove it from the buildQueue
				if (isOkToRemoveQueue) {
//					System.out.println("here I am!!! Killing: " + buildQueue.getItem().metaType.getName());
					buildQueue.removeCurrentItem();
				}
				
				// don't actually loop around in here
				break;
			}
			// otherwise, if we can skip the current item
			else if (buildQueue.canSkipCurrentItem()) {
				// skip it and get the next one
				buildQueue.skipCurrentItem();
				currentItem = buildQueue.getItem();
			} else {
				// so break out
//				//FileUtils.appendTextToFile("log.txt", "\n frame count debug BuildManager break out :: " + System.currentTimeMillis());
				break;
			}
		}
		
//		//FileUtils.appendTextToFile("log.txt", "\n frame count debug BuildManager End :: " + System.currentTimeMillis());
	}

	/// 해당 MetaType 을 build 할 수 있는 producer 를 찾아 반환합니다
	/// @param t 빌드하려는 대상의 타입
	/// @param closestTo 파라메타 입력 시 producer 후보들 중 해당 position 에서 가장 가까운 producer 를 리턴합니다
	/// @param producerID 파라메타 입력 시 해당 ID의 unit 만 producer 후보가 될 수 있습니다
	public Unit getProducer(MetaType t, Position closestTo, int producerID) {
		// get the type of unit that builds this
		UnitType producerType = t.whatBuilds();
		
		Unit tempProducer = null;


		// make a set of all candidate producers
		List<Unit> candidateProducers = new ArrayList<Unit>();
//		for (Unit unit : Prebot.Broodwar.self().getUnits()) {
		List<Unit> selectPorducer = UnitUtils.getUnitList(CommonCode.UnitFindRange.COMPLETE);
		for (Unit unit : selectPorducer) {
			
			//test용 날릴것. hkk
//			if(tank) System.out.println("======== unit of selectPorducer ==>> " + unit.getType() +" : " + unit.getID());
			
			if (unit == null)
				continue;

			// reasons a unit can not train the desired type
			if (unit.getType() != producerType) {
				continue;
			}
			if (!unit.exists()) {
				continue;
			}
			if (!unit.isCompleted()) {
				continue;
			}
			if (unit.isTraining()) {
				continue;
			}
			if (!unit.isPowered()) {
				continue;
			}
			// if unit is lifted, unit should land first
			if (unit.isLifted()) {
				continue;
			}
			
			if (unit.isUpgrading() || unit.isResearching()) {
				continue;
			}
			
			if (producerID != -1 && unit.getID() != producerID)	{ 
				continue; 
			}
			if (unit.isConstructing() && (producerType == UnitType.Terran_Factory || producerType == UnitType.Terran_Starport || producerType == UnitType.Terran_Science_Facility || producerType == UnitType.Terran_Command_Center)) {
				continue;
			}

			
			
			if (t.isUnit()) {
				// if the type dd an addon and the producer doesn't have
				// one
				// C++ : typedef std::pair<BWAPI::UnitType, int> ReqPair;
				Pair<UnitType, Integer> ReqPair = null;

				Map<UnitType, Integer> requiredUnitsMap = t.getUnitType().requiredUnits();
				
				//test용 날릴것. hkk
//				if(t.getUnitType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
//				
//					System.out.println(" requiredUnitsMap ==> " + requiredUnitsMap);
//				}
				
				boolean able = true;
				
				if (requiredUnitsMap != null) {
					Iterator<UnitType> it = requiredUnitsMap.keySet().iterator();

					// for (final Pair<UnitType, Integer> pair :
					// t.getUnitType().requiredUnits())
					while (it.hasNext()) {
						UnitType requiredType = it.next();
						if (requiredType.isAddon()) {
							if (unit.getAddon() == null || (unit.getAddon().getType() != requiredType)) {
//								if(tank) System.out.println("======== unit don't have addon & continue==>> " + unit.getType() +" : " + unit.getID());
//								continue;
								able = false;
							}
						}
					}
				}
				
				if(!able) continue;

				// if the type is an addon
				if (t.getUnitType().isAddon()) {
					// if the unit already has an addon, it can't make one
					if (unit.getAddon() != null) {
						continue;
					}

					// 모건물은 건설되고 있는 중에는 isCompleted = false, isConstructing =
					// true, canBuildAddon = false 이다가
					// 건설이 완성된 후 몇 프레임동안은 isCompleted = true 이지만, canBuildAddon
					// = false 인 경우가 있다
					if (!unit.canBuildAddon()) {
						continue;
					}

					// if we just told this unit to build an addon, then it will
					// not be building another one
					// this deals with the frame-delay of telling a unit to
					// build an addon and it actually starting to build
					if (unit.getLastCommand().getUnitCommandType() == UnitCommandType.Build_Addon && TimeUtils.elapsedFrames(unit.getLastCommandFrame()) < 10) {
						continue;
					}
					
					
//					20180804. hkk. Comsat_Station 의 경우 CommandCenter 가 baseLocation 이 아니면 짓지 않는다.
					if(t.getUnitType() == UnitType.Terran_Comsat_Station
						&& unit.getType() == UnitType.Terran_Command_Center) {
						boolean comsat_ret = false;
						for(BaseLocation baseLocation : BWTA.getBaseLocations())
						{
							if(baseLocation.getTilePosition().getX() == unit.getTilePosition().getX()
								&& baseLocation.getTilePosition().getY() == unit.getTilePosition().getY()) {
//								//FileUtils.appendTextToFile("log.txt", "\n CommandCenter is right postion ==>> " + baseLocation.getTilePosition());
								comsat_ret = true;
								break;
							}
						}
						if(comsat_ret == false) continue;
					}

					boolean isBlocked = false;

					// if the unit doesn't have space to build an addon, it
					// can't make one
					TilePosition addonPosition = new TilePosition(
							unit.getTilePosition().getX() + unit.getType().tileWidth(),
							unit.getTilePosition().getY() + unit.getType().tileHeight() - t.getUnitType().tileHeight());

					for (int i = 0; i < t.getUnitType().tileWidth(); ++i) {
						for (int j = 0; j < t.getUnitType().tileHeight(); ++j) {
							TilePosition tilePos = new TilePosition(addonPosition.getX() + i, addonPosition.getY() + j);

							// if the map won't let you build here, we can't
							// build it.
							// 맵 타일 자체가 건설 불가능한 타일인 경우 + 기존 건물이 해당 타일에 이미 있는경우
							if (!MyBotModule.Broodwar.isBuildable(tilePos, true)) {
								isBlocked = true;
							}

							// if there are any units on the addon tile, we
							// can't build it
							// 아군 유닛은 Addon 지을 위치에 있어도 괜찮음. (적군 유닛은 Addon 지을 위치에
							// 있으면 건설 안되는지는 아직 불확실함)
							for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(tilePos.getX(), tilePos.getY())) {
								//System.out.println("Construct " + t.getName() + " beside " + unit.getType() + "("
								//		+ unit.getID() + ")" + ", units on Addon Tile " + tilePos.getX() + ","
								//		+ tilePos.getY() + " is " + u.getType() + "(ID : " + u.getID() + " Player : "
								//		+ u.getPlayer().getName() + ")");
								if (u.getPlayer() != InformationManager.Instance().selfPlayer) {
									isBlocked = false;
								}
							}
						}
					}

					if (isBlocked) {
						continue;
					}
				}
			}
			
//			if(t.getUnitType() == UnitType.Terran_Vulture || t.getUnitType() == UnitType.Terran_Goliath) {
////				//FileUtils.appendTextToFile("log.txt", "\n setProducer of vulture");
//				
////				20180818. hkk. 배정된 팩토리에 머신샵이 있을경우 일단 임시생산자로 지정. 다시 루프를 돌려 생산자가 머신샵이 없을 경우 그걸로 배정. 끝까지 돌렸으나 머신샵 없는 생산자가 없을 경우 그냥 배정.
//				if(tempProducer == null) {
//					//FileUtils.appendTextToFile("log.txt", "\n setProducer of vulture initset :: " + unit.getType() + " ::  addon :: " + unit.getAddon());
//					tempProducer = unit;
//				}
//				
//				if(tempProducer.getAddon() != null && unit.getAddon() == null) {
//					//FileUtils.appendTextToFile("log.txt", "\n getProducer of vulture :: factory that has addon to change non-addon");
//					tempProducer = unit;
//				}
//				
//				if(tempProducer.getAddon() != null) {
//					//FileUtils.appendTextToFile("log.txt", "\n setProducer of vulture continue :: " + tempProducer.getType() + " ::  addon :: " + tempProducer.getAddon());
//					continue;
//				}
//				
//				//FileUtils.appendTextToFile("log.txt", "\n setProducer of vulture set this tempProducer :: " + tempProducer.getType() + " ::  addon :: " + tempProducer.getAddon());
//				candidateProducers.add(tempProducer); // C++ :
//				
//			}else {
//				candidateProducers.add(unit); // C++ :
//			}
			candidateProducers.add(unit); // C++ :
			

			//test 용 날릴것. hkk
//			if(tank) System.out.println("add candidateProducers ==> " + unit.getType() + " : " + unit.getID());
			// if we haven't cut it, add it to the set of candidates
			
											// candidateProducers.insert(unit);
			

		}
		
		//test 용 날릴것 .hkk
//		if(tank) {
//			
//			
//			
//			for(Unit unit_list : candidateProducers) {
//				System.out.println("candidateProducers ==>> " + unit_list.getType() + " : " + unit_list.getID());
//			}
//		}

		return getClosestUnitToPosition(candidateProducers, closestTo);
	}

	/// 해당 MetaType 을 build 할 수 있는 producer 를 찾아 반환합니다
	public Unit getProducer(MetaType t, Position closestTo) {
		return getProducer(t, closestTo, -1);
	}

	/// 해당 MetaType 을 build 할 수 있는 producer 를 찾아 반환합니다
	public Unit getProducer(MetaType t) {
		return getProducer(t, Position.None, -1);
	}

	/*
	/// 해당 MetaType 을 build 할 수 있는, getProducer 리턴값과 다른 producer 를 찾아 반환합니다<br>
	/// 프로토스 종족 유닛 중 Protoss_Archon / Protoss_Dark_Archon 을 빌드할 때 사용합니다
	public Unit getAnotherProducer(Unit producer, Position closestTo) {
		if (producer == null)
			return null;

		Unit closestUnit = null;

		List<Unit> candidateProducers = new ArrayList<Unit>();
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null) {
				continue;
			}
			if (unit.getType() != producer.getType()) {
				continue;
			}
			if (unit.getID() == producer.getID()) {
				continue;
			}
			if (!unit.isCompleted()) {
				continue;
			}
			if (unit.isTraining()) {
				continue;
			}
			if (!unit.exists()) {
				continue;
			}
			if (unit.getHitPoints() + unit.getEnergy() <= 0) {
				continue;
			}

			candidateProducers.add(unit); // C++ :
											// candidateProducers.insert(unit);
		}

		return getClosestUnitToPosition(candidateProducers, closestTo);
	}
	*/
	
	public Unit getClosestUnitToPosition(final List<Unit> units, Position closestTo) {
		if (units.size() == 0) {
			return null;
		}

		// if we don't care where the unit is return the first one we have
		if (closestTo == Position.None || closestTo == Position.Invalid || closestTo == Position.Unknown || closestTo.isValid() == false) {
			return units.get(0); // C++ : return units.begin();
		}

		Unit closestUnit = null;
		double minDist = 1000000000;

		for (Unit unit : units) {
			if (unit == null)
				continue;

			double distance = unit.getDistance(closestTo);
			if (closestUnit == null || distance < minDist) {
				closestUnit = unit;
				minDist = distance;
			}
		}

		return closestUnit;
	}

	// 지금 해당 유닛을 건설/생산 할 수 있는지에 대해 자원, 서플라이, 테크 트리, producer 만을 갖고 판단한다<br>
	// 해당 유닛이 건물일 경우 건물 지을 위치의 적절 여부 (탐색했었던 타일인지, 건설 가능한 타일인지, 주위에 Pylon이 있는지,<br>
	// Creep이 있는 곳인지 등) 는 판단하지 않는다
	public boolean canMakeNow(Unit producer, MetaType t) {
		if (producer == null) {
			return false;
		}

		boolean canMake = hasEnoughResources(t);
		
//		if(tank) {
//			System.out.println("******** " + producer.getID() +" 's  canBuildAddon && isConstructing==>> " + producer.canBuildAddon() + " && " + producer.isConstructing());
//			//System.out.println("producer.isConstructing(); ==>> " + producer.isConstructing());
//		}

		if (canMake) {
			if (t.isUnit()) {
				// MyBotModule.Broodwar.canMake : Checks all the requirements
				// include resources, supply, technology tree, availability, and
				// required units
				canMake = MyBotModule.Broodwar.canMake(t.getUnitType(), producer);
			} else if (t.isTech()) {
				canMake = MyBotModule.Broodwar.canResearch(t.getTechType(), producer);
			} else if (t.isUpgrade()) {
				canMake = MyBotModule.Broodwar.canUpgrade(t.getUpgradeType(), producer);
			}
		}

		//TODO 맞을런지?
		if(producer.getType() == UnitType.Terran_Factory || producer.getType() == UnitType.Terran_Starport || producer.getType() == UnitType.Terran_Science_Facility || producer.getType() == UnitType.Terran_Command_Center){
			if(producer.canBuildAddon()==false && producer.isConstructing() == true){
				canMake = false;
			}
		}
		// 테란 Addon 건물의 경우 (Addon 건물을 지을수 있는지는 getProducer 함수에서 이미 체크완료)
		// 모건물이 Addon 건물 짓기 전에는 canBuildAddon = true,
		// isConstructing = false, canCommand = true 이다가
		// Addon 건물을 짓기 시작하면 canBuildAddon = false,
		// isConstructing = true, canCommand = true 가 되고 (Addon
		// 건물 건설 취소는 가능하나 Train 등 커맨드는 불가능)
		// 완성되면 canBuildAddon = false, isConstructing = false 가
		// 된다
			// 테란 Addon 건물의 경우 정상적으로 buildAddon 명령을 내려도 SCV가 모건물
			// 근처에 있을 때 한동안 buildAddon 명령이 취소되는 경우가 있어서
			// 모건물이 isConstructing = true 상태로 바뀐 것을 확인한 후
			// buildQueue 에서 제거해야한다
		
		return canMake;
	}

	// 건설 가능 위치를 찾는다<br>
	// seedLocationStrategy 가 SeedPositionSpecified 인 경우에는 그 근처만 찾아보고,<br>
	// SeedPositionSpecified 이 아닌 경우에는 seedLocationStrategy 를 조금씩 바꿔가며 계속 찾아본다.<br>
	// (MainBase . MainBase 주위 . MainBase 길목 . MainBase 가까운 앞마당 . MainBase 가까운 앞마당의 길목 . 탐색 종료)
	public TilePosition getDesiredPosition(UnitType unitType, TilePosition seedPosition,BuildOrderItem.SeedPositionStrategy seedPositionStrategy) {
        TilePosition desiredPosition = null;
        
//        20180819. hkk. 1분에 한번씩 초기화

//        if(Prebot.Broodwar.getFrameCount() % (24*60) == 1) {
	        mainBaseLocationFull = false;
	        secondStartLocationFull = false;
			firstChokePointFull = false;
			firstExpansionLocationFull = false;
			secondChokePointFull = false;
			fisrtSupplePointFull = false;
//        }


        
        int count = 0;
		while (count < 15) {
//		while (true) {
	    	count++;
//	    	//FileUtils.appendTextToFile("log.txt", "\n while getDesiredPosition :: " + System.currentTimeMillis() + " :: " + unitType + " :: "+ seedPosition + " :: " + seedPositionStrategy);
            if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.MainBaseLocation) {
                if (mainBaseLocationFull) {
                    seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondChokePoint;//TODO 다음 검색 위치
                }
            } else if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.FirstChokePoint) {
                if (firstChokePointFull) {
                    seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondChokePoint;//TODO 다음 검색 위치
                }
            } else if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation) {
                if (firstExpansionLocationFull) {
                    seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondChokePoint;//TODO 다음 검색 위치
                }
            } else if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.NextSupplePoint) {
                if (fisrtSupplePointFull) {
//                	20180815. hkk. 서플라이포인트가 Full 일 경우 작은 건물은 메인베이스가 Full 이더라도 지을수 있는 공간이 있을수 있으므로, 일단 찾아보고 null 이 나올경우 아래에서 처리
                	seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
//                    if (mainBaseLocationFull) {
//                        seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondMainBaseLocation;
//                    } else {
//                        seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;
//                    }
                }
            }

            if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.SecondChokePoint) {
                if (secondChokePointFull) {
                    seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondMainBaseLocation;//TODO 다음 검색 위치
                }
            }
            if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.SecondMainBaseLocation) {
                if (secondStartLocationFull) {
                    seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.LastBuilingPoint;//TODO 다음 검색 위치
                }
            }

//	    	//FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy before :: buildManager :: " + System.currentTimeMillis()+ " :: " + unitType + " :: "+ seedPosition + " :: " + seedPositionStrategy);
            desiredPosition = ConstructionPlaceFinder.Instance().getBuildLocationWithSeedPositionAndStrategy(unitType, seedPosition, seedPositionStrategy);
//            //FileUtils.appendTextToFile("log.txt", "\n getBuildLocationWithSeedPositionAndStrategy after :: buildManager :: " + System.currentTimeMillis()+ " :: " + unitType + " :: "+ seedPosition + " :: " + seedPositionStrategy + " :: " + desiredPosition);
            
            if(desiredPosition == null) {
//            	20180815. hkk. seedPosition 이 지정되어 들어올경우 null이 나와도 SeedPositionStrategy 가 의미가 없으므로 1번만 찾는다.
            	
                if (TilePositionUtils.isValidTilePosition(seedPosition)) {
//                	//FileUtils.appendTextToFile("log.txt", "\n getDesiredPosition desiredPosition is null break :: " + unitType + " :: seedPosition :: "+ seedPosition);
                	break;
                }
            	
            	if(unitType == UnitType.Terran_Supply_Depot || unitType == UnitType.Terran_Academy || unitType == UnitType.Terran_Armory) {
            		if(seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.MainBaseLocation) {
//            			20180815. hkk. 서플라이등의 건물이 supply area 가 아닌 메인베이스일경우 area가 full 이며, desiredP=null 인것은 메인베이스도 자리가 없다는 뜻 
            			
            			seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.SecondMainBaseLocation;
            		}else if(seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.SecondMainBaseLocation) {
            			break;
            		}
            	}
//            	//FileUtils.appendTextToFile("log.txt", "\n getDesiredPosition desiredPosition is null :: "+ unitType + " :: "+ seedPosition + " :: " + seedPositionStrategy);
                if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {
                    break;
                }
                if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.getLastBuilingFinalLocation) {
                    break;
                }
                if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.LastBuilingPoint) {
                    seedPositionStrategy = BuildOrderItem.SeedPositionStrategy.getLastBuilingFinalLocation;
                }
                if (seedPositionStrategy == BuildOrderItem.SeedPositionStrategy.NextExpansionPoint) {
                	break;
                }
            }else {
//            	//FileUtils.appendTextToFile("log.txt", "\n getDesiredPosition desiredPosition not null break:: "+ unitType + " :: "+ desiredPosition + " :: " + seedPositionStrategy);
            	break;
            }
        }
		return desiredPosition;
	}

	// 사용가능 미네랄 = 현재 보유 미네랄 - 사용하기로 예약되어있는 미네랄
	public int getAvailableMinerals() {
		return MyBotModule.Broodwar.self().minerals() - ConstructionManager.Instance().getReservedMinerals();
	}

	// 사용가능 가스 = 현재 보유 가스 - 사용하기로 예약되어있는 가스
	public int getAvailableGas() {
		return MyBotModule.Broodwar.self().gas() - ConstructionManager.Instance().getReservedGas();
	}

	// return whether or not we meet resources, including building reserves
	public boolean hasEnoughResources(MetaType type) {
		// return whether or not we meet the resources
//		//FileUtils.appendTextToFile("log.txt", "\n hasEnoughResources :: " + type + " :: M :" + getAvailableMinerals() + " :: G : " + getAvailableGas());
//		return (type.mineralPrice() <= getAvailableMinerals()) && (type.gasPrice() <= getAvailableGas());
		if((type.mineralPrice() <= getAvailableMinerals()) && (type.gasPrice() <= getAvailableGas())) {
//			//FileUtils.appendTextToFile("log.txt", "\n hasEnoughResources :: " + type + " :: M :" + getAvailableMinerals() + " :: G : " + getAvailableGas());
			return true;
		}
		
		return false;
			
	}

	// selects a unit of a given type
	public Unit selectUnitOfType(UnitType type, Position closestTo) {
		// if we have none of the unit type, return null right away
		if (MyBotModule.Broodwar.self().completedUnitCount(type) == 0) {
			return null;
		}

		Unit unit = null;

		// if we are concerned about the position of the unit, that takes
		// priority
		if (closestTo != Position.None) {
			double minDist = 1000000000;

			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type) {
					double distance = u.getDistance(closestTo);
					if (unit == null || distance < minDist) {
						unit = u;
						minDist = distance;
					}
				}
			}

			// if it is a building and we are worried about selecting the unit
			// with the least
			// amount of training time remaining
		} else if (type.isBuilding()) {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type && u.isCompleted() && !u.isTraining() && !u.isLifted() && u.isPowered()) {

					return u;
				}
			}
			// otherwise just return the first unit we come across
		} else {
			for (Unit u : MyBotModule.Broodwar.self().getUnits()) {
				if (u.getType() == type && u.isCompleted() && u.getHitPoints() > 0 && !u.isLifted() && u.isPowered()) {
					return u;
				}
			}
		}

		// return what we've found so far
		return null;
	}

	/// BuildOrderItem 들의 목록을 저장하는 buildQueue 를 리턴합니다
	public BuildOrderQueue getBuildQueue() {
		return buildQueue;
	}

	/// seedPositionStrategy 을 현재 게임상황에 맞게 seedPosition 으로 바꾸어 리턴합니다
	private Position getSeedPositionFromSeedLocationStrategy(BuildOrderItem.SeedPositionStrategy seedLocationStrategy) {
		Position seedPosition = null;
		Chokepoint tempChokePoint;
		BaseLocation tempBaseLocation;
		TilePosition tempTilePosition = null;
		Region tempBaseRegion;
		int vx, vy;
		double d, theta;
		int bx, by;

		switch (seedLocationStrategy) {
		case MainBaseLocation:
			tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
			if (tempBaseLocation != null) {
				seedPosition = tempBaseLocation.getPosition(); 
			}
			break;
		case MainBaseBackYard:
			tempBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
			tempChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
			tempBaseRegion = BWTA.getRegion(tempBaseLocation.getPosition());

			//std::cout << "y";

			// (vx, vy) = BaseLocation 와 ChokePoint 간 차이 벡터 = 거리 d 와 각도 t 벡터. 단위는 position
			// 스타크래프트 좌표계 : 오른쪽으로 갈수록 x 가 증가 (데카르트 좌표계와 동일). 아래로 갈수록 y가 증가 (y축만 데카르트 좌표계와 반대)
			// 삼각함수 값은 데카르트 좌표계에서 계산하므로, vy를 부호 반대로 해서 각도 t 값을 구함 

			// MainBaseLocation 이 null 이거나, ChokePoint 가 null 이면, MainBaseLocation 주위에서 가능한 곳을 리턴한다
			if (tempBaseLocation != null && tempChokePoint != null) {
	
				// BaseLocation 에서 ChokePoint 로의 벡터를 구한다
				vx = tempChokePoint.getCenter().getX() - tempBaseLocation.getPosition().getX();
				//std::cout << "vx : " << vx ;
				vy = (tempChokePoint.getCenter().getY() - tempBaseLocation.getPosition().getY()) * (-1);
				//std::cout << "vy : " << vy;
				d = Math.sqrt(vx * vx + vy * vy) * 0.5; // BaseLocation 와 ChokePoint 간 거리보다 조금 짧은 거리로 조정. BaseLocation가 있는 Region은 대부분 직사각형 형태이기 때문
				//std::cout << "d : " << d;
				theta = Math.atan2(vy, vx + 0.0001); // 라디안 단위
				//std::cout << "t : " << t;
	
				// cos(t+90), sin(t+180) 등 삼각함수 Trigonometric functions of allied angles 을 이용. y축에 대해서는 반대부호로 적용
	
				// BaseLocation 에서 ChokePoint 반대쪽 방향의 Back Yard : 데카르트 좌표계에서 (cos(t+180) = -cos(t), sin(t+180) = -sin(t))
				bx = tempBaseLocation.getTilePosition().getX() - (int)(d * Math.cos(theta) / BuildConfig.TILE_SIZE);
				by = tempBaseLocation.getTilePosition().getY() + (int)(d * Math.sin(theta) / BuildConfig.TILE_SIZE);
				//std::cout << "i";
				tempTilePosition = new TilePosition(bx, by);
				// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
				
				//std::cout << "k";
				// 해당 지점이 같은 Region 에 속하고 Buildable 한 타일인지 확인
				if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) || tempBaseRegion != BWTA.getRegion(new Position(bx*BuildConfig.TILE_SIZE, by*BuildConfig.TILE_SIZE))) {
					//std::cout << "l";
	
					// BaseLocation 에서 ChokePoint 방향에 대해 오른쪽으로 90도 꺾은 방향의 Back Yard : 데카르트 좌표계에서 (cos(t-90) = sin(t),   sin(t-90) = - cos(t))
					bx = tempBaseLocation.getTilePosition().getX() + (int)(d * Math.sin(theta) / BuildConfig.TILE_SIZE);
					by = tempBaseLocation.getTilePosition().getY() + (int)(d * Math.cos(theta) / BuildConfig.TILE_SIZE);
					tempTilePosition = new TilePosition(bx, by);
					// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
					//std::cout << "m";
	
					if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false)) {
						// BaseLocation 에서 ChokePoint 방향에 대해 왼쪽으로 90도 꺾은 방향의 Back Yard : 데카르트 좌표계에서 (cos(t+90) = -sin(t),   sin(t+90) = cos(t))
						bx = tempBaseLocation.getTilePosition().getX() - (int)(d * Math.sin(theta) / BuildConfig.TILE_SIZE);
						by = tempBaseLocation.getTilePosition().getY() - (int)(d * Math.cos(theta) / BuildConfig.TILE_SIZE);
						tempTilePosition = new TilePosition(bx, by);
						// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
	
						if (!tempTilePosition.isValid() || !MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) || tempBaseRegion != BWTA.getRegion(new Position(bx*BuildConfig.TILE_SIZE, by*BuildConfig.TILE_SIZE))) {
	
							// BaseLocation 에서 ChokePoint 방향 절반 지점의 Back Yard : 데카르트 좌표계에서 (cos(t),   sin(t))
							bx = tempBaseLocation.getTilePosition().getX() + (int)(d * Math.cos(theta) / BuildConfig.TILE_SIZE);
							by = tempBaseLocation.getTilePosition().getY() - (int)(d * Math.sin(theta) / BuildConfig.TILE_SIZE);
							tempTilePosition = new TilePosition(bx, by);
							// std::cout << "ConstructionPlaceFinder MainBaseBackYard tempTilePosition " << tempTilePosition.x << "," << tempTilePosition.y << std::endl;
							//std::cout << "m";
						}
	
					}
				}
				//std::cout << "z";
				if (tempTilePosition.isValid() == false 
					|| MyBotModule.Broodwar.isBuildable(tempTilePosition.getX(), tempTilePosition.getY(), false) == false) {
					seedPosition = tempTilePosition.toPosition();
				}
				else {
					seedPosition = tempBaseLocation.getPosition();
				}
			}
			//std::cout << "w";
			// std::cout << "ConstructionPlaceFinder MainBaseBackYard desiredPosition " << desiredPosition.x << "," << desiredPosition.y << std::endl;
			break;

		case FirstExpansionLocation:
			tempBaseLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
			if (tempBaseLocation != null) {
				seedPosition = tempBaseLocation.getPosition();
			}
			break;

		case FirstChokePoint:
			tempChokePoint = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().selfPlayer);
			if (tempChokePoint != null) {
				seedPosition = tempChokePoint.getCenter();
			}
			break;

		case SecondChokePoint:
			tempChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
			if (tempChokePoint != null) {
				seedPosition = tempChokePoint.getCenter();
			}
			break;
		default:
			break;
		}

		return seedPosition;
	}

	/// buildQueue 의 Dead lock 여부를 판단하기 위해, 가장 우선순위가 높은 BuildOrderItem 의 producer 가 존재하게될 것인지 여부를 리턴합니다
	public boolean isProducerWillExist(UnitType producerType) {
		boolean isProducerWillExist = true;

		if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
				&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) {
			// producer 가 건물 인 경우 : 건물이 건설 중인지 추가 파악
			// 만들려는 unitType = Addon 건물. Lair. Hive. Greater Spire. Sunken
			// Colony. Spore Colony. 프로토스 및 테란의 지상유닛 / 공중유닛.
			if (producerType.isBuilding()) {
				if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {
					isProducerWillExist = false;
				}
			}
			// producer 가 건물이 아닌 경우 : producer 가 생성될 예정인지 추가 파악
			// producerType : 일꾼. Larva. Hydralisk, Mutalisk
			else {
					isProducerWillExist = false;
			}
		}

		return isProducerWillExist;
	}

	public void checkBuildOrderQueueDeadlockAndInsert() {
		
		BuildOrderQueue buildQueue = BuildManager.Instance().getBuildQueue();
		if (!buildQueue.isEmpty()) {
			BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

			// 건물이나 유닛의 경우
			if (currentItem.metaType.isUnit()) {
				UnitType unitType = currentItem.metaType.getUnitType();//TODO 가스가 필요한 건물이면서 현재 refinery 가 없으면 짓는다
				final Map<UnitType, Integer> requiredUnits = unitType.requiredUnits();

				Iterator<UnitType> it = requiredUnits.keySet().iterator();
				// 선행 건물/유닛이 있는데
				if (requiredUnits.size() > 0) {
					while (it.hasNext()) {
						UnitType requiredUnitType = it.next(); // C++ : u.first;
						if (requiredUnitType != UnitType.None) {
							if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
									&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
								// 선행 건물이 건설 예정이지도 않으면 만들기
								if (requiredUnitType.isBuilding()) {
									if (BuildManager.Instance().buildQueue.getItemCount(requiredUnitType) == 0
											&& ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
//										int needcnt=0;
//										int requirecnt=0;
//								
//										for (Unit unit : MyBotModule.Broodwar.self().getUnits()){
//											if(unit.getType() == unitType && unit.isCompleted()){
//												needcnt++;
//											}
//											if(unit.getType() == requiredUnitType){
//												requirecnt++;
//											}
//										}
//										if(needcnt > requirecnt){		
//											System.out.println("Inserting blocked unit: " + requiredUnitType);
											BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(requiredUnitType), true);
										//}
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	public void checkBuildOrderQueueDeadlockAndAndFixIt() {
		// 빌드오더를 수정할 수 있는 프레임인지 먼저 판단한다
		// this will be true if any unit is on the first frame if it's training
		// time remaining
		// this can cause issues for the build order search system so don't plan
		// a search on these frames
		boolean canPlanBuildOrderNow = true;
		for (final Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.getRemainingTrainTime() == 0) {
				continue;
			}

			UnitCommand unitCommand = unit.getLastCommand();
			if (unitCommand != null) {

				UnitCommandType unitCommandType = unitCommand.getUnitCommandType();
				if (unitCommandType != UnitCommandType.None) {
					if (unitCommand.getUnit() != null) {
						UnitType trainType = unitCommand.getUnit().getType();
						if (unit.getRemainingTrainTime() == trainType.buildTime()) {
							canPlanBuildOrderNow = false;
							break;
						}
					}
				}
			}

		}
		if (!canPlanBuildOrderNow) {
			return;
		}

		// BuildQueue 의 HighestPriority 에 있는 BuildQueueItem 이 skip 불가능한 것인데,
		// 선행조건이 충족될 수 없거나, 실)행이 앞으로도 계속 불가능한 경우, dead lock 이 발생한다
		// 선행 건물을 BuildQueue에 추가해넣을지, 해당 BuildQueueItem 을 삭제할지 전략적으로 판단해야 한다
		BuildOrderQueue buildQueue = BuildManager.Instance().getBuildQueue();
		if (!buildQueue.isEmpty()) {
			BuildOrderItem currentItem = buildQueue.getHighestPriorityItem();

			// if (buildQueue.canSkipCurrentItem() == false)
			if (currentItem.blocking == true) {
				boolean isDeadlockCase = false;

				// producerType을 먼저 알아낸다
				UnitType producerType = currentItem.metaType.whatBuilds();

				// 건물이나 유닛의 경우
				if (currentItem.metaType.isUnit()) {
					UnitType unitType = currentItem.metaType.getUnitType();
					TechType requiredTechType = unitType.requiredTech();
					final Map<UnitType, Integer> requiredUnits = unitType.requiredUnits();

					/*
					 * std::cout + "To make " + unitType.getName() +
					 * ", producerType " + producerType.getName() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType) + std::endl;
					 */

					// 건물을 생산하는 유닛이나, 유닛을 생산하는 건물이 존재하지 않고, 건설 예정이지도 않으면 dead
					// lock
					if (isProducerWillExist(producerType) == false) {
						isDeadlockCase = true;
					}

					// Refinery 건물의 경우, Refinery 가 건설되지 않은 Geyser가 있는 경우에만 가능
//					if (!isDeadlockCase && unitType == InformationManager.Instance().getRefineryBuildingType()) {
					if (!isDeadlockCase && unitType == InformationManager.Instance().getRefineryBuildingType()) {
						
//						//FileUtils.appendTextToFile("log.txt", "\n checkBuildOrderQueueDeadlockAndAndFixIt :: refinery lock check");
						boolean hasAvailableGeyser = true;

						// Refinery가 지어질 수 있는 장소를 찾아본다
						TilePosition testLocation = getDesiredPosition(unitType, currentItem.seedLocation,
								currentItem.seedLocationStrategy);
						
//						//FileUtils.appendTextToFile("log.txt", "\n checkBuildOrderQueueDeadlockAndAndFixIt :: getDesiredPosition :: " + testLocation);

						// Refinery 를 지으려는 장소를 찾을 수 없으면 dead lock
						if (testLocation == TilePosition.None || testLocation == TilePosition.Invalid
								|| testLocation.isValid() == false) {
							//System.out.println("Build Order Dead lock case . Cann't find place to construct " + unitType); // C++ : unitType.getName()
							hasAvailableGeyser = false;
						} else {
							// Refinery 를 지으려는 장소에 Refinery 가 이미 건설되어 있다면 dead lock
							for (Unit u : MyBotModule.Broodwar.getUnitsOnTile(testLocation)) {
								if (u.getType().isRefinery() && u.exists()) {
									hasAvailableGeyser = false;
									break;
								}
							}
						}

						if (hasAvailableGeyser == false) {
							isDeadlockCase = true;
						}
					}

					// 선행 기술 리서치가 되어있지 않고, 리서치 중이지도 않으면 dead lock
					if (!isDeadlockCase && requiredTechType != TechType.None) {
						if (MyBotModule.Broodwar.self().hasResearched(requiredTechType) == false) {
							if (MyBotModule.Broodwar.self().isResearching(requiredTechType) == false) {
								isDeadlockCase = true;
							}
						}
					}
					
					int getAddonPossibeCnt = 0;
					
					if (currentItem.metaType.getUnitType().isAddon()){ 
						UnitType ProducerType = currentItem.metaType.getUnitType().whatBuilds().first;
						
						for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
							if(ProducerType == unit.getType() && unit.isCompleted() ){
//								
								if(InitialBuildProvider.Instance().getAdaptStrategyStatus() != InitialBuildProvider.AdaptStrategyStatus.BEFORE) {
//								if (TempBuildSourceCode.Instance().isInitialBuildOrderFinished()) {
									if(unit.canBuildAddon() == false){
										continue;
									}
								}
								if(currentItem.metaType.getUnitType()  != UnitType.Terran_Comsat_Station){
									if (isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+1) == false
											||isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+1) == false
											||isBuildableTile(unit.getTilePosition().getX()+4, unit.getTilePosition().getY()+2) == false
											||isBuildableTile(unit.getTilePosition().getX()+5, unit.getTilePosition().getY()+2) == false)
									{
										//System.out.println("something is blocking addon place, so no cnt");
										continue;
									}
								}
								getAddonPossibeCnt++;
							}
						}
						if(getAddonPossibeCnt == 0){
//							System.out.println("deadlock because no place to addon");
							isDeadlockCase = true;
						}
					}
					
					Iterator<UnitType> it = requiredUnits.keySet().iterator();
					// 선행 건물/유닛이 있는데
					if (!isDeadlockCase && requiredUnits.size() > 0) {
						// for (Unit u : it)
						while (it.hasNext()) {
							UnitType requiredUnitType = it.next(); // C++ : u.first;
							if (requiredUnitType != UnitType.None) {
								/*
								 * std::cout + "pre requiredUnitType " +
								 * requiredUnitType.getName() +
								 * " completedUnitCount " +
								 * MyBotModule.Broodwar.self().
								 * completedUnitCount(requiredUnitType) +
								 * " incompleteUnitCount " +
								 * MyBotModule.Broodwar.self().
								 * incompleteUnitCount(requiredUnitType) +
								 * std::endl;
								 */

								// 선행 건물 / 유닛이 존재하지 않고, 생산 중이지도 않고
								if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
										&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
									// 선행 건물이 건설 예정이지도 않으면 dead lock
									if (requiredUnitType.isBuilding()) {
										if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
											isDeadlockCase = true;
										}
									}
								}
							}
						}
					}

					// 건물이 아닌 지상/공중 유닛인 경우, 서플라이가 400 꽉 찼으면 dead lock
					if (!isDeadlockCase && !unitType.isBuilding() && MyBotModule.Broodwar.self().supplyTotal() == 400
							&& MyBotModule.Broodwar.self().supplyUsed() + unitType.supplyRequired() > 400) {
						isDeadlockCase = true;
					}

					// 건물이 아닌 지상/공중 유닛인데, 서플라이가 부족하면 dead lock 상황이 되긴 하지만, 
					// 이 경우는 빌드를 취소하기보다는, StrategyManager 등에서 서플라이 빌드를 추가함으로써 풀도록 한다
//					if (!isDeadlockCase && !unitType.isBuilding()
//							&& MyBotModule.Broodwar.self().supplyUsed() + unitType.supplyRequired() > MyBotModule.Broodwar.self().supplyTotal()) 
//					{
//						//isDeadlockCase = true;
//					}

					// Pylon 이 해당 지역 주위에 먼저 지어져야 하는데, Pylon 이 해당 지역 주위에 없고, 예정되어있지도 않으면 dead lock
//					if (!isDeadlockCase && unitType.isBuilding() && unitType.requiresPsi()
//							&& currentItem.seedLocationStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {
//
//						boolean hasFoundPylon = false;
//						List<Unit> ourUnits = MyBotModule.Broodwar
//								.getUnitsInRadius(currentItem.seedLocation.toPosition(), 4 * Config.TILE_SIZE);
//
//						for (Unit u : ourUnits) {
//							if (u.getPlayer() == MyBotModule.Broodwar.self() && u.getType() == UnitType.Protoss_Pylon) {
//								hasFoundPylon = true;
//							}
//						}
//
//						if (hasFoundPylon == false) {
//							isDeadlockCase = true;
//						}
//					}

					// Creep 이 해당 지역 주위에 Hatchery나 Creep Colony 등을 통해 먼저 지어져야 하는데, 해당 지역 주위에 지어지지 않고 있으면 dead lock
//					if (!isDeadlockCase && unitType.isBuilding() && unitType.requiresCreep()
//							&& currentItem.seedLocationStrategy == BuildOrderItem.SeedPositionStrategy.SeedPositionSpecified) {
//						boolean hasFoundCreepGenerator = false;
//						List<Unit> ourUnits = MyBotModule.Broodwar
//								.getUnitsInRadius(currentItem.seedLocation.toPosition(), 4 * Config.TILE_SIZE);
//
//						for (Unit u : ourUnits) {
//							if (u.getPlayer() == MyBotModule.Broodwar.self() && (u.getType() == UnitType.Zerg_Hatchery
//									|| u.getType() == UnitType.Zerg_Lair || u.getType() == UnitType.Zerg_Hive
//									|| u.getType() == UnitType.Zerg_Creep_Colony
//									|| u.getType() == UnitType.Zerg_Sunken_Colony
//									|| u.getType() == UnitType.Zerg_Spore_Colony)) {
//								hasFoundCreepGenerator = true;
//							}
//						}
//
//						if (hasFoundCreepGenerator == false) {
//							isDeadlockCase = true;
//						}
//					}

				}
				// 테크의 경우, 해당 리서치를 이미 했거나, 이미 하고있거나, 리서치를 하는 건물 및 선행건물이 존재하지않고
				// 건설예정이지도 않으면 dead lock
				else if (currentItem.metaType.isTech()) {
					TechType techType = currentItem.metaType.getTechType();
					UnitType requiredUnitType = techType.requiredUnit();

					/*
					 * System.out.println("To research " + techType.toString() +
					 * ", hasResearched " +
					 * MyBotModule.Broodwar.self().hasResearched(techType) +
					 * ", isResearching " +
					 * MyBotModule.Broodwar.self().isResearching(techType) +
					 * ", producerType " + producerType.toString() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType));
					 */

					if (MyBotModule.Broodwar.self().hasResearched(techType)
							|| MyBotModule.Broodwar.self().isResearching(techType)) {
						isDeadlockCase = true;
					} 
					else if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) 
					{
						if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) 
						{

							// 테크 리서치의 producerType이 Addon 건물인 경우, Addon 건물 건설이
							// 명령 내려졌지만 시작되기 직전에는 getUnits, completedUnitCount,
							// incompleteUnitCount 에서 확인할 수 없다
							// producerType의 producerType 건물에 의해 Addon 건물 건설의
							// 명령이 들어갔는지까지 확인해야 한다
							if (producerType.isAddon()) {

								boolean isAddonConstructing = false;

								UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

								if (producerTypeOfProducerType != UnitType.None) {

									for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
										if (unit == null)
											continue;
										if (unit.getType() != producerTypeOfProducerType) {
											continue;
										}

										// 모건물이 완성되어있고, 모건물이 해당 Addon 건물을 건설중인지
										// 확인한다
										if (unit.isCompleted() && unit.isConstructing()
												&& unit.getBuildType() == producerType) {
											isAddonConstructing = true;
											break;
										}
									}
								}

								if (isAddonConstructing == false) {
									isDeadlockCase = true;
								}
							} else {
								isDeadlockCase = true;
							}
						}
					} 
					else if (requiredUnitType != UnitType.None) {
						/*
						 * std::cout + "To research " + techType.getName() +
						 * ", requiredUnitType " + requiredUnitType.getName() +
						 * " completedUnitCount " +
						 * MyBotModule.Broodwar.self().completedUnitCount(
						 * requiredUnitType) + " incompleteUnitCount " +
						 * MyBotModule.Broodwar.self().incompleteUnitCount(
						 * requiredUnitType) + std::endl;
						 */

						if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
							if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType,
									null) == 0) {
								isDeadlockCase = true;
							}
						}
					}
				}
				// 업그레이드의 경우, 해당 업그레이드를 이미 했거나, 이미 하고있거나, 업그레이드를 하는 건물 및 선행건물이
				// 존재하지도 않고 건설예정이지도 않으면 dead lock
				else if (currentItem.metaType.isUpgrade()) {
					UpgradeType upgradeType = currentItem.metaType.getUpgradeType();
					int maxLevel = MyBotModule.Broodwar.self().getMaxUpgradeLevel(upgradeType);
					int currentLevel = MyBotModule.Broodwar.self().getUpgradeLevel(upgradeType);
					UnitType requiredUnitType = upgradeType.whatsRequired();

					/*
					 * std::cout + "To upgrade " + upgradeType.getName() +
					 * ", maxLevel " + maxLevel + ", currentLevel " +
					 * currentLevel + ", isUpgrading " +
					 * MyBotModule.Broodwar.self().isUpgrading(upgradeType) +
					 * ", producerType " + producerType.getName() +
					 * " completedUnitCount " +
					 * MyBotModule.Broodwar.self().completedUnitCount(
					 * producerType) + " incompleteUnitCount " +
					 * MyBotModule.Broodwar.self().incompleteUnitCount(
					 * producerType) + ", requiredUnitType " +
					 * requiredUnitType.getName() + std::endl;
					 */

					if (currentLevel >= maxLevel || MyBotModule.Broodwar.self().isUpgrading(upgradeType)) {
						isDeadlockCase = true;
					} else if (MyBotModule.Broodwar.self().completedUnitCount(producerType) == 0
							&& MyBotModule.Broodwar.self().incompleteUnitCount(producerType) == 0) {
						if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {

							// 업그레이드의 producerType이 Addon 건물인 경우, Addon 건물 건설이
							// 시작되기 직전에는 getUnits, completedUnitCount,
							// incompleteUnitCount 에서 확인할 수 없다
							// producerType의 producerType 건물에 의해 Addon 건물 건설이
							// 시작되었는지까지 확인해야 한다
							if (producerType.isAddon()) {

								boolean isAddonConstructing = false;

								UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

								if (producerTypeOfProducerType != UnitType.None) {

									for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
										if (unit == null)
											continue;
										if (unit.getType() != producerTypeOfProducerType) {
											continue;
										}
										// 모건물이 완성되어있고, 모건물이 해당 Addon 건물을 건설중인지
										// 확인한다
										if (unit.isCompleted() && unit.isConstructing()
												&& unit.getBuildType() == producerType) {
											isAddonConstructing = true;
											break;
										}
									}
								}

								if (isAddonConstructing == false) {
									isDeadlockCase = true;
								}
							} else {
								isDeadlockCase = true;
							}
						}
					} else if (requiredUnitType != UnitType.None) {
						if (MyBotModule.Broodwar.self().completedUnitCount(requiredUnitType) == 0
								&& MyBotModule.Broodwar.self().incompleteUnitCount(requiredUnitType) == 0) {
							if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType,
									null) == 0) {
								isDeadlockCase = true;
							}
						}
					}
				}

				if (!isDeadlockCase) {
					// producerID 를 지정했는데, 해당 ID 를 가진 유닛이 존재하지 않으면 dead lock
					if (currentItem.producerID != -1 ) {
						boolean isProducerAlive = false;
						for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
							if (unit != null && unit.getID() == currentItem.producerID && unit.exists() && unit.getHitPoints() > 0) {
								isProducerAlive = true;
								break;
							}
						}
						if (isProducerAlive == false) {
							isDeadlockCase = true;
						}
					}
				}

				if (isDeadlockCase) {
//					System.out.println(	"Build Order Dead lock case . remove BuildOrderItem " + currentItem.metaType.getName());

					buildQueue.removeCurrentItem();
				}

			}
		}
	}
	
	public final boolean isBuildableTile(int x, int y)
	{
		TilePosition tp = new TilePosition(x, y);
		if (!tp.isValid())
		{
			//System.out.println("Invalid");
			return false;
		}

		// 맵 데이터 뿐만 아니라 빌딩 데이터를 모두 고려해서 isBuildable 체크
		//if (BWAPI::Broodwar->isBuildable(x, y) == false)
		if (MyBotModule.Broodwar.isBuildable(x, y, true) == false)
		{
			//System.out.println("not buildable at: " + x + ", " + y);
			return false;
		}

		// constructionWorker 이외의 다른 유닛이 있으면 false를 리턴한다
		if(MyBotModule.Broodwar.getUnitsOnTile(x, y).size() > 0){
//			List<Unit> temp= MyBotModule.Broodwar.getUnitsOnTile(x, y);
//			for(Unit u : temp){
//				System.out.println("unit: "+ u.getType() + " at " + u.getPosition().toString());
//			}
			//System.out.println("there is unit");
			return false;
		}
		
		return true;
	}
};