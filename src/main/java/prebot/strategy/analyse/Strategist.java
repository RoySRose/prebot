package prebot.strategy.analyse;

import prebot.strategy.analyse.Clue.ClueInfo;
import prebot.strategy.analyse.Clue.ClueType;
import prebot.strategy.constant.EnemyStrategy;
import prebot.strategy.manage.ClueManager;

public abstract class Strategist {

	public abstract EnemyStrategy strategyToApply();
	
	public boolean hasInfo(ClueInfo info) {
		return ClueManager.Instance().containsClueInfo(info);
	}
	
	public boolean hasType(ClueType type) {
		return ClueManager.Instance().containsClueType(type);
	}
	
	public boolean hasAnyInfo(ClueInfo... infos) {
		for (ClueInfo info : infos) {
			if (hasInfo(info)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAllInfo(ClueInfo... infos) {
		for (ClueInfo info : infos) {
			if (!hasInfo(info)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean hasAnyType(ClueType... types) {
		for (ClueType type : types) {
			if (hasType(type)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasAllType(ClueType... types) {
		for (ClueType type : types) {
			if (!hasType(type)) {
				return false;
			}
		}
		return true;
	}
}
