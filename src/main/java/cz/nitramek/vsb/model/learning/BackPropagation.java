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
//            double d = exceptedOutput[0];
//            double y0 = realOutput[0];
//            double delta = y0 * (1 - y0) * (y0 - d);
//            double hiddenWeight = outputs.get(0).getHiddenWeight();
//            outputs.get(0).setHiddenWeight(hiddenWeight - delta * learningCoeff);
//            for (Connection c : outputs.get(0).getIncoming()) {
//                c.setWeight(c.getWeight() - learningCoeff * delta * c.getFrom().processNoListeners());
//            }
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
