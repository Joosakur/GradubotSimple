package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.pareto;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;

import java.util.*;

/**
 * Created by joza on 16.8.2016.
 */
public class ParetoCutter {

    private volatile ParetoMap paretoMap = new ParetoMap();
    private volatile Map<Long, Node> allNodes = new HashMap<>();

    public ParetoCutter(RequiredTypes requiredTypes) {

    }

    public synchronized void addNode(Node node, RequiredTypes requiredTypes){

        /*Set<UnitType> unitTypes = new HashSet<>();
        unitTypes.addAll(node.state.ownedBuildings.keySet());
        unitTypes.addAll(node.state.ownedTroops.keySet());
        unitTypes.addAll(requiredTypes.keySet());
        unitTypes.add(UnitType.UnitTypes.Terran_SCV);
        unitTypes.add(UnitType.UnitTypes.Terran_Supply_Depot);

        ParetoMap currentMap = paretoMap;
        while (unitTypes.size() > 0){
            if(currentMap.getUnitType() == null){
                currentMap.setUnitType(unitTypes.iterator().next());
            }
            int count = node.state.countReady(currentMap.getUnitType());
            unitTypes.remove(currentMap.getUnitType());
            currentMap = currentMap.getNextMap(count);
        }

        currentMap.setAsTerminal();
        currentMap.getNodes().add(node);*/

        allNodes.put(node.getId(), node);
    }

    public synchronized List<Node> nodesDominatedByThis(Node node) {
        List<Node> dominatedNodes = new ArrayList<>();
        for(Node by : allNodes.values()){
            if(node.dominates(by))
                dominatedNodes.add(by);
        }
        return dominatedNodes;
        //return nodesDominatedByThis(paretoMap, node);
    }

    private synchronized List<Node> nodesDominatedByThis(ParetoMap currentMap, Node node){
        SortedMap<Integer, ParetoMap> maps = currentMap.headMap(node.state.countReady(currentMap.getUnitType()), true);
        List<Node> dominatedNodes = new ArrayList<>();

        for(ParetoMap map : maps.values()){
            if(map.isTerminal()){
                for(Node oldNode : map.getNodes()){
                    if(node.dominates(oldNode))
                        dominatedNodes.add(oldNode);
                }
            }
            else {
                if(isThisNodeDominated(map, node))
                    dominatedNodes.addAll(nodesDominatedByThis(map, node));
            }
        }

        return dominatedNodes;
    }

    public synchronized boolean isThisNodeDominated(Node node){
        for(Node by : allNodes.values()){
            if(by.dominates(node))
                return true;
        }
        return false;
        //return isThisNodeDominated(paretoMap, node);
    }

    private synchronized boolean isThisNodeDominated(ParetoMap currentMap, Node node){
        SortedMap<Integer, ParetoMap> maps = currentMap.tailMap(node.state.countReady(currentMap.getUnitType()));

        for(ParetoMap map : maps.values()){
            if(map.isTerminal()){
                for(Node oldNode : map.getNodes()){
                    if(oldNode.dominates(node))
                        return true;
                }
            }
            else {
                if(isThisNodeDominated(map, node))
                    return true;
            }
        }

        return false;
    }

    public synchronized void removeSubTree(Node root){
        Stack<Node> stack = new Stack<>();
        stack.add(root);
        while (!stack.isEmpty()){
            Node node = stack.pop();
            if(node.children != null) {
                for (Node child : node.children) {
                    stack.add(child);
                }
            }
            removeNode(node);
        }
    }

    private void removeNode(Node node) {
        allNodes.remove(node.getId());
        /*ParetoMap map = paretoMap;
        while (map != null && !map.isTerminal()){
            map = map.get(node.state.countReady(map.getUnitType()));
        }
        if(map != null && map.isTerminal())
            map.getNodes().remove(node);
        */
    }

}
