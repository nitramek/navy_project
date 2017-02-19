package cz.nitramek.vsb.gui;


import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicEdge;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.*;

import cz.nitramek.vsb.MyNode;
import cz.nitramek.vsb.model.InputNeuron;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.transfer.TransferFunction;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class MainFrame extends JFrame {

    public static final String INPUT_NODE_PREFIX = "Input_";
    public static final String OUTPUT_NODE_PREFIX = "Output_";
    public static final String INTERNAL_NODE_PREFIX = "Internal_";
    public static final String EDGE_PREFIX = "Edge_";
    private final GraphicGraph graph;
    private final GraphMouseManager graphMouseManager;
    private int itemNextId = 0;
    private GraphicNode selectedNode;

    public MainFrame() {
        super("Navy!");
        getContentPane().setLayout(new BorderLayout());

        graph = new GraphicGraph("Tutorial 1");


        try {
            Path stylesPath = Paths.get(getClass().getResource("/styles.css").toURI());
            String styles = Files.lines(stylesPath).collect(joining());
            graph.addAttribute("ui.stylesheet", styles);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        Layout layout = Layouts.newLayoutAlgorithm();
        layout.setForce(layout.getForce() / 4);
        viewer.enableAutoLayout(layout);
        ViewPanel view = viewer.addDefaultView(false);
        view.addKeyListener(new KeyAdapter() {
            public boolean autoLayout = true;

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 'r') {
                    view.getCamera().resetView();
                    viewer.enableAutoLayout();
                }
                if (e.getKeyChar() == 'c') {
                    if (selectedNode == null) {
                        graphMouseManager.getSelectedElement().ifPresent(ge -> {
                            if (ge instanceof GraphicNode) {
                                selectedNode = (GraphicNode) ge;
                                System.out.println("Selected node " + selectedNode.getId());
                            } else {
                                System.out.println("Node not selected");
                            }
                        });
                    } else {
                        graphMouseManager.getSelectedElement().ifPresent(ge -> {
                            graph.addEdge(EDGE_PREFIX + itemNextId, selectedNode.getId(), ge
                                    .getId(), true);
                            itemNextId++;
                            System.out.println(format("Added edge from %s to %s", selectedNode
                                    .getId(), ge.getId()));
                            selectedNode = null;
                        });
                    }
                }
                if (e.getKeyChar() == 'd') {
                    graphMouseManager.getSelectedElement().ifPresent(ge -> {
                        if (ge instanceof GraphicEdge) {
                            graph.removeEdge(ge.getId());
                        }
                        if (ge instanceof GraphicNode) {
                            graph.removeNode(ge.getId());
                        }
                    });
                }
                if (e.getKeyChar() == 'a') {
                    if (autoLayout) {
                        System.out.println("Auto layout disabled");
                        viewer.disableAutoLayout();
                        view.getCamera().setAutoFitView(autoLayout);
                    } else {
                        System.out.println("Auto layout enabled");
                        view.getCamera().setAutoFitView(autoLayout);
                        viewer.enableAutoLayout();
                    }
                    autoLayout = !autoLayout;
                }
            }
        });

        view.addMouseWheelListener(e -> {
            double diff = e.getPreciseWheelRotation() * e.getScrollAmount() * 0.05;
            double viewPercent = view.getCamera().getViewPercent();
            view.getCamera().setViewPercent(viewPercent + diff);
        });
        graphMouseManager = new GraphMouseManager(graph, view);
        view.addMouseListener(graphMouseManager);

        getContentPane().add(view, BorderLayout.CENTER);

        getContentPane().add(new JLabel("r - go to default view, c - select first node, press c, " +
                        "select second one pres c to connect, d - select element to delete it"),
                BorderLayout.NORTH);


        JPanel controlPanel = new JPanel(new GridBagLayout());
        getContentPane().add(controlPanel, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        JButton comp = new JButton("Start computation");
        comp.addActionListener(this::startComputation);
        controlPanel.add(comp, gbc);
        JButton cleanButton = new JButton("Clean graph");
        cleanButton.addActionListener(e -> graph.clear());
        gbc.gridy = 1;
        controlPanel.add(cleanButton, gbc);
        gbc.gridy = 2;
        JButton addInputButton = new JButton("Add input node");
        addInputButton.addActionListener(e -> this.addNodeAction(OUTPUT_NODE_PREFIX).addAttribute
                (MyNode.CLASS_ATTRIBUTE_NAME, "input"));
        controlPanel.add(addInputButton, gbc);

        gbc.gridy = 3;
        JButton addInternalButton = new JButton("Add internal node");
        addInternalButton.addActionListener(e -> this.addNodeAction(INTERNAL_NODE_PREFIX)
                .addAttribute(MyNode.CLASS_ATTRIBUTE_NAME, "internal"));
        controlPanel.add(addInternalButton, gbc);

        gbc.gridy = 4;
        JButton addOutputButton = new JButton("Add output node");
        addOutputButton.addActionListener(e -> this.addNodeAction(INPUT_NODE_PREFIX).addAttribute
                (MyNode.CLASS_ATTRIBUTE_NAME, "output"));
        controlPanel.add(addOutputButton, gbc);
    }

    private Node addNodeAction(String prefix) {
        Node node = graph.addNode(prefix + itemNextId);
        node.addAttribute(MyNode.LABEL_ATTRIBUTE_NAME, itemNextId);
        itemNextId++;
        return node;
    }

    private void startComputation(ActionEvent e) {
        transferToModel();
    }

    private NeuralNetwork transferToModel() {
        List<InputNeuron> inputNeurons = graph.getNodeSet().stream()
                .filter(n -> n.getId().startsWith(INPUT_NODE_PREFIX))
                .map(MyNode::wrap)
                .map(this::parseInputNeuronIntoNode)
                .collect(toList());
        return new NeuralNetwork(null, null);
    }

    private InputNeuron parseInputNeuronIntoNode(MyNode node) {
        node.addClass("input");
        double value = node.getLabelAsDouble();
        InputNeuron inputNeuron = new InputNeuron(value, TransferFunction.BINARY);
        inputNeuron.setListener(node::setLabel);
        return inputNeuron;
    }

}
