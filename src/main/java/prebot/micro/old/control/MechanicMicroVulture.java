package prebot.micro.old.control;


import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import prebot.common.LagObserver;
import prebot.common.main.Prebot;
import prebot.common.util.CommandUtils;
import prebot.common.util.TimeUtils;
import prebot.micro.constant.MicroConfig;
import prebot.micro.constant.MicroCode.CombatStrategyDetail;
import prebot.micro.constant.MicroCode.SquadOrderType;
import prebot.micro.old.OldMicroUtils;
import prebot.micro.old.OldCombatManager;
import prebot.micro.old.OldKitingOption;
import prebot.micro.old.OldSquadOrder;
import prebot.strategy.InformationManager;
import prebot.strategy.SpiderMineManger;
import prebot.strategy.UnitInfo;
import prebot.strategy.VultureTravelManager;

public class MechanicMicroVulture extends MechanicMicroAbstract {

	private OldSquadOrder order = null;
	private List<UnitInfo> enemiesInfo = new ArrayList<>();
	
	private List<Unit> notVultureUnitList = new ArrayList<>();
	private int saveUnitLevel = 1;
	
	private boolean attackWithMechanics = false;
	private int stickToMechanicRadius = 0;
	
	public void prepareMechanic(OldSquadOrder order, List<UnitInfo> enemiesInfo) {
		this.order = order;
		this.enemiesInfo = OldMicroUtils.filterTargetInfos(enemiesInfo, false);
	}
	
	public void prepareMechanicAdditional(List<Unit> vultureList, List<Unit> tankList, List<Unit> goliathList, int saveUnitLevel, boolean attackWithMechanics) {
		this.notVultureUnitList.clear();
		this.notVultureUnitList.addAll(tankList);
		this.notVultureUnitList.addAll(goliathList);
		this.saveUnitLevel = saveUnitLevel;
		this.attackWithMechanics = attackWithMechanics && notVultureUnitList.size() > 0;
		if (this.attackWithMechanics) {
			this.stickToMechanicRadius = 120 + (int) (Math.log(vultureList.size()) * 15);
			if (saveUnitLevel == 0) {
				this.stickToMechanicRadius += 100;
			}
		}
	}
	
	public void executeMechanicMicro(Unit vulture) {
		if (!TimeUtils.executeUnitRotation(vulture, LagObserver.groupsize())) {
			return;
		}
		
		MechanicMicroDecision decision = MechanicMicroDecision.makeDecision(vulture, enemiesInfo, null, saveUnitLevel); // 0: flee, 1: kiting, 2: attack

		OldKitingOption kOpt = OldKitingOption.vultureKitingOption();
		Position retreatPosition = order.getPosition();
		switch (decision.getDecision()) {
		case 0: // flee 아예 싸울 생각이 없는 도망 : 성큰, 캐논, 시즈 등 접근금지, 또는 regroup 시
			if (order.getType() == SquadOrderType.WATCH || order.getType() == SquadOrderType.CHECK) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			}
			kOpt.setGoalPosition(retreatPosition);
			OldMicroUtils.preciseFlee(vulture, decision.getEnemyPosition(), kOpt);
			break;
			
		case 1: // kiting
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			if (order.getType() == SquadOrderType.WATCH) {
				BaseLocation myBase = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
				if (myBase != null) {
					retreatPosition = myBase.getPosition();
				}
			} else if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					retreatPosition = travelBase.getPosition();
				}
			}

			boolean haveToFight = true;
			Unit closeMechanic = null;
			if (attackWithMechanics) {
				haveToFight = false;
				int closeDist = 9999999;
				for (Unit mechanicUnit : notVultureUnitList) {
					int dist = vulture.getDistance(mechanicUnit.getPosition());
					if (dist < closeDist) {
						closeMechanic = mechanicUnit;
						closeDist = dist;
						// 가까운 곳에 메카닉유닛이 있으면 싸운다.
						if (closeDist < stickToMechanicRadius) {
							haveToFight = true;
							break;
						}
					}
				}
			} else {
				for (Unit notVultureUnit : notVultureUnitList) {
					if (vulture.getDistance(notVultureUnit) < MicroConfig.Common.MAIN_SQUAD_COVERAGE) {
						kOpt = OldKitingOption.defaultKitingOption();
						retreatPosition = notVultureUnit.getPosition();
						break;
					}
				}
			}

			if (haveToFight) {
				Unit enemy = OldMicroUtils.getUnitIfVisible(decision.getTargetInfo());
				if (enemy != null && enemy.getType() == UnitType.Terran_Vulture_Spider_Mine && vulture.isInWeaponRange(enemy)) {
					vulture.holdPosition();
				} else {
					kOpt.setGoalPosition(retreatPosition);
					OldMicroUtils.preciseKiting(vulture, decision.getTargetInfo(), kOpt);
				}
			} else {
				CommandUtils.move(vulture, closeMechanic.getPosition());
			}
			break;
			
		case 2: // attack move
			if (useReservedSpiderMine(vulture) || reserveSpiderMine(vulture) || removeSpiderMine(vulture)) {
				break;
			}
			
			// checker : 각각의 목표지역(travelBase)으로 이동. (order position은 null이다.)
			// watcher : 목표지역(적base)으로 이동. 앞에 보이지 않는 적이 있으면 본진base로 후퇴.
			Position movePosition = order.getPosition();
			if (order.getType() == SquadOrderType.CHECK) {
				BaseLocation travelBase = VultureTravelManager.Instance().getBestTravelSite(vulture.getID());
				if (travelBase != null) {
					movePosition = travelBase.getPosition();
				}
			}
			
			if (MicroConfig.Common.versusMechanicSet()) {
				// 테란전용 go
				int distToOrder = vulture.getDistance(movePosition);
				if (distToOrder <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE + 50) { // orderPosition의 둘러싼 대형을 만든다.
					if (vulture.isIdle() || vulture.isBraking()) {
						if (!vulture.isBeingHealed()) {
							Position randomPosition = OldMicroUtils.randomPosition(vulture.getPosition(), 100);
							CommandUtils.attackMove(vulture, randomPosition);
						}
					}
				} else {
					CommandUtils.attackMove(vulture, movePosition);
				}
				
			} else {
				// 이동지역까지 attackMove로 간다.
				if (vulture.getDistance(movePosition) > order.getRadius()) {
//					if (saveUnit) {
						CommandUtils.move(vulture, movePosition);
//					} else {
//						CommandUtil.attackMove(vulture, movePosition);
//					}
					
				} else { // 목적지 도착
					if (vulture.isIdle() || vulture.isBraking()) {
						Position randomPosition = OldMicroUtils.randomPosition(vulture.getPosition(), 100);
						CommandUtils.attackMove(vulture, randomPosition);
					}
				}
			}
			break;
		}
	}
	
	private boolean useReservedSpiderMine(Unit vulture) {
		// 마인매설이 예약된 벌처라면 매설실행
		Position positionToMine = SpiderMineManger.Instance().getPositionReserved(vulture);
		if (positionToMine != null) {
			CommandUtils.useTechPosition(vulture, TechType.Spider_Mines, positionToMine);
			return true;
		}
		return false;
	}
	
	private boolean reserveSpiderMine(Unit vulture) {
		
		Position minePosition = null;
		
		if (OldCombatManager.Instance().getDetailStrategyFrame(CombatStrategyDetail.MINE_STRATEGY_FOR_TERRAN) > 0) {
			BaseLocation enemyFirstExpansion = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
			if (enemyFirstExpansion != null) {
				int distance = vulture.getDistance(enemyFirstExpansion.getPosition());
				if (distance < MicroConfig.Tank.SIEGE_MODE_MAX_RANGE && OldMicroUtils.isSafePlace(enemyFirstExpansion.getPosition())) {
					minePosition = SpiderMineManger.Instance().positionToMine(vulture, enemyFirstExpansion.getPosition(), true, MicroConfig.Vulture.spiderMineNumPerPosition * 2);
				}
			}
			
			if (minePosition == null) {
				Position enemyReadyPos = InformationManager.Instance().getReadyToAttackPosition(InformationManager.Instance().enemyPlayer);
				
				int distance = vulture.getDistance(enemyReadyPos);
				if (distance <= MicroConfig.Tank.SIEGE_MODE_MAX_RANGE) {
					minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroConfig.Vulture.spiderMineNumPerPosition);
				}
			}
			
		} else {
			minePosition = SpiderMineManger.Instance().goodPositionToMine(vulture, MicroConfig.Vulture.spiderMineNumPerGoodPosition);
			
			if (minePosition == null && order.getType() == SquadOrderType.WATCH) {
//				// 적 유닛에게 마인 선물하기
//				if (InformationManager.Instance().enemyRace == Race.Terran && saveUnitLevel == 0) {
//					minePosition = SpiderMineManger.Instance().enemyPositionToMine(vulture, enemiesInfo);
//				}
				int mineCount = Prebot.Broodwar.self().allUnitCount(UnitType.Terran_Vulture_Spider_Mine);
				if (mineCount <= MicroConfig.Vulture.MINE_MAX_NUM) {
					// 맵 구석구석 마인 심기
					Region vultureRegion = BWTA.getRegion(vulture.getPosition());
					BaseLocation base = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer);
					List<BaseLocation> occupiedBases = InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().selfPlayer);

					int minePrepareLevel = SpiderMineManger.Instance().getMineInMyBaseLevel(); // 0: 본진매설X, 점령지역조금, 1: 본진매설X, 점령지역많이, 2: 본진조금, 점령지역많이
					boolean vultureInMyBaseRegion = vultureRegion == BWTA.getRegion(base.getPosition());
					if (!vultureInMyBaseRegion || minePrepareLevel >= 2) { // 본진 region에는 마인 설치안함(단 패스트 다크, 패스트 럴커 등인 경우 매설)
						boolean occupiedRegion = false;
						for (BaseLocation occupiedBase : occupiedBases) { // 앞마당 포함한 점령지역에 마인을 적게 매설함(단, 히드라웨이브, 드라군 푸시인 경우 많이 매설)
							if (vultureRegion == BWTA.getRegion(occupiedBase.getPosition())) {
								occupiedRegion = true;
								break;
							}
						}
						if (!occupiedRegion || (!vultureInMyBaseRegion && minePrepareLevel >= 1)) {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroConfig.Vulture.spiderMineNumPerPosition); // 그외에는 좀 많이
						} else {
							minePosition = SpiderMineManger.Instance().positionToMine(vulture, vulture.getPosition(), false, MicroConfig.Vulture.spiderMineNumPerGoodPosition);
						}
					}
				}
			}
		}
		if (minePosition != null) { // 매설할 마인이 있다면 종료
			return true;
		}
		return false;
	}
	
	private boolean removeSpiderMine(Unit vulture) {
		return SpiderMineManger.Instance().removeMine(vulture);
	}

}
