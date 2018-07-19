package prebot.build.provider.items.tech;

import bwapi.TechType;
import bwapi.UpgradeType;
import prebot.build.prebot1.BuildManager;
import prebot.build.provider.DefaultBuildableItem;
import prebot.build.provider.ResearchSelector;
import prebot.build.provider.UpgradeSelector;
import prebot.common.MetaType;
import prebot.common.main.Prebot;

//Wraith Mana
public class BuilderApolloReactor extends DefaultBuildableItem {

  //ResearchSelector researchSelector;

  public BuilderApolloReactor(MetaType metaType){
      super(metaType);
  }

  public final boolean buildCondition(){
  	boolean ApolloReactor = false;
  	
  	ApolloReactor = (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Apollo_Reactor) == 1 ? true : false)
				|| (Prebot.Broodwar.self().isUpgrading(UpgradeType.Apollo_Reactor) ? true : false);
  	
  	if(!ApolloReactor) {
  		return false;
  	}
  	
//		boolean VM = (Prebot.Broodwar.self().hasResearched(TechType.Spider_Mines)) || (Prebot.Broodwar.self().isResearching(TechType.Spider_Mines));
//		boolean TS = (Prebot.Broodwar.self().hasResearched(TechType.Tank_Siege_Mode)) || (Prebot.Broodwar.self().isResearching(TechType.Tank_Siege_Mode)) ;
//		boolean GR = (Prebot.Broodwar.self().getUpgradeLevel(UpgradeType.Charon_Boosters) == 1 ? true : false)
//				|| (Prebot.Broodwar.self().isUpgrading(UpgradeType.Charon_Boosters) ? true : false);
  	
		boolean WraithCloacking = false;
		WraithCloacking = (Prebot.Broodwar.self().hasResearched(TechType.Cloaking_Field)) || (Prebot.Broodwar.self().isResearching(TechType.Cloaking_Field));
		
  	if(WraithCloacking) {
  		if(BuildManager.Instance().buildQueue.getItemCount(TechType.Cloaking_Field) == 0) {
  			return true;
  		}
  	}

      return false;
  }
}
