package cz.nitramek.vsb.gui;


import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.Node;
import lombok.Getter;

public class NeuralNetworkTransformer {
    private static final int INPUT_OFFSET_X = 20;
    private static final int INPUT_Y = 5;

    private int nextInputPositionX = 5;

    private Object graphParent;

    @Getter
    private NeuralNetwork neuralNetwork;

    public NeuralNetworkTransformer() {

    }

    public Node addInput(String data) {
        nextInputPositionX += INPUT_OFFSET_X;

        Node node = Node.create();
        neuralNetwork.getInputs().add(node);
        return node;
    }

}
