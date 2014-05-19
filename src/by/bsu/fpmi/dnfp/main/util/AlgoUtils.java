package by.bsu.fpmi.dnfp.main.util;

import by.bsu.fpmi.dnfp.exception.AntitheticalConstraintsException;
import by.bsu.fpmi.dnfp.exception.LogicalFailException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.model.NumerableObject;
import by.bsu.fpmi.dnfp.main.model.Support;
import by.bsu.fpmi.dnfp.main.model.Tree;
import by.bsu.fpmi.dnfp.main.model.factory.NumerableObjectFactory;
import by.bsu.fpmi.dnfp.main.net.AbstractNet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                ArcUtils.createArtificialArc(arcNumber--, arcs, totalIntensity, artificialNode, previousArtificialNode);
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
                    arc.setFlow(arc.getCapacity()); // TODO: ???
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
        roots.addAll(periodNodes.stream()
                .filter(node -> node.getSign() == Node.Sign.PLUS && !NodeUtils.hasArcFromSet(node, periodTreeArcs))
                .collect(Collectors.toList()));
        return roots;
    }

    public static Set<Node> getRootByArcs(Set<Arc> arcs, int period) {
        Set<Arc> periodArcs = ArcUtils.getArcs(arcs, period);
        Set<Node> periodNodes = NodeUtils.getNodes(periodArcs);
        return AlgoUtils.getRoots(periodNodes, period);
    }

    private static Set<Node> getRoots(Set<Node> nodes, int period) {
        return nodes.stream().map(node -> getRoot(node, period)).collect(Collectors.toSet());
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
        node.getChildren().stream().filter(child -> child.getPotential() == null && NodeUtils
                .isNotMinusIntermediate(child, node, nodeCount)).forEach(child -> {
            calcChildPotential(child, node, tree);
            calcPotentials(child, tree, nodeCount);
        });

        Node parent = node.getParent();
        if (parent != null && parent.getPotential() == null && NodeUtils
                .isNotMinusIntermediate(node, parent, nodeCount)) {
            calcParentPotential(parent, node, tree);
            calcPotentials(parent, tree, nodeCount);
        }

        if (node.getSign() != Node.Sign.NONE) {
            if (node.getSign() == Node.Sign.PLUS) {
                Set<Node> children = NodeUtils.getEndNodes(tree.getFakeArcs(), node);
                children.stream().filter(child -> child.getPotential() == null).forEach(child -> {
                    calcChildPotential(child, node, tree);
                    calcPotentials(child, tree, nodeCount);
                });
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
        if (arc.getBeginNode() == child) {
            child.setPotential(parent.getPotential() + arc.getCost());
        } else {
            child.setPotential(parent.getPotential() - arc.getCost());
        }
    }

    private static void calcParentPotential(Node parent, Node child, Tree tree) {
        Arc arc = ArcUtils.getArc(tree.getArcs(), parent, child);
        if (arc == null) {
            arc = ArcUtils.getArc(tree.getFakeArcs(), parent, child);
        }
        if (arc.getBeginNode() == parent) {
            parent.setPotential(child.getPotential() + arc.getCost());
        } else {
            parent.setPotential(child.getPotential() - arc.getCost());
        }
    }

    public static void calcLeaps(Collection<Arc> arcs) {
        Set<Arc> intermediateArcs = ArcUtils.getIntermediateArcs(arcs);
        for (Arc arc : intermediateArcs) {
            arc.setLeap(arc.getBeginNode().getPotential() - arc.getEndNode().getPotential() - arc.getCost());
        }
    }

    public static void calcEstimates(Collection<Arc> arcs) {
        arcs.stream().filter(arc -> arc.getPeriod() >= 0).forEach(arc -> {
            Node beginNode = arc.getBeginNode();
            Node endNode = arc.getEndNode();
            arc.setEstimate(beginNode.getPotential() - endNode.getPotential() - arc.getCost());
        });
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

    public static void calcDirections(AbstractNet net) {
        int periodCount = net.getPeriodCount();
        int nodeCount = net.getNodeCount();

        // For a 0 period
        Support support = getSupport(net.getTree(), 0);
        double[][] A = getMatrixA(support, nodeCount);
        double[][] PreF = getMatrixPreF(net.getArcs(), support, nodeCount, net.getArcCount(), 0);
        double[] v = getNoSupportV(net.getTree(), 0);
        double[] l = new double[nodeCount];
        double[] result = AlgebraUtils.calcResult(A, l, PreF, v);
        fillDirections(support, l, result, nodeCount);

        for (int period = 1; period < periodCount; period++) {
            support = getSupport(net.getTree(), period);
            A = getMatrixA(support, nodeCount);
            PreF = getMatrixPreF(net.getArcs(), support, nodeCount, net.getArcCount(), period);
            v = getNoSupportV(net.getTree(), period);
            result = AlgebraUtils.calcResult(A, l, PreF, v);
            fillDirections(support, l, result, nodeCount);
        }
    }

    private static void fillDirections(Support support, double[] l, double[] result, int nodeCount) {
        List<Node> supportNodes = support.getSupportNodes();
        int supportNodeSize = supportNodes.size();
        for (int i = 0; i < supportNodeSize; i++) {
            Node node = supportNodes.get(i);
            node.setDirection(result[i]);
            l[node.getNumber() % nodeCount] = result[i];
        }

        List<Arc> supportArcs = support.getSupportArcs();
        for (int i = 0; i < supportArcs.size(); i++) {
            supportArcs.get(i).setDirection(result[supportNodeSize + i]);
        }
    }

    private static double[][] getMatrixA(Support support, int nodeCount) {
        double A[][] = new double[nodeCount][];

        int supportSize = support.getSize();
        for (int i = 0; i < nodeCount; i++) {
            A[i] = new double[supportSize];
        }

        List<Node> supportNodes = support.getSupportNodes();
        int supportNodeSize = supportNodes.size();
        for (int i = 0; i < supportNodeSize; i++) {
            int row = supportNodes.get(i).getNumber() % nodeCount;
            A[row][i] = 1;
        }

        List<Arc> supportArcs = support.getSupportArcs();
        for (int i = 0, size = supportArcs.size(), column = supportNodeSize; i < size; i++, column++) {
            Arc arc = supportArcs.get(i);
            int row = arc.getBeginNode().getNumber() % nodeCount;
            A[row][column] = 1;
            row = arc.getEndNode().getNumber() % nodeCount;
            A[row][column] = -1;
        }

        return A;
    }

    private static double[][] getMatrixPreF(Map<Integer, Arc> arcs, Support support, int nodeCount, int arcCount,
                                            int period) {
        List<Arc> noSupportArcs = new ArrayList<>();
        Set<Integer> supportArcNumbers = new HashSet<>();
        support.getSupportArcs().stream().forEach((arc) -> supportArcNumbers.add(arc.getNumber()));
        int arcNumber = period > 0 ? (period - 1) * (arcCount + nodeCount) + arcCount : 0;
        for (int limit = arcNumber + arcCount; arcNumber < limit; arcNumber++) {
            if (!supportArcNumbers.contains(arcNumber)) {
                noSupportArcs.add(arcs.get(arcNumber));
            }
        }

        double[][] PreF = new double[nodeCount][];
        int noSupportArcSize = noSupportArcs.size();
        for (int i = 0; i < nodeCount; i++) {
            PreF[i] = new double[noSupportArcSize];
        }

        for (int i = 0; i < noSupportArcSize; i++) {
            Arc arc = noSupportArcs.get(i);
            int row = arc.getBeginNode().getNumber() % nodeCount;
            PreF[row][i] = 1;
            row = arc.getEndNode().getNumber() % nodeCount;
            PreF[row][i] = -1;
        }

        return PreF;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static double[] getNoSupportV(Tree tree, int period) {
        List<Double> noSupportV = new ArrayList<>();
        List<Arc> arcs = new ArrayList<>(ArcUtils.getArcs(tree.getArcs(), period));
        Collections.sort(arcs, (arc1, arc2) -> arc1.getNumber() - arc2.getNumber());
        for (int i = 0; i < arcs.size(); i++) {
            Arc arc = arcs.get(i);
            if (!ArcUtils.hasZeroEstimate(arc)) {
                noSupportV.add(arc.getEstimate() > 0 ? arc.getCapacity() - arc.getFlow() : -arc.getFlow());
            }
        }

        double[] v = new double[noSupportV.size()];
        for (int i = 0; i < noSupportV.size(); i++) {
            v[i] = noSupportV.get(i);
        }
        return v;
    }

    private static Support getSupport(Tree tree, int period) {
        List<Arc> supportArcs = new ArrayList<>();
        List<Node> supportNodes = new ArrayList<>();

        int intermediatePeriod = -period - 1;
        for (Arc arc : tree.getArcs()) {
            if (arc.getPeriod() == period) {
                supportArcs.add(arc);
            } else if (arc.getPeriod() == intermediatePeriod) {
                supportNodes.add(arc.getBeginNode());
            }
        }

        return new Support(period, supportArcs, supportNodes);
    }
}
