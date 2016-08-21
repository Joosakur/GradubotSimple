package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import jnibwapi.types.UnitType;

import java.util.List;
import java.util.Map;

/**
 * Created by joza on 16.11.2014.
 */
public class TimeCutRequirements extends AbstractBranchCutStrategy {

    @Override
    public boolean rejectBranch(Node node, Solution bestSolution, RequiredTypes requiredTypes) {
        called++;
        long minTime = 0;

        for(UnitType unitType : requiredTypes.keySet()){
            if(node.state.countReady(unitType) >= requiredTypes.get(unitType).getMin()) continue;
            if(node.state.countReadyOrUnderProduction(unitType) >= requiredTypes.get(unitType).getMin()){
                int missing = requiredTypes.get(unitType).getMin() - node.state.countReady(unitType);
                for (Map.Entry<Integer, List<UnitType>> entry : node.state.productionTimes.entrySet()) {
                    if(entry.getValue().contains(unitType)){
                        for(UnitType typeCompleting : node.state.productionTimes.get(entry.getKey())){
                            if(typeCompleting.getID() == unitType.getID()) missing--;
                        }
                    }
                    if(missing <= 0) {
                        long local = entry.getKey();
                        if(local > minTime) minTime = local;
                        break;
                    }
                }
            }
            else {
                long local = node.state.currentTime + unitType.getBuildTime() + getMinTimeForPrerequisites(unitType, node);
                if (local > minTime) minTime = local;
            }
        }

        if(bestSolution != null && minTime >= bestSolution.getTime()) {
            cut++;
            return true;
        }

        return false;
    }


    private long getMinTimeForPrerequisites(UnitType unitType, Node node) {
        long time = 0;
        for (Integer id : unitType.getRequiredUnits().keySet()) {
            UnitType req = UnitType.UnitTypes.getUnitType(id);
            if(node.state.countReady(req) > 0) continue; //already completed
            else if(node.state.producedTypes.containsKey(req)){ //under production
                for (Map.Entry<Integer, List<UnitType>> entry : node.state.productionTimes.entrySet()) {
                    if(entry.getValue().contains(req)){
                        long local = entry.getKey() - node.state.currentTime;
                        if(local > time) time = local;
                        break;
                    }
                }
            }
            else { //production not yet started
                long local = req.getBuildTime();
                local += getMinTimeForPrerequisites(req, node);
                if(local > time) time = local;
            }
        }
        return time;
    }
}
