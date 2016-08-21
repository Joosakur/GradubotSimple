package fi.helsinki.cs.gradubot.production.optimize.meta;

import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.CutIfExceedsTime;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.TimeCutMinerals;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.TimeCutRequirements;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.BuildOptimizerBasicAntColonyImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joza on 3.8.2016.
 */
public class TrialOptimizer {

    public static final int MAX_TIME_MS = 6000;

    private BuildOptimizerBasicAntColonyImpl optimizer;
    private double percentBetterAvg;

    private int antCount = 100;
    private double depositRatio = 1;
    private double evaporationRatio = 0;
    private double minPheromone = 1;
    private double maxPheromone = 15;
    private double goForHighestChance = 0.6;
    private double pheromoneExp = 1.2;
    private double attractivenessHeuristic1Exp = 1;
    private double attractivenessHeuristic2Exp = 1;
    private double attractivenessHeuristic3Exp = 1;

    public TrialOptimizer(int antCount, double depositRatio, double evaporationRatio, double minPheromone, double maxPheromone, double goForHighestChance,
                          double pheromoneExp, double attractivenessHeuristic1Exp, double attractivenessHeuristic2Exp, double attractivenessHeuristic3Exp) {
        this.antCount = antCount;
        this.depositRatio = depositRatio;
        this.evaporationRatio = evaporationRatio;
        this.minPheromone = minPheromone;
        this.maxPheromone = maxPheromone;
        this.goForHighestChance = goForHighestChance;
        this.pheromoneExp = pheromoneExp;
        this.attractivenessHeuristic1Exp = attractivenessHeuristic1Exp;
        this.attractivenessHeuristic2Exp = attractivenessHeuristic2Exp;
        this.attractivenessHeuristic3Exp = attractivenessHeuristic3Exp;
    }

    public void runTrials(List<Scenario> scenarios) {
        List<BranchCutStrategy> strategies = getBranchCutStrategies();
        percentBetterAvg = 0;
        for (Scenario scenario : scenarios){
            optimizer = new BuildOptimizerBasicAntColonyImpl(scenario.getRequiredTypes(), strategies, scenario.getInitialSolution());
            optimizer.setParameters(antCount, depositRatio, evaporationRatio, minPheromone, maxPheromone, goForHighestChance,
                    pheromoneExp, attractivenessHeuristic1Exp, attractivenessHeuristic2Exp, attractivenessHeuristic3Exp);
            optimizer.start();
            long startTime = System.currentTimeMillis();
            while (!optimizer.hasFinished() && System.currentTimeMillis() < startTime + MAX_TIME_MS) {
                optimizer.runIteration();
            }
            double percentBetter = 100.0 * (scenario.getInitialSolution().getTime() - optimizer.getBestSolution().getTime()) / scenario.getInitialSolution().getTime();
            System.out.println(percentBetter);
            percentBetterAvg += percentBetter;
        }
        percentBetterAvg /= scenarios.size();
        optimizer = null;
        System.out.println(percentBetterAvg+" *****");
        String output = antCount+";"+depositRatio+";"+evaporationRatio+";"+minPheromone+";"+maxPheromone+";"+goForHighestChance+";"
                +pheromoneExp+";"+attractivenessHeuristic1Exp+";"+attractivenessHeuristic2Exp+";"+attractivenessHeuristic3Exp+";"+percentBetterAvg+";\n";
        try {
            Files.write(Paths.get("detailed-data.csv"), output.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<BranchCutStrategy> getBranchCutStrategies() {
        List<BranchCutStrategy> strategies = new ArrayList<>();
        strategies.add(new CutIfExceedsTime());
        strategies.add(new TimeCutRequirements());
        strategies.add(new TimeCutMinerals());
        return strategies;
    }

    public BuildOptimizerBasicAntColonyImpl getOptimizer() {
        return optimizer;
    }

    public double getPercentBetterAvg() {
        return percentBetterAvg;
    }

    public int getAntCount() {
        return antCount;
    }

    public double getDepositRatio() {
        return depositRatio;
    }

    public double getEvaporationRatio() {
        return evaporationRatio;
    }

    public double getMinPheromone() {
        return minPheromone;
    }

    public double getMaxPheromone() {
        return maxPheromone;
    }

    public double getGoForHighestChance() {
        return goForHighestChance;
    }

    public double getPheromoneExp() {
        return pheromoneExp;
    }

    public double getAttractivenessHeuristic1Exp() {
        return attractivenessHeuristic1Exp;
    }

    public double getAttractivenessHeuristic2Exp() {
        return attractivenessHeuristic2Exp;
    }

    public double getAttractivenessHeuristic3Exp() {
        return attractivenessHeuristic3Exp;
    }
}
