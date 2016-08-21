package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.pareto;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joza on 16.8.2016.
 */
public class ParetoArray {

    private UnitType unitType;
    private boolean terminal;
    private List<Node> nodes;
    private ParetoArray[] arrays;

    public ParetoArray getOrCreateNextArray(int count) {
        ParetoArray nextArray = arrays[count];
        if(nextArray == null){
            nextArray = new ParetoArray();
            arrays[count] = nextArray;
        }

        return nextArray;
    }

    public ParetoArray getChildArray(int count) {
        return arrays[count];
    }

    public List<ParetoArray> getArraysWorseThan(int count){
        List<ParetoArray> worse = new ArrayList<>(count + 1);
        for(int i=0; i<=count; i++){
            if(arrays[i] != null)
                worse.add(arrays[i]);
        }
        return worse;
    }

    public List<ParetoArray> getArraysBetterThan(int count){
        List<ParetoArray> worse = new ArrayList<>(count + 1);
        for(int i=count; i<arrays.length; i++){
            if(arrays[i] != null)
                worse.add(arrays[i]);
        }
        return worse;
    }

    public void setAsTerminal(){
        terminal = true;
        nodes = new ArrayList<>();
    }

    public boolean isTerminal() {
        return terminal;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public void initSize(int maxCount){
        arrays = new ParetoArray[maxCount+1];
    }

    public ParetoArray[] getArrays() {
        return arrays;
    }
}
