package cz.nitramek.vsb.gui;


import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicElement;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.*;

import cz.nitramek.vsb.MyNode;
import cz.nitramek.vsb.model.Connection;
import cz.nitramek.vsb.model.InputNeuron;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.Neuron;
import cz.nitramek.vsb.model.transfer.TransferFunction;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class MainFrame extends JFrame {

    public static final String INPUT_NODE_PREFIX = "Input_";
    public static final String OUTPUT_NODE_PREFIX = "Output_";
    public static final String INTERNAL_NODE_PREFIX = "Internal_";
    public static final String EDGE_PREFIX = "Edge_";
    public static final String NEURON_ATTRIBUTE = "ui.neuron";
    private final GraphicGraph graph;
    private final GraphMouseManager graphMouseManager;
    private int itemNextId = 0;
    private GraphicNode selectedNode;

    private TransferFunction selectedTransferFunction;

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
            public void keyPressed(KeyEvent e) {
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
                            createConnectionBetween(selectedNode, ge);
                            selectedNode = null;
                        });
                    }
                }
                if (e.getKeyChar() == 'd') {
                    //TODO removal
//                    graphMouseManager.getSelectedElement().ifPresent(ge -> {
//                        if (ge instanceof GraphicEdge) {
//                            GraphicEdge edge = (GraphicEdge) ge;
//                            graph.removeEdge(edge.from, edge.to);
//                            Connection connection = edge.getAttribute("connection");
//                            connection.getFrom().getIncoming().remove(connection);
//                            connection.getFrom().getOutgoing().remove(connection);
//                        }
//                        else if (ge instanceof GraphicNode) {
//                            graph.removeNode(ge.getId());
//                        }
//                    });
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
        ComboItem[] comboItems = {new
                ComboItem(TransferFunction.BINARY, () -> "Binary")};
        JComboBox<ComboItem> transferFunctionJComboBox = new JComboBox<>(comboItems);

        transferFunctionJComboBox.addActionListener(e -> {
            selectedTransferFunction = transferFunctionJComboBox.getModel()
                    .getElementAt(transferFunctionJComboBox.getSelectedIndex()).item;
        });
        selectedTransferFunction = comboItems[0].item;
        controlPanel.add(transferFunctionJComboBox, gbc);


        gbc.gridy++;
        JButton comp = new JButton("Start computation");
        comp.addActionListener(this::startComputation);
        controlPanel.add(comp, gbc);

        gbc.gridy++;
        JButton cleanButton = new JButton("Clean graph");
        cleanButton.addActionListener(e -> graph.clear());
        controlPanel.add(cleanButton, gbc);


        gbc.gridy++;
        JButton addInputButton = new JButton("Add input node");
        addInputButton.addActionListener(e -> this.addInputNode());
        controlPanel.add(addInputButton, gbc);

        gbc.gridy++;
        JButton addInternalButton = new JButton("Add internal node");
        addInternalButton.addActionListener(e -> this.addInternalNode());
        controlPanel.add(addInternalButton, gbc);

        gbc.gridy++;
        JButton addOutputButton = new JButton("Add output node");
        addOutputButton.addActionListener(e -> this.addOutputNode());
        controlPanel.add(addOutputButton, gbc);

        gbc.gridy++;
        JTextField valueChangeField = new JTextField();
        controlPanel.add(valueChangeField, gbc);


        gbc.gridy++;
        JButton changeInputValue = new JButton("Change input value");
        changeInputValue.addActionListener(e -> {
            graphMouseManager.getSelectedElement().ifPresent(ge -> {
                Neuron neuron = ge.getAttribute(NEURON_ATTRIBUTE);
                if (neuron instanceof InputNeuron) {
                    InputNeuron inputNeuron = (InputNeuron) neuron;
                    double inputValue = Double.parseDouble(valueChangeField.getText());
                    inputNeuron.setInput(inputValue);
                    ge.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, String.valueOf(valueChangeField
                            .getText()));
                    System.out.println("Changed seleceted neuron input value");
                }
            });
        });
        controlPanel.add(changeInputValue, gbc);
    }

    private void createConnectionBetween(GraphicNode fromNode, GraphicElement toNode) {
        Edge edge = graph.addEdge(EDGE_PREFIX + itemNextId,
                this.selectedNode.getId(), toNode.getId(), true);
        Neuron fromNeuron = fromNode.getAttribute(NEURON_ATTRIBUTE);
        Neuron toNeuron = toNode.getAttribute(NEURON_ATTRIBUTE);
        Connection connection = new Connection(fromNeuron, toNeuron);
        fromNeuron.getOutgoing().add(connection);
        toNeuron.getIncoming().add(connection);
        edge.setAttribute("connection", connection);
        connection.setListener(v -> edge.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, String.valueOf
                (connection.getWeight())));
        itemNextId++;
        System.out.println(format("Added edge from %s to %s", this.selectedNode
                .getId(), toNode.getId()));
    }

    private void startComputation(ActionEvent actionEvent) {
        Thread workingThread = new Thread(() -> {
            List<InputNeuron> inputNeurons = new ArrayList<>();
            List<Neuron> outputNeurons = new ArrayList<>();
            for (Node n : graph.getNodeSet()) {
                MyNode node = MyNode.wrap(n);
                if (node.hasClass("input")) {
                    InputNeuron inputNeuron = n.getAttribute(NEURON_ATTRIBUTE);
                    inputNeurons.add(inputNeuron);
                } else if (node.hasClass("output")) {
                    Neuron neuron = n.getAttribute(NEURON_ATTRIBUTE);
                    outputNeurons.add(neuron);
                }
            }
            NeuralNetwork nn = new NeuralNetwork(inputNeurons, outputNeurons);
            double[] outputVector = nn.process();
            System.out.println(Arrays.toString(outputVector));
        });
        workingThread.start();
    }

    private Node addNodeAction(String prefix) {
        Node node = graph.addNode(prefix + itemNextId);
        node.addAttribute(MyNode.LABEL_ATTRIBUTE_NAME, itemNextId);
        itemNextId++;
        return node;
    }

    private void addInputNode() {
        Node node = addNodeAction(INPUT_NODE_PREFIX);
        InputNeuron neuron = new InputNeuron(node.getId(), 1, selectedTransferFunction);

        neuron.setListener(v -> node.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, String.valueOf(v)));
        node.setAttribute(MyNode.CLASS_ATTRIBUTE_NAME, "input");
        node.setAttribute(NEURON_ATTRIBUTE, neuron);
    }

    private void addInternalNode() {
        Node node = addNodeAction(INTERNAL_NODE_PREFIX);
        Neuron neuron = new Neuron(node.getId(), selectedTransferFunction);
        neuron.setListener(v -> node.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, String.valueOf(v)));
        node.setAttribute(NEURON_ATTRIBUTE, neuron);
        node.setAttribute(MyNode.CLASS_ATTRIBUTE_NAME, "internal");
    }

    private void addOutputNode() {
        Node node = addNodeAction(OUTPUT_NODE_PREFIX);
        Neuron neuron = new Neuron(node.getId(), selectedTransferFunction);
        neuron.setListener(v -> node.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, String.valueOf(v)));
        node.setAttribute(NEURON_ATTRIBUTE, neuron);
        node.setAttribute(MyNode.CLASS_ATTRIBUTE_NAME, "output");
    }


    static class ComboItem {
        TransferFunction item;
        Supplier<String> stringSupplier;

        public ComboItem(TransferFunction item, Supplier<String> stringSupplier) {
            this.item = item;
            this.stringSupplier = stringSupplier;
        }

        @Override
        public String toString() {
            return stringSupplier.get();
        }
    }
}
