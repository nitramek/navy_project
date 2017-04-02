package cz.nitramek.vsb.model;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

import cz.nitramek.vsb.model.nodes.Connection;
import cz.nitramek.vsb.model.nodes.InputNeuron;
import cz.nitramek.vsb.model.nodes.Neuron;
import lombok.Data;

import static java.util.Comparator.comparing;

@Data
public class NeuralNetwork {

    private final List<InputNeuron> inputNeurons;
    private final List<Neuron> outputs;

    public NeuralNetwork(List<InputNeuron> inputNeurons, List<Neuron> outputs) {
        this.inputNeurons = inputNeurons;
        this.inputNeurons.sort(comparing(Neuron::getId));
        this.outputs = outputs;
        this.outputs.sort(comparing(Neuron::getId));
    }


    public double[] process(double[] input) {
        if (input.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Input Array must have the same size as the number of input neurons");
        }
        for (int i = 0; i < input.length; i++) {
            inputNeurons.get(i).setInput(input[i]);
        }
        return outputs.stream()
                .mapToDouble(Neuron::process)
                .toArray();
    }

    public double[] processNoListeners(double[] input) {
        if (input.length != inputNeurons.size()) {
            throw new IllegalArgumentException("Input Array must have the same size as the number of input neurons");
        }
        for (int i = 0; i < input.length; i++) {
            inputNeurons.get(i).setInput(input[i]);
        }
        return outputs.stream()
                .mapToDouble(Neuron::processNoListeners)
                .toArray();
    }


    public int getWeightsCount() {
        int weightCount = 0;
        List<Neuron> visitedNeurons = new ArrayList<>();
        Stack<Neuron> stack = new Stack<>();
        stack.addAll(inputNeurons);
        do {
            Neuron visitingNeuron = stack.pop();
            if (!visitedNeurons.contains(visitingNeuron)) {
                visitedNeurons.add(visitingNeuron);
                if (!(visitingNeuron instanceof InputNeuron)) {
                    //input neurons dont have hidden weight
                    weightCount++;
                }
                weightCount += visitingNeuron.getOutgoing().size();
                List<Neuron> nextLayer = visitingNeuron.getOutgoing().stream()
                        .map(Connection::getTo)
                        .filter(n -> !visitedNeurons.contains(n))
                        .filter(n -> !stack.contains(n))
                        .collect(Collectors.toList());
                //get only those whose have not been visited arent yet planned to be visited
                stack.addAll(nextLayer);

            }
        } while (!stack.empty());
        return weightCount;
    }

    public double[] getWeights() {
        List<Neuron> visitedNeurons = new ArrayList<>();
        Stack<Neuron> stack = new Stack<>();
        List<Double> weights = new ArrayList<>();
        stack.addAll(inputNeurons);
        do {
            Neuron visitingNeuron = stack.pop();
            if (!visitedNeurons.contains(visitingNeuron)) {
                visitedNeurons.add(visitingNeuron);
                if (!(visitingNeuron instanceof InputNeuron)) {
                    //input neurons dont have hidden weight
                    weights.add(visitingNeuron.getHiddenWeight());
                }
                visitingNeuron.getOutgoing().forEach(c -> weights.add(c.getWeight()));
                List<Neuron> nextLayer = visitingNeuron.getOutgoing().stream()
                        .map(Connection::getTo)
                        .filter(n -> !visitedNeurons.contains(n))
                        .filter(n -> !stack.contains(n))
                        .collect(Collectors.toList());
                //get only those whose have not been visited arent yet planned to be visited
                stack.addAll(nextLayer);

            }
        } while (!stack.empty());
        return weights.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public void setWeights(double[] weights) {
        List<Neuron> visitedNeurons = new ArrayList<>();
        Stack<Neuron> stack = new Stack<>();
        Queue<Double> weightsQ = Arrays.stream(weights).boxed()
                .collect(Collectors.toCollection(ArrayDeque::new));
        stack.addAll(inputNeurons);
        do {
            Neuron visitingNeuron = stack.pop();
            if (!visitedNeurons.contains(visitingNeuron)) {
                visitedNeurons.add(visitingNeuron);
                if (!(visitingNeuron instanceof InputNeuron)) {
                    //input neurons dont have hidden weight
                    visitingNeuron.setHiddenWeight(weightsQ.poll());
                }
                visitingNeuron.getOutgoing().forEach(c -> c.setWeight(weightsQ.poll()));
                List<Neuron> nextLayer = visitingNeuron.getOutgoing().stream()
                        .map(Connection::getTo)
                        .filter(n -> !visitedNeurons.contains(n))
                        .filter(n -> !stack.contains(n))
                        .collect(Collectors.toList());
                //get only those whose have not been visited arent yet planned to be visited
                stack.addAll(nextLayer);

            }
        } while (!stack.empty());
    }


}
