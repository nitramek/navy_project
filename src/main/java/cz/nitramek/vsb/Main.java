package cz.nitramek.vsb;

import javax.swing.*;

import cz.nitramek.vsb.gui.MainFrame;

public class Main {

    public static void main(String... args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 640);
        frame.setVisible(true);

    }
}
