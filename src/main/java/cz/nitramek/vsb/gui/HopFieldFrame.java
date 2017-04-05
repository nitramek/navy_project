package cz.nitramek.vsb.gui;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

public class HopFieldFrame extends JFrame {

    public static final int COL_WIDTH = 50;
    public static final int ROW_WIDTH = 50;
    private static int ROWS = 5;
    private static int COLS = 5;
    private final Canvas canvas;
    private final JButton storeBtn;
    private final JButton guessBtn;
    private final JButton resetBtn;
    private final JButton clearCanvasBtn;
    private final GraphCreatorCalls graphCreatorCalls;
    private Set<double[]> saved;
    private JFrame nextTo;


    public HopFieldFrame(JFrame nextTo, Consumer<ActionEvent> computeCall, GraphCreatorCalls graphCreatorCalls) {
        super("Hopfield visualization");
        this.nextTo = nextTo;
        this.graphCreatorCalls = graphCreatorCalls;
        saved = new HashSet<>();
        getContentPane().setLayout(new BorderLayout());
        setSize(COLS * COL_WIDTH + 10, ROW_WIDTH * ROWS + 80);
        canvas = new Canvas();
        add(canvas, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new GridLayout(2, 2));
        storeBtn = new JButton("Store");
        guessBtn = new JButton("Guess");
        resetBtn = new JButton("Reset mem");
        clearCanvasBtn = new JButton("Clear");
        controlPanel.add(guessBtn);
        controlPanel.add(clearCanvasBtn);
        controlPanel.add(storeBtn);
        controlPanel.add(resetBtn);
        add(controlPanel, BorderLayout.SOUTH);
        setUpListeners(computeCall);
    }

    public void createANN() {
        IntStream.range(0, ROWS * COLS)
                .forEach(i -> graphCreatorCalls.createOutput());
        for (int i = 0; i < ROWS * COLS; i++) {
            for (int j = 0; j < ROWS * COLS; j++) {
                if (i != j) {
                    graphCreatorCalls.createConnectionBetweenOutputs(i, j, false);
                }
            }
        }
    }

    private void setUpListeners(Consumer<ActionEvent> computeCall) {
        storeBtn.addActionListener(e -> {
            System.out.println("Saved: ");
            logToConsole();
            saved.add(canvas.data);
        });
        guessBtn.addActionListener(computeCall::accept);
        resetBtn.addActionListener(e -> {
            System.out.println("Cleared network memory");
            saved.clear();
        });
        clearCanvasBtn.addActionListener(e -> {
            Arrays.fill(canvas.data, -1);
            canvas.repaint();
        });
    }

    private void logToConsole() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (canvas.data[row * COLS + col] > 0) {
                    System.out.print('o');
                } else {
                    System.out.print(' ');
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public void visualize(double[] data) {
        canvas.data = data;
        System.out.println("Guess pattern was: ");
        logToConsole();
        canvas.repaint();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        Point ref = nextTo.getLocation();
        ref.translate(nextTo.getWidth(), 0);
        setLocation(ref);
    }

    public double[] getData() {
        return canvas.data;
    }

    public Collection<double[]> getSaved() {
        return saved;
    }

    public void clear() {
        saved.clear();
        Arrays.fill(canvas.data, -1);
    }

    public static class Canvas extends JComponent {


        volatile double data[] = new double[ROWS * COLS];

        public Canvas() {
            Arrays.fill(data, -1);
            addMouseListener(new MouseInputAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = e.getY() / ROW_WIDTH;
                    int col = e.getX() / COL_WIDTH;
                    if (data[row * COLS + col] > 0) {
                        data[row * COLS + col] = -1;
                    } else {
                        data[row * COLS + col] = 1;
                    }
                    Canvas.this.repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.clearRect(0, 0, this.getWidth(), this.getHeight());
            g.setColor(Color.black);
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (data[row * COLS + col] > 0) {
                        g.fillRect(col * COL_WIDTH, row * ROW_WIDTH, (col + 1) * COL_WIDTH, (row + 1) * ROW_WIDTH);
                    } else {
                        g.clearRect(col * COL_WIDTH, row * ROW_WIDTH, (col + 1) * COL_WIDTH, (row + 1) * ROW_WIDTH);
                        g.drawRect(col * COL_WIDTH, row * ROW_WIDTH, (col + 1) * COL_WIDTH, (row + 1) * ROW_WIDTH);
                    }
                }
            }
        }


    }
}
