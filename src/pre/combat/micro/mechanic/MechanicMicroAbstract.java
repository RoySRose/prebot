package pre.combat.micro.mechanic;

import java.util.List;

import bwapi.Unit;
import pre.UnitInfo;
import pre.combat.SquadOrder;

public abstract class MechanicMicroAbstract {
	abstract public void prepareMechanic(SquadOrder order, List<UnitInfo> enemiesInfo);
	abstract public void executeMechanicMicro(Unit unit);
}
