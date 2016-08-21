package fi.helsinki.cs.gradubot.production.optimize.solutions.simpleSolution;

import fi.helsinki.cs.gradubot.production.optimize.framework.AbstractBuildOptimizer;
import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.utility.codeutils.MinMaxValues;
import jnibwapi.types.UnitType;

/**
 * Created by joza on 4.8.2016.
 */
public class BuildOptimizerMultiSimpleImpl extends AbstractBuildOptimizer<Node> {

    public BuildOptimizerMultiSimpleImpl(RequiredTypes requiredTypes) {
        super(requiredTypes, null, null);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void runIteration(){
        requiredTypes.addAllPrequisites();
        if(!requiredTypes.containsKey(UnitType.UnitTypes.Terran_SCV)){
            requiredTypes.put(UnitType.UnitTypes.Terran_SCV, new MinMaxValues(4, 16));
        }

        int minWorkers = requiredTypes.getMinNeededCount(UnitType.UnitTypes.Terran_SCV);
        int maxWorkers = requiredTypes.getMaxNeededCount(UnitType.UnitTypes.Terran_SCV);
        int minBarrackses = requiredTypes.getMinNeededCount(UnitType.UnitTypes.Terran_Barracks);
        int maxBarrackses = requiredTypes.getMaxNeededCount(UnitType.UnitTypes.Terran_Barracks);
        int minFactories = requiredTypes.getMinNeededCount(UnitType.UnitTypes.Terran_Factory);
        int maxFactories = requiredTypes.getMaxNeededCount(UnitType.UnitTypes.Terran_Factory);
        int minStarports = requiredTypes.getMinNeededCount(UnitType.UnitTypes.Terran_Starport);
        int maxStarports = requiredTypes.getMaxNeededCount(UnitType.UnitTypes.Terran_Starport);

        int workers = minWorkers;
        do {
            int barrackses = minBarrackses;
            do {
                int factories = minFactories;
                do {
                    int starports = minStarports;
                    do {
                        RequiredTypes trial = new RequiredTypes();
                        trial.putAll(requiredTypes);
                        trial.addType(UnitType.UnitTypes.Terran_SCV, workers);
                        if(barrackses > 0)
                            trial.addType(UnitType.UnitTypes.Terran_Barracks, barrackses);
                        if(factories > 0)
                            trial.addType(UnitType.UnitTypes.Terran_Factory, factories);
                        if(starports > 0)
                            trial.addType(UnitType.UnitTypes.Terran_Starport, starports);
                        Solution solution = runTrial(trial);
                        if(solution.isBetterThan(bestSolution)){
                            bestSolution = solution;
                        }
                        starports++;
                    } while (starports <= maxStarports);
                    factories++;
                } while (factories <= maxFactories);
                barrackses++;
            } while (barrackses <= maxBarrackses);
            workers++;
        } while (workers <= maxWorkers);

        setRunning(false);
    }

    private Solution runTrial(RequiredTypes trialTypes){
        BuildOptimizerSimpleImpl simple = new BuildOptimizerSimpleImpl(trialTypes);
        simple.start();
        while (!simple.hasFinished()) {
            simple.runIteration();
        }
        return simple.getBestSolution();
    }


}
