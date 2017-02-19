package cz.nitramek.vsb.model;

import java.util.List;

import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Neuron {

    private final TransferFunction transferFunction;

    @Setter(AccessLevel.NONE)
    private List<Connection> incoming;

    private ValueListener listener;

    public Neuron(TransferFunction transferFunction, List<Connection> incoming) {
        this.transferFunction = transferFunction;
        this.incoming = incoming;
    }

    protected void fireNeuronValuePass(double value) {
        if (listener != null) {
            listener.valueChange(value);
        }
    }

    public double process() {
        double output = this.incoming.stream()
                .mapToDouble(c -> c.getWeight() * c.getFrom().process())
                .sum();
        fireNeuronValuePass(output);
        return output;
    }

}
