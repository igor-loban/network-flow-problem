package by.bsu.fpmi.cnfp.main;

import by.bsu.fpmi.cnfp.exception.AntitheticalConstraintsException;
import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;
import by.bsu.fpmi.cnfp.main.model.NumerableObject;
import by.bsu.fpmi.cnfp.main.model.Tree;
import by.bsu.fpmi.cnfp.main.model.factory.NumerableObjectFactory;

import java.util.HashMap;
import java.util.HashSet;
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
        for (int period = 1, nodeNumber = -1, arcNumber = -1; period <= periodCount; period++, nodeNumber--) {
            Node artificialNode = new Node(nodeNumber);
            nodes.put(nodeNumber, artificialNode);

            if (nodeNumber < -1) {
                if (totalIntensity < 0) {
                    throw new AntitheticalConstraintsException(
                            "Total intensity of periods from 1 to " + period + " equals " + totalIntensity + ".");
                }
                Node previousArtificialNode = nodes.get(nodeNumber + 1);
                createArtificialArc(arcNumber--, arcs, totalIntensity, previousArtificialNode, artificialNode);
            }

            int maxNumber = period * nodeCount;
            for (int number = (period - 1) * nodeCount + 1; number <= maxNumber; number++) {
                Node node = nodes.get(number);
                createArtificialArc(arcNumber--, arcs, artificialNode, node);
                totalIntensity += node.getIntensity();
            }
        }
    }

    private static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, Node artificialNode, Node node) {
        createArtificialArc(arcNumber, arcs, node.getIntensity(), artificialNode, node);
    }

    private static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, double capacity, Node beginNode,
                                            Node endNode) {
        Arc artificialArc = new Arc(arcNumber);
        arcs.put(artificialArc.getNumber(), artificialArc);
        artificialArc.setCapacity(Math.abs(capacity));
        artificialArc.setCost(Double.MAX_VALUE);
        if (capacity > 0 || capacity == 0) {
            setupLinks(beginNode, endNode, artificialArc);
        } else {
            setupLinks(endNode, beginNode, artificialArc);
        }
    }

    private static void setupLinks(Node beginNode, Node endNode, Arc arc) {
        arc.setBeginNode(beginNode);
        arc.setEndNode(endNode);
        beginNode.addExitArc(arc);
        endNode.addIncomingArc(arc);
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
        Set<Arc> incomingArcs = tree.populate(getArtificialArcs(node.getIncomingArcs()));
        Set<Arc> exitArcs = tree.populate(getArtificialArcs(node.getExitArcs()));
        Set<Node> leafs = new HashSet<>();
        leafs.addAll(getBeginNodes(incomingArcs));
        leafs.addAll(getEndNodes(exitArcs));
        for (Node leaf : leafs) {
            node.addDescendant(leaf);
            leaf.setAncestor(node);
            populateTree(tree, leaf);
        }
    }

    private static Set<Arc> getArtificialArcs(Set<Arc> arcs) {
        Set<Arc> artificialArcs = new HashSet<>();
        for (Arc arc : arcs) {
            if (isArtificial(arc)) {
                artificialArcs.add(arc);
            }
        }
        return artificialArcs;
    }

    private static Set<Node> getBeginNodes(Set<Arc> arcs) {
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            nodes.add(arc.getBeginNode());
        }
        return nodes;
    }

    private static Set<Node> getEndNodes(Set<Arc> arcs) {
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            nodes.add(arc.getEndNode());
        }
        return nodes;
    }
}
