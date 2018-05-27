package prebot.micro.control;


import java.util.List;

import bwapi.Unit;
import prebot.micro.SquadOrder;
import prebot.strategy.UnitInfo;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
