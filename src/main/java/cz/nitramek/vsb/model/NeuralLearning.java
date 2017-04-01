package cz.nitramek.vsb.model;


import java.util.ArrayList;
import java.util.List;

import cz.nitramek.vsb.Tuple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static cz.nitramek.vsb.Utils.sqr;
import static java.lang.String.format;

@RequiredArgsConstructor
@Slf4j
public class NeuralLearning {

    @Getter
    private final List<Tuple<double[], double[]>> trainingSet;

    @Getter
    boolean isLearning;

    @Getter
    int epoch;

    int trainingSetIndex;

    float epochError;

    /**
     * You can learningStep learning process by yourself or you can just press learn
     */
    public void startLearning() {
        this.isLearning = true;
        this.epoch = 0;
        this.epochError = 0;
    }

    public void stopLearning() {
        isLearning = false;
    }

    public List<List<Tuple<double[], double[]>>> learn(NeuralNetwork ann) {
        startLearning();
        val processData = new ArrayList<List<Tuple<double[], double[]>>>();
        do {
            val epochProcess = new ArrayList<Tuple<double[], double[]>>(trainingSet.size() * 3);
            for (int i = 0; i < trainingSet.size(); i++) {
                epochProcess.add(learningStep(ann));
            }
            processData.add(epochProcess);
        } while (epochError > 0);
        stopLearning();
        return processData;
    }

    /**
     * Performs one learningStep in learning process
     */
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
