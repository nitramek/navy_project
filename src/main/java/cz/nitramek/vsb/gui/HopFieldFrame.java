package cz.nitramek.vsb.gui;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

public class HopFieldFrame extends JFrame {

    public static final int COL_WIDTH = 50;
    public static final int ROW_WIDTH = 100;
    private static int ROWS = 5;
    private static int COLS = 3;
    private final Canvas canvas;
    private final JButton trainBtn;
    private final JButton guessBtn;

    private Set<double[]> saved;
    private JFrame nextTo;


    public HopFieldFrame(JFrame nextTo, Consumer<ActionEvent> computeCall) {
        super("Hopfield visualization");
        this.nextTo = nextTo;
        saved = new HashSet<>();
        getContentPane().setLayout(new BorderLayout());
        setSize(3 * COL_WIDTH + 20, ROW_WIDTH * 5 + 20);
        canvas = new Canvas();
        add(canvas, BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        trainBtn = new JButton("Train");
        guessBtn = new JButton("Guess");
        controlPanel.add(trainBtn);
        controlPanel.add(guessBtn);
        add(controlPanel, BorderLayout.SOUTH);
        setUpListeners(computeCall);
    }

    private void setUpListeners(Consumer<ActionEvent> computeCall) {
        trainBtn.addActionListener(e -> saved.add(canvas.data));
        guessBtn.addActionListener(computeCall::accept);
    }

    public void visualize(double[] data) {
        canvas.data = data;
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
