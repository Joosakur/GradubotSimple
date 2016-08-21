package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.genetics;

import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;

import java.util.Random;

/**
 * Created by joza on 25.4.2015.
 */
public class IntGene extends Gene<Integer> {

    private static Random random = new Random();

    public IntGene(Integer minValue, Integer maxValue, double initMean, double initStDev) {
        super(minValue, maxValue, initMean, initStDev);
    }

    public IntGene(Integer minValue, Integer maxValue, boolean logarithmic) {
        super(minValue, maxValue, logarithmic);
    }

    @Override
    public void mutate(double ratio) {
        value = Math.random() < 0.5 ? (int) (value - value*ratio) : (int) (value + value*ratio);
    }

    @Override
    public void randomizeEven() {
        value = (int) Math.round(Math.random()*(maxValue-minValue) + minValue);
    }

    @Override
    public void randomizeGaussian(double initMean, double initStDev) {
        value = (int) Math.round(random.nextGaussian()*initStDev + initMean);
        if(value < minValue) value = minValue;
        if(value > maxValue) value = maxValue;
    }

    @Override
    public void randomizeLogarithmic() {
        double doubleValue = Randomizer.logRandom((double) minValue, (double) maxValue);
        value = (int) Math.round(doubleValue);
    }


}
