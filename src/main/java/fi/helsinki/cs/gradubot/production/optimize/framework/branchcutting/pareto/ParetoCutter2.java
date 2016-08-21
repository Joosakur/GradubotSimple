package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.pareto;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import jnibwapi.types.UnitType;

import java.util.*;

/**
 * Created by joza on 16.8.2016.
 */
public class ParetoCutter2 {

    private ParetoArray paretoArray = new ParetoArray();
    private RequiredTypes requiredTypes;
    Set<UnitType> unitTypes = new HashSet<>();

    public ParetoCutter2(RequiredTypes requiredTypes) {
        this.requiredTypes = requiredTypes;
        unitTypes.addAll(requiredTypes.keySet());
        unitTypes.add(UnitType.UnitTypes.Terran_SCV);
        unitTypes.add(UnitType.UnitTypes.Terran_Supply_Depot);
    }

    public void addNode(Node node){
        Set<UnitType> unitTypesCopy = new HashSet<>(unitTypes);

        ParetoArray currentArray = paretoArray;
        while (unitTypesCopy.size() > 0){
            if(currentArray.getUnitType() == null){
                currentArray.setUnitType(unitTypesCopy.iterator().next());
                int maxCount = getMaxCount(currentArray.getUnitType(), requiredTypes);
                currentArray.initSize(maxCount);
            }
            int count = node.state.countReady(currentArray.getUnitType());
            unitTypesCopy.remove(currentArray.getUnitType());
            currentArray = currentArray.getOrCreateNextArray(count);
        }

        currentArray.setAsTerminal();
        currentArray.getNodes().add(node);
    }

    private int getMaxCount(UnitType unitType, RequiredTypes requiredTypes) {
        int count = requiredTypes.getMaxNeededCount(unitType);
        if(count > 0){
            return count;
        }
        if(unitType == UnitType.UnitTypes.Terran_SCV){
            return 25;
        }
        if(unitType == UnitType.UnitTypes.Terran_Supply_Depot){
            return 25;
        }
        return 0;
    }

    public List<Node> nodesDominatedByThis(Node node) {
        List<Node> dominatedNodes = new ArrayList<>();
        evals2++;
        Stack<ParetoArray> arrayStack = new Stack<>();
        arrayStack.push(paretoArray);
        while (!arrayStack.isEmpty()){
            ParetoArray array = arrayStack.pop();
            if(array.isTerminal()){
                for(Node old : array.getNodes()){
                    if(node.dominates(old)){
                        dominatedNodes.add(old);
                    }
                }
            }
            else {
                int count = node.state.countReady(array.getUnitType());
                for(int i=Math.max(0, count-3); i<=count; i++){
                    ParetoArray childArray = array.getArrays()[i];
                    if(childArray != null)
                        arrayStack.push(childArray);
                }
            }
        }
        yes2 += dominatedNodes.size();
        return dominatedNodes;
    }

    private int yes = 0;
    private int evals = 0;
    private int yes2 = 0;
    private int evals2 = 0;
    public boolean isThisNodeDominated(Node node){
        evals++;
        Stack<ParetoArray> arrayStack = new Stack<>();
        arrayStack.push(paretoArray);
        while (!arrayStack.isEmpty()){
            ParetoArray array = arrayStack.pop();

            if(array.isTerminal()){
                for(Node old : array.getNodes()){
                    if(old != null && old.dominates(node)){
                        yes++;
                        return true;
                    }
                }
            }
            else {
                int count = node.state.countReady(array.getUnitType());
                for(int i=count; i<Math.min(count+4, array.getArrays().length); i++){
                    ParetoArray childArray = array.getArrays()[i];
                    if(childArray != null)
                        arrayStack.push(childArray);
                }
            }
        }
        return false;
    }

    public void removeSubTree(Node root){
        Stack<Node> stack = new Stack<>();
        stack.add(root);
        while (!stack.isEmpty()){
            Node node = stack.pop();
            //synchronized (node){
                if(node.children != null) {
                    for (Node child : node.children) {
                        if(child != null)
                            stack.add(child);
                    }
                }
            //}
            removeNode(node);
        }
    }

    private void removeNode(Node node) {
        ParetoArray array = paretoArray;
        while (array != null && !array.isTerminal()){
            array = array.getChildArray(node.state.countReady(array.getUnitType()));
        }
        if(array != null && array.isTerminal())
            array.getNodes().remove(node);
    }

}
