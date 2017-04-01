package cz.nitramek.vsb.model;


import java.util.List;

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
                .sorted(comparing(Neuron::getId))
                .mapToDouble(Neuron::process)
                .toArray();
    }
}
