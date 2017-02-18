package cz.nitramek.vsb.model;


import java.util.Collections;

import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.Getter;
import lombok.Setter;

public class InputNeuron extends Neuron {

    @Setter
    @Getter
    private double input;

    public InputNeuron(double input, TransferFunction transferFunction) {
        super(transferFunction, Collections.emptyList());
        this.input = input;
    }

    @Override
    public double process() {
        fireNeuronValuePass(input);
        return this.input;
    }
}
