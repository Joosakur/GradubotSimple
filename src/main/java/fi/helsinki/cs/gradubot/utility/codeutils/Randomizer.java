package fi.helsinki.cs.gradubot.utility.codeutils;

import java.util.*;

/**
 * Created by joza on 9.11.2014.
 */
public class Randomizer {

    public static <T extends Probabilistic> T weightedRandom(List<T> options){
        double totalWeight = 0.0;
        for (T option : options)
            totalWeight += option.getWeight();

        double r = Math.random() * totalWeight;

        if(totalWeight == 0){
            return options.get((int) Math.floor(Math.random()*options.size()));
        }

        double weightCount = 0.0;
        for (T option : options) {
            weightCount += option.getWeight();
            //if (true)
            if(weightCount >= r)
                return option;
        }

        throw new RuntimeException("Should never be shown.");

    }

    public static <T> T pickRandom(Collection<T> items){
        if(items==null || items.size()==0) return null;
        int ind = (int) Math.floor(Math.random() * items.size());
        return (new ArrayList<>(items)).get(ind);
    }

    public static double logRandom(double lowerLimit, double upperLimit) {
        double result;
        double logLower = Math.log(lowerLimit);
        double logUpper = Math.log(upperLimit);
        result = Math.exp(Math.random() * (logUpper - logLower) + logLower);
        if (result < lowerLimit) {
            return lowerLimit;
        } else if (result > upperLimit) {
            return upperLimit;
        }
        return result;
    }
}
