package cz.nitramek.vsb;

import org.graphstream.graph.Node;

import java.util.Optional;

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

    public double getLabelAsDouble() {
        return Double.parseDouble(getLabel());
    }

    private String getLabel() {
        return node.getAttribute(LABEL_ATTRIBUTE_NAME);
    }

    public void setLabel(double label) {
        setLabel(String.valueOf(label));
    }

    public void addClass(String clasz) {
        node.addAttribute(CLASS_ATTRIBUTE_NAME, clasz);
    }

    public boolean hasClass(String clasz) {
        return Optional.ofNullable(node.getAttribute(CLASS_ATTRIBUTE_NAME))
                .map(String.class::cast)
                .filter(cls -> cls.equals(clasz))
                .isPresent();
    }


}
