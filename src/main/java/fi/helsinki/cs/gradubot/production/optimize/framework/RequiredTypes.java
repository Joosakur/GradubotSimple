package fi.helsinki.cs.gradubot.production.optimize.framework;

import fi.helsinki.cs.gradubot.utility.codeutils.MinMaxValues;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by joza on 31.7.2016.
 */
public class RequiredTypes extends HashMap<UnitType, MinMaxValues> {

    public void addAllPrequisites(){
        /*copy the UnitTypes to an array to avoid concurrent modification exception*/
        UnitType[] requiredTypesArr = this.keySet().toArray(new UnitType[this.keySet().size()]);
        /*go through required types and add their prerequisites*/
        for(UnitType requiredType : requiredTypesArr){
            addPrerequisitesFor(requiredType); //works recursively
        }

        if(!this.containsKey(UnitType.UnitTypes.Terran_Refinery)){ //if refinery is not already included
            for(UnitType requiredType : this.keySet()){ //check if some unitType needs gas
                if(requiredType.getGasPrice() > 0){
                    this.put(UnitType.UnitTypes.Terran_Refinery, new MinMaxValues(1, 2)); //then add 1-2 refineries to required units
                    break;
                }
            }
        }
    }

    private void addPrerequisitesFor(UnitType forUnit){
        /*make a list of other UnitTypes which are prerequisites for this UnitType*/
        List<UnitType> prerequisites = new ArrayList<>();
        for(int id : forUnit.getRequiredUnits().keySet()){
            prerequisites.add(UnitType.UnitTypes.getUnitType(id));
        }
        if(prerequisites.isEmpty()) return;

        for(UnitType prerequisite : prerequisites){
            /*Add the prerequisites to requirements if not already included*/
            if(!this.containsKey(prerequisite) && !prerequisite.equals(UnitType.UnitTypes.Terran_SCV) && !prerequisite.equals(UnitType.UnitTypes.Terran_Supply_Depot)) {
                /*Minimum count should be 1. Maximum count should also be 1, unless this unit type is a building which produces units of required types.
                * In that case maximum count should be the number of units needed which can be built from this building. */
                int min = 1;
                int max = 0;

                if(prerequisite.isBuilding() && prerequisite.isProduceCapable()){
                    for(UnitType requiredType : this.keySet()){
                        if(!requiredType.isBuilding() && requiredType.getWhatBuildID() == prerequisite.getID())
                            max += this.get(requiredType).getMax();
                    }
                }
                if(max < 1) max = 1;
                if(prerequisite.getID() == UnitType.UnitTypes.Terran_Barracks.getID() && max > 5) max = 5;
                if(prerequisite.getID() == UnitType.UnitTypes.Terran_Factory.getID() && max > 4) max = 4;
                if(prerequisite.getID() == UnitType.UnitTypes.Terran_Starport.getID() && max > 4) max = 4;

                this.put(prerequisite, new MinMaxValues(min, max));

                /*Recursively add the prerequisites of this prerequisite UnitType*/
                addPrerequisitesFor(prerequisite);
            }
        }
    }


    public int getMinNeededCount(UnitType unitType){
        MinMaxValues values = get(unitType);
        if(values == null)
            return 0;
        else return values.getMin();
    }

    public int getMaxNeededCount(UnitType unitType){
        MinMaxValues values = get(unitType);
        if(values == null)
            return 0;
        else return values.getMax();
    }

    public void addType(UnitType unitType, int count){
        addType(unitType, count, count);
    }

    public void addType(UnitType unitType, int minCount, int maxCount){
        put(unitType, new MinMaxValues(minCount, maxCount));
    }

}
