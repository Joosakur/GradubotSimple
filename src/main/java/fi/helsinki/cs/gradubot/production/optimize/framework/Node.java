package fi.helsinki.cs.gradubot.production.optimize.framework;

import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.Buildings;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.InvalidStateException;
import fi.helsinki.cs.gradubot.production.optimize.framework.simulation.State;
import fi.helsinki.cs.gradubot.utility.codeutils.MinMaxValues;
import jnibwapi.types.RaceType;
import jnibwapi.types.UnitType;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by joza on 7.10.2014.
 */
public class Node {

    private static final int TERRAN_ID = RaceType.RaceTypes.Terran.getID();

    public static int idCounter = 0;

    private int id;
    public State state;
    public UnitType action;
    public List<Node> children;
    public Node parent;
    public boolean cut = false;

    public Node() {
        id = idCounter++;
    }

    public Node(Node parent) {
        id = idCounter++;
        this.parent = parent;
    }


    /* This method copies the state from the parent node and simulates the state forward until UnitType set in action field can be produced.
     * Method returns in a state where the production has just started. */
    public void copyStateAndSimulateAction() throws InvalidStateException {
        copyStateFromParent();
        state.runAction(action);
    }

    /* This method generates the child nodes for this node and assigns them a UnitType to action field which shall be produced next.
     * The state will not be copied and action will not be simulated by this method yet */
    public List<Node> generateChildNodes(RequiredTypes requiredTypes){
        List<UnitType> possibleToProduce = new ArrayList<>();
        for(UnitType unitType : UnitType.UnitTypes.getAllUnitTypes()){
            if(unitType.getRaceID() != TERRAN_ID) continue;

            //supply depot always possible
            if(unitType.equals(UnitType.UnitTypes.Terran_Supply_Depot)){
                possibleToProduce.add(unitType);
                continue;
            }

            //skip worker if already enough of them to fully saturate bases and build some stuff
            if(unitType.equals(UnitType.UnitTypes.Terran_SCV)){
                if(isBaseSaturatedByWorkers()) {
                    continue;
                }
            }

            //skip other types that we don't need more of
            if(isMaxNeededCountOfUnitsReached(requiredTypes, unitType)) {
                continue;
            }

            //skip invalid actions that can not be started before some other action is chosen first
            if(!state.isNextActionValid(unitType)) {
                continue;
            }

            //otherwise add to list
            possibleToProduce.add(unitType);
        }

        children = new ArrayList<>(possibleToProduce.size());
        for(UnitType unitType : possibleToProduce){
            Node node = createNodeOfSameClass();
            node.action = unitType;
            node.parent = this;
            children.add(node);
        }

        return children;
    }

    private Node createNodeOfSameClass() {
        try {
            return this.getClass().getDeclaredConstructor(Node.class).newInstance(this);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new InvalidStateException("Node object creation failed");
    }

    private boolean isBaseSaturatedByWorkers() {
        return state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_SCV) >=
                16 * state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_Command_Center) +
                3 * state.countReadyOrUnderProduction(UnitType.UnitTypes.Terran_Refinery)
                + 3;
    }

    private boolean isMaxNeededCountOfUnitsReached(Map<UnitType, MinMaxValues> requiredTypes, UnitType unitType) {
        if(!requiredTypes.containsKey(unitType)) {
            if(unitType.equals(UnitType.UnitTypes.Terran_SCV)) return false;
            else return true;
        }
        else return state.countReadyOrUnderProduction(unitType) >= requiredTypes.get(unitType).getMax();
    }

    public void copyStateFromParent() {
        state = new State(
                parent.state.currentTime,
                parent.state.minerals,
                parent.state.gas,
                parent.state.supplyUsed,
                parent.state.supplyProvided,
                parent.state.mineralWorkers,
                parent.state.gasWorkers
        );
        state.ownedTroops = new HashMap<>(parent.state.ownedTroops);
        state.ownedBuildings = new HashMap<>(parent.state.ownedBuildings.size()+1);
        for(Map.Entry<UnitType, Buildings> entry : parent.state.ownedBuildings.entrySet()){
            Buildings buildings = new Buildings(entry.getValue().totalCount, entry.getValue().availableCount);
            state.ownedBuildings.put(entry.getKey(), buildings);
        }
        state.producedTypes = new HashMap<>(parent.state.producedTypes);
        state.productionTimes = new TreeMap<>();
        for(Map.Entry<Integer, List<UnitType>> entry : parent.state.productionTimes.entrySet()){
            List<UnitType> unitTypes = new ArrayList<>(entry.getValue());
            state.productionTimes.put(entry.getKey(), unitTypes);
        }
    }


    public void removeCutChildren() {
        Iterator<Node> childIterator = children.iterator();
        while (childIterator.hasNext()){
            Node child = childIterator.next();
            if (child.cut) {
                childIterator.remove();
            }
        }
    }

    public boolean dominates(Node other){
        if(this.equals(other))
            return false;

        if(this.state.currentTime > other.state.currentTime)
            return false;
        if(this.state.minerals < other.state.minerals)
            return false;
        if(this.state.gas < other.state.gas)
            return false;

        Set<UnitType> unitTypes = new HashSet<>();
        unitTypes.addAll(other.state.ownedBuildings.keySet());
        unitTypes.addAll(other.state.ownedTroops.keySet());
        unitTypes.addAll(other.state.producedTypes.keySet());
        for(UnitType unitType : unitTypes){
            if(this.state.countReady(unitType) < other.state.countReady(unitType))
                return false;
            if(this.state.countReadyOrUnderProduction(unitType) < other.state.countReadyOrUnderProduction(unitType))
                return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (id != node.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public long getId() {
        return id;
    }

}
