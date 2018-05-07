package prebot.main.manager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Pair;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.build.BuildOrderItem;
import prebot.build.BuildOrderQueue;
import prebot.build.ConstructionPlaceFinder;
import prebot.build.MetaType;
import prebot.common.code.Config;
import prebot.common.util.TimeUtils;
import prebot.common.util.UnitUtils;
import prebot.main.Prebot;

/// 빌드(건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 순차적으로 실행하기 위해 빌드 큐를 관리하고, 빌드 큐에 있는 명령을 하나씩 실행하는 class<br>
/// 빌드 명령 중 건물 건설 명령은 ConstructionManager로 전달합니다
/// @see ConstructionManager
public class BuildManager extends GameManager {

	/// BuildOrderItem 들의 목록을 저장하는 buildQueue
	public BuildOrderQueue buildQueue = new BuildOrderQueue();

	private static BuildManager instance = new BuildManager();

	/// static singleton 객체를 리턴합니다
	public static BuildManager Instance() {
		return instance;
	}

	/// buildQueue 에 대해 Dead lock 이 있으면 제거하고, 가장 우선순위가 높은 BuildOrderItem 를 실행되도록 시도합니다
	public GameManager update() {
		// 1초(24프레임)에 4번 정도만 실행해도 충분하다 if (Prebot.Game.getFrameCount() % 6 != 0) return;

		if (buildQueue.isEmpty()) {
			return this;
		}

		// Dead Lock 을 체크해서 제거한다
		checkBuildOrderQueueDeadlockAndAndFixIt();
		// Dead Lock 제거후 Empty 될 수 있다
		if (buildQueue.isEmpty()) {
			return this;
		}

		// the current item to be used
		BuildOrderItem currentItem = buildQueue.getHighestItem();

		// while there is still something left in the buildQueue
		while (!buildQueue.isEmpty()) {
			boolean isOkToRemoveQueue = true;

			// seedPosition 을 도출한다
			Position seedPosition = null;
			if (currentItem.seedLocation != TilePosition.None && currentItem.seedLocation != TilePosition.Invalid && currentItem.seedLocation != TilePosition.Unknown
					&& currentItem.seedLocation.isValid()) {
				seedPosition = currentItem.seedLocation.toPosition();
			} else {
				seedPosition = ConstructionPlaceFinder.Instance().getSeedPositionFromSeedStrategy(currentItem.seedLocationStrategy);
			}

			// this is the unit which can produce the currentItem
			Unit producer = getProducer(currentItem.metaType, seedPosition, currentItem.producerID);

			boolean canMake = false;

			// 건물을 만들수 있는 유닛(일꾼)이나, 유닛을 만들수 있는 유닛(건물 or 유닛)이 있으면
			if (producer != null) {
				// check to see if we can make it right now
				// 지금 해당 유닛을 건설/생산 할 수 있는지에 대해 자원, 서플라이, 테크 트리, producer 만을 갖고 판단한다
				canMake = canMakeNow(producer, currentItem.metaType);
			}

			// if we can make the current item, create it
			if (producer != null && canMake == true) {
				MetaType t = currentItem.metaType;

				if (t.isUnit()) {
					if (t.getUnitType().isBuilding()) {
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
							// ConstructionPlaceFinder 를 통해 건설 가능 위치 desiredPosition 를 알아내서
							// ConstructionManager 의 ConstructionTask Queue에 추가를 해서 desiredPosition 에 건설을 하게 한다.
							// ConstructionManager 가 건설 도중에 해당 위치에 건설이 어려워지면 다시
							// ConstructionPlaceFinder 를 통해 건설 가능 위치를 desiredPosition 주위에서 찾을 것이다
							TilePosition desiredPosition = ConstructionPlaceFinder.Instance().getDesiredPosition(t.getUnitType(), currentItem.seedLocation, currentItem.seedLocationStrategy);
							if (desiredPosition != TilePosition.None) {
								// Send the construction task to the
								// construction manager
								ConstructionManager.Instance().addConstructionTask(t.getUnitType(), desiredPosition);
							} else {
								// 건물 가능 위치가 없는 경우는, Protoss_Pylon 가 없거나, Creep
								// 이 없거나, Refinery 가 이미 다 지어져있거나, 정말 지을 공간이 주위에
								// 없는 경우인데,
								// 대부분의 경우 Pylon 이나 Hatchery가 지어지고 있는 중이므로, 다음
								// frame 에 건물 지을 공간을 다시 탐색하도록 한다.
								System.out.print("There is no place to construct " + currentItem.metaType.getUnitType() + " strategy " + currentItem.seedLocationStrategy);
								if (currentItem.seedLocation != null)
									System.out.print(" seedPosition " + currentItem.seedLocation.getX() + "," + currentItem.seedLocation.getY());
								if (desiredPosition != null)
									System.out.print(" desiredPosition " + desiredPosition.getX() + "," + desiredPosition.getY());
								isOkToRemoveQueue = false;
							}
						}
					}
					// 지상유닛 / 공중유닛의 경우
					else {
						producer.train(t.getUnitType());
					}
				}
				// if we're dealing with a tech research
				else if (t.isTech()) {
					producer.research(t.getTechType());
				} else if (t.isUpgrade()) {
					producer.upgrade(t.getUpgradeType());
				}

				// remove it from the buildQueue
				if (isOkToRemoveQueue) {
					buildQueue.removeCurrentItem();
				}

				// don't actually loop around in here
				break;
			}
			// otherwise, if we can skip the current item
			else if (buildQueue.canSkipCurrentItem()) {
				// skip it and get the next one
				buildQueue.skipCurrentItem();
				currentItem = buildQueue.getNextItem();
			} else {
				// so break out
				break;
			}
		}
		return this;
	}

	/// 해당 MetaType 을 build 할 수 있는 producer 를 찾아 반환합니다
	/// @param t 빌드하려는 대상의 타입
	/// @param closestTo 파라메타 입력 시 producer 후보들 중 해당 position 에서 가장 가까운 producer 를 리턴합니다
	/// @param producerID 파라메타 입력 시 해당 ID의 unit 만 producer 후보가 될 수 있습니다
	public Unit getProducer(MetaType metaType, Position closestTo, int producerID) {
		// get the type of unit that builds this
		UnitType producerType = metaType.whatBuilds();

		// make a set of all candidate producers
		List<Unit> candidateProducers = new ArrayList<Unit>();
		for (Unit unit : Prebot.Game.self().getUnits()) {
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

			if (producerID != -1 && unit.getID() != producerID) {
				continue;
			}

			if (metaType.isUnit()) {
				// if the type requires an addon and the producer doesn't have
				// one
				// C++ : typedef std::pair<BWAPI::UnitType, int> ReqPair;
				Pair<UnitType, Integer> ReqPair = null;

				Map<UnitType, Integer> requiredUnitsMap = metaType.getUnitType().requiredUnits();
				if (requiredUnitsMap != null) {
					Iterator<UnitType> it = requiredUnitsMap.keySet().iterator();

					// for (final Pair<UnitType, Integer> pair :
					// t.getUnitType().requiredUnits())
					while (it.hasNext()) {
						UnitType requiredType = it.next();
						if (requiredType.isAddon()) {
							if (unit.getAddon() == null || (unit.getAddon().getType() != requiredType)) {
								continue;
							}
						}
					}
				}

				// if the type is an addon
				if (metaType.getUnitType().isAddon()) {
					// if the unit already has an addon, it can't make one
					if (unit.getAddon() != null) {
						continue;
					}

					// 모건물은 건설되고 있는 중에는 isCompleted = false, isConstructing = true, canBuildAddon = false 이다가
					// 건설이 완성된 후 몇 프레임동안은 isCompleted = true 이지만, canBuildAddon = false 인 경우가 있다
					if (!unit.canBuildAddon()) {
						continue;
					}

					// if we just told this unit to build an addon, then it will not be building another one
					// this deals with the frame-delay of telling a unit to build an addon and it actually starting to build
					if (unit.getLastCommand().getUnitCommandType() == UnitCommandType.Build_Addon // C++ : unit.getLastCommand().getType()
							&& (TimeUtils.elapsedFrames(unit.getLastCommandFrame()) < 10)) {
						continue;
					}

					boolean isBlocked = false;

					// if the unit doesn't have space to build an addon, it
					// can't make one
					TilePosition addonPosition = new TilePosition(unit.getTilePosition().getX() + unit.getType().tileWidth(),
							unit.getTilePosition().getY() + unit.getType().tileHeight() - metaType.getUnitType().tileHeight());

					for (int i = 0; i < metaType.getUnitType().tileWidth(); ++i) {
						for (int j = 0; j < metaType.getUnitType().tileHeight(); ++j) {
							TilePosition tilePos = new TilePosition(addonPosition.getX() + i, addonPosition.getY() + j);

							// if the map won't let you build here, we can't build it.
							// 맵 타일 자체가 건설 불가능한 타일인 경우 + 기존 건물이 해당 타일에 이미 있는경우
							if (!Prebot.Game.isBuildable(tilePos, true)) {
								isBlocked = true;
							}

							// if there are any units on the addon tile, we can't build it
							// 아군 유닛은 Addon 지을 위치에 있어도 괜찮음. (적군 유닛은 Addon 지을 위치에 있으면 건설 안되는지는 아직 불확실함)
							for (Unit u : Prebot.Game.getUnitsOnTile(tilePos.getX(), tilePos.getY())) {
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

			// if we haven't cut it, add it to the set of candidates
			candidateProducers.add(unit); // candidateProducers.insert(unit);
		}

		return UnitUtils.getClosestUnitToPosition(candidateProducers, closestTo);
	}

	/// 해당 MetaType 을 build 할 수 있는 producer 를 찾아 반환합니다
	public Unit getProducer(MetaType t, Position closestTo) {
		return getProducer(t, closestTo, -1);
	}

	/// 해당 MetaType 을 build 할 수 있는 producer 를 찾아 반환합니다
	public Unit getProducer(MetaType t) {
		return getProducer(t, Position.None, -1);
	}

	// 지금 해당 유닛을 건설/생산 할 수 있는지에 대해 자원, 서플라이, 테크 트리, producer 만을 갖고 판단한다<br>
	// 해당 유닛이 건물일 경우 건물 지을 위치의 적절 여부 (탐색했었던 타일인지, 건설 가능한 타일인지, 주위에 Pylon이 있는지,<br>
	// Creep이 있는 곳인지 등) 는 판단하지 않는다
	public boolean canMakeNow(Unit producer, MetaType t) {
		if (producer == null) {
			return false;
		}

		boolean canMake = hasEnoughResources(t);

		if (canMake) {
			if (t.isUnit()) {
				// MyBotModule.Broodwar.canMake : Checks all the requirements
				// include resources, supply, technology tree, availability, and required units
				canMake = Prebot.Game.canMake(t.getUnitType(), producer);
			} else if (t.isTech()) {
				canMake = Prebot.Game.canResearch(t.getTechType(), producer);
			} else if (t.isUpgrade()) {
				canMake = Prebot.Game.canUpgrade(t.getUpgradeType(), producer);
			}
		}

		return canMake;
	}

	// 사용가능 미네랄 = 현재 보유 미네랄 - 사용하기로 예약되어있는 미네랄
	public int getAvailableMinerals() {
		return Prebot.Game.self().minerals() - ConstructionManager.Instance().getReservedMinerals();
	}

	// 사용가능 가스 = 현재 보유 가스 - 사용하기로 예약되어있는 가스
	public int getAvailableGas() {
		return Prebot.Game.self().gas() - ConstructionManager.Instance().getReservedGas();
	}

	// return whether or not we meet resources, including building reserves
	public boolean hasEnoughResources(MetaType type) {
		// return whether or not we meet the resources
		return (type.mineralPrice() <= getAvailableMinerals()) && (type.gasPrice() <= getAvailableGas());
	}

	// selects a unit of a given type
	public Unit selectUnitOfType(UnitType type, Position closestTo) {
		// if we have none of the unit type, return null right away
		if (Prebot.Game.self().completedUnitCount(type) == 0) {
			return null;
		}

		Unit unit = null;

		// if we are concerned about the position of the unit, that takes
		// priority
		if (closestTo != Position.None) {
			double minDist = 1000000000;

			for (Unit u : Prebot.Game.self().getUnits()) {
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
			for (Unit u : Prebot.Game.self().getUnits()) {
				if (u.getType() == type && u.isCompleted() && !u.isTraining() && !u.isLifted() && u.isPowered()) {

					return u;
				}
			}
			// otherwise just return the first unit we come across
		} else {
			for (Unit u : Prebot.Game.self().getUnits()) {
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

	/// buildQueue 의 Dead lock 여부를 판단하기 위해, 가장 우선순위가 높은 BuildOrderItem 의 producer 가 존재하게될 것인지 여부를 리턴합니다
	public boolean isProducerWillExist(UnitType producerType) {
		boolean isProducerWillExist = true;

		if (Prebot.Game.self().completedUnitCount(producerType) == 0 && Prebot.Game.self().incompleteUnitCount(producerType) == 0) {
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
				// Larva 는 시간이 지나면 Hatchery, Lair, Hive 로부터 생성되기 때문에 해당 건물이 있는지
				// 추가 파악
				if (producerType == UnitType.Zerg_Larva) {
					if (Prebot.Game.self().completedUnitCount(UnitType.Zerg_Hatchery) == 0 && Prebot.Game.self().incompleteUnitCount(UnitType.Zerg_Hatchery) == 0
							&& Prebot.Game.self().completedUnitCount(UnitType.Zerg_Lair) == 0 && Prebot.Game.self().incompleteUnitCount(UnitType.Zerg_Lair) == 0
							&& Prebot.Game.self().completedUnitCount(UnitType.Zerg_Hive) == 0
							&& Prebot.Game.self().incompleteUnitCount(UnitType.Zerg_Hive) == 0) {
						if (ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0
								&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) {
							isProducerWillExist = false;
						}
					}
				}
				// Hydralisk, Mutalisk 는 Egg 로부터 생성되기 때문에 추가 파악
				else if (producerType.getRace() == Race.Zerg) {
					boolean isInEgg = false;
					for (Unit unit : Prebot.Game.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == producerType) {
							isInEgg = true;
						}
						// 갓태어난 유닛은 아직 반영안되어있을 수 있어서, 추가 카운트를 해줘야함
						if (unit.getType() == producerType && unit.isConstructing()) {
							isInEgg = true;
						}
					}
					if (isInEgg == false) {
						isProducerWillExist = false;
					}
				} else {
					isProducerWillExist = false;
				}
			}
		}

		return isProducerWillExist;
	}

	public void checkBuildOrderQueueDeadlockAndAndFixIt() {
		// 빌드오더를 수정할 수 있는 프레임인지 먼저 판단한다
		// this will be true if any unit is on the first frame if it's training
		// time remaining
		// this can cause issues for the build order search system so don't plan
		// a search on these frames
		boolean canPlanBuildOrderNow = true;
		for (final Unit unit : Prebot.Game.self().getUnits()) {
			if (unit.getRemainingTrainTime() == 0) {
				continue;
			}

			UnitCommand unitCommand = unit.getLastCommand();
			if (unitCommand == null || unitCommand.getUnitCommandType() == UnitCommandType.None || unitCommand.getUnit() == null) {
				continue;
			}

			UnitType trainType = unitCommand.getUnit().getType();
			if (unit.getRemainingTrainTime() == trainType.buildTime()) {
				canPlanBuildOrderNow = false;
				break;
			}
		}

		if (!canPlanBuildOrderNow) {
			return;
		}

		// BuildQueue 의 HighestPriority 에 있는 BuildQueueItem 이 skip 불가능한 것인데,
		// 선행조건이 충족될 수 없거나, 실행이 앞으로도 계속 불가능한 경우, dead lock 이 발생한다
		// 선행 건물을 BuildQueue에 추가해넣을지, 해당 BuildQueueItem 을 삭제할지 전략적으로 판단해야 한다
		BuildOrderQueue buildQueue = BuildManager.Instance().getBuildQueue();
		if (buildQueue.isEmpty()) {
			return;
		}

		BuildOrderItem currentItem = buildQueue.getHighestItem();
		if (!currentItem.blocking) {
			return;
		}
		boolean isDeadlockCase = false;

		// producerType을 먼저 알아낸다
		UnitType producerType = currentItem.metaType.whatBuilds();

		// 건물이나 유닛의 경우
		if (currentItem.metaType.isUnit()) {
			UnitType unitType = currentItem.metaType.getUnitType();
			TechType requiredTechType = unitType.requiredTech();
			final Map<UnitType, Integer> requiredUnits = unitType.requiredUnits();
			int requiredSupply = unitType.supplyRequired();

			/*
			 * std::cout + "To make " + unitType.getName() + ", producerType " + producerType.getName() + " completedUnitCount " + MyBotModule.Broodwar.self().completedUnitCount(
			 * producerType) + " incompleteUnitCount " + MyBotModule.Broodwar.self().incompleteUnitCount( producerType) + std::endl;
			 */

			// 건물을 생산하는 유닛이나, 유닛을 생산하는 건물이 존재하지 않고, 건설 예정이지도 않으면 dead
			// lock
			if (!isProducerWillExist(producerType)) {
				isDeadlockCase = true;
			}

			// Refinery 건물의 경우, Refinery 가 건설되지 않은 Geyser가 있는 경우에만 가능
			if (!isDeadlockCase && unitType == UnitType.Terran_Refinery) {
				boolean hasAvailableGeyser = true;

				// Refinery가 지어질 수 있는 장소를 찾아본다
				TilePosition testLocation = ConstructionPlaceFinder.Instance().getDesiredPosition(unitType, currentItem.seedLocation, currentItem.seedLocationStrategy);

				// Refinery 를 지으려는 장소를 찾을 수 없으면 dead lock
				if (testLocation == TilePosition.None || testLocation == TilePosition.Invalid || testLocation.isValid() == false) {
					System.out.println("Build Order Dead lock case . Cann't find place to construct " + unitType); // C++
					hasAvailableGeyser = false;
				} else {
					// Refinery 를 지으려는 장소에 Refinery 가 이미 건설되어 있다면 dead
					// lock
					for (Unit u : Prebot.Game.getUnitsOnTile(testLocation)) {
						if (u.getType().isRefinery() && u.exists()) {
							hasAvailableGeyser = false;
							System.out.println("Build Order Dead lock case -> Refinery Building was built already at " + testLocation.getX() + ", " + testLocation.getY());
							break;
						}
					}
				}

				if (!hasAvailableGeyser) {
					isDeadlockCase = true;
				}
			}

			// 선행 기술 리서치가 되어있지 않고, 리서치 중이지도 않으면 dead lock
			if (!isDeadlockCase && requiredTechType != TechType.None) {
				if (!Prebot.Game.self().hasResearched(requiredTechType)) {
					if (!Prebot.Game.self().isResearching(requiredTechType)) {
						isDeadlockCase = true;
					}
				}
			}

			Iterator<UnitType> it = requiredUnits.keySet().iterator();
			// 선행 건물/유닛이 있는데
			if (!isDeadlockCase && requiredUnits.size() > 0) {
				// for (Unit u : it)
				while (it.hasNext()) {
					UnitType requiredUnitType = it.next(); // C++ :
															// u.first;

					if (requiredUnitType != UnitType.None) {

						/*
						 * std::cout + "pre requiredUnitType " + requiredUnitType.getName() + " completedUnitCount " + MyBotModule.Broodwar.self().
						 * completedUnitCount(requiredUnitType) + " incompleteUnitCount " + MyBotModule.Broodwar.self(). incompleteUnitCount(requiredUnitType) + std::endl;
						 */

						// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
						// Zerg_Mutalisk 나 Zerg_Scourge 를 만들려고하는데 Zerg_Greater_Spire 만 있는 경우 deadlock 으로 판정하는 버그 수정

						// 만들려는 유닛이 Zerg_Mutalisk 이거나 Zerg_Scourge 이고, 선행 유닛이 Zerg_Spire 인 경우, Zerg_Greater_Spire 가 있으면 dead lock 이 아니다
						if ((unitType == UnitType.Zerg_Mutalisk || unitType == UnitType.Zerg_Scourge) && requiredUnitType == UnitType.Zerg_Spire
								&& Prebot.Game.self().allUnitCount(UnitType.Zerg_Greater_Spire) > 0) {
							isDeadlockCase = false;
						} else

						// BasicBot 1.1 Patch End //////////////////////////////////////////////////

						// 선행 건물 / 유닛이 존재하지 않고, 생산 중이지도 않고
						if (Prebot.Game.self().completedUnitCount(requiredUnitType) == 0 && Prebot.Game.self().incompleteUnitCount(requiredUnitType) == 0) {
							// 선행 건물이 건설 예정이지도 않으면 dead lock
							if (requiredUnitType.isBuilding()) {
								if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
									isDeadlockCase = true;
								}
							}
							// 선행 유닛이 Larva 인 Zerg 유닛의 경우, Larva,
							// Hatchery, Lair, Hive 가 하나도 존재하지 않고, 건설
							// 예정이지 않은 경우에 dead lock
							else if (requiredUnitType == UnitType.Zerg_Larva) {
								if (Prebot.Game.self().completedUnitCount(UnitType.Zerg_Hatchery) == 0
										&& Prebot.Game.self().incompleteUnitCount(UnitType.Zerg_Hatchery) == 0
										&& Prebot.Game.self().completedUnitCount(UnitType.Zerg_Lair) == 0
										&& Prebot.Game.self().incompleteUnitCount(UnitType.Zerg_Lair) == 0
										&& Prebot.Game.self().completedUnitCount(UnitType.Zerg_Hive) == 0
										&& Prebot.Game.self().incompleteUnitCount(UnitType.Zerg_Hive) == 0) {
									if (ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hatchery, null) == 0
											&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Lair, null) == 0
											&& ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Zerg_Hive, null) == 0) {
										isDeadlockCase = true;
									}
								}
							}
						}
					}
				}
			}

			// 건물이 아닌 지상/공중 유닛인 경우, 서플라이가 400 꽉 찼으면 dead lock
			if (!isDeadlockCase && !unitType.isBuilding() && Prebot.Game.self().supplyTotal() == 400
					&& Prebot.Game.self().supplyUsed() + unitType.supplyRequired() > 400) {
				isDeadlockCase = true;
			}

			// 건물이 아닌 지상/공중 유닛인데, 서플라이가 부족하면 dead lock 상황이 되긴 하지만,
			// 이 경우는 빌드를 취소하기보다는, StrategyManager 등에서 서플라이 빌드를 추가함으로써 풀도록 한다
			if (!isDeadlockCase && !unitType.isBuilding() && Prebot.Game.self().supplyUsed() + unitType.supplyRequired() > Prebot.Game.self().supplyTotal()) {
				// isDeadlockCase = true;
			}

		}
		// 테크의 경우, 해당 리서치를 이미 했거나, 이미 하고있거나, 리서치를 하는 건물 및 선행건물이 존재하지않고
		// 건설예정이지도 않으면 dead lock
		else if (currentItem.metaType.isTech()) {
			TechType techType = currentItem.metaType.getTechType();
			UnitType requiredUnitType = techType.requiredUnit();

			/*
			 * System.out.println("To research " + techType.toString() + ", hasResearched " + MyBotModule.Broodwar.self().hasResearched(techType) + ", isResearching " +
			 * MyBotModule.Broodwar.self().isResearching(techType) + ", producerType " + producerType.toString() + " completedUnitCount " +
			 * MyBotModule.Broodwar.self().completedUnitCount( producerType) + " incompleteUnitCount " + MyBotModule.Broodwar.self().incompleteUnitCount( producerType));
			 */

			if (Prebot.Game.self().hasResearched(techType) || Prebot.Game.self().isResearching(techType)) {
				isDeadlockCase = true;
			} else if (Prebot.Game.self().completedUnitCount(producerType) == 0 && Prebot.Game.self().incompleteUnitCount(producerType) == 0) {
				if (ConstructionManager.Instance().getConstructionQueueItemCount(producerType, null) == 0) {

					// 테크 리서치의 producerType이 Addon 건물인 경우, Addon 건물 건설이
					// 명령 내려졌지만 시작되기 직전에는 getUnits, completedUnitCount,
					// incompleteUnitCount 에서 확인할 수 없다
					// producerType의 producerType 건물에 의해 Addon 건물 건설의
					// 명령이 들어갔는지까지 확인해야 한다
					if (producerType.isAddon()) {

						boolean isAddonConstructing = false;

						UnitType producerTypeOfProducerType = producerType.whatBuilds().first;

						if (producerTypeOfProducerType != UnitType.None) {

							for (Unit unit : Prebot.Game.self().getUnits()) {
								if (unit == null)
									continue;
								if (unit.getType() != producerTypeOfProducerType) {
									continue;
								}

								// 모건물이 완성되어있고, 모건물이 해당 Addon 건물을 건설중인지
								// 확인한다
								if (unit.isCompleted() && unit.isConstructing() && unit.getBuildType() == producerType) {
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
				/*
				 * std::cout + "To research " + techType.getName() + ", requiredUnitType " + requiredUnitType.getName() + " completedUnitCount " +
				 * MyBotModule.Broodwar.self().completedUnitCount( requiredUnitType) + " incompleteUnitCount " + MyBotModule.Broodwar.self().incompleteUnitCount( requiredUnitType)
				 * + std::endl;
				 */

				if (Prebot.Game.self().completedUnitCount(requiredUnitType) == 0 && Prebot.Game.self().incompleteUnitCount(requiredUnitType) == 0) {
					if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
						isDeadlockCase = true;
					}
				}
			}
		}
		// 업그레이드의 경우, 해당 업그레이드를 이미 했거나, 이미 하고있거나, 업그레이드를 하는 건물 및 선행건물이
		// 존재하지도 않고 건설예정이지도 않으면 dead lock
		else if (currentItem.metaType.isUpgrade()) {
			UpgradeType upgradeType = currentItem.metaType.getUpgradeType();
			int maxLevel = Prebot.Game.self().getMaxUpgradeLevel(upgradeType);
			int currentLevel = Prebot.Game.self().getUpgradeLevel(upgradeType);
			UnitType requiredUnitType = upgradeType.whatsRequired();

			/*
			 * std::cout + "To upgrade " + upgradeType.getName() + ", maxLevel " + maxLevel + ", currentLevel " + currentLevel + ", isUpgrading " +
			 * MyBotModule.Broodwar.self().isUpgrading(upgradeType) + ", producerType " + producerType.getName() + " completedUnitCount " +
			 * MyBotModule.Broodwar.self().completedUnitCount( producerType) + " incompleteUnitCount " + MyBotModule.Broodwar.self().incompleteUnitCount( producerType) +
			 * ", requiredUnitType " + requiredUnitType.getName() + std::endl;
			 */

			if (currentLevel >= maxLevel || Prebot.Game.self().isUpgrading(upgradeType)) {
				isDeadlockCase = true;
			} else if (Prebot.Game.self().completedUnitCount(producerType) == 0 && Prebot.Game.self().incompleteUnitCount(producerType) == 0) {
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

							for (Unit unit : Prebot.Game.self().getUnits()) {
								if (unit == null)
									continue;
								if (unit.getType() != producerTypeOfProducerType) {
									continue;
								}
								// 모건물이 완성되어있고, 모건물이 해당 Addon 건물을 건설중인지
								// 확인한다
								if (unit.isCompleted() && unit.isConstructing() && unit.getBuildType() == producerType) {
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
				if (Prebot.Game.self().completedUnitCount(requiredUnitType) == 0 && Prebot.Game.self().incompleteUnitCount(requiredUnitType) == 0) {
					if (ConstructionManager.Instance().getConstructionQueueItemCount(requiredUnitType, null) == 0) {
						isDeadlockCase = true;
					}
				}
			}
		}

		if (!isDeadlockCase) {
			// producerID 를 지정했는데, 해당 ID 를 가진 유닛이 존재하지 않으면 dead lock
			if (currentItem.producerID != -1) {
				boolean isProducerAlive = false;
				for (Unit unit : Prebot.Game.self().getUnits()) {
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
			System.out.println("Build Order Dead lock case . remove BuildOrderItem " + currentItem.metaType.getName());
			buildQueue.removeCurrentItem();
		}
	}

};