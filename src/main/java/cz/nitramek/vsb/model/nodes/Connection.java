package cz.nitramek.vsb.model.nodes;


import cz.nitramek.vsb.model.ValueListener;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = "listener")
public class Connection {

    private final Neuron from;
    private final Neuron to;

    private double weight;
    private ValueListener listener;

    public Connection(Neuron from, Neuron to) {
        this.from = from;
        this.to = to;
        this.weight = Math.random();
    }

    public double getIncomingData() {
        fireValuePassed();
        return weight * from.process();
    }

    private void fireValuePassed() {
        if (listener != null) {
            listener.valueChange(weight);
        }
    }
}
