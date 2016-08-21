package fi.helsinki.cs.gradubot.production.optimize.framework;

import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by joza on 3.11.2014.
 */
public class Solution {

    private List<UnitType> actions = new ArrayList<>();
    private Node finalNode;
    private int time;

    public Solution(Node node, RequiredTypes requiredTypes) {
        finalNode = node;
        time = node.state.getGoalCompletionTime(requiredTypes);
    }

    public int getTime() {
        return time;
    }

    public List<UnitType> getActions(){
        if(actions.size() == 0){
            Node node = finalNode;
            while (node.action != null){
                actions.add(node.action);
                node = node.parent;
            }
            Collections.reverse(actions);
        }

        return actions;
    }

    public void print(){
        System.out.println("Solution time was "+time+" frames " +
                "("+finalNode.state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_SCV)+" workers, "+finalNode.state.getTotalValue()+" value) " +
                "with build order:");
        getActions();
        for(int i=0; i<actions.size(); i++){
            System.out.println((i+1)+": "+actions.get(i).getName());
        }
    }

    public Node getFinalNode() {
        return finalNode;
    }

    public boolean isBetterThan(Solution o) {
        if(o == null)
            return true;

        if(this.time < o.getTime())
            return true;
        if(this.time > o.getTime())
            return false;

        int scv = finalNode.state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_SCV);
        int oScv = o.finalNode.state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_SCV);
        if(scv > oScv)
            return true;
        if(scv < oScv)
            return false;

        int totalCost = finalNode.state.getTotalValue();
        int oTotalCost = o.finalNode.state.getTotalValue();
        if(totalCost < oTotalCost)
            return true;
        else return false;
    }
}
