package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.genetics;

/**
 * Created by joza on 25.4.2015.
 */
public abstract class Gene<T extends Number> {
    protected T value;
    protected T minValue;
    protected T maxValue;

    public Gene(T minValue, T maxValue, double initMean, double initStDev) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        randomizeGaussian(initMean, initStDev);
    }

    public Gene(T minValue, T maxValue, boolean logarithmic) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        if(logarithmic)
            randomizeLogarithmic();
        else
            randomizeEven();
    }


    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract void mutate(double ratio);

    public abstract void randomizeEven();
    public abstract void randomizeGaussian(double initMean, double initStDev);
    public abstract void randomizeLogarithmic();


}
