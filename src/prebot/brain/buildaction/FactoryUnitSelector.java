package prebot.brain.buildaction;

import bwapi.UnitType;
import prebot.common.code.Code.UnitFindRange;
import prebot.common.util.UnitUtils;

public class FactoryUnitSelector {
	
	public static int rateVulture;
	public static int rateTank;
	public static int rateGoliath;
	
	
	public static UnitType factoryUnitSelect() {
		int vultureCount = UnitUtils.getUnitCount(UnitType.Terran_Vulture, UnitFindRange.ALL);
		int tankCount = UnitUtils.getUnitCount(UnitType.Terran_Siege_Tank_Tank_Mode, UnitFindRange.ALL) + UnitUtils.getUnitCount(UnitType.Terran_Siege_Tank_Siege_Mode, UnitFindRange.ALL);
		int goliathCount = UnitUtils.getUnitCount(UnitType.Terran_Goliath, UnitFindRange.ALL);
		
		
		return UnitType.Terran_Vulture;
	}
	
	

}
