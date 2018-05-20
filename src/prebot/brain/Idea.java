package prebot.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.UnitType;
import bwta.BaseLocation;
import prebot.brain.knowledge.Knowledge;
import prebot.brain.manager.StrategyManager;
import prebot.brain.stratgy.enemy.EnemyBuild;
import prebot.common.code.Code.CommonCode;
import prebot.common.code.Code.InitialBuildType;

public class Idea {
	public static Idea of() {
		return StrategyManager.Instance().getIdea();
	}

	// Strategy
	public List<Knowledge> newKnowledgeList = new ArrayList<>();
	public List<EnemyBuild> enemyBuildList = new ArrayList<>();

	
	// Build & Construction
	/**
	 * 초반빌드 타입
	 */
	public InitialBuildType initialBuildType = InitialBuildType.NONE;
	
	/**
	 * 초반빌드 종료 후 팩토리 유닛 비율
	 */
	public Map<UnitType, Integer> factoryUnitRatio = new HashMap<>();
	
	/**
	 * 다음 확장 지역
	 */
	public BaseLocation nextExpansionBase = null;
	
	/**
	 * 다음 확장 지역이 안전한지 여부.
	 * true가 아닌 경우, 확장을 늦추거나, 커맨드센터를 띄워서 옮기는 방식을 택하는 것이 좋다.
	 */
	public boolean nextExpansionBaseIsSafe = true;
	
	/**
	 * 다크 템플러가 존개할 수 없는 시간. (너무 빠르게 터렛 공사를 하지 않기 위함)
	 * 최초에는 최소 패스트다크템플러 대비 최소 frame이고, 정찰에 의해 점차 길어진다.
	 * 엔지니어링베이과 터렛빌드타임이 고려되어야 되어 대비가 필요하다. (다크템플러의 이동시간이 추가적으로 고려되어질 수 있다.)
	 * darkTemplarSafeTime > TimeUtils.elapsedFrames()이면 다크템플러가 존재할 수 없다.
	 */
	public int darkTemplarSafeFrame = CommonCode.NONE;
	
		
	
	
	// Worker
	public boolean gasAdjustment = false; // true인 경우 gasAdjustmentWorkerCount에 따른 가스조절 
	public int gasAdjustmentWorkerCount = 0;
	
	// Combat
	public int checkerMaxCount = 0;
	public boolean assignScvScout = false;
	
	public Position campPosition = null;
	public Position attackPosition = null;
	
	// Worker & Combat
	public BaseLocation enemeyBaseExpected = null;

}
