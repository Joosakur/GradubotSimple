package fi.helsinki.cs.gradubot.production.optimize.framework.simulation;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import jnibwapi.types.UnitType;

import java.util.*;

/**
 * Created by joza on 7.10.2014.
 */
public class State {

    public static boolean USE_SIMULATION_METHOD_2 = false;

    public final static double MINERALS_PER_WORKER_PER_FRAME = 0.045;
    public final static double GAS_PER_WORKER_PER_FRAME = 0.07;

    public int currentTime;
    public int minerals;
    public int gas;
    public int supplyUsed;
    public int supplyProvided;
    public int mineralWorkers;
    public int gasWorkers;

    public Map<UnitType, Integer> ownedTroops = new HashMap<>(); // <unit type, count ready>
    public Map<UnitType, Buildings> ownedBuildings = new HashMap<>(); // <unit type, (total count, available count)>
    public Map<UnitType, Integer> producedTypes = new HashMap<>(); // <unit type, count under production>, both troops and buildings
    public TreeMap<Integer, List<UnitType>> productionTimes = new TreeMap<>();

    public State(int currentTime, int minerals, int gas, int supplyUsed, int supplyProvided, int mineralWorkers, int gasWorkers) {
        this.currentTime = currentTime;
        this.minerals = minerals;
        this.gas = gas;
        this.supplyUsed = supplyUsed;
        this.supplyProvided = supplyProvided;
        this.mineralWorkers = mineralWorkers;
        this.gasWorkers = gasWorkers;
    }

    public void runAction(UnitType typeToProduce) throws InvalidStateException {
        if(USE_SIMULATION_METHOD_2) {
            runAction2(typeToProduce);
            return;
        }
        int timeWhenActionPossible = timeWhenActionPossible(typeToProduce);
        while (timeWhenActionPossible > currentTime){
            int simulateUntilTime = timeWhenActionPossible;
            if(!productionTimes.isEmpty())
                simulateUntilTime = Math.min(simulateUntilTime, timeWhenNextProductionCompletes());
            int timeToSimulate = simulateUntilTime - currentTime;
            simulateTime(timeToSimulate);
        }
        if(timeWhenActionPossible == currentTime)
            startProduction(typeToProduce);
        else
            System.out.println("went too far");
    }

    public int timeWhenActionPossible(UnitType typeToProduce) {
        int timeWhenPossible = Math.max(timeWhenEnoughResources(typeToProduce), timeWhenPrerequisitesReady(typeToProduce));
        if(isProducedFromBuilding(typeToProduce)){
            timeWhenPossible = Math.max(timeWhenPossible, timeWhenProductionBuildingFree(typeToProduce));
        }

        timeWhenPossible = Math.max(timeWhenPossible, timeWhenEnoughSupply(typeToProduce));
        timeWhenPossible = Math.max(timeWhenPossible, timeWhenEnoughWorkers(typeToProduce));

        return timeWhenPossible;
    }

    public int timeWhenEnoughWorkers(UnitType typeToProduce){
        if(!UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID()).isWorker()) return currentTime;
        if(mineralWorkers > 0) return currentTime;
        for(Map.Entry<Integer, List<UnitType>> entry : productionTimes.entrySet()){
            for(UnitType completingType : entry.getValue()){
                if(completingType.isWorker() || UnitType.UnitTypes.getUnitType(completingType.getWhatBuildID()).isWorker())
                    return entry.getKey();
            }
        }
        throw new InvalidStateException("never enough workers");
    }

    public boolean everEnoughSupply(UnitType typeToProduce){
        int totalSupply = supplyProvided + typeToProduce.getSupplyProvided() - supplyUsed - typeToProduce.getSupplyRequired();
        for(UnitType produced : producedTypes.keySet()){
            totalSupply += producedTypes.get(produced) * produced.getSupplyProvided();
        }

        return totalSupply >= 0;
    }

    public int timeWhenEnoughSupply(UnitType typeToProduce) {
        if(supplyProvided >= supplyUsed + typeToProduce.getSupplyRequired()) {
            return currentTime;
        }

        int extraSupply = 0;

        for(Map.Entry<Integer, List<UnitType>> entry : productionTimes.entrySet()) {
            for(UnitType unitType : entry.getValue()){
                extraSupply += unitType.getSupplyProvided() - unitType.getSupplyRequired();
                if(supplyProvided + extraSupply > supplyUsed + typeToProduce.getSupplyRequired()) {
                    return entry.getKey();
                }
            }
        }

        throw new InvalidStateException();
    }

    public boolean isNextActionValid(UnitType typeToProduce){
        //if its a refinery make sure we have enough workers
        if(UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID()).isWorker()){
            int c = mineralWorkers;
            for(UnitType u : producedTypes.keySet()){
                if(u.isWorker() || UnitType.UnitTypes.getUnitType(u.getWhatBuildID()).isWorker())
                    c++;
                if(c > 0) continue;
            }
            if(c==0) return false;
        }

        if(typeToProduce.getID() == UnitType.UnitTypes.Terran_Refinery.getID()){
            return countReadyOrUnderProduction(UnitType.UnitTypes.Terran_SCV) > (countReadyOrUnderProduction(UnitType.UnitTypes.Terran_Refinery) + 1)*3;
        }

        return everEnoughGas(typeToProduce) && prerequisitesEverReady(typeToProduce) && everEnoughSupply(typeToProduce);
    }


    public boolean everEnoughGas(UnitType typeToProduce){
        if(typeToProduce.getGasPrice() == 0) return true;
        else return unitTypeExistOrIsUnderProduction(UnitType.UnitTypes.Terran_Refinery);
    }

    public int timeWhenEnoughResources(UnitType typeToProduce) {
        int mineralsNeeded = typeToProduce.getMineralPrice();
        int gasNeeded = typeToProduce.getGasPrice();
        if(mineralsNeeded <= minerals && gasNeeded <= gas) return currentTime;

        int tempMinerals = minerals;
        int tempGas = gas;
        int tempMineralWorkers = mineralWorkers;
        int tempGasWorkers = gasWorkers;
        int tempTime = currentTime;
        int extraRefinerys = 0;
        TreeMap<Integer, List<UnitType>> resourceProductionChangeTimes = null;
        int completionEstimate;

        while (true){
            //calculate when we would reach the needed resources with current production
            double mineralTime = 0;
            if(mineralsNeeded > tempMinerals) {
                mineralTime = (mineralsNeeded - tempMinerals) / (MINERALS_PER_WORKER_PER_FRAME * tempMineralWorkers);
                if(tempMineralWorkers == 0)
                    mineralTime = 99999999;
            }
            double gasTime = 0;
            if(gasNeeded > tempGas) {
                gasTime = (gasNeeded - tempGas) / (GAS_PER_WORKER_PER_FRAME * tempGasWorkers);
                if(tempGasWorkers == 0)
                    gasTime = 99999999;
            }


            completionEstimate = tempTime + (int) Math.ceil(Math.max(mineralTime, gasTime));

            //return the estimate if there will be no changes in production before that
            if(productionTimes.isEmpty() || productionTimes.firstKey() > completionEstimate) {
                return completionEstimate;
            }

            //at first time make a list of changes in production
            if(resourceProductionChangeTimes == null){
                resourceProductionChangeTimes = new TreeMap<Integer, List<UnitType>>();
                for(Map.Entry<Integer, List<UnitType>> entry : productionTimes.entrySet()){
                    for(UnitType completingType : entry.getValue()){
                        if (completingType.isWorker() || completingType.isRefinery()
                                || UnitType.UnitTypes.getUnitType(completingType.getWhatBuildID()).isWorker()) {
                            if(resourceProductionChangeTimes.containsKey(entry.getKey()))
                                resourceProductionChangeTimes.get(entry.getKey()).add(completingType);
                            else{
                                List<UnitType> unitTypes = new ArrayList<UnitType>(1);
                                unitTypes.add(completingType);
                                resourceProductionChangeTimes.put(entry.getKey(), unitTypes);
                            }
                        }
                    }
                }
            }
            if(resourceProductionChangeTimes.size() == 0) {
                return completionEstimate;
            }

            //simulate production until first production change
            int dt = resourceProductionChangeTimes.firstKey() - tempTime;
            tempTime += dt;
            tempMinerals += MINERALS_PER_WORKER_PER_FRAME * tempMineralWorkers * dt;
            tempGas += GAS_PER_WORKER_PER_FRAME * tempGasWorkers * dt;


            //then update production
            for(UnitType completingType : resourceProductionChangeTimes.firstEntry().getValue()){
                tempMineralWorkers++; //either one is built or one is freed from building task
                if(completingType.getID() == UnitType.UnitTypes.Terran_Refinery.getID())
                    extraRefinerys++;
            }


            while (tempMineralWorkers > 1 && tempGasWorkers < (countReady(UnitType.UnitTypes.Terran_Refinery)+extraRefinerys)*3){
                tempGasWorkers++;
                tempMineralWorkers--;
            }

            //and remove the handled entry
            resourceProductionChangeTimes.remove(tempTime);

        }
    }

    public boolean prerequisitesEverReady(UnitType typeToProduce){
        for(int prereqId : typeToProduce.getRequiredUnits().keySet()){
            if(!unitTypeExistOrIsUnderProduction(UnitType.UnitTypes.getUnitType(prereqId)))
                return false;
        }
        if(!unitTypeExistOrIsUnderProduction(UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID())))
                return false;

        return true;
    }

    public int timeWhenPrerequisitesReady(UnitType typeToProduce) {
        Set<UnitType> prerequisites = new HashSet<UnitType>();
        for(int requiredType : typeToProduce.getRequiredUnits().keySet()){
            if(!ownedBuildings.containsKey(UnitType.UnitTypes.getUnitType(requiredType))
                && !ownedTroops.containsKey(UnitType.UnitTypes.getUnitType(requiredType))) { //we dont have the requirement yet
                prerequisites.add(UnitType.UnitTypes.getUnitType(requiredType));
            }
        }

        int timeWhenPrerequisitesReady = currentTime;

        if(prerequisites.isEmpty()) {
            return currentTime;
        }

        for(Map.Entry<Integer, List<UnitType>> entry : productionTimes.entrySet()){
            for(UnitType completingType : entry.getValue()){
                if(prerequisites.contains(completingType)){
                    prerequisites.remove(completingType);
                    timeWhenPrerequisitesReady = entry.getKey();
                    if(prerequisites.isEmpty()) {
                        return timeWhenPrerequisitesReady;
                    }
                }
            }
        }

        throw new InvalidStateException("Can not start production: prerequisites would never be ready for "+typeToProduce.getName());
    }

    private boolean isProducedFromBuilding(UnitType typeToProduce) {
        return UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID()).isBuilding();
    }

    public int timeWhenProductionBuildingFree(UnitType typeToProduce) {
        int builtBy = typeToProduce.getWhatBuildID();
        if(ownedBuildings.containsKey(UnitType.UnitTypes.getUnitType(builtBy)) &&
            ownedBuildings.get(UnitType.UnitTypes.getUnitType(builtBy)).availableCount > 0){
            return currentTime;
        }


        //check when the first building of that type under construction completes
        //or when the first building of that type gets freed, which ever comes first
        for(Map.Entry<Integer, List<UnitType>> productions : productionTimes.entrySet()){
            for(UnitType productionType : productions.getValue()){
                if(productionType.getID() == builtBy || productionType.getWhatBuildID() == builtBy) {
                    return productions.getKey();
                }
            }
        }

        throw new InvalidStateException("Producing building never available");
    }

    private int timeWhenNextProductionCompletes() {
        return productionTimes.firstKey();
    }

    private void simulateTime(int frames){
        if(frames<0)
            System.out.println("TIME TRAVEL");
        generateResources(frames);
        currentTime += frames;

        if(productionTimes.containsKey(currentTime)){ //productions completing on this frame
            for(UnitType type : productionTimes.get(currentTime)){
                completeProduction(type);
            }
            //
            productionTimes.remove(currentTime);
        }

    }

    private void generateResources(int frames) {
        minerals += MINERALS_PER_WORKER_PER_FRAME * mineralWorkers * frames;
        gas += GAS_PER_WORKER_PER_FRAME * gasWorkers * frames;
    }

    private void startProduction(UnitType typeToProduce) {
        if(UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID()).isWorker()) mineralWorkers--;
        UnitType builtBy = UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID());
        if(builtBy.isBuilding()) {
            Buildings buildings = ownedBuildings.get(builtBy);
            if(buildings.availableCount > 0) buildings.availableCount -= 1;
            else throw new InvalidStateException("Can not start production, producing building not available");
        }


        if(producedTypes.containsKey(typeToProduce)) producedTypes.put(typeToProduce, producedTypes.get(typeToProduce) + 1);
        else producedTypes.put(typeToProduce, 1);

        int completionTime = currentTime + typeToProduce.getBuildTime();

        if(productionTimes.containsKey(completionTime)) productionTimes.get(completionTime).add(typeToProduce);
        else{
            List<UnitType> types = new ArrayList<UnitType>(2); //unlikely to complete two things on same frame so save space
            types.add(typeToProduce);
            productionTimes.put(completionTime, types);
        }

        int p1 = 0;
        int p2 = 0;
        for(UnitType t : producedTypes.keySet()){
            p1 += producedTypes.get(t);
        }
        for(Integer t : productionTimes.keySet()){
            p2 += productionTimes.get(t).size();
        }
        if(p1 != p2)
            System.out.println("whats wrong");

        minerals -= typeToProduce.getMineralPrice();
        gas -= typeToProduce.getGasPrice();
        supplyUsed += typeToProduce.getSupplyRequired();
        if(mineralWorkers < 0)
            throw new InvalidStateException("Can not start production, invalid resources");
        if(minerals < -10)
            throw new InvalidStateException("Can not start production, invalid resources");
        if(gas < -10)
            throw new InvalidStateException("Can not start production, invalid resources");
        if(supplyUsed > supplyProvided)
            throw new InvalidStateException("Can not start production, invalid resources");
    }

    private void completeProduction(UnitType unitType) {
        if(unitType.isBuilding()){
            if(ownedBuildings.containsKey(unitType)) ownedBuildings.get(unitType).newBuildingCreated();
            else ownedBuildings.put(unitType, new Buildings(1,1));

        }
        else{
            if(ownedTroops.containsKey(unitType)) ownedTroops.put(unitType, ownedTroops.get(unitType) + 1);
            else ownedTroops.put(unitType, 1);
        }
        if(UnitType.UnitTypes.getUnitType(unitType.getWhatBuildID()).isWorker() || unitType.isWorker())
            mineralWorkers++; //free up the builder

        UnitType builtBy = UnitType.UnitTypes.getUnitType(unitType.getWhatBuildID());
        if(builtBy.isBuilding()) {
            ownedBuildings.get(builtBy).availableCount++; //free the producing building
        }


        supplyProvided += unitType.getSupplyProvided();

        producedTypes.put(unitType, producedTypes.get(unitType) - 1);
        if(producedTypes.get(unitType) == 0) producedTypes.remove(unitType);

        while (mineralWorkers > 1 && gasWorkers < countReady(UnitType.UnitTypes.Terran_Refinery)*3){
            gasWorkers++;
            mineralWorkers--;
        }

    }

    public boolean unitTypeExistOrIsUnderProduction(UnitType unitType){
        if(unitType.isBuilding() && ownedBuildings.containsKey(unitType)) return true;
        if(!unitType.isBuilding() && ownedTroops.containsKey(unitType)) return true;
        if(producedTypes.containsKey(unitType)) return true;
        return false;
    }

    public int countReadyOrUnderProduction(UnitType unitType){
        int count = countReady(unitType);
        if(producedTypes.containsKey(unitType))
            count += producedTypes.get(unitType);
        return count;
    }

    public int countReady(UnitType unitType){
        int count = 0;
        if(unitType.isBuilding() && ownedBuildings.containsKey(unitType))
            count += ownedBuildings.get(unitType).totalCount;
        else if(ownedTroops.containsKey(unitType))
            count += ownedTroops.get(unitType);
        return count;
    }


    private void runAction2(UnitType typeToProduce){
        while (!isActionPossible(typeToProduce)){
            simulateTime2(20);
        }

        startProduction(typeToProduce);
    }

    private boolean isActionPossible(UnitType typeToProduce) {
        if(!enoughResources(typeToProduce)) return false;
        if(!arePrerequisitesReady(typeToProduce)) return false;
        if(isProducedFromBuilding(typeToProduce)){
            if(!isProductionBuildingFree(typeToProduce)) return false;
        }
        if(!enoughSupply(typeToProduce)) return false;
        return true;
    }

    private boolean enoughResources(UnitType typeToProduce){
        return typeToProduce.getMineralPrice() <= minerals && typeToProduce.getGasPrice() <= gas;
    }

    private boolean enoughSupply(UnitType typeToProduce){
        return supplyProvided > supplyUsed + typeToProduce.getSupplyRequired();
    }

    private boolean arePrerequisitesReady(UnitType typeToProduce){
        for(int requiredType : typeToProduce.getRequiredUnits().keySet()){
            UnitType requiredUnit = UnitType.UnitTypes.getUnitType(requiredType);
            if(requiredUnit.isBuilding()){
                if(!ownedBuildings.containsKey(UnitType.UnitTypes.getUnitType(requiredType))) {
                    return false;
                }
            }
            else{
                if(!ownedTroops.containsKey(UnitType.UnitTypes.getUnitType(requiredType)))
                    return false;
            }
        }

        return true;
    }

    private boolean isProductionBuildingFree(UnitType typeToProduce){
        Buildings buildings = ownedBuildings.get(UnitType.UnitTypes.getUnitType(typeToProduce.getWhatBuildID()));
        if(buildings != null)
            return buildings.availableCount > 0;
        else return false;
    }


    private void simulateTime2(int frames){
        generateResources(frames);
        currentTime += frames;

        while (!productionTimes.isEmpty() && productionTimes.firstKey() <= currentTime ){ //productions completing
            for(UnitType type : productionTimes.firstEntry().getValue()){
                completeProduction(type);
            }
            productionTimes.remove(productionTimes.firstKey());
        }
    }

    public int getGoalCompletionTime(RequiredTypes requiredTypes) {
        int time = currentTime;
        for(UnitType unitType : requiredTypes.keySet()){
            int missing = requiredTypes.getMinNeededCount(unitType) - countReady(unitType);
            if(missing <= 0) continue;
            if(producedTypes.containsKey(unitType) && producedTypes.get(unitType) >= missing){
                for(Map.Entry<Integer, List<UnitType>> entry : productionTimes.entrySet()){
                    for(UnitType producedType : entry.getValue()){
                        if(producedType.getID() == unitType.getID()) missing--;
                    }
                    if(missing <= 0){
                        if(entry.getKey() > time) time = entry.getKey();
                        break;
                    }
                }
            }
            if(missing <= 0) continue;
            throw new RuntimeException("Goals will not complete without more actions.");
        }

        return time;
    }

    public int getTotalValue(){
        int value = 0;
        for(Map.Entry<UnitType, Buildings> entry : ownedBuildings.entrySet()){
            value += entry.getValue().totalCount * (entry.getKey().getMineralPrice() + 2*entry.getKey().getGasPrice());
        }
        for(Map.Entry<UnitType, Integer> entry : ownedTroops.entrySet()){
            value += entry.getValue() * (entry.getKey().getMineralPrice() + 2*entry.getKey().getGasPrice());
        }
        for(Map.Entry<UnitType, Integer> entry : producedTypes.entrySet()){
            value += entry.getValue() * (entry.getKey().getMineralPrice() + 2*entry.getKey().getGasPrice());
        }
        return value;
    }
}
