package cz.nitramek.vsb.model.learning;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.evolution.Boundary;
import cz.nitramek.vsb.evolution.EvolutionAlgorithm;
import cz.nitramek.vsb.evolution.SomaAllToOne;
import cz.nitramek.vsb.model.NeuralNetwork;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class EvolutionLearning extends NeuralLearning {

    private EvolutionAlgorithm evolutionAlgorithm;

    public EvolutionLearning(List<Tuple<double[], double[]>> trainingSet, NeuralNetwork ann, int maximumEpoch) {
        super(trainingSet, ann, 0.3, maximumEpoch);
        List<Boundary> weightBoundaries = Stream.generate(() -> new Boundary(-0.5, 0.5))
                .limit(ann.getWeightsCount())
                .collect(Collectors.toList());
        evolutionAlgorithm = new SomaAllToOne(weightBoundaries, 40, maximumEpoch,
                this::calculateErrorForWeights, false);
    }


    @Override
    public double learnSingleEpoch(List<Tuple<double[], double[]>> epochProgressData) {
        double error = 0;
        evolutionAlgorithm.advance();
        ann.setWeights(evolutionAlgorithm.getBest().getParameters());
        for (val trainingItem : trainingSet) {
            double[] input = trainingItem.getFirst();
            double[] expectedOutputVec = trainingItem.getSecond();
            double[] realOutputVec = ann.processNoListeners(input);
            if (epochProgressData != null) {
                epochProgressData.add(Tuple.make(input, realOutputVec));
            }
            error += getErrorForInput(expectedOutputVec, realOutputVec);
        }
        epoch++;
        log.info("Epoch error from evolution learning " + error);
        return error;
    }

    private double calculateErrorForWeights(double[] weights) {
        ann.setWeights(weights);
        double epochError = 0;
        for (val trainingItem : trainingSet) {
            double[] input = trainingItem.getFirst();
            double[] expectedOutputVec = trainingItem.getSecond();
            double[] realOutputVec = ann.processNoListeners(input);
            epochError += getErrorForInput(expectedOutputVec, realOutputVec);
        }
        return epochError;
    }
}
