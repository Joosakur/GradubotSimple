package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness;

/**
 * Created by joza on 6.8.2016.
 */
public abstract class AbstractAttractivenessEvaluator implements AttractivenessEvaluator {
    protected double exp = 1;

    public AbstractAttractivenessEvaluator(double exp) {
        this.exp = exp;
    }

    @Override
    public double getExp() {
        return exp;
    }
}
