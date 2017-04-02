package cz.nitramek.vsb.evolution;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import lombok.Getter;
import lombok.NonNull;


public class Individual {


    private static int idGenerator = 0;
    @Getter
    private final double parameters[];
    private final int id;
    @Getter
    private double fitness;

    public Individual(double[] parameters, double fitness) {
        this(parameters);
        this.fitness = fitness;

    }

    public Individual(double[] parameters) {
        this.parameters = parameters.clone();
        this.id = Individual.idGenerator++;
    }

    public static Individual generate(List<Boundary> boundaries, Random random, boolean discrete) {
        double[] parameters = boundaries.stream()
                .mapToDouble(p -> discrete ? p.randomInt(random) : p.randomDouble(random))
                .toArray();
        return new Individual(parameters);
    }

    public int getDimension() {
        return this.parameters.length;
    }

    public double getParam(int index) {
        return this.parameters[index];
    }

    public void setParam(int index, double param) {
        this.parameters[index] = param;
    }

    public void replaceParam(int index, DoubleUnaryOperator operator) {
        this.parameters[index] = operator.applyAsDouble(this.parameters[index]);
    }


    public void replaceParam(double[] parameters) {
        assert parameters.length == this.parameters.length;
        System.arraycopy(this.parameters, 0, parameters, 0, this.parameters.length);
    }

    public double getFitness(@NonNull EvaluatingFunction evaluatingEvaluatingFunction) {
        this.updateFitness(evaluatingEvaluatingFunction);
        return fitness;
    }

    public void updateFitness(@NonNull EvaluatingFunction evaluatingEvaluatingFunction) {
        this.fitness = evaluatingEvaluatingFunction.getValue(this.getParameters());
    }

    public Individual clone() {
        return new Individual(this.getParameters().clone(), this.fitness);
    }

    @Override
    public String toString() {
        return "cz.nitramek.vsb.evolution.Individual{" +
                "parameters=" + Arrays.toString(parameters) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Individual that = (Individual) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
