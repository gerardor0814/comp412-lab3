import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GraphNode {

    private Integer label;
    private Set<GraphNode> successors;
    private Set<GraphNode> predecessors;


    public GraphNode(Integer label) {
        this.label = label;
        this.successors = new HashSet<>();
        this.predecessors = new HashSet<>();
    }

    public void addSuccessor(GraphNode successor) {
        this.successors.add(successor);
    }

    public void addPredecessor(GraphNode predecessor) {
        this.predecessors.add(predecessor);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (GraphNode succesor : this.successors) {
            str.append(succesor.label).append(",");
        }
        if (!str.isEmpty()) {
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public Integer getLabel() {
        return label;
    }
}
