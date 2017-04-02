package cz.nitramek.vsb.model.learning;

import java.util.List;

import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.nodes.InputNeuron;
import cz.nitramek.vsb.model.nodes.Neuron;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static cz.nitramek.vsb.Utils.sqr;
import static java.lang.String.format;

@Slf4j
public class DeltaNeuralLearning extends NeuralLearning {

    public DeltaNeuralLearning(List<Tuple<double[], double[]>> trainingSet, NeuralNetwork ann, int maximumEpoch) {
        super(trainingSet, ann, 0, maximumEpoch);
    }

    protected double singleInputLearn(double[] input, double[] expectedOutputVec, double[] realOutputVec) {
        double error = 0;
        for (int i = 0; i < realOutputVec.length; i++) {
            double realOutput = realOutputVec[i];
            double expectedOutput = expectedOutputVec[i];
            if (realOutput != expectedOutput) {
                double delta = expectedOutput - realOutput;
                error += sqr(delta);
                log.info(format("Desired result is %s, actual is %s", expectedOutput, realOutput));
                Neuron outputNeuron = ann.getOutputs().get(i);
                outputNeuron.setHiddenWeight(outputNeuron.getHiddenWeight() + 0.33 * delta);
                outputNeuron.getIncoming().forEach(c -> {
                    double nInput = ((InputNeuron) c.getFrom()).getInput();
                    c.setWeight(c.getWeight() + 0.33 * nInput * delta);
                });
            }
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
