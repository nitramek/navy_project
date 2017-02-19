package cz.nitramek.vsb.model;


import lombok.Data;

@Data
public class Connection {

    private final Neuron from;

    private double weight;
    private ValueListener listener;

    public Connection(Neuron from) {
        this.from = from;
        this.weight = Math.random();
    }
}
