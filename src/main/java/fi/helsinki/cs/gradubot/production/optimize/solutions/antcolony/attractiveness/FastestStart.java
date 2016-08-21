package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.AntNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joza on 6.8.2016.
 */
public class FastestStart extends AbstractAttractivenessEvaluator {

    public FastestStart(double exp) {
        super(exp);
    }

    @Override
    public List<Double> calculateAttractivenessValues(List<AntNode> nodes, RequiredTypes requiredTypes) {
        double sum = 0;
        for(AntNode node : nodes){
            sum += node.state.currentTime - node.parent.state.currentTime;
        }

        List<Double> values = new ArrayList<>(nodes.size());
        for(AntNode node : nodes){
            if(sum == 0)
                values.add(1.0);
            else
                values.add(1 - (node.state.currentTime - node.parent.state.currentTime)/sum);
        }

        return values;
    }
}
