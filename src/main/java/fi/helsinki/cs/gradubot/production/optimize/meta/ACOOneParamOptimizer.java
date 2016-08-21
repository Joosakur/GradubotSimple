package fi.helsinki.cs.gradubot.production.optimize.meta;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.CutIfExceedsTime;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.TimeCutMinerals;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.TimeCutRequirements;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.BuildOptimizerBasicAntColonyImpl;
import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;
import jnibwapi.types.UnitType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by joza on 9.8.2016.
 */
public class ACOOneParamOptimizer {

    public static final int MAX_TIME_MS = 6000;

    private List<Scenario> scenarios;

    public void run(){
        createScenarios();
        optimizeAntCount();
        System.out.println("done");
    }

    private void optimizeDepositRatio(){
        ParameterSetDouble depRatios = new ParameterSetDouble();
        depRatios.initByCount(0.01, 100, 50, true);
        List<Double> params = Arrays.asList(100.0, 0.0, 0.03, 0.35, 11.0, 0.35, 0.83, 5.0, 1.1, 0.3);
        optimizeValue(depRatios, params, 1, "deposit-ratio.csv");
    }
    private void optimizeAlpha(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(0.01, 1.0, 0.01);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.03, 0.35, 11.0, 0.35, 0.83, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 6, "alpha.csv");
    }
    private void optimizeBeta1(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(1.0, 6, 0.05);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.03, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 7, "beta-1.csv");
    }
    private void optimizeBeta2(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(0.0, 2.0, 0.01);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.03, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 8, "beta-2.csv");
    }
    private void optimizeBeta3(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(0.0, 1.0, 0.01);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.03, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 9, "beta-3.csv");
    }
    private void optimizePherEvap(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByCount(0.0001, 0.1, 1000, true);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.03, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 2, "pher-evap.csv");
    }
    private void optimizePherMin(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(0.0, 1.0, 0.001);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.01, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 3, "pher-min.csv");
    }
    private void optimizePherMax(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByCount(2.0, 100.0, 1000, true);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.01, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 4, "pher-max.csv");
    }
    private void optimizeGreediness(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(0.0, 1.0, 0.001);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.01, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 5, "q0.csv");
    }
    private void optimizeAntCount(){
        ParameterSetDouble values = new ParameterSetDouble();
        values.initByDelta(1.0, 40.0, 1.0);
        List<Double> params = Arrays.asList(100.0, 1.0, 0.01, 0.35, 11.0, 0.35, 1.0, 5.0, 1.1, 0.3);
        optimizeValue(values, params, 0, "ant-count.csv");
    }


    private void optimizeValue(ParameterSetDouble values, List<Double> params, int optimizedIndex, String filename){
        for(int i=0; i<200; i++){
            long time = 0;
            double value = Randomizer.pickRandom(values.getValues());
            for(Scenario scenario : scenarios){
                BuildOptimizerBasicAntColonyImpl optimizer = new BuildOptimizerBasicAntColonyImpl(scenario.getRequiredTypes(), getBranchCutStrategies(), scenario.getInitialSolution());
                params.set(optimizedIndex, value);
                optimizer.setParameters(params.get(0).intValue(), params.get(1), params.get(2), params.get(3), params.get(4),
                        params.get(5), params.get(6), params.get(7), params.get(8), params.get(9));
                optimizer.start();
                long startTime = System.currentTimeMillis();
                while (!optimizer.hasFinished() && System.currentTimeMillis() < startTime + MAX_TIME_MS){
                    optimizer.runIteration();
                }
                time += optimizer.getBestSolution().getTime();
            }
            String output = value+";"+time+";\n";
            try {
                System.out.println("iteration "+(i+1));
                Files.write(Paths.get(filename), output.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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


    private List<BranchCutStrategy> getBranchCutStrategies() {
        List<BranchCutStrategy> strategies = new ArrayList<>();
        strategies.add(new CutIfExceedsTime());
        strategies.add(new TimeCutRequirements());
        strategies.add(new TimeCutMinerals());
        return strategies;
    }

}
