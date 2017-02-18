package cz.nitramek.vsb.model;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;

@Data(staticConstructor = "create")
public class Node {
    @NonNull
    private String text = "";

    @NonNull
    private List<Edge> inputEdges = new ArrayList<>();

    @NonNull
    private List<Edge> outputEdges = new ArrayList<>();

}
