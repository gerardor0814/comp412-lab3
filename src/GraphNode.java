import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GraphNode {
    /**
     Type = -1: conflict
            -2: serial
            >0: virtual register
     **/

    private Integer index;
    private String label;
    private Set<Pair<Integer, GraphNode>> successors;
    private Set<Pair<Integer, GraphNode>> predecessors;
    private Set<Integer> usedNodes;
    private int priority;


    public GraphNode(String label, Integer index) {
        this.label = label;
        this.index = index;
        this.successors = new HashSet<>();
        this.predecessors = new HashSet<>();
        this.usedNodes = new HashSet<>();
        this.priority = 0;
    }

    public void addSuccessor(Integer type, GraphNode successor) {
        this.successors.add(new Pair<>(type, successor));
    }

    public Set<Pair<Integer, GraphNode>> getSuccessors() {
        return successors;
    }

    public void addPredecessor(Integer type, GraphNode predecessor) {
        this.predecessors.add(new Pair<>(type, predecessor));
    }

    public Set<Pair<Integer, GraphNode>> getPredecessors() {
        return predecessors;
    }

    public boolean hasSuccessor(Integer node) {
        return usedNodes.contains(node);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void addUsedNode(Integer node) {
        this.usedNodes.add(node);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Pair<Integer, GraphNode> succesor : this.successors) {
            str.append("(").append(succesor.y().index).append(",").append(succesor.x()).append(")");
        }
        return str.toString();
    }

    public String getLabel() {
        return label;
    }

    public Integer getIndex() {
        return index;
    }
}
