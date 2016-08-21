package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.genetics;

import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;

import java.util.Random;

/**
 * Created by joza on 25.4.2015.
 */
public class DoubleGene extends Gene<Double> {

    private static Random random = new Random();

    public DoubleGene(Double minValue, Double maxValue, double initMean, double initStDev) {
        super(minValue, maxValue, initMean, initStDev);
    }

    public DoubleGene(Double minValue, Double maxValue, boolean logarithmic) {
        super(minValue, maxValue, logarithmic);
    }

    @Override
    public void mutate(double ratio) {
        value = Math.random() < 0.5 ? value - value*ratio : value + value*ratio;
    }

    @Override
    public void randomizeEven() {
        value = Math.random()*(maxValue-minValue) + minValue;
    }

    @Override
    public void randomizeGaussian(double initMean, double initStDev) {
        value = random.nextGaussian()*initStDev + initMean;
        if(value < minValue) value = minValue;
        if(value > maxValue) value = maxValue;
    }

    @Override
    public void randomizeLogarithmic() {
        value = Randomizer.logRandom(minValue, maxValue);
    }

}
