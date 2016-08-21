package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;

/**
 * Created by joza on 3.11.2014.
 */
public class CutIfExceedsTime extends AbstractBranchCutStrategy {

    @Override
    public boolean rejectBranch(Node node, Solution bestSolution, RequiredTypes requiredTypes) {
        called++;
        if(bestSolution != null && node.state.currentTime > bestSolution.getTime()){
            cut++;
            return true;
        }

        return false;
    }

}
