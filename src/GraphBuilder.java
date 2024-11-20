import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

    private IRNode head;
    private Map<Integer, GraphNode> graphMap;
    private List<GraphNode> graphNodeList;

    public GraphBuilder(IRNode head) {
        this.head = head;
        graphMap = new HashMap<>();
        graphNodeList = new ArrayList<>();
    }

    public void build() {
        IRNode currentNode = this.head;
        IRNode prevNode = null;
        int currentOp = 1;
        GraphNode currentGraphNode;
        while (currentNode != null) {
            currentGraphNode = new GraphNode(currentOp);
            switch (currentNode.getOpCategory()) {
                case 0 -> {
                    // load
                    if (currentNode.getOpCode() == 0) {
                        // conflict edge
                        if (prevNode != null) {
                            // store
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                currentGraphNode.addSuccessor(graphNodeList.getLast());
                            }
                        }
                        // use
                        currentGraphNode.addSuccessor(this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentGraphNode);

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                    }
                    // store
                    else{
                        // serial edge
                        int graphNodeIndex = graphNodeList.size() - 1;
                        while (prevNode != null) {
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                break;
                            }

                            if ((prevNode.getOpCategory() == 0 && prevNode.getOpCode() == 0) || prevNode.getOpCategory() == 3) {
                                currentGraphNode.addSuccessor(graphNodeList.get(graphNodeIndex));
                            }

                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }

                        //use
                        currentGraphNode.addSuccessor(this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentGraphNode);

                        //use
                        currentGraphNode.addSuccessor(this.graphMap.get(currentNode.getVR(3)));
                        this.graphMap.get(currentNode.getVR(3)).addPredecessor(currentGraphNode);

                        }
                    }
                    case 1 -> {
                        // loadI

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                    }
                    case 2 -> {
                        // arith

                        // use
                        currentGraphNode.addSuccessor(this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentGraphNode);

                        // use
                        currentGraphNode.addSuccessor(this.graphMap.get(currentNode.getVR(2)));
                        this.graphMap.get(currentNode.getVR(2)).addPredecessor(currentGraphNode);

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                    }

                    // output
                    case 3 -> {
                        // conflict edge
                        if (prevNode != null) {
                            // store
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                currentGraphNode.addSuccessor(graphNodeList.getLast());
                            }
                        }
                    }
                }
            graphNodeList.add(currentGraphNode);
            currentOp++;
            prevNode = currentNode;
            currentNode = currentNode.getNext();
        }
    }

    public String getGraphMap() {
        StringBuilder graphMapString = new StringBuilder();
        for (GraphNode node : graphNodeList) {
            graphMapString.append(node.getLabel()).append(": successors: ").append(node);
            graphMapString.append("\n");
        }
        return graphMapString.toString();
    }
}
