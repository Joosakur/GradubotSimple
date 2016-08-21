package fi.helsinki.cs.gradubot.production.optimize.framework.simulation;

/**
 * Created by joza on 11.10.2014.
 */
public class Buildings {

    public int totalCount;
    public int availableCount;

    public Buildings(int totalCount, int availableCount) {
        this.totalCount = totalCount;
        this.availableCount = availableCount;
    }

    public void newBuildingCreated(){
        totalCount++;
        availableCount++;
    }
}
