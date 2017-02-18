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
    private final List<Connection> incoming;

    double process() {
        return this.incoming.stream()
                .mapToDouble(c -> c.getWeight() * c.getOutgoing().process())
                .sum();
    }

}
