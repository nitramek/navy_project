package cz.nitramek.vsb;

import org.graphstream.graph.Node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "wrap")
public class MyNode {

    public static final String LABEL_ATTRIBUTE_NAME = "ui.label";
    public static final String CLASS_ATTRIBUTE_NAME = "ui.class";

    @Getter
    private final Node node;


    public void setLabel(String label) {
        node.addAttribute(LABEL_ATTRIBUTE_NAME, label);
    }

    public void setLabel(double label) {
        setLabel(String.valueOf(label));
    }

    public void addClass(String clasz) {
        node.addAttribute(CLASS_ATTRIBUTE_NAME, clasz);
    }


}
