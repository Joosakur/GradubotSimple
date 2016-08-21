package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.AntNode;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joza on 6.8.2016.
 */
public class CountProduced extends AbstractAttractivenessEvaluator {

    public CountProduced(double exp) {
        super(exp);
    }

    @Override
    public List<Double> calculateAttractivenessValues(List<AntNode> nodes, RequiredTypes requiredTypes) {
        List<Double> values = new ArrayList<>(nodes.size());
        for(AntNode node : nodes){
            values.add(calculateAttractiveness(node, requiredTypes));
        }
        return values;
    }

    private double calculateAttractiveness(AntNode node, RequiredTypes requiredTypes) {
        UnitType action = node.action;

        if(!requiredTypes.containsKey(action))
            return 0.5;

        int count = node.state.countReadyOrUnderProduction(action);
        if(count == 0)
            return 1;

        int minCount = requiredTypes.getMinNeededCount(action);
        if(count <= minCount)
            return 1 - 0.5 * count / minCount;

        int maxCount = requiredTypes.getMaxNeededCount(action);
        return  0.5 * (maxCount - count) / (maxCount - minCount);

    }
}
