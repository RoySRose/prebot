package prebot.micro.old.control;


import java.util.List;

import bwapi.Unit;
import prebot.micro.old.OldSquadOrder;
import prebot.strategy.UnitInfo;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(OldSquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
