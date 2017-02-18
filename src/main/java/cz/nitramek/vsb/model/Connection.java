package cz.nitramek.vsb.model;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Connection {

    private final Neuron outgoing;

    private final Neuron incoming;

    private double weight;
}
