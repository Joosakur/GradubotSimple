package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony;

import fi.helsinki.cs.gradubot.production.optimize.framework.Node;
import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness.AttractivenessEvaluator;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness.CountProduced;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness.FastestStart;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.attractiveness.SupplyNeed;
import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.genetics.Gene;
import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by joza on 9.11.2014.
 */
public class Ant {
    private static int counter = 0;
    public double pherExp;
    public double goForHighestChance;
    private int id;
    public boolean finished = false;
    public Stack<AntNode> path = new Stack<>();
    public AntNode currentNode;
    public Solution solution;
    public List<AttractivenessEvaluator> attractivenessEvaluators;
    public List<Gene> dna = new ArrayList<>();
    public Solution personalBestSolution;
    public double fitness;
    public RequiredTypes requiredTypes;

    public Ant(List<Gene> dna, RequiredTypes requiredTypes) {
        this.dna = dna;
        this.requiredTypes = requiredTypes;
        initFromDna();
        id = counter++;
    }

    public void initFromDna() {
        pherExp = (double) dna.get(0).getValue();
        attractivenessEvaluators = new ArrayList<>();
        attractivenessEvaluators.add(new FastestStart((Double) dna.get(2).getValue()));
        attractivenessEvaluators.add(new CountProduced((Double) dna.get(3).getValue()));
        attractivenessEvaluators.add(new SupplyNeed((Double) dna.get(4).getValue()));
    }

    public Ant(RequiredTypes requiredTypes, List<Double> attractivenessExpList) {
        this.requiredTypes = requiredTypes;
        attractivenessEvaluators = new ArrayList<>();
        attractivenessEvaluators.add(new FastestStart(attractivenessExpList.get(0)));
        attractivenessEvaluators.add(new CountProduced(attractivenessExpList.get(1)));
        attractivenessEvaluators.add(new SupplyNeed(attractivenessExpList.get(2)));
        id = counter++;
    }

    public void reset(){
        finished = false;
        path.clear();
        solution = null;
        currentNode = null;
    }

    public void moveToNode(AntNode node){
        currentNode = node;
        path.push(node);
    }

    public void reverse(){
        path.pop();
        if(currentNode.parent != null)
            currentNode = (AntNode) currentNode.parent;
        else
            throw new RuntimeException("Trying to reverse to null parent");
    }

    public void solutionFound() {
        finished = true;
        solution = new Solution(currentNode, requiredTypes);
        if(personalBestSolution == null || personalBestSolution.getTime() > solution.getTime()) {
            personalBestSolution = solution;
        }
    }

    public void die() {
        finished = true;
    }

    public double getPercentComplete() {
        int mineralSumRequired = 0;
        int gasSumRequired = 0;
        int timeSumRequired = 0;
        int mineralSumCompleted = 0;
        int gasSumCompleted = 0;
        int timeSumCompleted = 0;


        for(UnitType type : requiredTypes.keySet()){
            if(type.getID() == UnitType.UnitTypes.Terran_Command_Center.getID()) continue;

            int minCount = requiredTypes.get(type).getMin();
            mineralSumRequired += type.getMineralPrice()*minCount;
            gasSumRequired += type.getGasPrice()*minCount;
            timeSumRequired += type.getBuildTime()*minCount;

            mineralSumCompleted += type.getMineralPrice() * Math.min(minCount, currentNode.state.countReadyOrUnderProduction(type));
            gasSumCompleted += type.getGasPrice() * Math.min(minCount, currentNode.state.countReadyOrUnderProduction(type));
            timeSumCompleted += type.getBuildTime() * Math.min(minCount, currentNode.state.countReady(type));
        }

        mineralSumCompleted += currentNode.state.minerals;
        gasSumCompleted += currentNode.state.gas;
        if(mineralSumCompleted > mineralSumRequired) mineralSumCompleted = mineralSumRequired;
        if(gasSumCompleted > gasSumRequired) gasSumCompleted = gasSumRequired;

        double resourceCompletion = 1.0 * (mineralSumCompleted + 2*gasSumCompleted)/(mineralSumRequired + 2*gasSumRequired);
        double timeCompletion = 1.0 * timeSumCompleted / timeSumRequired;
        return Math.min(resourceCompletion, timeCompletion);
    }

    public List<AntNode> evaluateChildrenWeights(int currentIteration) {
        List<AntNode> antNodeChildren = new ArrayList<>();
        for(Node node : currentNode.children){
            AntNode antNode =  (AntNode) node;
            antNode.evaporate(currentIteration);
            antNodeChildren.add(antNode);
        }

        for(AntNode antNode : antNodeChildren){
            antNode.setWeight(Math.pow(antNode.getPheromone(), pherExp));
        }

        for(AttractivenessEvaluator evaluator : attractivenessEvaluators){
            List<Double> values = evaluator.calculateAttractivenessValues(antNodeChildren, requiredTypes);
            for(int i=0; i<antNodeChildren.size(); i++){
                AntNode child = antNodeChildren.get(i);
                double weight = child.getWeight() * Math.pow(values.get(i), evaluator.getExp());
                if(weight != weight)
                    System.out.println("nan");
                child.setWeight(weight);
            }
        }

        return antNodeChildren;
    }

    public Node chooseNextAction(int iteration) {
        if(currentNode.children.size() == 1){
            return currentNode.children.get(0);
        }

        List<AntNode> weightedChildren = evaluateChildrenWeights(iteration);

        if(Math.random() < goForHighestChance){ //with a chance go straight to highest weight
            double highestWeight = -Double.MAX_VALUE;
            AntNode highest = null;
            for(AntNode child : weightedChildren){
                if(child.getWeight() > highestWeight){
                    highest = child;
                    highestWeight = child.getWeight();
                }
            }
            return highest;
        }
        else { //use weighted probability to decide
            Node node = Randomizer.weightedRandom(weightedChildren);
            return node;
        }
    }

    public double getFitness() {
        return fitness;
    }
}
