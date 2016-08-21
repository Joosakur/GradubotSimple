package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.pareto;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import jnibwapi.types.UnitType;

import java.util.*;

/**
 * Created by joza on 16.8.2016.
 */
public class ParetoMap extends TreeMap<Integer, ParetoMap> {

    private UnitType unitType;
    private boolean terminal;
    private Set<Node> nodes;

    public ParetoMap(UnitType unitType) {
        this.unitType = unitType;
    }

    public ParetoMap() {
    }

    public ParetoMap getNextMap(int count) {
        ParetoMap nextMap = this.get(count);
        if(nextMap == null){
            nextMap = new ParetoMap();
            this.put(count, nextMap);
        }

        return nextMap;
    }


    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public void setAsTerminal(){
        terminal = true;
        nodes = new HashSet<>();
    }

    public boolean isTerminal() {
        return terminal;
    }

    public Set<Node> getNodes() {
        return nodes;
    }
}
