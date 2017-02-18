package cz.nitramek.vsb.model;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;

@Data(staticConstructor = "create")
public class NeuralNetwork {

    @NonNull
    private List<Node> inputs = new ArrayList<>();

    @NonNull
    private List<Node> outputs = new ArrayList<>();
}
