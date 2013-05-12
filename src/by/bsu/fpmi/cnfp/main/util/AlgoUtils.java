package by.bsu.fpmi.cnfp.main.util;

import by.bsu.fpmi.cnfp.exception.AntitheticalConstraintsException;
import by.bsu.fpmi.cnfp.exception.LogicalFailException;
import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Tree;
import by.bsu.fpmi.cnfp.main.model.Node;
import by.bsu.fpmi.cnfp.main.model.NumerableObject;
import by.bsu.fpmi.cnfp.main.model.factory.NumerableObjectFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Igor Loban
 */
public final class AlgoUtils {
    private AlgoUtils() {
    }

    public static boolean isArtificial(NumerableObject object) {
        return object.getNumber() < 0;
    }

    public static <T extends NumerableObject, S extends NumerableObject> Map<Integer, T> createStubs(
            Map<Integer, T> sourcePool, NumerableObjectFactory<T, S> factory) {
        Map<Integer, T> stubs = new HashMap<>();
        for (T source : sourcePool.values()) {
            T stub = factory.createStub(source);
            stubs.put(stub.getNumber(), stub);
        }
        return stubs;
    }

    public static <T extends NumerableObject, S extends NumerableObject> void fillStubs(Map<Integer, T> stubPool,
                                                                                        Map<Integer, T> sourcePool,
                                                                                        Map<Integer, S> addingPool,
                                                                                        NumerableObjectFactory<T,
                                                                                                S> factory) {
        for (T stub : stubPool.values()) {
            T source = sourcePool.get(stub.getNumber());
            factory.fillStub(stub, source, stubPool, addingPool);
        }
    }

    /**
     * Ввести искусственные вершины, из которых выходят источники и нейтральные узлы и входят стоки (на каждом уровне)
     * Искусственные вершины соединяются дугами из уровня в уровень. Присвоить искусственным дугам стоимость M, где M -
     * большое число (штраф)
     */
    public static void addArtificialNodes(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount,
                                          int periodCount) {
        double totalIntensity = 0;
        for (int period = 0, nodeNumber = -1, arcNumber = -1; period < periodCount; period++, nodeNumber--) {
            Node artificialNode = new Node(nodeNumber, period);
            nodes.put(nodeNumber, artificialNode);

            if (nodeNumber < -1) {
                if (totalIntensity < 0) {
                    throw new AntitheticalConstraintsException(
                            "Total intensity of periods from 0 to " + period + " equals " + totalIntensity + ".");
                }
                Node previousArtificialNode = nodes.get(nodeNumber + 1);
                ArcUtils.createArtificialArc(arcNumber--, arcs, totalIntensity, previousArtificialNode, artificialNode);
            }

            int maxNumber = (period + 1) * nodeCount;
            for (int number = period * nodeCount + 1; number <= maxNumber; number++) {
                Node node = nodes.get(number);
                ArcUtils.createArtificialArc(arcNumber--, arcs, artificialNode, node);
                totalIntensity += node.getIntensity();
            }
        }
    }

    /**
     * Дерево будет состоять из искуственных дуг
     */
    public static Tree createInitialTree(Map<Integer, Node> nodes) {
        Node root = nodes.get(-1);
        Tree tree = new Tree(root);
        populateTree(tree, root);
        return tree;
    }

    private static void populateTree(Tree tree, Node node) {
        Set<Arc> incomingArcs = tree.populate(ArcUtils.getArtificialArcs(node.getIncomingArcs()));
        Set<Arc> exitArcs = tree.populate(ArcUtils.getArtificialArcs(node.getExitArcs()));
        Set<Node> leafs = new HashSet<>();
        leafs.addAll(NodeUtils.getBeginNodes(incomingArcs));
        leafs.addAll(NodeUtils.getEndNodes(exitArcs));
        for (Node leaf : leafs) {
            node.addDescendant(leaf);
            leaf.setAncestor(node);
            populateTree(tree, leaf);
        }
    }

    public static void createInitialFlow(Map<Integer, Arc> arcs) {
        for (Arc arc : arcs.values()) {
            if (isArtificial(arc)) {
                Node beginNode = arc.getBeginNode();
                Node endNode = arc.getEndNode();
                if (isArtificial(beginNode) && isArtificial(endNode)) {
                    arc.setFlow(arc.getCapacity());
                } else if (isArtificial(beginNode)) {
                    arc.setFlow(endNode.getIntensity());
                } else if (isArtificial(endNode)) {
                    arc.setFlow(beginNode.getIntensity());
                } else {
                    throw new LogicalFailException("Artificial arc links two non-artificial nodes.");
                }
            } else {
                arc.setFlow(0);
            }
        }
    }

    public static void createDynamicSupport(Tree tree, int periodCount) {
        for (int period = 0; period < periodCount - 1; period++) {
            for (Node root : tree.getRoots(period)) {
                Set<Arc> intermediateTreeArcs = getIntermediateTreeArcs(root);
                addFakeArcs(intermediateTreeArcs, tree);
            }
        }
    }

    private static Set<Arc> getIntermediateTreeArcs(Node root) {
        Set<Arc> intermediateTreeArcs = new HashSet<>();
        addIntermediateTreeArcs(root, intermediateTreeArcs);
        return intermediateTreeArcs;
    }

    private static void addIntermediateTreeArcs(Node parent, Set<Arc> collector) {
        int period = parent.getPeriod();
        for (Node child : parent.getDescendants()) {
            int childPeriod = child.getPeriod();
            if (period + 1 == childPeriod) {
                Arc arc = ArcUtils.getArc(parent.getExitArcs(), child);
                collector.add(arc);
            } else if (period == childPeriod) {
                addIntermediateTreeArcs(child, collector);
            }
        }
    }

    private static void addFakeArcs(Set<Arc> intermediateTreeArcs, Tree tree) {
        if (intermediateTreeArcs.size() == 1) {
            return;
        }

        List<Arc> arcs = new ArrayList<>(intermediateTreeArcs);
        Node plusNode = arcs.remove(0).getEndNode();
        plusNode.setSign(Node.Sign.PLUS);
        Set<Node> minusNodes = new HashSet<>(arcs.size());
        for (Arc arc : arcs) {
            Node endNode = arc.getEndNode();
            endNode.setSign(Node.Sign.MINUS);
            minusNodes.add(endNode);
        }
        for (Node minusNode : minusNodes) {
            tree.addFakeArc(plusNode, minusNode);
        }
    }

    public static Set<Node> getRoots(Set<Node> nodes, int period) {
        Set<Node> roots = new HashSet<>();
        for (Node node : nodes) {
            roots.add(getRoot(node, period));
        }
        return roots;
    }

    private static Node getRoot(Node node, int period) {
        Node parent = node.getAncestor();
        if (parent == null || parent.getPeriod() != period) {
            return node;
        }
        return getRoot(parent, period);
    }
}
