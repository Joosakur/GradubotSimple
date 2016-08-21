package fi.helsinki.cs.gradubot.production.optimize.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joza on 3.8.2016.
 */
public class ParameterSetDouble {

    private List<Double> values;
    private boolean logarithmic;
    private double minValue;
    private double maxValue;


    public void initByCount(double minValue, double maxValue, int valueCount, boolean logarithmic){
        if(valueCount < 2)
            throw new IllegalArgumentException();

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.logarithmic = logarithmic;
        values = new ArrayList<>(valueCount);

        if(logarithmic){
            double minLog = Math.log10(minValue);
            double maxLog = Math.log10(maxValue);
            double deltaValue = (maxLog - minLog) / (valueCount-1);
            double value = minLog;
            for (int i=0; i<valueCount; i++){
                values.add(Math.pow(10, value));
                value += deltaValue;
            }        }
        else {
            double deltaValue = (maxValue - minValue) / (valueCount-1);
            double value = minValue;
            for (int i=0; i<valueCount; i++){
                values.add(value);
                value += deltaValue;
            }
        }
    }

    public void initByDelta(double minValue, double maxValue, double delta){
        double dcount = (maxValue - minValue) / delta + 1;
        int count = (int) dcount;
        if(count != dcount)
            throw new IllegalArgumentException();

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.logarithmic = false;
        values = new ArrayList<>(count);

        double value = minValue;
        for(int i=0; i<count; i++){
            values.add(value);
            value += delta;
        }
    }

    public List<Double> getValues() {
        return values;
    }

    public boolean isLogarithmic() {
        return logarithmic;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }
}
