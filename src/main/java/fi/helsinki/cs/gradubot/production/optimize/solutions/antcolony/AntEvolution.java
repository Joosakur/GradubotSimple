package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony;

/*
import fi.helsinki.cs.gradubot.production.optimize.framework.BuildOptimizer;
import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;
import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.CutIfExceedsTime;
import fi.helsinki.cs.gradubot.production.optimize.framework.evaluation.EvaluationStrategyTimeImpl;
import fi.helsinki.cs.gradubot.production.optimize.framework.evaluation.SolutionEvaluator;
import fi.helsinki.cs.gradubot.production.optimize.framework.evaluation.SolutionEvaluatorSingleStrategyImpl;
import fi.helsinki.cs.gradubot.production.optimize.framework.goals.GoalChecker;
import fi.helsinki.cs.gradubot.production.optimize.solutions.simpleSolution.BuildOptimizerSimpleImpl;
import fi.helsinki.cs.gradubot.utility.AbstractAnytimeWorker;
import fi.helsinki.cs.gradubot.utility.codeutils.MinMaxValues;
import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;
import jnibwapi.types.UnitType;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class AntEvolution extends AbstractAnytimeWorker<Solution> {

    public static final int ANT_COUNT = 86;
    public static final int EVOLUTION_FREQUENCY = 256;
    public static final int RESET_FREQUENCY = 256;
    public static final int MAX_GENERATIONS = 30;
    public static final int STRAIGHT_TO_NEXT_GENERATION_COUNT = 30;
    public static final int PRODUCES_OFFSPRING_COUNT = 80;
    public static final double MUTATION_CHANGE = 0.1;

    private int round = 0;
    private int generation;

    private List<GoalChecker> goalCheckers;
    private SolutionEvaluator solutionEvaluator = new SolutionEvaluatorSingleStrategyImpl(new EvaluationStrategyTimeImpl());
    private BuildOptimizerBasicAntColonyImpl antColony;

    double initialGuessTime;
    private List<Ant> ants;
    private Map<UnitType, MinMaxValues> requiredTypes;
    private Solution globalBest;

    public static List<PrintWriter> writers;

    public AntEvolution(List<GoalChecker> goalCheckers){
        this.goalCheckers = goalCheckers;
        initAntColony();
        //initWriters();
    }

    private void initAntColony() {
        Solution initSolution = calculateInitialGuess(goalCheckers);
        initialGuessTime = initSolution.getTime();
        List<BranchCutStrategy> branchCutStrategies = new ArrayList<>();
        branchCutStrategies.add(new CutIfExceedsTime());
        antColony = new BuildOptimizerBasicAntColonyImpl(goalCheckers, solutionEvaluator, branchCutStrategies, initSolution);
        requiredTypes = antColony.getRequiredTypes();
        if(ants==null)
            createAnts(requiredTypes);
        antColony.setAnts(ants);
        antColony.start();
    }


    private Solution calculateInitialGuess(List<GoalChecker> goalCheckers) {
        BuildOptimizer buildOptimizer = new BuildOptimizerSimpleImpl(goalCheckers);
        while (!buildOptimizer.hasFinished()) buildOptimizer.startOrContinue(1000);
        return buildOptimizer.getResult();
    }

    private void createAnts(Map<UnitType, MinMaxValues> requiredTypes) {
        ants = new ArrayList<>(ANT_COUNT);
        for(int i=0; i<ANT_COUNT; i++){
            Ant ant = new Ant(requiredTypes);
            ants.add(ant);
        }
    }

    long time;

    @Override
    public void runIteration() {
        if(round==0) time = System.currentTimeMillis();
        round++;

        if(round % EVOLUTION_FREQUENCY == 0) {
            ants = evolution();
            antColony.setAnts(ants);
            generation++;
            System.out.println(
                    generation + ";"
                            + getGeneAverage(0,0) + ";"
                            + getGeneAverage(0,1) + ";"
                            + getGeneAverage(1,0) + ";"
                            + getGeneAverage(2,0) + ";"
                            + getGeneAverage(3,0) + ";"
                            + getGeneAverage(4,0) + ";"
            );
        }
        if(round % RESET_FREQUENCY == 0) {
            initAntColony();
            generation=0;
        }

        antColony.runIteration();
    }

    private double getGeneAverage(int i, int j){
        double sum = 0;
        for(Ant ant : ants){
            sum += ant.dna.get(i).getGenes()[j].getValue().doubleValue();
        }
        return sum / ants.size();
    }



    private List<Ant> evolution(){
        List<Ant> nextGeneration = new ArrayList<>(ANT_COUNT);
        List<Ant> parents = new ArrayList<>(PRODUCES_OFFSPRING_COUNT);

        Collections.sort(ants, new Comparator<Ant>() {
            @Override
            public int compare(Ant a1, Ant a2) {
                return -Double.compare(a1.fitness, a2.fitness);
            }
        });

        for(int i=0; i<ANT_COUNT; i++){
            Ant ant = ants.get(i);
            if(i<STRAIGHT_TO_NEXT_GENERATION_COUNT) {
                ant.personalBestSolution = null;
                nextGeneration.add(ants.get(i));
            }
            if(i<PRODUCES_OFFSPRING_COUNT) parents.add(ants.get(i));
        }

        int parentMax = PRODUCES_OFFSPRING_COUNT / 10;
        while (nextGeneration.size() < ANT_COUNT){
            if(parentMax < PRODUCES_OFFSPRING_COUNT) parentMax++;
            Ant parent1 = Randomizer.pickRandom(parents.subList(0,parentMax));
            Ant parent2 = Randomizer.pickRandom(parents.subList(0,parentMax));
            while (parent1 == parent2) parent2 = Randomizer.pickRandom(parents.subList(0,parentMax));
            Ant child = mate(parent1, parent2);
            nextGeneration.add(child);
        }


        for(Ant ant : ants){
            ant.personalBestSolution = null;
            ant.path.clear();
            ant.currentNode = null;
            ant.personalBestSolution = null;
        }
        for(Ant ant : parents){
            ant.personalBestSolution = null;
            ant.path.clear();
            ant.currentNode = null;
            ant.personalBestSolution = null;
        }
        for(Ant ant : nextGeneration){
            ant.fitness = 0;
            ant.personalBestSolution = null;
            ant.path.clear();
            ant.currentNode = null;
            ant.personalBestSolution = null;
        }

        return nextGeneration;
    }

    private Ant mate(Ant parent1, Ant parent2) {
        Ant child = new Ant(requiredTypes);
        for(int i=0; i<child.dna.size(); i++){
            for(int j=0; j<child.dna.get(i).getGenes().length; j++){
                child.dna.get(i).getGenes()[j].setValue( (Math.random() < 0.5) ? parent1.dna.get(i).getGenes()[j].getValue() : parent2.dna.get(i).getGenes()[j].getValue() );
                if(Math.random() < MUTATION_CHANGE) child.dna.get(i).getGenes()[j].mutate();
            }
        }
        return child;
    }

    @Override
    public Solution getResult() {
        return antColony.getResult();
    }

    private void initWriters() {
        try {
            writers = new ArrayList<>();
            writers.add(new PrintWriter("g0-0.csv", "UTF-8"));
            writers.add(new PrintWriter("g1-0.csv", "UTF-8"));
            writers.add(new PrintWriter("g2-0.csv", "UTF-8"));
            writers.add(new PrintWriter("g3-0.csv", "UTF-8"));
            writers.add(new PrintWriter("g3-1.csv", "UTF-8"));
            writers.add(new PrintWriter("solution-times.csv", "UTF-8"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
*/