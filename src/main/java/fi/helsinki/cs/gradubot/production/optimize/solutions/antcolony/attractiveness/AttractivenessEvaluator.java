package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness;

import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.AntNode;

import java.util.List;

/**
 * Created by joza on 10.1.2015.
 */
public interface AttractivenessEvaluator {

    List<Double> calculateAttractivenessValues(List<AntNode> nodes, RequiredTypes requiredTypes);

    double getExp();

}
