package prebot.manager.build;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;
import prebot.manager.build.BuildOrderItem.SeedPositionStrategy;

/// 빌드 오더 목록 자료구조 class
public class BuildOrderQueue {

	private int highestPriority;
	private int lowestPriority;
	private int defaultPrioritySpacing;

	/// iteration 을 하기 위한 참고값<br>
	/// highest priority 인 BuildOrderItem 으로부터 몇개나 skip 했는가.
	private int numSkippedItems;

	/// BuildOrderItem 들을 Double Ended Queue 자료구조로 관리합니다<br>
	/// lowest priority 인 BuildOrderItem은 front 에, highest priority 인 BuildOrderItem 은 back 에 위치하게 합니다
	private Deque<BuildOrderItem> queue = new ArrayDeque<BuildOrderItem>();

	public BuildOrderQueue() {
		highestPriority = 0;
		lowestPriority = 0;
		defaultPrioritySpacing = 10;
		numSkippedItems = 0;
	}

	/// clears the entire build order queue
	public void clearAll() {
		// clear the queue
		queue.clear();

		// reset the priorities
		highestPriority = 0;
		lowestPriority = 0;
	}

	/// returns the highest priority item
	public BuildOrderItem getHighestItem() {
		// reset the number of skipped items to zero
		numSkippedItems = 0;

		// the queue will be sorted with the highest priority at the back
		// C 에서는 highest 가 back 에 있지만, JAVA 에서는 highest 가 fist 에 있다
		return queue.getFirst(); // queue.back(); C++
	}

	/// returns the highest priority item
	public BuildOrderItem getNextItem() {
		// the queue will be sorted with the highest priority at the back
		Object[] tempArr = queue.toArray();
		// return (BuildOrderItem)tempArr[queue.size() - 1 - numSkippedItems];
		return (BuildOrderItem) tempArr[numSkippedItems];
	}

	public int getItemCount(UnitType unitType) {
		return getItemCount(new MetaType(unitType), null);
	}

	public int getItemCount(TechType techType) {
		return getItemCount(new MetaType(techType), null);
	}

	public int getItemCount(UpgradeType upgradeType) {
		return getItemCount(new MetaType(upgradeType), null);
	}

	public int getItemCount(UnitType unitType, TilePosition queryTilePosition) {
		return getItemCount(new MetaType(unitType), queryTilePosition);
	}

	/// BuildOrderQueue에 해당 type 의 Item 이 몇 개 존재하는지 리턴한다. queryTilePosition 을 입력한 경우, 건물에 대해서 추가 탐색한다
	private int getItemCount(MetaType queryType, TilePosition queryTilePosition) {
		// queryTilePosition 을 입력한 경우, 거리의 maxRange. 타일단위
		int maxRange = 16;
		int itemCount = 0;
		Object[] tempArr = queue.toArray();

		// for each unit in the queue
		for (int i = 0; i < queue.size(); i++) {

			final MetaType item = ((BuildOrderItem) tempArr[queue.size() - 1 - i]).metaType;
			TilePosition itemPosition = ((BuildOrderItem) tempArr[queue.size() - 1 - i]).seedLocation;
			Point seedPositionPoint = null;
			if (queryTilePosition != null) {
				seedPositionPoint = new Point(queryTilePosition.getX(), queryTilePosition.getY());
			} else {
				queryTilePosition = TilePosition.None;
			}

			if (queryType.isUnit() && item.isUnit()) {
				if (item.getUnitType() == queryType.getUnitType()) {
					if (queryType.getUnitType().isBuilding() && queryTilePosition != TilePosition.None) {
						if (itemPosition.getDistance(new TilePosition((int) seedPositionPoint.getX(), (int) seedPositionPoint.getY())) <= maxRange) {
							itemCount++;
						}
					} else {
						itemCount++;
					}
				}
			} else if (queryType.isTech() && item.isTech()) {
				if (item.getTechType() == queryType.getTechType()) {
					itemCount++;
				}
			} else if (queryType.isUpgrade() && item.isUpgrade()) {
				if (item.getUpgradeType() == queryType.getUpgradeType()) {
					itemCount++;
				}
			}
		}
		return itemCount;
	}

	/// increments skippedItems
	public void skipCurrentItem() {
		// make sure we can skip
		if (canSkipCurrentItem()) {
			// skip it
			numSkippedItems++;
		}
	}

	public boolean canSkipCurrentItem() {
		// does the queue have more elements
		boolean bigEnough = queue.size() > (int) (1 + numSkippedItems);

		if (!bigEnough) {
			return false;
		}

		// is the current highest priority item not blocking a skip
		Object[] tempArr = queue.toArray();
		// boolean highestNotBlocking = !((BuildOrderItem)tempArr[queue.size() - 1 - numSkippedItems]).blocking;
		boolean highestNotBlocking = !((BuildOrderItem) tempArr[numSkippedItems]).blocking;

		// this tells us if we can skip
		return highestNotBlocking;
	}
	
	public void qHigh(UnitType unitType, boolean blocking) {
		qHigh(new MetaType(unitType), blocking);
	}

	public void qHigh(TechType techType, boolean blocking) {
		qHigh(new MetaType(techType), blocking);
	}

	public void qHigh(UpgradeType upgradeType, boolean blocking) {
		qHigh(new MetaType(upgradeType), blocking);
	}
	
	public void qHigh(MetaType metaType, boolean blocking) {
		BuildOrderItem buildOrderItem = new BuildOrderItem(metaType, getHighestPriority(), blocking, -1);
		queueItem(buildOrderItem);
	}

	public void qHigh(UnitType unitType, SeedPositionStrategy seedPositionStrategy, boolean blocking) {
		BuildOrderItem buildOrderItem = new BuildOrderItem(new MetaType(unitType), seedPositionStrategy, getHighestPriority(), blocking, -1);
		queueItem(buildOrderItem);
	}
	
	public void qHigh(UnitType unitType, TilePosition seedPosition, boolean blocking) {
		BuildOrderItem buildOrderItem = new BuildOrderItem(new MetaType(unitType), seedPosition, getHighestPriority(), blocking, -1);
		queueItem(buildOrderItem);
	}
	
	public void qLow(UnitType unitType, boolean blocking) {
		qHigh(new MetaType(unitType), blocking);
	}

	public void qLow(TechType techType, boolean blocking) {
		qLow(new MetaType(techType), blocking);
	}

	public void qLow(UpgradeType upgradeType, boolean blocking) {
		qLow(new MetaType(upgradeType), blocking);
	}
	
	public void qLow(MetaType metaType, boolean blocking) {
		BuildOrderItem buildOrderItem = new BuildOrderItem(metaType, getLowestPriority(), blocking, -1);
		queueItem(buildOrderItem);
	}

	public void qLow(UnitType unitType, SeedPositionStrategy seedPositionStrategy, boolean blocking) {
		BuildOrderItem buildOrderItem = new BuildOrderItem(new MetaType(unitType), seedPositionStrategy, getLowestPriority(), blocking, -1);
		queueItem(buildOrderItem);
	}
	
	public void qLow(UnitType unitType, TilePosition seedPosition, boolean blocking) {
		BuildOrderItem buildOrderItem = new BuildOrderItem(new MetaType(unitType), seedPosition, getLowestPriority(), blocking, -1);
		queueItem(buildOrderItem);
	}
	
	private int getLowestPriority() {
		return 0;
	}

	private int getHighestPriority() {
		return highestPriority + defaultPrioritySpacing;
	}

	/// queues something with a given priority
	public void queueItem(BuildOrderItem b) {
		// if the queue is empty, set the highest and lowest priorities
		if (queue.isEmpty()) {
			highestPriority = b.priority;
			lowestPriority = b.priority;
		}

		// push the item into the queue
		if (b.priority <= lowestPriority) {
			queue.addLast(b); // C++ : queue.push_front(b);
		} else {
			queue.addFirst(b); // C++ : queue.push_back(b);
		}

		// if the item is somewhere in the middle, we have to sort again
		if ((queue.size() > 1) && (b.priority < highestPriority) && (b.priority > lowestPriority)) {
			// sort the list in ascending order, putting highest priority at the top
			// C++ std::sort(queue.begin(), queue.end());
			Object[] tempArr = queue.toArray();
			Arrays.sort(tempArr);
			queue.clear();
			for (int i = 0; i < tempArr.length; i++) {
				queue.add((BuildOrderItem) tempArr[i]);
			}
		}

		// update the highest or lowest if it is beaten
		highestPriority = (b.priority > highestPriority) ? b.priority : highestPriority;
		lowestPriority = (b.priority < lowestPriority) ? b.priority : lowestPriority;
	}


	/// removes the highest priority item
	public void removeHighestItem() {
		// remove the back element of the vector
		// queue.pop_back();
		queue.removeFirst();

		// if the list is not empty, set the highest accordingly
		// highestPriority = queue.isEmpty() ? 0 : queue.back().priority;
		highestPriority = queue.isEmpty() ? 0 : queue.getLast().priority;
		lowestPriority = queue.isEmpty() ? 0 : lowestPriority;
	}

	/// skippedItems 다음의 item 을 제거합니다
	public void removeCurrentItem() {
		// remove the back element of the vector
		// C++ : queue.erase(queue.begin() + queue.size() - 1 - numSkippedItems);

		Object[] tempArr = queue.toArray();
		BuildOrderItem currentItem = (BuildOrderItem) tempArr[numSkippedItems];
		// System.out.println("BuildOrderQueue currentItem to remove is " + currentItem.metaType.getName());
		queue.remove(currentItem);

		// assert((int)(queue.size()) < size);

		// if the list is not empty, set the highest accordingly
		// C++ : highestPriority = queue.isEmpty() ? 0 : queue.back().priority;
		highestPriority = queue.isEmpty() ? 0 : queue.getFirst().priority;
		lowestPriority = queue.isEmpty() ? 0 : lowestPriority;
	}

	/// returns the size of the queue
	public int size() {
		return queue.size();
	}

	public boolean isEmpty() {
		return (queue.size() == 0);
	}

	/// overload the bracket operator for ease of use
	public BuildOrderItem operator(int i) {
		Object[] tempArr = queue.toArray();
		return (BuildOrderItem) tempArr[i];
	}

	public Deque<BuildOrderItem> getQueue() {
		return queue;
	}
}