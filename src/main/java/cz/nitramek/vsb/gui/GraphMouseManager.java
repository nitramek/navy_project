package cz.nitramek.vsb.gui;


import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.DefaultMouseManager;

import java.awt.event.MouseEvent;
import java.util.Optional;

public class GraphMouseManager extends DefaultMouseManager {
    private GraphicElement selectedElement;

    public GraphMouseManager(GraphicGraph graph, View view) {
        super();
        super.init(graph, view);
    }

    @Override
    protected void mouseButtonPressOnElement(GraphicElement element, MouseEvent event) {
        super.mouseButtonPressOnElement(element, event);
        selectedElement = element;
    }

    public Optional<GraphicElement> getSelectedElement() {
        return Optional.ofNullable(selectedElement);
    }
}
