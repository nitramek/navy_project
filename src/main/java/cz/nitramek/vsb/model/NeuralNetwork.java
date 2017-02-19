package cz.nitramek.vsb.model;


import java.util.List;

import lombok.Data;

@Data
public class NeuralNetwork {

    private final List<InputNeuron> inputs;

    private final List<Neuron> outputs;


    public double[] process() {
        return outputs.stream()
                .mapToDouble(Neuron::process)
                .toArray();
    }
}
