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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import cz.nitramek.vsb.MyNode;
import cz.nitramek.vsb.model.Connection;
import cz.nitramek.vsb.model.InputNeuron;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.Neuron;
import cz.nitramek.vsb.model.transfer.TransferFunction;
import javafx.util.Pair;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class MainFrame extends JFrame {

    public static final String INPUT_NODE_PREFIX = "Input_";
    public static final String OUTPUT_NODE_PREFIX = "Output_";
    public static final String INTERNAL_NODE_PREFIX = "Internal_";
    public static final String EDGE_PREFIX = "Edge_";
    public static final String NEURON_ATTRIBUTE = "ui.neuron";
    public static final Pattern SINGLE_INPUT_PATTERN = Pattern.compile("\\s*\\[([0-9]+),([0-9]+)\\]\\s*");
    public static final Pattern INPUT_SEPARATOR_PATTERN = Pattern.compile(";");
    private boolean isLearning = true;
    private GraphicGraph graph;
    private GraphMouseManager graphMouseManager;
    private int itemNextId = 0;
    private GraphicNode selectedNode;

    private TransferFunction selectedTransferFunction;
    private TransferFunction proxyTransferFunction = input -> selectedTransferFunction.transfer(input);

    private Map<String, List<Double>> inputs = new HashMap<>();
    private int inputIndex = 0;

    private NeuralNetwork nn;

    public MainFrame() {
        super("Navy!");
        getContentPane().setLayout(new BorderLayout());

        ViewPanel view = setupGraph();
        getContentPane().add(view, BorderLayout.CENTER);

        getContentPane().add(new JLabel("" +
                        "<html>" +
                        "r - go to default view, c - select first node, c - select second one pres c to connect, " +
                        "d - select element to delete it, " +
                        "<br>" +
                        "Input format is [id, value];[id, value], next line means another input, " +
                        "which can be run with pressing Start computation again, you can add output this way too" +
                        "</html>"),
                BorderLayout.NORTH);


        JPanel controlPanel = new JPanel(new GridBagLayout());
        getContentPane().add(controlPanel, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        ComboItem[] comboItems = {
                new ComboItem(TransferFunction.BINARY, "Binary"),
                new ComboItem(TransferFunction.PERCEPTRON, "Perceptron"),
                new ComboItem(TransferFunction.HYPERBOLIC, "Hyperbolic"),
                new ComboItem(TransferFunction.LOGISTIC, "Logistic"),
        };
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
        cleanButton.addActionListener(e -> {
            graph.clear();
            setGraphStyles();
            itemNextId = 0;
        });
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


        JFileChooser fileChooser = new JFileChooser(Paths.get(".").toAbsolutePath().toFile());
        fileChooser.setFileFilter(new FileNameExtensionFilter(".neuron", "neuron"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addActionListener(e -> {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                parseInputFile(selectedFile);
            }
        });
        gbc.gridy++;
        JButton openFileDialogButton = new JButton("Open inputs");
        openFileDialogButton.addActionListener(e -> {
            fileChooser.showOpenDialog(MainFrame.this);
        });
        controlPanel.add(openFileDialogButton, gbc);

        gbc.gridy++;
        JButton switchLearning = new JButton("Stop learning");
        switchLearning.addActionListener(e -> {
            if (isLearning) {
                switchLearning.setText("Stop Learning");
            } else {
                switchLearning.setText("Start learning");
            }
            isLearning = !isLearning;
        });
        controlPanel.add(switchLearning, gbc);


    }

    private ViewPanel setupGraph() {
        graph = new GraphicGraph("Tutorial 1");
        setGraphStyles();

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

        Layout layout = Layouts.newLayoutAlgorithm();
        layout.setForce(layout.getForce() / 10);
        viewer.enableAutoLayout(layout);
        ViewPanel view = viewer.addDefaultView(false);
        view.addKeyListener(new KeyAdapter() {
            public boolean autoLayout = true;

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'r') { //reset
                    view.getCamera().resetView();
                    viewer.enableAutoLayout();
                }
                if (e.getKeyChar() == 'c') { //make connection
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

        view.addMouseWheelListener(e -> { //zooming
            double diff = e.getPreciseWheelRotation() * e.getScrollAmount() * 0.05;
            double viewPercent = view.getCamera().getViewPercent();
            view.getCamera().setViewPercent(viewPercent + diff);
        });
        graphMouseManager = new GraphMouseManager(graph, view);
        view.addMouseListener(graphMouseManager);
        return view;
    }

    private void parseInputFile(File selectedFile) {
        try {
            inputs = Files.lines(selectedFile.toPath())
                    .flatMap(INPUT_SEPARATOR_PATTERN::splitAsStream)
                    .map(input -> {
                        Matcher m = SINGLE_INPUT_PATTERN.matcher(input);
                        if (!m.find()) {
                            String msg = format("Input %s cannot be matched with desired format", input);
                            JOptionPane.showMessageDialog(MainFrame.this, msg);
                            throw new RuntimeException(msg);
                        }
                        String id = m.group(1);
                        Double value = Double.parseDouble(m.group(2));
                        return new Pair<>(id, value);
                    })
                    .collect(groupingBy(Pair::getKey, mapping(Pair::getValue, toList())));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    private void setGraphStyles() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass()
                .getResourceAsStream("/styles.css")))) {
            String styles = br.lines()
                    .collect(joining());
            graph.addAttribute("ui.stylesheet", styles);
        } catch (FileSystemNotFoundException | IOException e) {
            e.printStackTrace();
        }
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
        connection.setListener(v -> {
            edge.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, format("%.2f", connection.getWeight()));
        });
        itemNextId++;
        System.out.println(format("Added edge from %s to %s", this.selectedNode
                .getId(), toNode.getId()));
    }

    private void startComputation(ActionEvent actionEvent) {
        Thread workingThread = new Thread(() -> {
            if (nn == null) {
                List<InputNeuron> inputNeurons = new ArrayList<>();
                List<Neuron> outputNeurons = new ArrayList<>();
                for (Node n : graph.getNodeSet()) {
                    MyNode node = MyNode.wrap(n);
                    if (node.hasClass("input")) {
                        InputNeuron inputNeuron = n.getAttribute(NEURON_ATTRIBUTE);
                        if (!inputs.isEmpty()) {
                            inputNeuron.setInput(inputs.get(n.getId().replace(INPUT_NODE_PREFIX, "")).get
                                    (inputIndex));
                        }
                        inputNeurons.add(inputNeuron);
                    } else if (node.hasClass("output")) {
                        Neuron neuron = n.getAttribute(NEURON_ATTRIBUTE);
                        outputNeurons.add(neuron);
                    }
                }
                nn = new NeuralNetwork(inputNeurons, outputNeurons);

            }
            System.out.println("Neuron network process");
            double[] outputVector = nn.process();

            if (!inputs.isEmpty()) {
                if (isLearning) {
                    nn.getOutputs().sort(Comparator.comparing(Neuron::getId));
                    int order = 0;
                    for (Neuron outputNeuron : nn.getOutputs()) {
                        String outputId = outputNeuron.getId().replace(OUTPUT_NODE_PREFIX, "");
                        Double desiredResult = inputs.get(outputId).get(inputIndex);
                        double actualResult = outputVector[order++];
                        System.out.println(format("Desired result is %s, actual is %s", desiredResult, actualResult));
                        double delta = desiredResult - actualResult;
                        if (desiredResult != actualResult) {
                            outputNeuron.setHiddenWeight(outputNeuron.getHiddenWeight() + 0.33 * delta);
                            outputNeuron.getIncoming().forEach(c -> {
                                double input = ((InputNeuron) c.getFrom()).getInput();
                                c.setWeight(c.getWeight() + 0.33 * input * delta);
                            });
                        }
                    }
                }
                inputIndex++; //move to next input/ouput vector
                inputIndex %= inputs.values().iterator().next().size(); //make it cycle
                nn.getInputs().forEach(in -> {

                    in.setInput(inputs.get(in.getId().replace(INPUT_NODE_PREFIX, "")).get(inputIndex));
                });
            }
            try (FileWriter fileWriter = new FileWriter("output.json", false)) {

                String input = nn.getInputs().stream()
                        .mapToDouble(InputNeuron::getInput)
                        .mapToObj(Double::toString)
                        .collect(joining(", "));
                String output = Arrays.stream(outputVector)
                        .sorted()
                        .mapToObj(Double::toString)
                        .collect(joining(", "));
                fileWriter.write(format("Input: %s\nResult: %s", input, output));

            } catch (IOException e) {
                e.printStackTrace();
            }
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
        InputNeuron neuron = new InputNeuron(node.getId(), 1, proxyTransferFunction);

        neuron.setListener(v -> {
            //sleep(300);
            String inputId = node.getId().substring(INPUT_NODE_PREFIX.length());
            String formatted = format("[Id: %s][%s]", inputId, v);
            node.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, formatted);
        });
        node.setAttribute(MyNode.CLASS_ATTRIBUTE_NAME, "input");
        node.setAttribute(NEURON_ATTRIBUTE, neuron);
    }

    private void addInternalNode() {
        Node node = addNodeAction(INTERNAL_NODE_PREFIX);
        Neuron neuron = new Neuron(node.getId(), proxyTransferFunction);
        neuron.setListener(v -> {
            //sleep(300);
            node.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, format("%.2f ", v));
        });
        node.setAttribute(NEURON_ATTRIBUTE, neuron);
        node.setAttribute(MyNode.CLASS_ATTRIBUTE_NAME, "internal");
    }

    private void addOutputNode() {
        Node node = addNodeAction(OUTPUT_NODE_PREFIX);
        Neuron neuron = new Neuron(node.getId(), proxyTransferFunction);
        neuron.setListener(v -> {
            //sleep(300);
            String inputId = node.getId().substring(OUTPUT_NODE_PREFIX.length());
            String formatted = format("[Id: %s][%s]", inputId, v);
            node.setAttribute(MyNode.LABEL_ATTRIBUTE_NAME, format("%.2f", v));
        });
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

        public ComboItem(TransferFunction item, String label) {
            this(item, () -> label);
        }

        @Override
        public String toString() {
            return stringSupplier.get();
        }
    }
}
