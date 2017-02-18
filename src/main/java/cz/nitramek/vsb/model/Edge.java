package cz.nitramek.vsb.model;


import lombok.Data;

@Data(staticConstructor = "create")
public class Edge {

    private double weight;

    private Node outgoing;
    private Node incoming;
}
