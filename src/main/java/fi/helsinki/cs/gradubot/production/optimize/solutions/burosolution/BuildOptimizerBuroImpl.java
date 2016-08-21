package fi.helsinki.cs.gradubot.production.optimize.solutions.burosolution;

import fi.helsinki.cs.gradubot.production.optimize.framework.AbstractBuildOptimizer;
import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;

import java.util.List;
import java.util.Stack;

/**
 * Created by joza on 12.10.2014.
 */
public class BuildOptimizerBuroImpl extends AbstractBuildOptimizer<Node> {

    public Stack<Node> nodeStack;

    public BuildOptimizerBuroImpl(RequiredTypes requiredTypes, List<BranchCutStrategy> branchCutStrategyList, Solution externalBestSolution) {
        super(requiredTypes, branchCutStrategyList, externalBestSolution);
        nodeStack = new Stack<>();
    }

    public void start() {
        super.start();
        nodeStack.push(root);
    }

    @Override
    public void runIteration() {
        super.runIteration();

        if(nodeStack.isEmpty()) {
            setRunning(false);
            return;
        }

        Node node = nodeStack.pop();
        try{
            if(node.parent != null)
                node.copyStateAndSimulateAction();
        } catch (Exception e){
            detachFromParent(node);
            return;
        }

        if(areGoalsReached(node)){
            Solution solution = new Solution(node, requiredTypes);
            if(bestSolution == null || solution.isBetterThan(bestSolution)){
                bestSolution = solution;
            }
            detachFromParent(node);
            return;
        }

        if(shouldBranchBeCut(node)) {
            detachFromParent(node);
            return;
        }

        for(Node child : node.generateChildNodes(requiredTypes)){
            nodeStack.push(child);
        }
    }

    private void detachFromParent(Node node) {
        //remove this child from parent
        if(node.parent != null)
            node.parent.children.remove(node);

        //if it was the last child, also remove parent from its parent recursively
        if(node.parent.children.size() == 0 && node.parent.parent != null){
            detachFromParent(node.parent);
        }
    }

}

