package cz.nitramek.vsb.gui;


public interface GraphCreatorCalls {
    int createInput();

    int createOutput();

    void createConnectionBetweenOutputs(int from, int to, boolean directional);

    void clear();
}
