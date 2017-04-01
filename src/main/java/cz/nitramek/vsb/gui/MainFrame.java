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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import cz.nitramek.vsb.MyNode;
import cz.nitramek.vsb.Tuple;
import cz.nitramek.vsb.model.Connection;
import cz.nitramek.vsb.model.InputNeuron;
import cz.nitramek.vsb.model.NeuralLearning;
import cz.nitramek.vsb.model.NeuralNetwork;
import cz.nitramek.vsb.model.Neuron;
import cz.nitramek.vsb.model.transfer.TransferFunction;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;


@SuppressWarnings("CodeBlock2Expr")
@Slf4j
public class MainFrame extends JFrame {


    public static final String INPUT_NODE_PREFIX = "Input_";
    public static final String OUTPUT_NODE_PREFIX = "Output_";
    public static final String INTERNAL_NODE_PREFIX = "Internal_";
    public static final String EDGE_PREFIX = "Edge_";
    public static final String NEURON_ATTRIBUTE = "ui.neuron";
    private final JLabel epochLabel;
    private final JButton switchLearningButton;
    private final JTextField inputField;
    private File selectedTrainingSetFile;
    private boolean isLearning = true;
    private GraphicGraph graph;
    private GraphMouseManager graphMouseManager;
    private int itemNextId = 0;
    private GraphicNode selectedNode;
    private TransferFunction selectedTransferFunction;
    private TransferFunction proxyTransferFunction = (input, k) -> selectedTransferFunction.transfer(input, k);
    private NeuralNetwork nn;
    private int maxEpochs = 10;
    private boolean autoLearn = true;
    private NeuralLearning neuralLearning;

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
                this.selectedTrainingSetFile = selectedFile;
                openLearningDialog();
            }
        });
        gbc.gridy++;
        JButton openFileDialogButton = new JButton("Open inputNeurons");
        openFileDialogButton.addActionListener(e -> {
            fileChooser.showOpenDialog(MainFrame.this);
        });
        controlPanel.add(openFileDialogButton, gbc);

        gbc.gridy++;
        switchLearningButton = new JButton("Stop learning");
        switchLearningButton.addActionListener(e -> {
            if (isLearning) {
                switchLearningButton.setText("Start Learning");
            } else {
                switchLearningButton.setText("Stop learning");
            }
            isLearning = !isLearning;
        });
        controlPanel.add(switchLearningButton, gbc);

        gbc.gridy++;
        JButton openLearningDialog = new JButton("Learning options");
        openLearningDialog.addActionListener(e -> openLearningDialog());
        controlPanel.add(openLearningDialog, gbc);

        gbc.gridy++;
        epochLabel = new JLabel("Current epoch: 0");
        controlPanel.add(epochLabel, gbc);

        gbc.gridy++;
        controlPanel.add(new JLabel("Input:"));

        gbc.gridy++;
        inputField = new JTextField();
        controlPanel.add(inputField, gbc);

    }

    private void openLearningDialog() {
        JDialog learningDialog = new JDialog(this, "Learning", true);
        learningDialog.getContentPane().setLayout(new GridLayout(3, 2));
        learningDialog.add(new JLabel("Max epochs"));
        SpinnerModel model = new SpinnerNumberModel(10, 0, 1000, 1);
        model.addChangeListener(l -> maxEpochs = (Integer) model.getValue());
        JSpinner spinner = new JSpinner(model);
        learningDialog.add(spinner);

        learningDialog.add(new JLabel("Auto learnWithTeacher"));
        JCheckBox autoLearnCheckbox = new JCheckBox();
        autoLearnCheckbox.setSelected(true);
        autoLearnCheckbox.addChangeListener(l -> autoLearn = autoLearnCheckbox.isSelected());
        learningDialog.add(autoLearnCheckbox);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(ee -> learningDialog.setVisible(false));
        learningDialog.add(cancel);


        JButton start = new JButton("Start Learning");
        start.addActionListener(ae -> {
            isLearning = true;
            switchLearningButton.setText("Stop learning");
            nn = prepareANN();
            neuralLearning = new NeuralLearning(FileData.parseInputFile(selectedTrainingSetFile));
            neuralLearning.startLearning();
            MainFrame.this.startComputation(ae);
            learningDialog.setVisible(false);
        });
        learningDialog.add(start);

        learningDialog.pack();
        learningDialog.setVisible(true);
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
                if (e.getKeyCode() == KeyEvent.VK_F5) {
                    startComputation(null);
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
        System.out.println(format("Added edge from %s to %s", this.selectedNode.getId(), toNode.getId()));
    }

    private void startComputation(ActionEvent actionEvent) {
        Thread workingThread = new Thread(() -> {
            System.out.println("Neuron network process");
            if (nn == null) {
                nn = prepareANN();
            }
            if (!isLearning) {
                double[] input = Pattern.compile(";").splitAsStream(inputField.getText())
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                double[] process = nn.process(input);
                System.out.println("Output is " + Arrays.toString(process));
            } else {
                //learning
                if (autoLearn) {
                    log.info("Staring auto learning");
                    val processData = neuralLearning.learn(nn);
                    saveLearningProcess(processData);
                } else {
                    log.info("Learning single step");
                    isLearning = neuralLearning.isLearning();
                    if (isLearning) {
                        neuralLearning.learningStep(nn);
                        updateEpochLabel(neuralLearning.getEpoch());
                    }
                }
            }
        });
        workingThread.start();
    }

    private int getInputNodeId(Node n) {
        return Integer.parseInt(n.getId().replace(INPUT_NODE_PREFIX, ""));
    }

    private NeuralNetwork prepareANN() {
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
        return new NeuralNetwork(inputNeurons, outputNeurons);
    }

    private void saveLearningProcess(List<List<Tuple<double[], double[]>>> outputVectors) {

        isLearning = false;
        switchLearningButton.setText("Start learning");
        JOptionPane.showMessageDialog(this, format("Learning took %s epochs", neuralLearning.getEpoch()));

        try (FileWriter fw = new FileWriter("output.neuronLearn", false)) {
            for (int epoch = 0; epoch < outputVectors.size(); epoch++) {
                fw.append("Epoch");
                fw.append(String.valueOf(epoch));
                fw.append(":\n");
                for (val singleProcessData : outputVectors.get(epoch)) {
                    fw.append("In: ");
                    String input = Arrays.stream(singleProcessData.getFirst())
                            .mapToObj(String::valueOf).
                                    collect(joining(", "));
                    fw.append(input);
                    fw.append("\n");
                    fw.append("Out: ");
                    String output = Arrays.stream(singleProcessData.getSecond())
                            .mapToObj(String::valueOf)
                            .collect(joining(", "));
                    fw.append(output);
                }
                fw.append("\n---------------next-epoch-----------------------\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateEpochLabel(int epoch) {
        epochLabel.setText("Current epoch: " + epoch);
    }

    private Node addNodeAction(String prefix) {
        Node node = graph.addNode(prefix + itemNextId);
        node.addAttribute(MyNode.LABEL_ATTRIBUTE_NAME, String.valueOf(itemNextId));
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
