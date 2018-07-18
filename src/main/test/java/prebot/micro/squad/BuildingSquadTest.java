package prebot.micro.squad;

import bwapi.Game;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import prebot.common.main.Prebot;
import prebot.common.util.UnitUtils;
import prebot.micro.control.BuildingFly;
import prebot.micro.control.building.BarracksControl;
import prebot.micro.control.building.CommandCenterControl;
import prebot.micro.control.building.ComsatControl;
import prebot.micro.control.building.EngineeringBayControl;
import prebot.micro.control.building.ScienceFacilityControl;
import prebot.strategy.InformationManager;
import prebot.strategy.UnitInfo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@RunWith(MockitoJUnitRunner.class)
public class BuildingSquadTest {



    @org.junit.Before
    public void setUp() throws Exception {
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    private BarracksControl barracksControl = new BarracksControl();
    private EngineeringBayControl engineeringBayControl = new EngineeringBayControl();
    private CommandCenterControl commandCenterControl = new CommandCenterControl();
    private ScienceFacilityControl scienceFacilityControl = new ScienceFacilityControl();
    private ComsatControl comsatControl = new ComsatControl();


    @Test
    public void executeFlyTest_EngineeringBay(){

        Unit unit = Mockito.mock(Unit.class);

        List<Unit> engineeringBayList = new ArrayList<>();
        engineeringBayList.add(unit);

        engineeringBayControl.setDefaultBuildingFly(engineeringBayList);
        assertEquals(engineeringBayControl.getBuildingFly(),BuildingFly.UP);
        engineeringBayControl.control(engineeringBayList, null);
        assertEquals(engineeringBayControl.getBuildingFly(),BuildingFly.UP);
        //assertNotNull(engineeringBayControl.getFlyPosition());

    }
    @Test
    public void executeFlyTest_Barrack(){
        Unit unit = Mockito.mock(Unit.class);
        List<Unit> barracksList = new ArrayList<>();
        barracksList.add(unit);
        //barracksControl.control(barracksList, null);
    }
    @Test
    public void executeFlyTest_CommandCenter(){

    }
    @Test
    public void executeFlyTest_ScienceFacility(){

        Unit unit = Mockito.mock(Unit.class);

        List<Unit> scienceFacilityList = new ArrayList<>();
        scienceFacilityList.add(unit);
        //scienceFacilityControl.control(scienceFacilityList, null);
    }
}