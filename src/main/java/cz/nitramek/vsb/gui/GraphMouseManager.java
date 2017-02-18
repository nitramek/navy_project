package cz.nitramek.vsb.gui;


import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.DefaultMouseManager;

import java.util.Optional;

public class GraphMouseManager extends DefaultMouseManager {
    public GraphMouseManager(GraphicGraph graph, View view) {
        super();
        super.init(graph, view);
    }

    public Optional<GraphicElement> getSelectedElement() {
        return Optional.ofNullable(curElement);
    }
}
