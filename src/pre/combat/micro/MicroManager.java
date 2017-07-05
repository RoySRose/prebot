package pre.combat.micro;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.Chokepoint;
import pre.MapGrid;
import pre.combat.SquadOrder;
import pre.combat.SquadOrder.SqaudOrderType;
import pre.util.CommandUtil;

public abstract class MicroManager {
	private List<Unit> units = new ArrayList<>();

	public List<Unit> getUnits() {
		return units;
	}
	public void setUnits(List<Unit> units) {
		this.units = units;
	}

	protected SquadOrder order;
	protected Position squadCenter = null;
	protected int squadRange = 0;
	
	
	protected abstract void executeMicro(List<Unit> targets);
	
	public void execute(SquadOrder inputOrder) {
		// Nothing to do if we have no units.
		if (units.isEmpty()) {
			return;
		}

		order = inputOrder;
		
		// If we have no combat order (attack or defend), we're done.
		if (!order.isCombatOrder()) {
			return;
		}

		// Discover enemies within region of interest.
		List<Unit> nearbyEnemies = new ArrayList<>();
		
		MapGrid.Instance().getUnitsNear(nearbyEnemies, order.getPosition(), order.getRadius(), false, true);

		// For attack but not defense, also include enemies near our units.
		if (order.getType() == SqaudOrderType.ATTACK || order.getType() == SqaudOrderType.BATTLE) {
			for (Unit unit : units) {
				MapGrid.Instance().getUnitsNear(nearbyEnemies, unit.getPosition(), unit.getType().sightRange(), false, true);
			}
		}

		executeMicro(nearbyEnemies);
	}
	
	public void regroup(Position regroupPosition) {
//	    Position ourBasePosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
//	    int regroupDistanceFromBase = MapTools.Instance().getGroundDistance(regroupPosition, ourBasePosition);

		for (Unit unit : units) {
//	        int unitDistanceFromBase = MapTools.Instance().getGroundDistance(unit.getPosition(), ourBasePosition);

			// A unit in stay-home mode should stay home, not "regroup" away from home.
			// A unit whose retreat path is blocked by enemies should do something else, at least attack-move.
//			if (unitDistanceFromBase > regroupDistanceFromBase) {
//	            UnitUtils.move(unit, ourBasePosition); } else
	        if (unit.getDistance(regroupPosition) > 96) {
				CommandUtil.move(unit, regroupPosition);
			} else {
				CommandUtil.attackMove(unit, unit.getPosition());
			}
		}
	}
	
	public void setSquadAreaRange(Position squadCenter, int squadRange) {
		this.squadCenter = squadCenter;
		this.squadRange = squadRange;
	}
	
	public boolean awayFromChokePoint(Unit unit) {
		if (order.getType() == SqaudOrderType.BATTLE) {
			return false;
		}
		
		boolean nearChokepoint = false;
		boolean nearChokePointIsOrderPosition = false;
        for (Chokepoint choke : BWTA.getChokepoints()) {
            if (choke.getCenter().getDistance(unit.getPosition()) < 64) {
            	nearChokepoint = true;
                if (choke.getDistance(order.getPosition()) < 64) {
                	nearChokePointIsOrderPosition = true;
                }
                break;
            }
        }
		
        if (nearChokepoint) {
        	if (nearChokePointIsOrderPosition) {
        		CommandUtil.move(unit, BWTA.getRegion(order.getPosition()).getCenter());
        	} else {
	        	CommandUtil.move(unit, order.getPosition());
        	}
        	return true;
        }
        
        return false;
	}
	
	public boolean inUnityThereIsStrength(Unit unit) {
		if (order.getType() != SqaudOrderType.ATTACK) {
			return false;
		}
		
		if (unit.getDistance(squadCenter) > squadRange - 100) {
        	CommandUtil.move(unit, squadCenter);
        	return true;
		}
		
		return false;
	}
	
}
