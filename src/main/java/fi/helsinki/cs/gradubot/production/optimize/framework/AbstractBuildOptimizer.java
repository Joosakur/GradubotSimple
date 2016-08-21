package fi.helsinki.cs.gradubot.production.optimize.framework;

import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.Buildings;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.State;
import fi.helsinki.cs.gradubot.utility.codeutils.MinMaxValues;
import jnibwapi.types.UnitType;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * Created by joza on 29.10.2014.
 *
 * This abstract class serves as a common root class for different kinds of tree-search based build order optimizers.
 * The parameterized type NODE is the class which is used as a node in the search-tree. That class needs to be or extend the basic version Node.
 */
public abstract class AbstractBuildOptimizer<NODE extends Node> {

    private boolean running;
    private boolean started;

    protected NODE root;
    protected Solution bestSolution;
    protected List<BranchCutStrategy> branchCutStrategies;
    protected RequiredTypes requiredTypes;

    public void start(){
        started = true;
        running = true;
    }

    /*This method should be used to progress the execution of the actual algorithm iteratively */
    public void runIteration(){
        if(!started || !running){
            throw new RuntimeException("Algorithm should be started first by calling the start method");
        }
    };

    public AbstractBuildOptimizer(RequiredTypes requiredTypes, List<BranchCutStrategy> branchCutStrategies, Solution externalBestSolution) {
        this.requiredTypes = requiredTypes;
        this.branchCutStrategies = branchCutStrategies;
        bestSolution = externalBestSolution;

        this.running = false;
        this.started = false;
        Node.idCounter = 0;

        try {
            Class clazz = this.getClass();
            while (true){
                try {
                    ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
                    break;
                } catch (ClassCastException e){
                    clazz = clazz.getSuperclass();
                }
            }
            root = (NODE) (((Class)((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0]).newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        root.state = new State(0, 0, 0, 8, 20, 4, 0);
        root.state.ownedBuildings.put(UnitType.UnitTypes.Terran_Command_Center, new Buildings(1, 1));
        root.state.ownedTroops.put(UnitType.UnitTypes.Terran_SCV, 4);

        requiredTypes.addAllPrequisites();
    }


    /*This method goes through the branch cut strategies and returns true if some of them asks the branch to be cut */
    protected boolean shouldBranchBeCut(Node currentNode) {;
        for(BranchCutStrategy branchCutStrategy : branchCutStrategies){
            if(branchCutStrategy.rejectBranch(currentNode, bestSolution, requiredTypes)){
                return true;
            }
        }
        return false;
    }

    /*Check if all required units and buildings are either ready or under production */
    protected boolean areGoalsReached(Node currentNode) {
        for(UnitType unitType : requiredTypes.keySet()){
            State state = currentNode.state;
            int count = 0;
            if(state.ownedTroops.get(unitType) != null)
                count += state.ownedTroops.get(unitType);
            if(state.ownedBuildings.get(unitType) != null)
                count += state.ownedBuildings.get(unitType).totalCount;
            if(state.producedTypes.get(unitType) != null)
                count += state.producedTypes.get(unitType);

            if(count < requiredTypes.getMinNeededCount(unitType)) return false;
        }

        return true;
    }

    public Map<UnitType, MinMaxValues> getRequiredTypes() {
        return requiredTypes;
    }


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean hasFinished(){
        return started && !running;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public List<BranchCutStrategy> getBranchCutStrategies() {
        return branchCutStrategies;
    }

    public void setBranchCutStrategies(List<BranchCutStrategy> branchCutStrategies) {
        this.branchCutStrategies = branchCutStrategies;
    }
}
