package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;

/**
 * Created by joza on 7.10.2014.
 */
public interface BranchCutStrategy {
    boolean rejectBranch(Node node, Solution bestSolution, RequiredTypes requiredTypes);
    int timesCalled();
    int timesCut();
}
