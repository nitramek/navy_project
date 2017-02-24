package cz.nitramek.vsb.model;


import java.util.Comparator;
import java.util.List;

import lombok.Data;

@Data
public class NeuralNetwork {

    private final List<InputNeuron> inputs;

    private final List<Neuron> outputs;


    public double[] process() {
        return outputs.stream()
                .sorted(Comparator.comparing(Neuron::getId))
                .mapToDouble(Neuron::process)
                .toArray();
    }
}
