package cz.nitramek.vsb.evolution;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SomaAllToOne extends EvolutionAlgorithm {

    private final double PRT;
    private final double pathLength;
    private final double step;
    private Individual best;

    public SomaAllToOne(List<Boundary> boundaries, int generationSize, int maximumGeneration,
                        EvaluatingFunction evaluatingFunction, boolean discrete, double PRT, double pathLength,
                        double step, double minDiv) {
        super(boundaries, generationSize, maximumGeneration, evaluatingFunction, discrete);
        this.PRT = PRT;
        this.pathLength = pathLength;
        this.step = step;
        this.getGeneration()
                .forEach(i -> i.updateFitness(this.getEvaluatingFunction()));
        this.setManipulation(this::perturbate);
    }

    /**
     * Default parameters PRT 0.1, PathLength 3, step = 0.11, minDiv -1
     */
    public SomaAllToOne(List<Boundary> boundaries, int generationSize, int maximumGeneration, EvaluatingFunction
            evaluatingFunction, boolean discrete) {
        this(boundaries, generationSize, maximumGeneration, evaluatingFunction, discrete, 0.1, 3, 0.11, -1);
    }

    private Individual perturbate(Individual individual) {
        final double[] startParams = individual.getParameters();
        Individual bestJump = individual;
        bestJump.updateFitness(this.getEvaluatingFunction());
        double params[] = individual.getParameters().clone();
        for (double t = step; t <= pathLength; t += step) {
            int[] prtVector = genPRTVector();
            for (int i = 0; i < getDimension(); i++) {
                params[i] = startParams[i] + (best.getParam(i) - startParams[i]) * t * prtVector[i];
                if (isDiscrete()) {
                    params[i] = (int) params[i];
                }
            }
            Individual jump = new Individual(params);
            jump.updateFitness(this.getEvaluatingFunction());
            if (jump.getFitness() < bestJump.getFitness()) {
                bestJump = jump;
            }
        }
        return bestJump;
    }

    @Override
    public void advance() {
        this.best = this.getBest();
        super.advance();
    }

    public int[] genPRTVector() {
        while (true) {
            int[] prt = IntStream.range(0, super.getDimension())
                    .mapToDouble(i -> Math.random())
                    .mapToInt(rand -> rand < this.PRT ? 1 : 0)
                    .toArray();
            if (Arrays.stream(prt).anyMatch(i -> i == 1)) {
                return prt;
            }
        }

    }
}
