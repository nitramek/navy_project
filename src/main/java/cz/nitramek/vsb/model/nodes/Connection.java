package cz.nitramek.vsb.model.nodes;


import cz.nitramek.vsb.model.ValueListener;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"from", "to"})
public class Connection {

    private final Neuron from;
    private final Neuron to;

    private double weight;
    private ValueListener listener;

    public Connection(Neuron from, Neuron to) {
        this.from = from;
        this.to = to;
        this.weight = Math.random() - 0.5;
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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getIncomingDataNoListener() {
        return weight * from.processNoListeners();
    }
}
