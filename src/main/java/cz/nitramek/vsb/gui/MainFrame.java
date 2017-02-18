package cz.nitramek.vsb.gui;


import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.ui.graphicGraph.GraphicGraph;
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
import java.util.Collections;

import javax.swing.*;

import cz.nitramek.vsb.MyNode;
import cz.nitramek.vsb.model.Connection;
import cz.nitramek.vsb.model.InputNeuron;
import cz.nitramek.vsb.model.Neuron;
import cz.nitramek.vsb.model.transfer.TransferFunction;

import static java.util.stream.Collectors.joining;

public class MainFrame extends JFrame {

    private Graph dataGraph;

    public MainFrame() {
        super("Navy!");
        getContentPane().setLayout(new BorderLayout());

        GraphicGraph graph = new GraphicGraph("Tutorial 1");
        String styles = "";
        try {
            Path stylesPath = Paths.get(getClass().getResource("/styles.css").toURI());
            styles = Files.lines(stylesPath).collect(joining());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        graph.addAttribute("ui.stylesheet", styles);
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        Layout layout = Layouts.newLayoutAlgorithm();
        viewer.enableAutoLayout(layout);
        ViewPanel view = viewer.addDefaultView(false);
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'r') {
                    view.getCamera().resetView();
                }
            }
        });

        view.addMouseWheelListener(e -> {
            double diff = e.getPreciseWheelRotation() * e.getScrollAmount() * 0.05;
            double viewPercent = view.getCamera().getViewPercent();
            view.getCamera().setViewPercent(viewPercent + diff);
        });
        GraphMouseManager mouseManager = new GraphMouseManager(graph, view);
        view.addMouseListener(mouseManager);

        getContentPane().add(view, BorderLayout.CENTER);


        InputNeuron inputNeuron = new InputNeuron(10, TransferFunction.BINARY);
        Connection conn = new Connection(inputNeuron);
        conn.setWeight(0);
        Neuron outputNeuron = new Neuron(TransferFunction.BINARY, Collections.singletonList
                (conn));

        MyNode output = MyNode.wrap(graph.addNode("output 1"));

        outputNeuron.setListener(output::setLabel);

        for (Connection connection : outputNeuron.getIncoming()) {
            MyNode input = MyNode.wrap(graph.addNode("A"));
            Edge edge = graph.addEdge("id", input.getNode().getId(), output.getNode().getId(),
                    true);
            edge.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, connection.getWeight());
            connection.getFrom().setListener(input::setLabel);
        }


        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        JButton comp = new JButton("Start computation");
        comp.addActionListener(e -> System.out.println(outputNeuron.process()));
        controlPanel.add(comp);
        getContentPane().add(controlPanel, BorderLayout.EAST);

    }

}
