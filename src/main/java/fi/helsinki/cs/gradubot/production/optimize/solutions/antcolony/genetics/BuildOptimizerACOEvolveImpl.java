package fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.genetics;import fi.helsinki.cs.gradubot.production.optimize.framework.RequiredTypes;import fi.helsinki.cs.gradubot.production.optimize.framework.Solution;import fi.helsinki.cs.gradubot.production.optimize.framework.branchcutting.BranchCutStrategy;import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.Ant;import fi.helsinki.cs.gradubot.production.optimize.solutions.antcolony.BuildOptimizerBasicAntColonyImpl;import fi.helsinki.cs.gradubot.utility.codeutils.Randomizer;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.List;public class BuildOptimizerACOEvolveImpl extends BuildOptimizerBasicAntColonyImpl {    private final double SMALL_MUTATION_PROBABILITY = 0.1;    private final double SMALL_MUTATION_RATIO = 0.1;    private final double BIG_MUTATION_PROBABILITY = 0.05;    private int EVOLVE_FREQUENCY = 5;    private int nextEvolution = EVOLVE_FREQUENCY;    public BuildOptimizerACOEvolveImpl(RequiredTypes requiredTypes, List<BranchCutStrategy> branchCutStrategies, Solution externalBestSolution) {        super(requiredTypes, branchCutStrategies, externalBestSolution);    }    @Override    public void populateAnts(int antCount){        ants = new ArrayList<>(antCount);        for(int i=0; i<antCount; i++){            List<Gene> dna = new ArrayList<>(5);            for(int j=0; j<5; j++){                dna.add(randomizeGene(j));            }            ants.add(new Ant(dna, requiredTypes));        }    }    @Override    public void runIteration() {        super.runIteration();        if(iteration == nextEvolution){            List<Ant> ants = getAnts();            List<Ant> nextGen = getNextGeneration(ants);            for(Ant ant : nextGen){                ant.fitness = 0;            }            setAnts(nextGen);            EVOLVE_FREQUENCY++;            nextEvolution += EVOLVE_FREQUENCY;        }    }    private List<Ant> getNextGeneration(List<Ant> prevGen) {        int size = prevGen.size();        sortByFitness(prevGen);        List<Ant> nextGen = new ArrayList<>(size);        nextGen.addAll(prevGen.subList(0, (int) (0.2*size)));        prevGen = prevGen.subList(0, (int) (0.8*size));        while (nextGen.size() < size){            Ant parent1 = Randomizer.pickRandom(prevGen);            Ant parent2 = Randomizer.pickRandom(prevGen);            if(parent1 == parent2 || parent1.fitness + parent2.fitness == 0)                continue;            Ant child = mate(parent1, parent2);            nextGen.add(child);        }        return nextGen;    }    private Ant mate(Ant parent1, Ant parent2) {        List<Gene> childDna = new ArrayList<>(parent1.dna.size());        for(int i=0; i<parent1.dna.size(); i++){            double p1prob = parent1.fitness / (parent1.fitness + parent2.fitness);            Gene gene = Math.random() < p1prob ? parent1.dna.get(i) : parent2.dna.get(i);            if(Math.random() < BIG_MUTATION_PROBABILITY)                gene = randomizeGene(i);            if(Math.random() < SMALL_MUTATION_PROBABILITY)                gene.mutate(SMALL_MUTATION_RATIO);            childDna.add(gene);        }        Ant child = new Ant(childDna, requiredTypes);        child.initFromDna();        return child;    }    private Gene randomizeGene(int i) {        switch (i){            case 0:                return new DoubleGene(0.5, 1.5, false);            case 1:                return new DoubleGene(0.0, 0.85, 0.35, 0.25);            case 2:                return Math.random() < 0.5 ? new DoubleGene(0.0, 3.5, 1.8, 0.5) : new DoubleGene(3.5, 8.0, 5.5, 1.0);            case 3:                return new DoubleGene(0.0, 2.0, 1.0, 0.5);            case 4:                return new DoubleGene(0.0, 1.5, 0.5, 0.3);        }        throw new IllegalArgumentException("no case for gene "+i);    }    private void sortByFitness(List<Ant> prevGen) {        Collections.sort(prevGen, new Comparator<Ant>() {            @Override            public int compare(Ant o1, Ant o2) {                if(o1.getFitness() > o2.getFitness())                    return -1;                if(o1.getFitness() < o2.getFitness())                    return 1;                return 0;            }        });    }}