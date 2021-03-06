package cz.nitramek.vsb.model.nodes;

import java.util.ArrayList;
import java.util.List;

import cz.nitramek.vsb.model.ValueListener;
import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(of = "id")
public class Neuron {

    private final TransferFunction transferFunction;
    @Setter(AccessLevel.NONE)
    private final String id;
    @Setter(AccessLevel.NONE)
    private List<Connection> incoming;
    @Setter(AccessLevel.NONE)
    private List<Connection> outgoing;
    private ValueListener listener;

    private double hiddenWeight;
    @Getter
    private double lastValue;

    /**
     * Constructs neuron with empty array of incoming connections
     */
    public Neuron(String id, TransferFunction transferFunction) {
        this.id = id;
        this.transferFunction = transferFunction;
        this.incoming = new ArrayList<>();
        this.outgoing = new ArrayList<>();
        this.hiddenWeight = 1;
    }

    public void fireNeuronValuePass(double value) {
        if (listener != null) {
            listener.valueChange(value);
        }
    }

    public double process() {
        double output = this.incoming.stream()
                .mapToDouble(Connection::getIncomingData)
                .sum();
        output += hiddenWeight;
        output = transferFunction.transfer(output, 1);
        fireNeuronValuePass(output);
        lastValue = output;
        return output;
    }

    public double processNoListeners() {
        double output = this.incoming.stream()
                .mapToDouble(Connection::getIncomingDataNoListener)
                .sum();
        output += hiddenWeight;
        output = transferFunction.transfer(output, 1);
        lastValue = output;
        return output;
    }
}
