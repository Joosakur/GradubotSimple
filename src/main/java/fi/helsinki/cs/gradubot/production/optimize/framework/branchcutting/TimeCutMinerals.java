package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.State;
import fi.helsinki.cs.gradubot.utility.codeutils.MinMaxValues;
import jnibwapi.types.UnitType;

import java.util.Map;

/**
 * Created by joza on 16.11.2014.
 */
public class TimeCutMinerals extends AbstractBranchCutStrategy{

    public boolean rejectBranch(Node node, Solution bestSolution, RequiredTypes requiredTypes) {
        called++;
        int mineralsNeeded = getNeededMinerals(node, requiredTypes);
        if(mineralsNeeded <= 0) return false;

        if(bestSolution != null && node.state.currentTime + estimateMinTimeToGetMinerals(mineralsNeeded, node) > bestSolution.getTime()){
            cut++;
            return true;
        }
        return false;
    }

    private long estimateMinTimeToGetMinerals(int mineralsNeeded, Node node) {
        int mineralsGathered = 0;
        int workers = node.state.mineralWorkers;
        for(UnitType u : node.state.producedTypes.keySet()){
            if(u.isWorker() || UnitType.UnitTypes.getUnitType(u.getWhatBuildID()).isWorker()) {
                workers += node.state.producedTypes.get(u);
            }
        }
        int time = 0;
        while (mineralsGathered < mineralsNeeded){
            mineralsGathered += workers * State.MINERALS_PER_WORKER_PER_FRAME * UnitType.UnitTypes.Terran_SCV.getBuildTime();
            if(mineralsGathered > mineralsNeeded){
                mineralsGathered -= workers * State.MINERALS_PER_WORKER_PER_FRAME * UnitType.UnitTypes.Terran_SCV.getBuildTime();
                time += (mineralsNeeded - mineralsGathered) / (workers * State.MINERALS_PER_WORKER_PER_FRAME);
                return time;
            }
            time += UnitType.UnitTypes.Terran_SCV.getBuildTime();
            workers += node.state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_Command_Center);
        }

        return time;
    }

    private int getNeededMinerals(Node node, Map<UnitType, MinMaxValues> requiredTypes) {
        int mineralsNeeded = 0;
        for (UnitType unitType : requiredTypes.keySet()) {
            int unitsNeeded = requiredTypes.get(unitType).getMin() - node.state.countReadyOrUnderProduction(unitType);
            if(unitsNeeded > 0)
                mineralsNeeded += unitsNeeded * unitType.getMineralPrice();
        }

        return mineralsNeeded - node.state.minerals;
    }

}
