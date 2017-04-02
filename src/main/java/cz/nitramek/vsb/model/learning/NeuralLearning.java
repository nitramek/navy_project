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

    protected final NeuralNetwork ann;
    protected final double acceptedError;
    protected final int maximumEpoch;
    @Getter
    protected int epoch;


    public List<List<Tuple<double[], double[]>>> autoLearn() {
        val progressData = new ArrayList<List<Tuple<double[], double[]>>>();
        epoch = 0;
        double epochError;
        do {
            val epochProgressData = new ArrayList<Tuple<double[], double[]>>();
            epochError = learnSingleEpoch(epochProgressData);
            progressData.add(epochProgressData);
        } while (epochError > acceptedError && epoch < maximumEpoch);
        return progressData;
    }

    /**
     * @param epochProgressData if you want to get information about epoch progress
     * @return epoch error
     */
    public abstract double learnSingleEpoch(List<Tuple<double[], double[]>> epochProgressData);

    public double learnSingleEpoch() {
        return learnSingleEpoch(null);
    }


}
