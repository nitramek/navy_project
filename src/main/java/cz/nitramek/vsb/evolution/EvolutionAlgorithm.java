package cz.nitramek.vsb.evolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


public class EvolutionAlgorithm {

    @Getter
    private final List<Boundary> boundaries;
    @Getter
    private final int generationSize;

    @Getter
    private final boolean discrete;
    @Getter
    private final int maximumGeneration;
    @Getter
    @Setter
    @NonNull
    protected List<Individual> generation;
    @Getter
    private int generationIndex = 1;
    @Getter
    @Setter
    @NonNull
    private EvaluatingFunction evaluatingFunction;
    @NonNull
    private Function<Individual, Individual> manipulation = Function.identity();


    public EvolutionAlgorithm(List<Boundary> boundaries, int generationSize, int maximumGeneration,
                              EvaluatingFunction evaluatingFunction, boolean discrete) {
        this.boundaries = boundaries;
        this.generationSize = generationSize;
        this.discrete = discrete;
        this.evaluatingFunction = evaluatingFunction;
        this.maximumGeneration = maximumGeneration;
        Random random = new Random();

        this.generation = IntStream.range(0, this.generationSize)
                .mapToObj(i -> Individual.generate(boundaries, random, discrete))
                .collect(Collectors.toList());
    }


    public void advance() {
        if (!this.isFinished()) {
            this.generation = this.generation.stream()
                    .map(this.manipulation)
                    .collect(Collectors.toList());
            generationIndex++;
        }
    }

    public int getDimension() {
        return this.boundaries.size();
    }

    public boolean isFinished() {
        return this.generationIndex > this.maximumGeneration;
    }

    protected Individual checkBoundaries(Individual individual) {
        double[] parameters = individual.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = this.boundaries.get(i).getInRange(parameters[i]);
        }
        individual.replaceParam(parameters);
        individual.updateFitness(this.evaluatingFunction);
        return individual;
    }

    public void setManipulation(Function<Individual, Individual> manipulation) {
        this.setManipulations(Collections.singletonList(manipulation));
    }

    public void setManipulations(List<Function<Individual, Individual>> manipulations) {
        this.manipulation = manipulations.stream()
                .reduce(Function.identity(), Function::andThen);
        this.manipulation = this.manipulation.andThen(this::checkBoundaries);
    }


    public Individual getBest() {
        return getGeneration().stream()
                .min(Comparator.comparing(i -> i.getFitness(this.evaluatingFunction)))
                .orElseThrow(() -> new RuntimeException("No individualks"));
    }
}
