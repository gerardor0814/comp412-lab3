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
    private IRNode op;
    private Set<Pair<Integer, GraphNode>> successors;
    private Set<Pair<Integer, GraphNode>> predecessors;
    private Set<Integer> usedNodes;
    private int delay;
    private int priority;


    public GraphNode(IRNode op, Integer index) {
        this.op = op;
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

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public void addUsedNode(Integer node) {
        this.usedNodes.add(node);
    }

    @Override
    public String toString() {
        return this.op.rewrittenString();
    }

    public IRNode getOp() {
        return op;
    }

    public Integer getIndex() {
        return index;
    }
}
