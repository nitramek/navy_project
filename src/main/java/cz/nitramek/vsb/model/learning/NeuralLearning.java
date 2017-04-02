package cz.nitramek.vsb.model.learning;


import java.util.ArrayList;
import java.util.List;

import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.model.NeuralNetwork;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@RequiredArgsConstructor
@Slf4j
public abstract class NeuralLearning {

    @Getter
    protected final List<Tuple<double[], double[]>> trainingSet;

    @Getter
    protected boolean isLearning;

    @Getter
    protected int epoch;

    protected int trainingSetIndex;

    protected float epochError;

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


    public Tuple<double[], double[]> learningStep(NeuralNetwork ann) {
        preStep();
        Tuple<double[], double[]> result = actualStep(ann);
        postStep();
        return result;
    }

    protected void preStep() {
        if (trainingSetIndex == 0) {
            epochError = 0;
        }
    }

    protected abstract Tuple<double[], double[]> actualStep(NeuralNetwork ann);

    protected void postStep() {
        trainingSetIndex++;
        if (trainingSetIndex >= trainingSet.size()) {
            trainingSetIndex = 0;
            epoch++;
        }
    }
}
