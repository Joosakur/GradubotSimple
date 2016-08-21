package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.utility.codeutils.Probabilistic;

/**
 * Created by joza on 9.11.2014.
 */
public class AntNode extends Node implements Probabilistic {
    private double pheromone = 1;
    private double weight;
    public int lastEvaporation = 0;
    public static double evaporationRatio;
    public static double minPheromone;
    public static double maxPheromone;

    public AntNode() {}

    public AntNode(Node parent) {
        super(parent);
    }

    public void evaporate(int iteration){
        int evaporations = iteration - lastEvaporation;
        for(int i=0; i<evaporations; i++){
            pheromone = (1 - evaporationRatio) * pheromone;
            if(pheromone < minPheromone){
                pheromone = minPheromone;
                break;
            }
        }
        lastEvaporation = iteration;
    }

    public void deposit(double amount){
        pheromone += amount;
        if(pheromone > maxPheromone)
            pheromone = maxPheromone;
    }

    public double getPheromone() {
        return pheromone;
    }

    public void setPheromone(double pheromone) {
        this.pheromone = pheromone;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


}
