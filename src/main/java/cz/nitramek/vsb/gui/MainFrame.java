package cz.nitramek.vsb.gui;


import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.*;

import static java.util.stream.Collectors.joining;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("Navy!");
        getContentPane().setLayout(new BorderLayout());

        Graph graph = new SingleGraph("Tutorial 1");
        String styles = "";
        try {
            Path stylesPath = Paths.get(getClass().getResource("/styles.css").toURI());
            styles = Files.lines(stylesPath).collect(joining());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        graph.addAttribute("ui.stylesheet", styles);
        Node a = graph.addNode("A");
        a.addAttribute("ui.label", "A");
        graph.addNode("B");
        graph.addNode("C");
        Edge edge = graph.addEdge("AB", "A", "B");
        edge.addAttribute("ui.label", "AB");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        Layout layout = Layouts.newLayoutAlgorithm();
        viewer.enableAutoLayout(layout);
        ViewPanel view = viewer.addDefaultView(false);
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyChar() == 'r'){
                    view.getCamera().resetView();
                }
            }
        });
        view.addMouseWheelListener(e -> {
            double diff = e.getPreciseWheelRotation() * e.getScrollAmount() * 0.05;
            double viewPercent = view.getCamera().getViewPercent();
            view.getCamera().setViewPercent(viewPercent + diff);
        });


        getContentPane().add(view, BorderLayout.CENTER);


        JPanel controlPanel = new JPanel();

    }

}
