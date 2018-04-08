package prebot.manager.information;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import bwapi.Unit;
import bwapi.UnitType;
import prebot.main.PreBot;

public class UnitData {

	/// Unit 과 UnitInfo 를 Map 형태로 저장하는 자료구조 <br>
	/// C++ 에서는 Unit 포인터를 Key 로 사용하지만, <br>
	/// JAVA 에서는 Unit 자료구조의 equals 메쏘드 때문에 오작동하므로 Unit.getID() 값을 Key 로 사용함
	Map<Integer, UnitInfo> unitAndUnitInfoMap = new HashMap<Integer, UnitInfo>();

	/// UnitType별 파괴/사망한 유닛 숫자 누적값<br>
	/// C++ 에서는 UnitType 의 열거형 값을 Key 로 사용하지만, <br>
	/// JAVA 에서는 UnitType 의 열거형 값이 부재하므로 Unit.getType() 값을 Key 로 사용함
	Map<String, Integer> numDeadUnits = new HashMap<String, Integer>();

	/// UnitType별 건설/훈련했던 유닛 숫자 누적값<br>
	/// C++ 에서는 UnitType 의 열거형 값을 Key 로 사용하지만, <br>
	/// JAVA 에서는 UnitType 의 열거형 값이 부재하므로 Unit.getType() 값을 Key 로 사용함
	Map<String, Integer> numCreatedUnits = new HashMap<String, Integer>();

	/// UnitType별 존재하는 유닛 숫자 카운트. 적군 유닛의 경우 식별된 유닛 숫자 카운트<br>
	/// C++ 에서는 UnitType 의 열거형 값을 Key 로 사용하지만, <br>
	/// JAVA 에서는 UnitType 의 열거형 값이 부재하므로 Unit.getType() 값을 Key 로 사용함
	Map<String, Integer> numUnits = new HashMap<String, Integer>();

	/// 사망한 유닛을 생산하는데 소요되었던 Mineral 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임)
	private int mineralsLost = 0;
	/// 사망한 유닛을 생산하는데 소요되었던 Gas 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임)
	private int gasLost = 0;

	/// unitAndUnitInfoMap 에서 제거해야할 데이터들
	Vector<Integer> badUnitstoRemove = new Vector<Integer>();

	public UnitData() {
		/*
		 * int maxTypeID = 220; for (final UnitType t : UnitType.allUnitTypes()) { maxTypeID = maxTypeID > t.getID() ? maxTypeID : t.getID(); }
		 * 
		 * Map<String,Integer> numDeadUnits = new HashMap<String,Integer>(); Map<String,Integer> numCreatedUnits = new HashMap<String,Integer>();
		 * Map<String,Integer> numUnits = new HashMap<String,Integer>();
		 */
	}

	/// 유닛의 상태정보를 업데이트합니다
	public void updateUnitInfo(Unit unit) {
		if (unit == null) {
			return;
		}

		boolean firstSeen = false;
		if (!unitAndUnitInfoMap.containsKey(unit.getID())) {
			firstSeen = true;
			unitAndUnitInfoMap.put(unit.getID(), new UnitInfo());
		}
		
		UnitInfo ui = unitAndUnitInfoMap.get(unit.getID());
		
		// TODO 살려야 하는것인지 prebot 소스 확인필요
//		if (ui.unitID == unit.getID() && unit.getType() != ui.type) {
//			if (ui.type != UnitType.None) {
//				removeUnitbyMorph(unit, ui.type);
//				firstSeen = true;
//				unitAndUnitInfoMap.put(unit.getID(), new UnitInfo());
//			}
//		}

		ui.unit = unit;
		ui.unitID = unit.getID();
		ui.player = unit.getPlayer();
		ui.type = unit.getType();
		ui.lastPosition = unit.getPosition();
		ui.completed = unit.isCompleted();
		
		int updateFrameGap = PreBot.Broodwar.getFrameCount() - ui.updateFrame;
		if (updateFrameGap > 0) {
			if (updateFrameGap == 1) {
				ui.hitPointsReduced = ui.lastHitPoints - unit.getHitPoints();
				if (ui.hitPointsReduced < 0) {
					ui.hitPointsReduced = 0;
				}
				ui.shieldsReduced = ui.lastShields - unit.getShields();
				if (ui.shieldsReduced < 0) {
					ui.shieldsReduced = 0;
				}
			} else {
				ui.hitPointsReduced = 0;
				ui.shieldsReduced = 0;
			}
		}
		ui.lastHitPoints = unit.getHitPoints();
		ui.lastShields = unit.getShields();
		ui.updateFrame = PreBot.Broodwar.getFrameCount();

		if (firstSeen) {
			// TODO prebot 소스에서는 아래 부분 주석처리됨 
			if (!numCreatedUnits.containsKey(unit.getType().toString())) {
				numCreatedUnits.put(unit.getType().toString(), 1);
			} else {
				numCreatedUnits.put(unit.getType().toString(), numCreatedUnits.get(unit.getType().toString()) + 1);
			}
			if (!numUnits.containsKey(unit.getType().toString())) {
				numUnits.put(unit.getType().toString(), 1);
			} else {
				numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) + 1);
			}
			// numCreatedUnits[unit.getType().getID()]++;
			// numUnits[unit.getType().getID()]++;
		}
	}

	/// 파괴/사망한 유닛을 자료구조에서 제거합니다
	public void removeUnit(Unit unit) {
		if (unit == null) {
			return;
		}

		mineralsLost += unit.getType().mineralPrice();
		gasLost += unit.getType().gasPrice();
		if (numUnits.get(unit.getType().toString()) == 1) {
			numUnits.remove(unit.getType().toString());
		} else {
			numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) - 1);
		}
		if (!numDeadUnits.containsKey(unit.getType().toString())) {
			numDeadUnits.put(unit.getType().toString(), 1);
		} else {
			numDeadUnits.put(unit.getType().toString(), numDeadUnits.get(unit.getType().toString()) + 1);
		}
		// numUnits[unit.getType().getID()]--;
		// numDeadUnits[unit.getType().getID()]++;

		unitAndUnitInfoMap.remove(unit.getID());
	}

	/// 포인터가 null 이 되었거나, 파괴되어 Resource_Vespene_Geyser로 돌아간 Refinery, 예전에는 건물이 있었던 걸로 저장해두었는데 지금은 파괴되어 없어진 건물 (특히, 테란의 경우 불타서 소멸한 건물) 데이터를 제거합니다
	public void removeBadUnits() {
		Iterator<Integer> it = unitAndUnitInfoMap.keySet().iterator();

		while (it.hasNext()) {
			UnitInfo ui = unitAndUnitInfoMap.get(it.next());
			if (isBadUnitInfo(ui)) {
				Unit unit = ui.unit;
				if (numUnits.get(unit.getType().toString()) != null) {
					numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) - 1);
				}

				badUnitstoRemove.add(unit.getID());
			}
		}

		if (badUnitstoRemove.size() > 0) {
			for (Integer i : badUnitstoRemove) {
				unitAndUnitInfoMap.remove(i);
			}
			badUnitstoRemove.clear();
		}
	}

	public final boolean isBadUnitInfo(final UnitInfo ui) {
		if (ui.unit == null) {
			return false;
		}

		// Cull away any refineries/assimilators/extractors that were destroyed and reverted to vespene geysers
		if (ui.unit.getType() == UnitType.Resource_Vespene_Geyser) {
			return true;
		}

		// If the unit is a building and we can currently see its position and it is not there
		if (ui.type.isBuilding() && PreBot.Broodwar.isVisible(ui.lastPosition.getX() / 32, ui.lastPosition.getY() / 32)
				&& !ui.unit.isVisible()) {
			return true;
		}

		return false;
	}

	/// 사망한 유닛을 생산하는데 소요되었던 Gas 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임)
	public final int getGasLost() {
		return gasLost;
	}

	/// 사망한 유닛을 생산하는데 소요되었던 Mineral 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임) 을 리턴합니다
	public final int getMineralsLost() {
		return mineralsLost;
	}

	/// 해당 UnitType 의 식별된 Unit 숫자를 리턴합니다
	public final int getNumUnits(String t) {
		if (numUnits.get(t.toString()) != null) {
			return numUnits.get(t.toString());
		} else {
			return 0;
		}
	}

	/// 해당 UnitType 의 식별된 Unit 파괴/사망 누적값을 리턴합니다
	public final int getNumDeadUnits(String t) {
		if (numDeadUnits.get(t.toString()) != null) {
			return numDeadUnits.get(t.toString());
		} else {
			return 0;
		}
	}

	/// 해당 UnitType 의 식별된 Unit 건설/훈련 누적값을 리턴합니다
	public final int getNumCreatedUnits(String t) {
		if (numCreatedUnits.get(t.toString()) != null) {
			return numCreatedUnits.get(t.toString());
		} else {
			return 0;
		}
	}

	public final Map<Integer, UnitInfo> getUnitAndUnitInfoMap() {
		return unitAndUnitInfoMap;
	}

	public Map<String, Integer> getNumDeadUnits() {
		return numDeadUnits;
	}

	public Map<String, Integer> getNumCreatedUnits() {
		return numCreatedUnits;
	}

	public Map<String, Integer> getNumUnits() {
		return numUnits;
	}
}