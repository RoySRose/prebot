package prebot.micro.control;

import java.util.List;

import bwapi.Unit;
import prebot.information.UnitInfo;

public abstract class Control {
	public List<Unit> unitList;
	public List<UnitInfo> euiList;
	
	public void prepare(List<Unit> unitList, List<UnitInfo> euiList) {
		this.unitList = unitList;
		this.euiList = euiList;
	}
	
	public abstract void control();
}
