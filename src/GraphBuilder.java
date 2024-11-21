import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

    /**
     Type = -1: conflict
     -2: serial
     >0: virtual register
     **/

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
        GraphNode currentGraphNode;
        while (currentNode != null) {
            currentGraphNode = new GraphNode(currentNode.rewrittenString(), currentNode.getLine());
            switch (currentNode.getOpCategory()) {
                case 0 -> {
                    // load
                    if (currentNode.getOpCode() == 0) {
                        // conflict edge
                        if (prevNode != null) {
                            // store
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                currentGraphNode.addSuccessor(-1, graphNodeList.getLast());
                            }
                        }
                        // use
                        currentGraphNode.addSuccessor(currentNode.getVR(1), this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentNode.getVR(1), currentGraphNode);

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                    }
                    // store
                    else{
                        //use
                        currentGraphNode.addSuccessor(currentNode.getVR(1), this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentNode.getVR(1), currentGraphNode);
                        currentGraphNode.addUsedNode(this.graphMap.get(currentNode.getVR(1)).getIndex());

                        //use
                        currentGraphNode.addSuccessor(currentNode.getVR(3), this.graphMap.get(currentNode.getVR(3)));
                        this.graphMap.get(currentNode.getVR(3)).addPredecessor(currentNode.getVR(3), currentGraphNode);
                        currentGraphNode.addUsedNode(this.graphMap.get(currentNode.getVR(3)).getIndex());

                        // serial edge
                        int graphNodeIndex = graphNodeList.size() - 1;
                        while (prevNode != null) {
                            if (!currentGraphNode.hasSuccessor(graphNodeList.get(graphNodeIndex).getIndex())) {
                                if (prevNode.getOpCategory() == 0 || prevNode.getOpCategory() == 3) {
                                    currentGraphNode.addSuccessor(-2, graphNodeList.get(graphNodeIndex));
                                    currentGraphNode.addUsedNode(graphNodeList.get(graphNodeIndex).getIndex());
                                }
                            }
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                break;
                            }
                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }
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
                        currentGraphNode.addSuccessor(currentNode.getVR(1), this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentNode.getVR(1), currentGraphNode);

                        // use
                        currentGraphNode.addSuccessor(currentNode.getVR(2), this.graphMap.get(currentNode.getVR(2)));
                        this.graphMap.get(currentNode.getVR(2)).addPredecessor(currentNode.getVR(2), currentGraphNode);

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                    }

                    // output
                    case 3 -> {
                        // conflict edge
                        if (prevNode != null) {
                            // store
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                currentGraphNode.addSuccessor(-1, graphNodeList.getLast());
                                currentGraphNode.addUsedNode(graphNodeList.getLast().getIndex());

                            }
                        }
                        prevNode = prevNode.getPrev();
                        int graphNodeIndex = graphNodeList.size() - 2;
                        while (prevNode != null) {
                            if (!currentGraphNode.hasSuccessor(graphNodeList.get(graphNodeIndex).getIndex())) {
                                if (prevNode.getOpCategory() == 3) {
                                    currentGraphNode.addSuccessor(-1, graphNodeList.get(graphNodeIndex));
                                    currentGraphNode.addUsedNode(graphNodeList.get(graphNodeIndex).getIndex());
                                }
                            }
                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }


                    }
                }
            graphNodeList.add(currentGraphNode);
            prevNode = currentNode;
            currentNode = currentNode.getNext();
        }
    }

    public String getGraphMap() {
        StringBuilder graphMapString = new StringBuilder();
        for (GraphNode node : graphNodeList) {
            graphMapString.append(node.getIndex()).append(": successors: ").append(node);
            graphMapString.append("\n");
        }
        return graphMapString.toString();
    }

    public String makeDotFile() {
        StringBuilder graphMapString = new StringBuilder();
        String currentLabel = "";
        graphMapString.append("digraph DG {\n");
        for (GraphNode node : graphNodeList) {
            graphMapString.append(node.getIndex()).append(" [label=\"").append(node.getLabel()).append("\"];\n");
        }

        for (GraphNode node : graphNodeList) {
            for (Pair<Integer, GraphNode> successor: node.getSuccessors()) {
                if (successor.x() == -1) {
                    currentLabel = " Conflict ";
                    graphMapString.append(node.getIndex()).append(" -> ").append(successor.y().getIndex()).append(" [label=\"").append(currentLabel).append("\"];\n");
                } else if (successor.x() == -2) {
                    currentLabel = " Serial ";
                    graphMapString.append(node.getIndex()).append(" -> ").append(successor.y().getIndex()).append(" [label=\"").append(currentLabel).append("\"];\n");
                } else {
                    currentLabel = " Data, vr";
                    graphMapString.append(node.getIndex()).append(" -> ").append(successor.y().getIndex()).append(" [label=\"").append(currentLabel).append(successor.x()).append("\"];\n");

                }
            }
        }

        graphMapString.append("}\n");
        return graphMapString.toString();
    }
}
