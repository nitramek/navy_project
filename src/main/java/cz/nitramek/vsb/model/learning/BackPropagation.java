package cz.nitramek.vsb.model.learning;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.nodes.Connection;
import cz.nitramek.vsb.model.nodes.Neuron;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static java.util.stream.Collectors.toList;

@Slf4j
public class BackPropagation extends NeuralLearning {


    private List<List<LearningConnections>> layers;

    public BackPropagation(List<Tuple<double[], double[]>> trainingSet, NeuralNetwork ann, double acceptedError, int
            maximumEpoch, double learningCoeef) {
        super(trainingSet, ann, acceptedError, maximumEpoch, learningCoeef);
        layers = new ArrayList<>();

        List<LearningConnections> upperConnections = ann.getOutputs().stream()
                .flatMap(n -> n.getIncoming().stream())
                .distinct()
                .map(LearningConnections::new)
                .collect(toList());
        while (!upperConnections.isEmpty()) {
            layers.add(upperConnections);
            upperConnections = upperConnections.stream()
                    .map(l -> l.c)
                    .map(Connection::getFrom)
                    .distinct()
                    .flatMap(n -> n.getIncoming().stream())
                    .distinct()
                    .map(LearningConnections::new)
                    .collect(toList());

        }

    }

    @Override
    public double learnSingleEpoch(List<Tuple<double[], double[]>> epochProgressData) {
        val epochProgress = new ArrayList<Tuple<double[], double[]>>();
        double globalError = 0;
        for (int i = 0; i < trainingSet.size(); i++) {
            double[] input = trainingSet.get(i).getFirst();
            double[] realOutput = ann.processNoListeners(input);
            double[] exceptedOutput = trainingSet.get(i).getSecond();
            List<Neuron> outputs = ann.getOutputs();
            layers.get(0).forEach(lc -> {
                int outIndex = outputs.indexOf(lc.c.getTo());
                double y0 = lc.c.getTo().getLastValue();
                double d = exceptedOutput[outIndex];
                double y1 = lc.c.getFrom().getLastValue();
                lc.deltaTo = (y0 - d) * y0 * (1 - y0);
                double weightDelta = learningCoeff * lc.deltaTo * y1;
                lc.newWeight = lc.c.getWeight() - weightDelta;
                lc.oldWeight = lc.c.getWeight();
                double hiddenDelta = learningCoeff * lc.deltaTo;
                lc.c.getTo().setHiddenWeight(lc.c.getTo().getHiddenWeight() - hiddenDelta);
            });
            for (int j = 1; j < layers.size(); j++) {
                List<LearningConnections> layer = layers.get(j);
                List<LearningConnections> layerBefore = layers.get(j - 1);
                layer.forEach(lc -> {
                    Neuron toNeuron = lc.c.getTo();
                    double y0 = toNeuron.getLastValue();
                    double y1 = lc.c.getFrom().getLastValue();
                    double weightSum = layerBefore.stream()
                            .filter(lb -> lb.c.getFrom().equals(toNeuron))//want only
                            // connected to me
                            .mapToDouble(lb -> lb.deltaTo * lb.c.getWeight())
                            .sum();
                    lc.deltaTo = y0 * (1 - y0) * weightSum;
                    double weightDelta = learningCoeff * lc.deltaTo * y1;
                    lc.newWeight = lc.c.getWeight() - weightDelta;
                    double hiddenDelta = learningCoeff * lc.deltaTo;
                    toNeuron.setHiddenWeight(toNeuron.getHiddenWeight() - hiddenDelta);
                });
            }
            layers.forEach(lcl -> lcl.forEach(lc -> lc.c.setWeight(lc.newWeight)));
            epochProgress.add(Tuple.make(input, realOutput));
            globalError += getErrorForInput(exceptedOutput, realOutput);
        }
        Optional.ofNullable(epochProgressData).ifPresent(epd -> epd.addAll(epochProgress));


        log.info("Global erorr " + globalError);
        epoch++;
        return globalError;
    }

    private double ouputDelta(double expectedOutput, double realOutput) {
        return realOutput - expectedOutput * realOutput * (1 - realOutput);
    }

    private class LearningConnections {
        Connection c;
        double newWeight;
        double deltaTo;
        double oldWeight;

        LearningConnections(Connection connection) {
            this.c = connection;
            newWeight = 0;
            deltaTo = 0;
            oldWeight = 0;
        }
    }

}
