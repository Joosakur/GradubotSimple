package fi.helsinki.cs.gradubot.production.optimize.meta;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.solutions.simpleSolution.BuildOptimizerMultiSimpleImpl;

/**
 * Created by joza on 4.8.2016.
 */
public class Scenario {
    private RequiredTypes requiredTypes;
    private Solution initialSolution;

    public Scenario(RequiredTypes requiredTypes) {
        this.requiredTypes = requiredTypes;
        BuildOptimizerMultiSimpleImpl optimizer = new BuildOptimizerMultiSimpleImpl(requiredTypes);
        optimizer.start();
        while (!optimizer.hasFinished())
            optimizer.runIteration();
        initialSolution = optimizer.getBestSolution();
    }

    public RequiredTypes getRequiredTypes() {
        return requiredTypes;
    }

    public void setRequiredTypes(RequiredTypes requiredTypes) {
        this.requiredTypes = requiredTypes;
    }

    public Solution getInitialSolution() {
        return initialSolution;
    }

    public void setInitialSolution(Solution initialSolution) {
        this.initialSolution = initialSolution;
    }

}
