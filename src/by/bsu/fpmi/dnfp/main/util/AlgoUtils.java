package by.bsu.fpmi.dnfp.main.util;

import by.bsu.fpmi.dnfp.exception.AntitheticalConstraintsException;
import by.bsu.fpmi.dnfp.exception.LogicalFailException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.model.NumerableObject;
import by.bsu.fpmi.dnfp.main.model.Tree;
import by.bsu.fpmi.dnfp.main.model.factory.NumerableObjectFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Igor Loban
 */
public final class AlgoUtils {
    public static final Double BIG_COST = Math.pow(10, 3);

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
                            "total intensity of periods from 0 to " + period + " equals " + totalIntensity + ".");
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
            node.addChild(leaf);
            leaf.setParent(node);
            populateTree(tree, leaf);
        }
    }

    public static void createInitialFlow(Collection<Arc> arcs) {
        for (Arc arc : arcs) {
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
                    throw new LogicalFailException("artificial arc links two non-artificial nodes.");
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

    private static void addIntermediateTreeArcs(Node parent, Set<Arc> arcCollector) {
        int period = parent.getPeriod();
        for (Node child : parent.getChildren()) {
            int childPeriod = child.getPeriod();
            if (period + 1 == childPeriod) {
                Arc arc = ArcUtils.getArc(parent.getExitArcs(), child);
                arcCollector.add(arc);
            } else if (period == childPeriod) {
                addIntermediateTreeArcs(child, arcCollector);
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

    public static Set<Node> getRootByFakeArcs(Tree tree, int period) {
        Set<Node> roots = new HashSet<>();
        Set<Arc> periodTreeArcs = ArcUtils.getArcs(tree.getArcs(), period);
        Set<Arc> periodFakeArcs = ArcUtils.getArcs(tree.getFakeArcs(), period);
        Set<Node> periodNodes = NodeUtils.getNodes(periodFakeArcs);
        for (Node node : periodNodes) {
            if (node.getSign() == Node.Sign.PLUS && !NodeUtils.hasArcFromSet(node, periodTreeArcs)) {
                roots.add(node);
            }
        }
        return roots;
    }

    public static Set<Node> getRootByArcs(Set<Arc> arcs, int period) {
        Set<Arc> periodArcs = ArcUtils.getArcs(arcs, period);
        Set<Node> periodNodes = NodeUtils.getNodes(periodArcs);
        return AlgoUtils.getRoots(periodNodes, period);
    }

    private static Set<Node> getRoots(Set<Node> nodes, int period) {
        Set<Node> roots = new HashSet<>();
        for (Node node : nodes) {
            roots.add(getRoot(node, period));
        }
        return roots;
    }

    private static Node getRoot(Node node, int period) {
        Node parent = node.getParent();
        if (parent == null || parent.getPeriod() != period) {
            return node;
        }
        return getRoot(parent, period);
    }

    public static void calcPseudoCost(Tree tree) {
        for (Arc fakeArc : tree.getFakeArcs()) {
            int period = fakeArc.getPeriod();
            Arc preBeginArc = ArcUtils.getArc(tree, period, fakeArc.getBeginNode());
            Arc preEndArc = ArcUtils.getArc(tree, period, fakeArc.getEndNode());
            double pseudoCost =
                    preBeginArc.getCost() - preEndArc.getCost() + getPathCost(tree, fakeArc.getEndNode().getParent(),
                            fakeArc.getBeginNode().getParent());
            fakeArc.setCost(pseudoCost);
        }
    }

    private static double getPathCost(Tree tree, Node endNode, Node beginNode) {
        if (endNode.getPeriod() != beginNode.getPeriod()) {
            throw new LogicalFailException("path cost can be calculated for nodes from one period.");
        }
        return calcPathCost(endNode, beginNode, tree.getArcs());
    }

    private static double calcPathCost(Node endNode, Node beginNode, Set<Arc> arcs) {
        if (endNode == beginNode) {
            return 0;
        }
        Node preEndNode = endNode.getParent();
        Arc arc = ArcUtils.getArc(arcs, preEndNode, endNode);
        if (arc == null) {
            throw new LogicalFailException("no arc between two nodes in tree.");
        }
        return (ArcUtils.isStraight(arc, preEndNode) ? arc.getCost() : -arc.getCost()) + calcPathCost(preEndNode,
                beginNode, arcs);
    }

    public static void calcPotentials(Tree tree, int nodeCount, int periodCount) {
        Set<Node> roots = tree.getRoots(periodCount - 1);
        roots.addAll(getRootByFakeArcs(tree, periodCount - 1));
        for (Node root : roots) {
            root.setPotential(0.0);
            calcPotentials(root, tree, nodeCount);
        }
    }

    private static void calcPotentials(Node node, Tree tree, int nodeCount) {
        for (Node child : node.getChildren()) {
            if (child.getPotential() == null && NodeUtils.isNotMinusIntermediate(child, node, nodeCount)) {
                calcChildPotential(child, node, tree);
                calcPotentials(child, tree, nodeCount);
            }
        }

        Node parent = node.getParent();
        if (parent != null && parent.getPotential() == null && NodeUtils
                .isNotMinusIntermediate(node, parent, nodeCount)) {
            calcParentPotential(parent, node, tree);
            calcPotentials(parent, tree, nodeCount);
        }

        if (node.getSign() != Node.Sign.NONE) {
            if (node.getSign() == Node.Sign.PLUS) {
                Set<Node> children = NodeUtils.getEndNodes(tree.getFakeArcs(), node);
                for (Node child : children) {
                    if (child.getPotential() == null) {
                        calcChildPotential(child, node, tree);
                        calcPotentials(child, tree, nodeCount);
                    }
                }
            } else {
                Arc fakeArc = ArcUtils.getArc(tree.getFakeArcs(), node);
                Node fakeParent = fakeArc.getBeginNode();
                if (fakeParent.getPotential() == null) {
                    calcParentPotential(fakeParent, node, tree);
                    calcPotentials(fakeParent, tree, nodeCount);
                }
            }
        }
    }

    private static void calcChildPotential(Node child, Node parent, Tree tree) {
        Arc arc = ArcUtils.getArc(tree.getArcs(), child, parent);
        if (arc == null) {
            arc = ArcUtils.getArc(tree.getFakeArcs(), child, parent);
        }
        child.setPotential(parent.getPotential() - arc.getCost());
    }

    private static void calcParentPotential(Node parent, Node child, Tree tree) {
        Arc arc = ArcUtils.getArc(tree.getArcs(), parent, child);
        if (arc == null) {
            arc = ArcUtils.getArc(tree.getFakeArcs(), parent, child);
        }
        parent.setPotential(arc.getCost() + child.getPotential());
    }

    public static void calcLeaps(Collection<Arc> arcs) {
        Set<Arc> intermediateArcs = ArcUtils.getIntermediateArcs(arcs);
        for (Arc arc : intermediateArcs) {
            arc.setLeap(arc.getBeginNode().getPotential() - arc.getEndNode().getPotential() - arc.getCost());
        }
    }

    public static void calcEstimates(Collection<Arc> arcs) {
        for (Arc arc : arcs) {
            if (arc.getPeriod() >= 0) {
                Node beginNode = arc.getBeginNode();
                Node endNode = arc.getEndNode();
                arc.setEstimate(beginNode.getPotential() - endNode.getPotential() - arc.getCost());
            }
        }
    }

    public static double calcEpsU(Collection<Arc> arcs) {
        double epsU = 0;
        for (Arc arc : arcs) {
            if (arc.getPeriod() >= 0) {
                if (arc.getEstimate() >= 0) {
                    epsU += arc.getEstimate() * (arc.getCapacity() - arc.getFlow());
                } else {
                    epsU -= arc.getEstimate() * arc.getFlow();
                }
            }
        }
        return epsU;
    }

    public static double calcEpsX(Collection<Arc> arcs) {
        double epsX = 0;
        for (Arc arc : arcs) {
            if (arc.getPeriod() < 0) {
                if (arc.getLeap() >= 0) {
                    epsX += arc.getLeap() * (arc.getCapacity() - arc.getFlow());
                } else {
                    epsX -= arc.getLeap() * arc.getFlow();
                }
            }
        }
        return epsX;
    }
}
