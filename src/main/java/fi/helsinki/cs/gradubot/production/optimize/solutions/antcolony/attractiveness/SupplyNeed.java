package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.AntNode;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joza on 6.8.2016.
 */
public class SupplyNeed extends AbstractAttractivenessEvaluator {

    public SupplyNeed(double exp) {
        super(exp);
    }

    @Override
    public List<Double> calculateAttractivenessValues(List<AntNode> nodes, RequiredTypes requiredTypes) {
        double supplyNeedUrgency = getCurrentSupplyNeedUrgency(nodes.get(0).parent, requiredTypes);

        List<Double> values = new ArrayList<>(nodes.size());
        for(AntNode node : nodes){
            double attr;
            if(node.action == UnitType.UnitTypes.Terran_Supply_Depot)
                attr = supplyNeedUrgency;
            else
                attr = 1 - supplyNeedUrgency;
            values.add(attr);
        }
        return values;
    }


    private double getCurrentSupplyNeedUrgency(Node node, RequiredTypes requiredTypes) {
        double currentFreeSupply = node.state.supplyProvided - node.state.supplyUsed;
        for(UnitType production : node.state.producedTypes.keySet()){
            currentFreeSupply += node.state.producedTypes.get(production) * production.getSupplyProvided();
        }

        Map<UnitType, Integer> leftToProduce = new HashMap<>();
        for(UnitType type : requiredTypes.keySet()){
            if(type.getSupplyRequired() == 0)
                continue;

            int count = node.state.countReadyOrUnderProduction(type);
            int minCount = requiredTypes.getMinNeededCount(type);
            if(count < minCount)
                leftToProduce.put(type, minCount - count);
        }

        int soonNeededSupply = 0;
        for(UnitType producer : node.state.ownedBuildings.keySet()){
            if(!producer.isProduceCapable())
                continue;
            for(UnitType type : leftToProduce.keySet()){
                if(type.getWhatBuildID() != producer.getID())
                    continue;
                soonNeededSupply += Math.max(leftToProduce.get(type), node.state.ownedBuildings.get(producer).totalCount) * type.getSupplyRequired();
            }
        }

        double supplyNeed = Math.max(0, soonNeededSupply - currentFreeSupply) / 200.0;
        if(supplyNeed != supplyNeed || supplyNeed < 0 || supplyNeed > 1)
            System.out.println("oops");
        return supplyNeed;
    }
}
