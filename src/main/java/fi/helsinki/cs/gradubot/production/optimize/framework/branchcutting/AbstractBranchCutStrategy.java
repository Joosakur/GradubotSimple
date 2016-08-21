package fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting;

/**
 * Created by joza on 2.8.2016.
 */
public abstract class AbstractBranchCutStrategy implements BranchCutStrategy{

    protected int called;
    protected int cut;

    @Override
    public int timesCut() {
        return cut;
    }

    @Override
    public int timesCalled() {
        return called;
    }
}
