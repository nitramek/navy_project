package cz.nitramek.vsb.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import cz.nitramek.vsb.gui.MainFrame;
import cz.nitramek.vsb.model.nodes.Connection;
import cz.nitramek.vsb.model.nodes.Neuron;
import lombok.val;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;


public class HopFieldNetwork extends NeuralNetwork {

    private final List<Connection> connections;

    public HopFieldNetwork(List<Neuron> neurons,
                           Collection<double[]> trainingSet) {
        super(Collections.emptyList(), neurons);
        connections = neurons.stream()
                .flatMap(n -> n.getOutgoing().stream())
                .distinct()
                .collect(toList());
        getOutputs().forEach(n -> n.setHiddenWeight(0));
        connections.forEach(c -> c.setWeight(0));

        for (val item : trainingSet) {
            connections.forEach(c -> {
                int i = connectionFromId(c);
                int j = connectionToId(c);
                double weight = c.getWeight();
                c.setWeight(weight + item[i] * item[j]);
            });

        }
    }

    private Connection findConnectionBetween(int fromId, int toId) {
        return connections.stream()
                .filter(c -> connectionFromId(c) == fromId)
                .filter(c -> connectionToId(c) == toId)
                .findAny()
                .orElseGet(() ->
                        connections.stream()
                                .filter(c -> connectionFromId(c) == toId)
                                .filter(c -> connectionToId(c) == fromId)
                                .findAny()
                                .orElseThrow(() ->
                                        new RuntimeException(format("There was no connection between %s-%s",
                                                fromId, toId))
                                ));
    }


    private int connectionFromId(Connection c) {
        String id = c.getFrom().getId().replace(MainFrame.OUTPUT_NODE_PREFIX, "");
        return Integer.parseInt(id);
    }

    private int connectionToId(Connection c) {
        String id = c.getTo().getId().replace(MainFrame.OUTPUT_NODE_PREFIX, "");
        return Integer.parseInt(id);
    }

    @Override
    public double[] process(double[] input) {

        boolean changed = true;
        double[] mi = input.clone();
        double[] mi_1 = input.clone();
        IntStream.range(0, input.length)
                .forEach(i -> getOutputs().get(i).setLastValue(mi[i]));
        while (changed) {
            for (int i = 0; i < getOutputs().size(); i++) {
                Neuron neuron = getOutputs().get(i);
                double output = neuron.getIncoming().stream()
                        .filter(p -> !p.getFrom().equals(neuron))
                        .mapToDouble(this::processConectionFrom)
                        .sum();
                output += neuron.getOutgoing().stream()
                        .filter(p -> !p.getTo().equals(neuron))
                        .mapToDouble(this::processConnectionTo)
                        .sum();
                output = neuron.getTransferFunction().transfer(output, 1);
                neuron.setLastValue(output);
                mi_1[i] = output;
            }
            changed = !Arrays.equals(mi, mi_1);
            System.arraycopy(mi_1, 0, mi, 0, mi.length);
        }
        getOutputs().forEach(n -> n.fireNeuronValuePass(n.getLastValue()));
        connections.forEach(Connection::fireValuePassed);
        return mi;
    }

    private double processConectionFrom(Connection c) {
        return c.getWeight() * c.getFrom().getLastValue();
    }

    private double processConnectionTo(Connection c) {
        return c.getWeight() * c.getTo().getLastValue();
    }

    @Override
    public double[] processNoListeners(double[] input) {
        return super.processNoListeners(input);
    }


}
