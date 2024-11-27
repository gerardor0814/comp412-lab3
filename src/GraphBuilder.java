import java.util.*;

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
            currentGraphNode = new GraphNode(currentNode, currentNode.getLine());
            switch (currentNode.getOpCategory()) {
                case 0 -> {
                    // load
                    if (currentNode.getOpCode() == 0) {
                        // conflict edge
                        int graphNodeIndex = graphNodeList.size() - 1;
                        while(prevNode != null) {
                            // store
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                if (!currentGraphNode.hasSuccessor(graphNodeList.get(graphNodeIndex))) {
                                    currentGraphNode.addSuccessor(-1, graphNodeList.get(graphNodeIndex));
                                    graphNodeList.get(graphNodeIndex).addPredecessor(-1, currentGraphNode);
                                    currentGraphNode.addUsedNode(graphNodeList.get(graphNodeIndex));
                                    break;
                                }
                            }
                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }
                        // use
                        currentGraphNode.addSuccessor(currentNode.getVR(1), this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentNode.getVR(1), currentGraphNode);

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                    }
                    // store
                    else {
                        //use
                        currentGraphNode.addSuccessor(currentNode.getVR(1), this.graphMap.get(currentNode.getVR(1)));
                        this.graphMap.get(currentNode.getVR(1)).addPredecessor(currentNode.getVR(1), currentGraphNode);
                        currentGraphNode.addUsedNode(this.graphMap.get(currentNode.getVR(1)));

                        //use
                        currentGraphNode.addSuccessor(currentNode.getVR(3), this.graphMap.get(currentNode.getVR(3)));
                        this.graphMap.get(currentNode.getVR(3)).addPredecessor(currentNode.getVR(3), currentGraphNode);
                        currentGraphNode.addUsedNode(this.graphMap.get(currentNode.getVR(3)));

                        // serial edge
                        int graphNodeIndex = graphNodeList.size() - 1;
                        while (prevNode != null) {
                            if (!currentGraphNode.hasSuccessor(graphNodeList.get(graphNodeIndex))) {
                                if (prevNode.getOpCategory() == 0 || prevNode.getOpCategory() == 3) {
                                    currentGraphNode.addSuccessor(-2, graphNodeList.get(graphNodeIndex));
                                    graphNodeList.get(graphNodeIndex).addPredecessor(-2, currentGraphNode);
                                    currentGraphNode.addUsedNode(graphNodeList.get(graphNodeIndex));
                                }
                            }
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                break;
                            }
                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }
                        }
                    currentGraphNode.setDelay(6);
                    }
                    case 1 -> {
                        // loadI

                        // def
                        this.graphMap.put(currentNode.getVR(3), currentGraphNode);

                        currentGraphNode.setDelay(1);

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

                        if (currentNode.getOpCode() == 2) {
                            currentGraphNode.setDelay(3);
                        } else {
                            currentGraphNode.setDelay(1);
                        }

                    }

                    // output
                    case 3 -> {
                        // conflict edge
                        int graphNodeIndex = graphNodeList.size() - 1;
                        while (prevNode != null) {
                            // store
                            if (prevNode.getOpCategory() == 0 && prevNode.getOpCode() != 0) {
                                currentGraphNode.addSuccessor(-1, graphNodeList.get(graphNodeIndex));
                                graphNodeList.get(graphNodeIndex).addPredecessor(-1, currentGraphNode);
                                currentGraphNode.addUsedNode(graphNodeList.get(graphNodeIndex));
                                break;
                            }
                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }

                        prevNode = currentNode.getPrev();

                        graphNodeIndex = graphNodeList.size() - 1;
                        while (prevNode != null) {
                            if (!currentGraphNode.hasSuccessor(graphNodeList.get(graphNodeIndex))) {
                                if (prevNode.getOpCategory() == 3) {
                                    currentGraphNode.addSuccessor(-2, graphNodeList.get(graphNodeIndex));
                                    graphNodeList.get(graphNodeIndex).addPredecessor(-2, currentGraphNode);
                                    currentGraphNode.addUsedNode(graphNodeList.get(graphNodeIndex));
                                    break;
                                }
                            }
                            graphNodeIndex--;
                            prevNode = prevNode.getPrev();
                        }
                        currentGraphNode.setDelay(1);
                    }
                }
            graphNodeList.add(currentGraphNode);
            prevNode = currentNode;
            currentNode = currentNode.getNext();
        }
    }

    public void addPriorities() {
        GraphNode currentNode;
        int maxPredecessorPriority = 0;
        for (int i = this.graphNodeList.size() - 1; i >= 0; i--) {
            currentNode = graphNodeList.get(i);
            for (Pair<Integer, GraphNode> node : currentNode.getPredecessors()) {
                if (node.y().getPriority() > maxPredecessorPriority) {
                    maxPredecessorPriority = node.y().getPriority();
                }
            }
            currentNode.setPriority(maxPredecessorPriority + (currentNode.getDelay() * 10) + currentNode.getSuccessors().size());
            maxPredecessorPriority = 0;
        }
    }

    public void schedule() {
        int cycle = 1;
        Map<GraphNode, Integer> ready = new HashMap<>();
        Map<GraphNode, Integer> active = new HashMap<>();
        Set<GraphNode> toRemove = new HashSet<>();

        for (GraphNode node : graphNodeList) {
            if (node.getSuccessors().isEmpty()) {
                ready.put(node, node.getPriority());
            }
        }

        GraphNode currentNode1;
        GraphNode currentNode2;
        GraphNode temp;
        int currentPrio;

        while (!(ready.isEmpty() && active.isEmpty())) {
            currentPrio = -1;
            currentNode1 = null;
            if (!ready.isEmpty()) {
                for (Map.Entry<GraphNode, Integer> entry : ready.entrySet()) {
                    if (entry.getValue() > currentPrio) {
                        currentNode1 = entry.getKey();
                        currentPrio = entry.getValue();
                    }
                }
                ready.remove(currentNode1);
                active.put(currentNode1, currentNode1.getDelay() + cycle);
            }

            currentPrio = -1;
            currentNode2 = null;
            if (!ready.isEmpty()) {
                for (Map.Entry<GraphNode, Integer> entry : ready.entrySet()) {
                    if (entry.getValue() > currentPrio) {
                        currentNode2 = entry.getKey();
                        currentPrio = entry.getValue();
                    }
                }
                ready.remove(currentNode2);
                active.put(currentNode2, currentNode2.getDelay() + cycle);
            }

            // if second node is load or store
            if (currentNode2 != null) {
                if (currentNode2.getOp().getOpCategory() == 0) {
                    // and it is not another load or store
                    if (currentNode1.getOp().getOpCategory() != 0) {
                        temp = currentNode2;
                        currentNode2 = currentNode1;
                        currentNode1 = temp;
                    // if it is another load or store, find a new non load or store op
                    } else {
                        if (currentNode1.getPriority() > currentNode2.getPriority()) {
                            currentPrio = -1;
                            temp = currentNode2;
                            currentNode2 = null;
                            if (!ready.isEmpty()) {
                                for (Map.Entry<GraphNode, Integer> entry : ready.entrySet()) {
                                    if (entry.getValue() > currentPrio && entry.getKey().getOp().getOpCategory() != 0) {
                                        currentNode2 = entry.getKey();
                                        currentPrio = entry.getValue();
                                    }
                                }
                                if (currentNode2 != null) {
                                    ready.remove(currentNode2);
                                    active.put(currentNode2, currentNode2.getDelay() + cycle);
                                }
                            }
                            ready.put(temp, temp.getPriority());
                        } else {
                            currentPrio = -1;
                            temp = currentNode1;
                            currentNode1 = null;
                            if (!ready.isEmpty()) {
                                for (Map.Entry<GraphNode, Integer> entry : ready.entrySet()) {
                                    if (entry.getValue() > currentPrio && entry.getKey().getOp().getOpCategory() != 0) {
                                        currentNode1 = entry.getKey();
                                        currentPrio = entry.getValue();
                                    }
                                }
                                if (currentNode1 != null) {
                                    ready.remove(currentNode1);
                                    active.put(currentNode1, currentNode1.getDelay() + cycle);
                                }
                            }
                            ready.put(temp, temp.getPriority());
                            temp = currentNode2;
                            currentNode2 = currentNode1;
                            currentNode1 = temp;
                        }
                    }
                }
            }

            // mult
            if (currentNode1 != null) {
                if (currentNode1.getOp().getOpCategory() == 2 && currentNode1.getOp().getOpCode() == 2) {
                    if (currentNode2 != null) {
                        if (currentNode1.getOp().getOpCategory() != 2 && currentNode1.getOp().getOpCode() != 2) {
                            temp = currentNode2;
                            currentNode2 = currentNode1;
                            currentNode1 = temp;
                        } else {
                            currentPrio = -1;
                            temp = currentNode1;
                            currentNode1 = null;
                            if (!ready.isEmpty()) {
                                for (Map.Entry<GraphNode, Integer> entry : ready.entrySet()) {
                                    if (entry.getValue() > currentPrio && entry.getKey().getOp().getOpCategory() != 2 && entry.getKey().getOp().getOpCode() != 2) {
                                        currentNode1 = entry.getKey();
                                        currentPrio = entry.getValue();
                                    }
                                }
                                if (currentNode1 != null) {
                                    ready.remove(currentNode2);
                                    active.put(currentNode2, currentNode2.getDelay() + cycle);
                                }
                            }
                            ready.put(temp, temp.getPriority());
                        }
                    } else {
                        currentNode2 = currentNode1;
                        currentNode1 = null;
                    }
                }
            }

            System.out.print("[ ");
            if (currentNode1 != null) {
                System.out.print(currentNode1.getOp().rewrittenString());
            } else {
                System.out.print("nop");
            }

            System.out.print(" ; ");
            if (currentNode2 != null) {
                System.out.print(currentNode2.getOp().rewrittenString());
            } else {
                System.out.print("nop");
            }

            System.out.println(" ]");

            cycle++;


            for (Map.Entry<GraphNode, Integer> entry : active.entrySet()) {
                if (entry.getValue() <= cycle) {
                    toRemove.add(entry.getKey());
                    for (Pair<Integer, GraphNode> predecessor : entry.getKey().getPredecessors()) {
                        predecessor.y().getSuccessors().remove(new Pair<>(predecessor.x(), entry.getKey()));
                        if (predecessor.y().getSuccessors().isEmpty()) {
                            ready.put(predecessor.y(), predecessor.y().getPriority());
                        }
                    }
                }
            }
            for (GraphNode remove : toRemove) {
                active.remove(remove);
            }
            toRemove.clear();

            for (Map.Entry<GraphNode, Integer> entry : active.entrySet()) {
                for (Pair<Integer, GraphNode> predecessor : entry.getKey().getPredecessors()) {
                    if (predecessor.x() == -2) {
                        predecessor.y().getSuccessors().remove(new Pair<>(predecessor.x(), entry.getKey()));
                        if (predecessor.y().getSuccessors().isEmpty()) {
                            ready.put(predecessor.y(), predecessor.y().getPriority());
                            toRemove.add(predecessor.y());
                        }
                    }
                }
                for (GraphNode remove : toRemove) {
                    entry.getKey().getPredecessors().remove(new Pair<>(-2, remove));
                }
                toRemove.clear();
            }

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
            graphMapString.append(node.getIndex()).append(" [label=\"").append(node.getOp().getLine()).append(": ").append(node.getOp().rewrittenString()).append("\nprio: ").append(node.getPriority()).append("\"];\n");
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
