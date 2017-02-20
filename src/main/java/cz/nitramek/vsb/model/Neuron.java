package cz.nitramek.vsb.model;

import java.util.ArrayList;
import java.util.List;

import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(exclude = {"incoming", "transferFunction", "outgoing"})
public class Neuron {

    private final TransferFunction transferFunction;
    @Setter(AccessLevel.NONE)
    private final String id;
    @Setter(AccessLevel.NONE)
    private List<Connection> incoming;
    @Setter(AccessLevel.NONE)
    private List<Connection> outgoing;
    private ValueListener listener;

    /**
     * Constructs neuron with empty array of incoming connections
     */
    public Neuron(String id, TransferFunction transferFunction) {
        this.id = id;
        this.transferFunction = transferFunction;
        this.incoming = new ArrayList<>();
        this.outgoing = new ArrayList<>();
    }

    protected void fireNeuronValuePass(double value) {
        if (listener != null) {
            listener.valueChange(value);
        }
    }

    public double process() {
        double output = this.incoming.stream()
                .mapToDouble(Connection::getIncomingData)
                .sum();
        fireNeuronValuePass(output);
        return output;
    }

}
