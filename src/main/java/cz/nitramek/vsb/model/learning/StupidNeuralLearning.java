package cz.nitramek.vsb.model.learning;

import java.util.List;

import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.nodes.InputNeuron;
import cz.nitramek.vsb.model.nodes.Neuron;
import lombok.extern.slf4j.Slf4j;

import static cz.nitramek.vsb.Utils.sqr;
import static java.lang.String.format;

@Slf4j
public class StupidNeuralLearning extends NeuralLearning {
    public StupidNeuralLearning(List<Tuple<double[], double[]>> trainingSet) {
        super(trainingSet);
    }

    @Override
    public Tuple<double[], double[]> learningStep(NeuralNetwork ann) {
        if (trainingSetIndex == 0) {
            epochError = 0;
        }
        double[] input = trainingSet.get(trainingSetIndex).getFirst();
        double[] expectedOutputVec = trainingSet.get(trainingSetIndex).getSecond();
        double[] realOutputVec = ann.process(input);

        for (int i = 0; i < realOutputVec.length; i++) {
            double realOutput = realOutputVec[i];
            double expectedOutput = expectedOutputVec[i];
            if (realOutput != expectedOutput) {
                double delta = expectedOutput - realOutput;
                epochError += sqr(delta);
                log.info(format("Desired result is %s, actual is %s", expectedOutput, realOutput));
                Neuron outputNeuron = ann.getOutputs().get(i);
                outputNeuron.setHiddenWeight(outputNeuron.getHiddenWeight() + 0.33 * delta);
                outputNeuron.getIncoming().forEach(c -> {
                    double nInput = ((InputNeuron) c.getFrom()).getInput();
                    c.setWeight(c.getWeight() + 0.33 * nInput * delta);
                });
            }
        }

        trainingSetIndex++;
        if (trainingSetIndex >= trainingSet.size()) {
            trainingSetIndex = 0;
            epoch++;
        }
        return Tuple.make(input, realOutputVec);
    }
}
