package cz.nitramek.vsb.model.learning;

import java.util.List;
import java.util.stream.IntStream;

import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.model.NeuralNetwork;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class FixedIncrementsLearning extends NeuralLearning {

    public static final double C = 1;

    public FixedIncrementsLearning(List<Tuple<double[], double[]>> trainingSet, NeuralNetwork ann, int maximumEpoch) {
        super(trainingSet, ann, 0, maximumEpoch);
        ann.setWeights(new double[]{1, 0, 0});
    }

    private static double dot(double[] first, double[] second) {
        return IntStream.range(0, first.length)
                .mapToDouble(i -> first[i] * second[i])
                .sum();
    }

    protected double singleInputLearn(double[] input, double[] expectedOutputVec, double[] realOutputVec) {
        double error = 0;
        double[] extendedInput = new double[]{input[0], input[1], 1};
        if (expectedOutputVec[0] == 0) {
            extendedInput[0] *= -1;
            extendedInput[1] *= -1;
            extendedInput[2] *= -1;
        }
        double[] weights = ann.getWeights();
        if (dot(extendedInput, weights) <= 0) {
            ann.setWeights(IntStream.range(0, extendedInput.length)
                    .mapToDouble(j -> weights[j] + C * extendedInput[j]).toArray());
            error = 1;
        }

        return error;
    }

    @Override
    public double learnSingleEpoch(List<Tuple<double[], double[]>> epochProgressData) {
        double epochError = 0;
        for (val trainingItem : trainingSet) {
            double[] input = trainingItem.getFirst();
            double[] expectedOutputVec = trainingItem.getSecond();
            double[] realOutputVec = ann.process(input);
            double singleInputError = singleInputLearn(input, expectedOutputVec, realOutputVec);
            epochError += singleInputError;
            if (epochProgressData != null)
                epochProgressData.add(Tuple.make(input, realOutputVec));
        }
        epoch++;
        return epochError;
    }
}
