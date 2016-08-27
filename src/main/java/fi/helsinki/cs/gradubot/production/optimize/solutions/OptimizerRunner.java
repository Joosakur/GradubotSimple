package fi.helsinki.cs.gradubot.production.optimize.solutions;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.CutIfExceedsTime;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.TimeCutMinerals;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.TimeCutRequirements;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.Buildings;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.State;
import fi.helsinki.cs.gradubot.production.optimize.meta.Scenario;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.BuildOptimizerBasicAntColonyImpl;
import fi.helsinki.cs.gradubot.production.optimize.solutions.burosolution.BuildOptimizerBuroImpl;
import fi.helsinki.cs.gradubot.production.optimize.solutions.simpleSolution.BuildOptimizerSimpleImpl;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joza on 1.8.2016.
 */
public class OptimizerRunner {

    List<Scenario> scenarios;

    private State root(){
        State state = new State(0, 50, 0, 8, 20, 4, 0);
        state.ownedBuildings.put(UnitType.UnitTypes.Terran_Command_Center, new Buildings(1, 1));
        state.ownedTroops.put(UnitType.UnitTypes.Terran_SCV, 4);
        return state;
    }

    private State doA(State state, UnitType unitType){
        state.runAction(unitType);
        System.out.println(state.currentTime);
        return state;
    }

    public void run(){

        RequiredTypes requiredTypes = new RequiredTypes();
        //requiredTypes.addType(UnitType.UnitTypes.Terran_Battlecruiser, 3);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Marine, 2);
        //requiredTypes.addType(UnitType.UnitTypes.Terran_Firebat, 1);


        State state;

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_SCV);
        state = doA(state, UnitType.UnitTypes.Terran_SCV);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_SCV);
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_SCV);
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_SCV);
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);

        System.out.println("----");


        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);
        state = doA(state, UnitType.UnitTypes.Terran_SCV);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);


        System.out.println("----");


        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);
        state = doA(state, UnitType.UnitTypes.Terran_SCV);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);



        System.out.println("----");


        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);
        state = doA(state, UnitType.UnitTypes.Terran_SCV);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);
        state = doA(state, UnitType.UnitTypes.Terran_Supply_Depot);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);
        state = doA(state, UnitType.UnitTypes.Terran_Refinery);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);
        state = doA(state, UnitType.UnitTypes.Terran_Marine);

        state = root();
        state = doA(state, UnitType.UnitTypes.Terran_Barracks);
        state = doA(state, UnitType.UnitTypes.Terran_Academy);




        BuildOptimizerSimpleImpl multiSimpleOptimizer = new BuildOptimizerSimpleImpl(requiredTypes);
        multiSimpleOptimizer.start();
        while (!multiSimpleOptimizer.hasFinished()) {
            multiSimpleOptimizer.runIteration();
        }

        List<BranchCutStrategy> strategies = new ArrayList<>();
        strategies.add(new CutIfExceedsTime());
        strategies.add(new TimeCutRequirements());
        strategies.add(new TimeCutMinerals());

        int iteration = 0;
        while (iteration < 30){
            iteration++;
            //BuildOptimizerBuroImpl optimizer = new BuildOptimizerBuroImpl(requiredTypes, strategies, multiSimpleOptimizer.getBestSolution());
            BuildOptimizerBasicAntColonyImpl optimizer = new BuildOptimizerBasicAntColonyImpl(requiredTypes, strategies, multiSimpleOptimizer.getBestSolution());
            //BuildOptimizerACOEvolveImpl optimizer = new BuildOptimizerACOEvolveImpl(requiredTypes, strategies, multiSimpleOptimizer.getBestSolution());
            optimizer.start();
            long time = System.currentTimeMillis();
            long time2 = System.currentTimeMillis();
            while (!optimizer.hasFinished() && System.currentTimeMillis() < time + 30000){
                optimizer.runIteration();
                int dt = 100;
                if(System.currentTimeMillis() > time + 1000)
                    dt = 500;
                if(System.currentTimeMillis() > time + 5000)
                    dt = 1000;

                if(System.currentTimeMillis() > time2 + dt){
                    System.out.println(System.currentTimeMillis() - time+";"+optimizer.getBestSolution().getTime()+";");
                    time2 = System.currentTimeMillis();
                }

            }

        }

        //System.out.println(System.currentTimeMillis() - time);
        //System.out.println(optimizer.getBestSolution().getTime());
        //optimizer.getBestSolution().print();
        System.out.println("done");
    }

    private void createScenarios() {
        scenarios = new ArrayList<>();
        RequiredTypes requiredTypes;
        Scenario scenario;

        requiredTypes = new RequiredTypes();
        requiredTypes.addType(UnitType.UnitTypes.Terran_Marine, 4);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Bunker, 1);
        scenario = new Scenario(requiredTypes);
        scenarios.add(scenario);

        requiredTypes = new RequiredTypes();
        requiredTypes.addType(UnitType.UnitTypes.Terran_Marine, 8);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Firebat, 4);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Medic, 4);
        scenario = new Scenario(requiredTypes);
        scenarios.add(scenario);

        requiredTypes = new RequiredTypes();
        requiredTypes.addType(UnitType.UnitTypes.Terran_Marine, 8);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Siege_Tank_Siege_Mode, 4);
        scenario = new Scenario(requiredTypes);
        scenarios.add(scenario);

        requiredTypes = new RequiredTypes();
        requiredTypes.addType(UnitType.UnitTypes.Terran_Marine, 1);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Firebat, 1);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Ghost, 1);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Siege_Tank_Tank_Mode, 1);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Goliath, 1);
        requiredTypes.addType(UnitType.UnitTypes.Terran_Battlecruiser, 1);
        scenario = new Scenario(requiredTypes);
        scenarios.add(scenario);
    }

    private void runTrialsBasicAco(RequiredTypes requiredTypes, Solution initialSolution) {

        List<BranchCutStrategy> strategies = new ArrayList<>();
        strategies.add(new CutIfExceedsTime());
        strategies.add(new TimeCutRequirements());
        strategies.add(new TimeCutMinerals());

        double systime = System.currentTimeMillis();
        BuildOptimizerBasicAntColonyImpl optimizer = new BuildOptimizerBasicAntColonyImpl(requiredTypes, strategies, initialSolution);
        optimizer.start();
        int iteration = 0;
        while (!optimizer.hasFinished()) {
            optimizer.runIteration();
            if(iteration % 40 == 0 || optimizer.hasFinished()){
                System.out.println(System.currentTimeMillis() - systime + ";" + optimizer.getBestSolution().getTime() + ";");
            }
            iteration++;
        }
        System.out.println("basic ant colony finished");
    }

    private void runTrialsBuro(RequiredTypes requiredTypes, Solution initialSolution) {
        List<BranchCutStrategy> strategies = new ArrayList<>();
        strategies.add(new CutIfExceedsTime());
        strategies.add(new TimeCutRequirements());
        strategies.add(new TimeCutMinerals());

        double systime = System.currentTimeMillis();
        BuildOptimizerBuroImpl optimizer = new BuildOptimizerBuroImpl(requiredTypes, strategies, initialSolution);
        optimizer.start();
        int iteration = 0;
        while (!optimizer.hasFinished()) {
            optimizer.runIteration();
            if(iteration % 100000 == 0 || optimizer.hasFinished()){
                System.out.println(System.currentTimeMillis() - systime + ";" + optimizer.getBestSolution().getTime() + ";");
                iteration = 0;
            }
            iteration++;
        }
        System.out.println("basic ant colony finished");
    }

}
