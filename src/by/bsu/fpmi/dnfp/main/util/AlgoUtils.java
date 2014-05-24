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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
        int nodeCountPerPeriod = net.getNodeCount() / periodCount;

        // For a 0 period
        Support support = getSupport(net.getTree(), net.getArcs().values(), 0);
        double[][] A = getMatrixA(support, nodeCountPerPeriod);
        double[][] PreF = getMatrixPreF(support, nodeCountPerPeriod, 0);
        double[] v = getNoSupportV(support.getNoSupportArcs());
        double[] l = new double[nodeCountPerPeriod];
        double[] result = AlgebraUtils.calcResult(A, l, PreF, v);
        fillDirections(support, l, result, nodeCountPerPeriod);

        for (int period = 1; period < periodCount; period++) {
            support = getSupport(net.getTree(), net.getArcs().values(), period);
            A = getMatrixA(support, nodeCountPerPeriod);
            PreF = getMatrixPreF(support, nodeCountPerPeriod, period);
            v = getNoSupportV(support.getNoSupportArcs());
            result = AlgebraUtils.calcResult(A, l, PreF, v);
            fillDirections(support, l, result, nodeCountPerPeriod);
        }
    }

    private static void fillDirections(Support support, double[] l, double[] result, int nodeCountPerPeriod) {
        List<Node> supportNodes = support.getSupportNodes();
        int delta = nodeCountPerPeriod - support.getSize();
        int size = delta >= 0 ? supportNodes.size() : supportNodes.size() + delta;
        int index = 0;
        for (int i = 0; i < size; i++, index++) {
            Node node = supportNodes.get(i);
            node.setDirection(result[index]);
            l[support.getIndex(node)] = result[index];
        }

        for (int i = size; i < supportNodes.size(); i++) {
            Node node = supportNodes.get(i);
            node.setDirection(0);
            l[support.getIndex(node)] = 0;
        }

        List<Arc> supportArcs = support.getSupportArcs();
        for (int i = 0; i < supportArcs.size(); i++, index++) {
            supportArcs.get(i).setDirection(result[index]);
        }
    }

    private static double[][] getMatrixA(Support support, int nodeCountPerPeriod) {
        double A[][] = new double[nodeCountPerPeriod][];

        for (int i = 0; i < nodeCountPerPeriod; i++) {
            A[i] = new double[nodeCountPerPeriod];
        }

        // Dirty hack for last period (without support nodes)
        List<Node> supportNodes = support.getSupportNodes();
        int column = 0;
        int delta = nodeCountPerPeriod - support.getSize();
        for (int i = 0; i < delta; i++, column++) {
            A[i][column] = 1;
        }

        int nodeLimit = delta >= 0 ? supportNodes.size() : supportNodes.size() + delta;
        for (int i = 0; i < nodeLimit; i++, column++) {
            int row = support.getIndex(supportNodes.get(i));
            A[row][column] = 1;
        }

        List<Arc> supportArcs = support.getSupportArcs();
        for (int i = 0, size = supportArcs.size(); i < size; i++, column++) {
            Arc arc = supportArcs.get(i);
            int row = support.getIndex(arc.getBeginNode());
            A[row][column] = 1;
            row = support.getIndex(arc.getEndNode());
            A[row][column] = -1;
        }

        return A;
    }

    private static double[][] getMatrixPreF(Support support, int nodeCountPerPeriod, int period) {
        List<Arc> noSupportArcs = support.getNoSupportArcs();
        int noSupportArcSize = noSupportArcs.size();
        double[][] PreF = new double[nodeCountPerPeriod][];
        for (int i = 0; i < nodeCountPerPeriod; i++) {
            PreF[i] = new double[noSupportArcSize];
        }

        for (int i = 0; i < noSupportArcSize; i++) {
            Arc arc = noSupportArcs.get(i);
            int row = support.getIndex(arc.getBeginNode());
            PreF[row][i] = 1;
            row = support.getIndex(arc.getEndNode());
            PreF[row][i] = -1;
        }

        return PreF;
    }

    private static double[] getNoSupportV(List<Arc> noSupportArcs) {
        List<Double> noSupportV = new ArrayList<>();
        for (Arc arc : noSupportArcs) {
            if (ArcUtils.hasZeroEstimate(arc)) {
                noSupportV.add(0.0);
            } else {
                noSupportV.add(arc.getEstimate() > 0 ? arc.getCapacity() - arc.getFlow() : -arc.getFlow());
            }
        }

        double[] result = new double[noSupportV.size()];
        for (int i = 0; i < noSupportV.size(); i++) {
            result[i] = noSupportV.get(i);
        }
        return result;
    }

    private static Support getSupport(Tree tree, Collection<Arc> arcs, int period) {
        List<Arc> supportArcs = new ArrayList<>();
        List<Arc> noSupportArcs;
        List<Node> supportNodes = new ArrayList<>();
        Set<Node> nodes = new TreeSet<>(Comparator.<Node>comparingInt(NumerableObject::getNumber));

        int intermediatePeriod = -period - 1;
        for (Arc arc : tree.getArcs()) {
            if (arc.getPeriod() == period) {
                supportArcs.add(arc);
                nodes.add(arc.getBeginNode());
                nodes.add(arc.getEndNode());
            } else if (arc.getPeriod() == intermediatePeriod) {
                supportNodes.add(arc.getBeginNode());
            }
        }

        noSupportArcs = arcs.stream().filter((arc) -> arc.getPeriod() == period && !supportArcs.contains(arc))
                .sorted((arc1, arc2) -> arc1.getNumber() - arc2.getNumber()).collect(Collectors.toList());
        for (Arc noSupportArc : noSupportArcs) {
            nodes.add(noSupportArc.getBeginNode());
            nodes.add(noSupportArc.getEndNode());
        }

        Map<Integer, Integer> nodeNumbers = new HashMap<>();
        int i = 0;
        for (Node node : nodes) {
            nodeNumbers.put(node.getNumber(), i++);
        }

        return new Support(period, supportArcs, noSupportArcs, supportNodes, nodeNumbers);
    }

    public static Arc calcSteps(Set<Arc> arcs) {
        double minStep = 1;
        Arc minArc = null;
        for (Arc arc : arcs) {
            double step = Double.POSITIVE_INFINITY;
            if (arc.getDirection() > 0) {
                step = (arc.getCapacity() - arc.getFlow()) / arc.getDirection();
            } else if (arc.getDirection() < 0) {
                step = -arc.getFlow() / arc.getDirection();
            }
            arc.setStep(step);
            if (minStep >= step) {
                minStep = step;
                minArc = arc;
            }
        }
        return minArc;
    }

    public static void changeSupport(AbstractNet net, Arc minArc) {
        setupCostAliases(net.getTree(), minArc);
        calcPseudoCostAliases(net.getTree());
        calcPotentialAliases(net.getTree(), net.getNodeCount(), net.getPeriodCount());
        Arc minArcAlias = getMinArcAlias(net.getArcs().values(), net.getTree().getArcs());
        changeTree(net, minArc, minArcAlias);
    }

    private static void setupCostAliases(Tree tree, Arc minArc) {
        tree.getArcs().stream().forEach((arc) -> arc.setCostAlias(0));
        minArc.setCostAlias(Math.signum(minArc.getDirection()));
    }

    private static void calcPseudoCostAliases(Tree tree) {
        for (Arc fakeArc : tree.getFakeArcs()) {
            int period = fakeArc.getPeriod();
            Arc preBeginArc = ArcUtils.getArc(tree, period, fakeArc.getBeginNode());
            Arc preEndArc = ArcUtils.getArc(tree, period, fakeArc.getEndNode());
            double pseudoCostAlias = preBeginArc.getCostAlias() - preEndArc.getCostAlias() + getPathCostAlias(tree,
                    fakeArc.getEndNode().getParent(), fakeArc.getBeginNode().getParent());
            fakeArc.setCostAlias(pseudoCostAlias);
        }
    }

    private static double getPathCostAlias(Tree tree, Node endNode, Node beginNode) {
        if (endNode.getPeriod() != beginNode.getPeriod()) {
            throw new LogicalFailException("path cost alias can be calculated for nodes from one period.");
        }
        return calcPathCostAlias(endNode, beginNode, tree.getArcs());
    }

    private static double calcPathCostAlias(Node endNode, Node beginNode, Set<Arc> arcs) {
        if (endNode == beginNode) {
            return 0;
        }
        Node preEndNode = endNode.getParent();
        Arc arc = ArcUtils.getArc(arcs, preEndNode, endNode);
        if (arc == null) {
            throw new LogicalFailException("no arc between two nodes in tree.");
        }
        return (ArcUtils.isStraight(arc, preEndNode) ? arc.getCostAlias() : -arc.getCostAlias()) + calcPathCostAlias(
                preEndNode, beginNode, arcs);
    }

    public static void calcPotentialAliases(Tree tree, int nodeCount, int periodCount) {
        Set<Node> roots = tree.getRoots(periodCount - 1);
        roots.addAll(getRootByFakeArcs(tree, periodCount - 1));
        for (Node root : roots) {
            root.setPotentialAlias(0.0);
            calcPotentialAliases(root, tree, nodeCount);
        }
    }

    private static void calcPotentialAliases(Node node, Tree tree, int nodeCount) {
        node.getChildren().stream().filter(child -> child.getPotentialAlias() == null && NodeUtils
                .isNotMinusIntermediate(child, node, nodeCount)).forEach(child -> {
            calcChildPotentialAlias(child, node, tree);
            calcPotentialAliases(child, tree, nodeCount);
        });

        Node parent = node.getParent();
        if (parent != null && parent.getPotentialAlias() == null && NodeUtils
                .isNotMinusIntermediate(node, parent, nodeCount)) {
            calcParentPotentialAlias(parent, node, tree);
            calcPotentialAliases(parent, tree, nodeCount);
        }

        if (node.getSign() != Node.Sign.NONE) {
            if (node.getSign() == Node.Sign.PLUS) {
                Set<Node> children = NodeUtils.getEndNodes(tree.getFakeArcs(), node);
                children.stream().filter(child -> child.getPotentialAlias() == null).forEach(child -> {
                    calcChildPotentialAlias(child, node, tree);
                    calcPotentialAliases(child, tree, nodeCount);
                });
            } else {
                Arc fakeArc = ArcUtils.getArc(tree.getFakeArcs(), node);
                Node fakeParent = fakeArc.getBeginNode();
                if (fakeParent.getPotentialAlias() == null) {
                    calcParentPotentialAlias(fakeParent, node, tree);
                    calcPotentialAliases(fakeParent, tree, nodeCount);
                }
            }
        }
    }

    private static void calcChildPotentialAlias(Node child, Node parent, Tree tree) {
        Arc arc = ArcUtils.getArc(tree.getArcs(), child, parent);
        if (arc == null) {
            arc = ArcUtils.getArc(tree.getFakeArcs(), child, parent);
        }
        if (arc.getBeginNode() == child) {
            child.setPotentialAlias(parent.getPotentialAlias() + arc.getCostAlias());
        } else {
            child.setPotentialAlias(parent.getPotentialAlias() - arc.getCostAlias());
        }
    }

    private static void calcParentPotentialAlias(Node parent, Node child, Tree tree) {
        Arc arc = ArcUtils.getArc(tree.getArcs(), parent, child);
        if (arc == null) {
            arc = ArcUtils.getArc(tree.getFakeArcs(), parent, child);
        }
        if (arc.getBeginNode() == parent) {
            parent.setPotentialAlias(child.getPotentialAlias() + arc.getCostAlias());
        } else {
            parent.setPotentialAlias(child.getPotentialAlias() - arc.getCostAlias());
        }
    }

    private static Arc getMinArcAlias(Collection<Arc> arcs, Set<Arc> treeArcs) {
        double minStepAlias = Double.POSITIVE_INFINITY;
        Arc minArcAlias = null;
        for (Arc arc : arcs) {
            if (treeArcs.contains(arc)) {
                continue;
            }

            double stepAlias = Double.POSITIVE_INFINITY;
            Node beginNode = arc.getBeginNode();
            Node endNode = arc.getEndNode();
            double delta = arc.getPeriod() >= 0 ? arc.getEstimate() : arc.getLeap();
            double difference = beginNode.getPotentialAlias() - endNode.getPotentialAlias();
            if (delta == 0) {
                if ((difference > 0 && arc.getFlow() != arc.getCapacity()) || (difference < 0 && arc.getFlow() != 0)) {
                    stepAlias = 0;
                }
            } else if (difference != 0 && delta / difference < 0) {
                stepAlias = -delta / difference;
            }

            if (minStepAlias >= stepAlias) {
                minStepAlias = stepAlias;
                minArcAlias = arc;
            }
        }
        return minArcAlias;
    }

    private static void changeTree(AbstractNet net, Arc minArc, Arc minArcAlias) {
        if (minArc.getNumber() < 0) {
            net.getArcs().remove(minArc.getNumber());
        }
        net.getTree().getArcs().remove(minArc);
        net.getTree().getArcs().add(minArcAlias);
    }

    public static void recalcPlan(Collection<Arc> values, double step) {
        values.stream().forEach((arc) -> arc.setFlow(arc.getFlow() + step * arc.getDirection()));
    }
}
