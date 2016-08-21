package fi.helsinki.cs.gradubot.production.optimize.solutions.simpleSolution;

import fi.helsinki.cs.gradubot.production.optimize.framework.AbstractBuildOptimizer;
import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.Buildings;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.InvalidStateException;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.State;
import jnibwapi.types.UnitType;

import java.util.ListIterator;
import java.util.Map;

/**
 * Created by joza on 12.10.2014.
 */
public class BuildOptimizerSimpleImpl extends AbstractBuildOptimizer<Node> {

    private Node currentNode;


    public BuildOptimizerSimpleImpl(RequiredTypes requiredTypes) {
        super(requiredTypes, null, null);
        currentNode = root;
    }

    @Override
    public void runIteration(){
        createAndSimulateChildStates();

        Node nextNode = chooseNextAction();
        currentNode = nextNode;

        if(areGoalsReached(currentNode)){
            Solution solution = new Solution(currentNode, requiredTypes);
            if(solution.isBetterThan(bestSolution)){
                bestSolution = solution;
            }
            currentNode = null;
            setRunning(false);
        }
    }

    private void createAndSimulateChildStates() {
        currentNode.generateChildNodes(requiredTypes);
        ListIterator<Node> childIterator = currentNode.children.listIterator();
        while (childIterator.hasNext()){
            Node child = childIterator.next();
            try {
                child.copyStateAndSimulateAction();
            }
            catch (InvalidStateException exception){
                childIterator.remove();
            }
        }
    }

    private Node chooseNextAction() {
        if(requiredTypes == null)
            throw new IllegalArgumentException("Required types are missing!");

        /*If we are running out of supply, then always build a supply depot */
        if(needMoreSupply()) {
            for(Node child : currentNode.children){
                if(child.action.equals(UnitType.UnitTypes.Terran_Supply_Depot))
                    return child;
            }
            throw new InvalidStateException("Needs more supply but no supply depot action found from child nodes");
        }

        /*If the command center is not producing a worker, then start producing one unless its not found in the list of actions
        * which means the base is already fully saturated. */
        if(currentNode.state.ownedBuildings.containsKey(UnitType.UnitTypes.Terran_Command_Center) &&
                currentNode.state.ownedBuildings.get(UnitType.UnitTypes.Terran_Command_Center).availableCount > 0){
            //we can immediately start a new worker
            for(Node child : currentNode.children){
                if(child.action.equals(UnitType.UnitTypes.Terran_SCV)) {
                    return child;
                }
            }
        }

        /*Simulate state on those children that produce a UnitType which has not yet reached the minimum count. Remove others.
        * Also remove supply depots. */
        ListIterator<Node> nodeListIterator = currentNode.children.listIterator();
        while (nodeListIterator.hasNext()){
            Node child = nodeListIterator.next();
            if(currentNode.state.countReadyOrUnderProduction(child.action) < requiredTypes.getMinNeededCount(child.action)
                    && child.action != UnitType.UnitTypes.Terran_Supply_Depot ){
                child.copyStateAndSimulateAction();
            }
            else nodeListIterator.remove();
        }

        /*Choose the action that can be executed soonest. In tie situation, take the one with smaller cost. */
        int minTime = Integer.MAX_VALUE;
        Node bestChild = null;
        for(Node child : currentNode.children){
            if(child.state.currentTime < minTime) {
                minTime = child.state.currentTime;
                bestChild = child;
            }
            else if(child.state.currentTime == minTime){
                if(child.action.getMineralPrice() + child.action.getGasPrice() > bestChild.action.getMineralPrice() + bestChild.action.getGasPrice()){
                    minTime = child.state.currentTime;
                    bestChild = child;
                }
            }
        }

        if(bestChild != null) return bestChild;
        else throw new InvalidStateException();
    }

    /* Use some heuristics to evaluate if we need to start building a supply depot */
    private boolean needMoreSupply() {
        State state = currentNode.state;
        int supplyUsed = state.supplyUsed;
        int supplyAvailable = state.supplyProvided;
        for(UnitType production : state.producedTypes.keySet()){
            supplyUsed += state.producedTypes.get(production) * production.getSupplyRequired();
            supplyAvailable += state.producedTypes.get(production) * production.getSupplyProvided();
        }

        for(Map.Entry<UnitType, Buildings> entry : state.ownedBuildings.entrySet()){
            if(entry.getKey().isProduceCapable()){
                supplyUsed += 2*entry.getValue().totalCount; //don't know if we will be using these, so more of a heuristical estimation
            }
        }

        int extra = 4;
        if(state.unitTypeExistOrIsUnderProduction(UnitType.UnitTypes.Terran_Control_Tower)) extra = 6;
        if(state.unitTypeExistOrIsUnderProduction(UnitType.UnitTypes.Terran_Physics_Lab)) extra = 12;

        return supplyAvailable < supplyUsed + extra;

    }

}
