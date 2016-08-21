package fi.helsinki.cs.gradubot.utility.codeutils;

/**
 * Created by joza on 2.11.2014.
 */
public class MinMaxValues {
    private int min;
    private int max;

    public MinMaxValues() {
    }

    public MinMaxValues(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

}
