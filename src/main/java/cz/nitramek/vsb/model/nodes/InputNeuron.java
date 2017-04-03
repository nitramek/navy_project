package cz.nitramek.vsb.model.nodes;


import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
public class InputNeuron extends Neuron {


    @Getter
    private double input;

    public InputNeuron(String id, double input, TransferFunction transferFunction) {
        super(id, transferFunction);
        this.input = input;
        this.setLastValue(input);
    }

    @Override
    public double process() {
        fireNeuronValuePass(input);
        return input;
    }

    public void setInput(double input) {
        this.input = input;
        this.setLastValue(input);
    }

    @Override
    public double processNoListeners() {
        return input;
    }
}
