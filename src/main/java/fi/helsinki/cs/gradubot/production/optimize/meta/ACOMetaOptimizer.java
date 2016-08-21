package fi.helsinki.cs.gradubot.production.optimize.meta;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;
import jnibwapi.types.UnitType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by joza on 3.8.2016.
 */
public class ACOMetaOptimizer {

    List<Scenario> scenarios;
    List<TrialOptimizer> trialOptimizers;

    ParameterSetDouble antCountValues = new ParameterSetDouble();
    ParameterSetDouble depositRatioValues = new ParameterSetDouble();
    ParameterSetDouble evaporationRatioValues = new ParameterSetDouble();
    ParameterSetDouble minPheromoneValues = new ParameterSetDouble();
    ParameterSetDouble maxPheromoneValues = new ParameterSetDouble();
    ParameterSetDouble goForHighestChanceValues = new ParameterSetDouble();
    ParameterSetDouble pheromoneExpValues = new ParameterSetDouble();
    ParameterSetDouble attractivenessHeuristic1ExpValues = new ParameterSetDouble();
    ParameterSetDouble attractivenessHeuristic2ExpValues = new ParameterSetDouble();
    ParameterSetDouble attractivenessHeuristic3ExpValues = new ParameterSetDouble();


    private final int OPTIMIZER_COUNT = 100;
    private final double STRAIGHT_TO_NEXT_GENERATION_RATIO = 0.1;
    private final double STRAIGHT_TO_DISCARD_RATIO = 0.1;
    private final double SMALL_MUTATION_PROBABILITY = 0.1;
    private final double SMALL_MUTATION_RATIO = 0.1;
    private final double BIG_MUTATION_PROBABILITY = 0.03;

    public void run(){
        init();

        for (int i=0; i < 100; i++){
            runTrials();
            writeOutput();
            evolve();
            System.out.println("generation "+i+" completed");
        }
        System.out.println("done");
    }

    private void init() {
        createScenarios();
        createValueSets();
        createTrialOptimizers();

        String labels = "ant count avg;std;deposit ratio avg;std;evaporation ratio avg;std;min pheromone avg;std;max pheromone avg;std;q0 avg;std;alpha avg;std;beta1 avg;std;beta2 avg;std;beta3 avg;std;percent better avg;std;\n";
        try {
            Files.write(Paths.get("meta-optimization-data.csv"), labels.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        labels = "ant count;deposit ratio;evaporation ratio;min pheromone;max pheromone;q0;alpha;beta1;beta2;beta3;percent better;\n";
        try {
            Files.write(Paths.get("detailed-data.csv"), labels.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
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

    private void createValueSets() {
        antCountValues.initByDelta(10, 150, 20);
        depositRatioValues.initByCount(0.01, 1.0, 20, true);
        evaporationRatioValues.initByCount(0.01, 0.8, 20, true);
        minPheromoneValues.initByDelta(0, 1, 0.05);
        maxPheromoneValues.initByCount(5, 100, 20, true);
        goForHighestChanceValues.initByDelta(0, 1, 0.05);
        pheromoneExpValues.initByDelta(0, 5, 0.25);
        attractivenessHeuristic1ExpValues.initByDelta(0, 5, 0.25);
        attractivenessHeuristic2ExpValues.initByDelta(0, 5, 0.25);
        attractivenessHeuristic3ExpValues.initByDelta(0, 5, 0.25);
    }

    private void createTrialOptimizers() {
        trialOptimizers = new ArrayList<>(OPTIMIZER_COUNT);

        for(int i=0; i<OPTIMIZER_COUNT; i++){
            TrialOptimizer optimizer = new TrialOptimizer(
                    Randomizer.pickRandom(antCountValues.getValues()).intValue(),
                    Randomizer.pickRandom(depositRatioValues.getValues()),
                    Randomizer.pickRandom(evaporationRatioValues.getValues()),
                    Randomizer.pickRandom(minPheromoneValues.getValues()),
                    Randomizer.pickRandom(maxPheromoneValues.getValues()),
                    Randomizer.pickRandom(goForHighestChanceValues.getValues()),
                    Randomizer.pickRandom(pheromoneExpValues.getValues()),
                    Randomizer.pickRandom(attractivenessHeuristic1ExpValues.getValues()),
                    Randomizer.pickRandom(attractivenessHeuristic2ExpValues.getValues()),
                    Randomizer.pickRandom(attractivenessHeuristic3ExpValues.getValues())
                    );
            trialOptimizers.add(optimizer);
        }
    }

    private void runTrials() {
        for(TrialOptimizer trialOptimizer : trialOptimizers){
            trialOptimizer.runTrials(scenarios);
        }
    }

    private void writeOutput() {
        try {
            double antCountAvg = 0;
            double depositRatioAvg = 0;
            double evaporationRatioAvg = 0;
            double minPherAvg = 0;
            double maxPherAvg = 0;
            double goForHighestAvg = 0;
            double pherExpAvg = 0;
            double attr1expAvg = 0;
            double attr2expAvg = 0;
            double attr3expAvg = 0;
            double percentBetterAvg = 0;
            
            for(TrialOptimizer trialOptimizer : trialOptimizers){
                antCountAvg += trialOptimizer.getAntCount();
                depositRatioAvg += trialOptimizer.getDepositRatio();
                evaporationRatioAvg += trialOptimizer.getEvaporationRatio();
                minPherAvg += trialOptimizer.getMinPheromone();
                maxPherAvg += trialOptimizer.getMaxPheromone();
                goForHighestAvg += trialOptimizer.getGoForHighestChance();
                pherExpAvg += trialOptimizer.getPheromoneExp();
                attr1expAvg += trialOptimizer.getAttractivenessHeuristic1Exp();
                attr2expAvg += trialOptimizer.getAttractivenessHeuristic2Exp();
                attr3expAvg += trialOptimizer.getAttractivenessHeuristic3Exp();
                percentBetterAvg += trialOptimizer.getPercentBetterAvg();
            }

            antCountAvg /= trialOptimizers.size();
            depositRatioAvg /= trialOptimizers.size();
            evaporationRatioAvg /= trialOptimizers.size();
            minPherAvg /= trialOptimizers.size();
            maxPherAvg /= trialOptimizers.size();
            goForHighestAvg /= trialOptimizers.size();
            pherExpAvg /= trialOptimizers.size();
            attr1expAvg /= trialOptimizers.size();
            attr2expAvg /= trialOptimizers.size();
            attr3expAvg /= trialOptimizers.size();
            percentBetterAvg /= trialOptimizers.size();

            double antCountStd = 0;
            double depositRatioStd = 0;
            double evaporationRatioStd = 0;
            double minPherStd = 0;
            double maxPherStd = 0;
            double goForHighestStd = 0;
            double pherExpStd = 0;
            double attr1expStd = 0;
            double attr2expStd = 0;
            double attr3expStd = 0;
            double percentBetterStd = 0;


            for(TrialOptimizer trialOptimizer : trialOptimizers){
                antCountStd += Math.pow(trialOptimizer.getAntCount() - antCountAvg, 2);
                depositRatioStd += Math.pow(trialOptimizer.getDepositRatio() - depositRatioAvg, 2);
                evaporationRatioStd += Math.pow(trialOptimizer.getEvaporationRatio() - evaporationRatioAvg, 2);
                minPherStd += Math.pow(trialOptimizer.getMinPheromone() - minPherAvg, 2);
                maxPherStd += Math.pow(trialOptimizer.getMaxPheromone() - maxPherAvg, 2);
                goForHighestStd += Math.pow(trialOptimizer.getGoForHighestChance() - goForHighestAvg, 2);
                pherExpStd += Math.pow(trialOptimizer.getPheromoneExp() - pherExpAvg, 2);
                attr1expStd += Math.pow(trialOptimizer.getAttractivenessHeuristic1Exp() - attr1expAvg, 2);
                attr2expStd += Math.pow(trialOptimizer.getAttractivenessHeuristic2Exp() - attr2expAvg, 2);
                attr3expStd += Math.pow(trialOptimizer.getAttractivenessHeuristic3Exp() - attr3expAvg, 2);
                percentBetterStd += Math.pow(trialOptimizer.getPercentBetterAvg() - percentBetterAvg, 2);
            }

            antCountStd = Math.sqrt(antCountStd / trialOptimizers.size());
            depositRatioStd = Math.sqrt(depositRatioStd / trialOptimizers.size());
            evaporationRatioStd = Math.sqrt(evaporationRatioStd / trialOptimizers.size());
            minPherStd = Math.sqrt(minPherStd / trialOptimizers.size());
            maxPherStd = Math.sqrt(maxPherStd / trialOptimizers.size());
            goForHighestStd = Math.sqrt(goForHighestStd / trialOptimizers.size());
            pherExpStd = Math.sqrt(pherExpStd / trialOptimizers.size());
            attr1expStd = Math.sqrt(attr1expStd / trialOptimizers.size());
            attr2expStd = Math.sqrt(attr2expStd / trialOptimizers.size());
            attr3expStd = Math.sqrt(attr3expStd / trialOptimizers.size());
            percentBetterStd = Math.sqrt(percentBetterStd / trialOptimizers.size());

            String output = antCountAvg+";"+antCountStd+";"+depositRatioAvg+";"+depositRatioStd+";"+evaporationRatioAvg+";"+evaporationRatioStd+";"
                    +minPherAvg+";"+minPherStd+";"+maxPherAvg+";"+maxPherStd+";"+goForHighestAvg+";"+goForHighestStd+";"
                    +pherExpAvg+";"+pherExpStd+";"+attr1expAvg+";"+attr1expStd+";"+attr2expAvg+";"+attr2expStd+";"+attr3expAvg+";"+attr3expStd+";"
                    +percentBetterAvg+";"+percentBetterStd+"\n";
            System.out.println(output);
            Files.write(Paths.get("meta-optimization-data.csv"), output.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void evolve() {
        List<TrialOptimizer> nextGeneration = new ArrayList<>(OPTIMIZER_COUNT);
        sortOptimizersByPerformanceDescending();
        int toNextGen = (int) (STRAIGHT_TO_NEXT_GENERATION_RATIO * OPTIMIZER_COUNT);
        nextGeneration.addAll(trialOptimizers.subList(0, toNextGen));
        for(int i=trialOptimizers.size()-1; i>trialOptimizers.size()*(1-STRAIGHT_TO_DISCARD_RATIO); i--){
            trialOptimizers.remove(i);
        }
        double worstFitness = trialOptimizers.get(trialOptimizers.size()-1).getPercentBetterAvg();

        while (nextGeneration.size() < OPTIMIZER_COUNT){
            TrialOptimizer parent1 = Randomizer.pickRandom(trialOptimizers);
            TrialOptimizer parent2 = Randomizer.pickRandom(trialOptimizers);
            double p1Fitness = parent1.getPercentBetterAvg() - worstFitness;
            double p2Fitness = parent2.getPercentBetterAvg() - worstFitness;
            try{
                double p1Prob = p1Fitness / (p1Fitness + p2Fitness);
                nextGeneration.add(produceChild(parent1, parent2, p1Prob));
            } catch (ArithmeticException e){
                continue; //both have zero fitness, try some other parents
            }
        }
        trialOptimizers = nextGeneration;
    }

    private TrialOptimizer produceChild(TrialOptimizer parent1, TrialOptimizer parent2, double p1Prob) {
        int antCount =  Math.random() < p1Prob ? parent1.getAntCount() : parent2.getAntCount();
        double depositRatio = Math.random() < p1Prob ? parent1.getDepositRatio() : parent2.getDepositRatio();
        double evaporationRatio = Math.random() < p1Prob ? parent1.getEvaporationRatio() : parent2.getEvaporationRatio();
        double minPher = Math.random() < p1Prob ? parent1.getMinPheromone() : parent2.getMinPheromone();
        double maxPher = Math.random() < p1Prob ? parent1.getMaxPheromone() : parent2.getMaxPheromone();
        double goForHighest = Math.random() < p1Prob ? parent1.getGoForHighestChance() : parent2.getGoForHighestChance();
        double pherExp = Math.random() < p1Prob ? parent1.getPheromoneExp() : parent2.getPheromoneExp();
        double attr1exp = Math.random() < p1Prob ? parent1.getAttractivenessHeuristic1Exp() : parent2.getAttractivenessHeuristic1Exp();
        double attr2exp = Math.random() < p1Prob ? parent1.getAttractivenessHeuristic2Exp() : parent2.getAttractivenessHeuristic2Exp();
        double attr3exp = Math.random() < p1Prob ? parent1.getAttractivenessHeuristic3Exp() : parent2.getAttractivenessHeuristic3Exp();


        if(Math.random() < BIG_MUTATION_PROBABILITY)
            antCount = Randomizer.pickRandom(antCountValues.getValues()).intValue();
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            depositRatio = Randomizer.pickRandom(depositRatioValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            evaporationRatio = Randomizer.pickRandom(evaporationRatioValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            minPher = Randomizer.pickRandom(minPheromoneValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            maxPher = Randomizer.pickRandom(maxPheromoneValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            goForHighest = Randomizer.pickRandom(goForHighestChanceValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            pherExp = Randomizer.pickRandom(pheromoneExpValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            attr1exp = Randomizer.pickRandom(attractivenessHeuristic1ExpValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            attr2exp = Randomizer.pickRandom(attractivenessHeuristic2ExpValues.getValues());
        if(Math.random() < BIG_MUTATION_PROBABILITY)
            attr3exp = Randomizer.pickRandom(attractivenessHeuristic3ExpValues.getValues());

        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            antCount += antCount * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            depositRatio += depositRatio * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            evaporationRatio += evaporationRatio * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            minPher += minPher * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            maxPher += maxPher * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            goForHighest += goForHighest * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            pherExp += pherExp * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            attr1exp += attr1exp * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            attr2exp += attr2exp * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);
        if(Math.random() < SMALL_MUTATION_PROBABILITY)
            attr3exp += attr3exp * (Math.random() < 0.5 ? SMALL_MUTATION_RATIO : -SMALL_MUTATION_RATIO);


        TrialOptimizer child = new TrialOptimizer(antCount, depositRatio, evaporationRatio, minPher, maxPher, goForHighest, pherExp, attr1exp, attr2exp, attr3exp);
        return child;
    }

    private void sortOptimizersByPerformanceDescending() {
        Collections.sort(trialOptimizers, new Comparator<TrialOptimizer>() {
            @Override
            public int compare(TrialOptimizer o1, TrialOptimizer o2) {
                if(o1.getPercentBetterAvg() > o2.getPercentBetterAvg()) return -1;
                if(o1.getPercentBetterAvg() < o2.getPercentBetterAvg()) return 1;
                return 0;
            }
        });
    }

}
