package cz.nitramek.vsb.model.nodes;


import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
public class InputNeuron extends Neuron {

    @Setter
    @Getter
    private double input;

    public InputNeuron(String id, double input, TransferFunction transferFunction) {
        super(id, transferFunction);
        this.input = input;
    }

    @Override
    public double process() {
        fireNeuronValuePass(input);
        return input;
    }

    @Override
    public double processNoListeners() {
        return input;
    }
}
