package by.bsu.fpmi.dnfp.main.util;

import by.bsu.fpmi.dnfp.exception.AntitheticalConstraintsException;
import by.bsu.fpmi.dnfp.exception.LogicalFailException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.model.NumObjComparator;
import by.bsu.fpmi.dnfp.main.model.NumerableObject;
import by.bsu.fpmi.dnfp.main.model.Support;
import by.bsu.fpmi.dnfp.main.model.Tree;
import by.bsu.fpmi.dnfp.main.model.factory.NumerableObjectFactory;
import by.bsu.fpmi.dnfp.main.net.AbstractNet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        Tree tree = new Tree();
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

    public static void calcPotentials(AbstractNet net, int nodeCount, int periodCount) {
        for (Node node : net.getNodes().values()) {
            node.setPotential(null);
        }

        Tree tree = net.getTree();
        Set<Node> roots = tree.getRoots(periodCount - 1);
        roots.addAll(getRootByFakeArcs(tree, periodCount - 1));
        for (Node root : roots) {
            root.setPotential(0.0);
            calcPotentials(root, tree, nodeCount);
        }

        //        for (Node node : net.getNodes().values()) {
        //            System.out.println("Node " + node.getNumber() + " has potential " + node.getPotential());
        //        }
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

    public static void calcDirections(AbstractNet net) {
        int periodCount = net.getPeriodCount();
        int nodeCountPerPeriod = getNodeCountPerPeriod(net, 0);

        // For a 0 period
        Support support = getSupport(net, 0, nodeCountPerPeriod);
        double[][] A = getMatrixA(support, nodeCountPerPeriod);
        double[][] PreF = getMatrixPreF(support, nodeCountPerPeriod);
        double[] v = getNoSupportV(support.getNoSupportArcs());
        double[] noSupportL = getNoSupportL(support, nodeCountPerPeriod);
        double[] l = new double[nodeCountPerPeriod];
        double[] intensities = getIntensities(net, support, nodeCountPerPeriod, 0);
        double[] result = AlgebraUtils.calcResult(A, l, PreF, v, noSupportL, intensities);
        System.out.println("Result: " + Arrays.toString(result));
        fillDirections(support, l, noSupportL, result);

        for (int period = 1; period < periodCount - 1; period++) {
            nodeCountPerPeriod = getNodeCountPerPeriod(net, period);
            support = getSupport(net, period, nodeCountPerPeriod);
            A = getMatrixA(support, nodeCountPerPeriod);
            PreF = getMatrixPreF(support, nodeCountPerPeriod);
            v = getNoSupportV(support.getNoSupportArcs());
            noSupportL = getNoSupportL(support, nodeCountPerPeriod);
            l = getNewL(l, nodeCountPerPeriod);
            intensities = getIntensities(net, support, nodeCountPerPeriod, period);
            result = AlgebraUtils.calcResult(A, l, PreF, v, noSupportL, intensities);
            System.out.println("Result: " + Arrays.toString(result));
            fillDirections(support, l, noSupportL, result);
        }

        int period = periodCount - 1;
        nodeCountPerPeriod = getNodeCountPerPeriod(net, period);
        support = getSupport2(net, period, nodeCountPerPeriod);
        A = getMatrixA(support, nodeCountPerPeriod);
        PreF = getMatrixPreF(support, nodeCountPerPeriod);
        v = getNoSupportV(support.getNoSupportArcs());
        noSupportL = new double[nodeCountPerPeriod];
        l = getNewL(l, nodeCountPerPeriod); // ???
        intensities = getIntensities(net, support, nodeCountPerPeriod, period); // ???
        result = AlgebraUtils.calcResult(A, l, PreF, v, noSupportL, intensities);
        System.out.println("Result: " + Arrays.toString(result));
        fillDirections(support, l, noSupportL, result);
    }

    private static Support getSupport2(AbstractNet net, int period, int nodeCountPerPeriod) {
        List<Arc> supportArcs = new ArrayList<>();
        List<Arc> noSupportArcs = new ArrayList<>();
        Set<Node> nodes = new TreeSet<>(new NumObjComparator());

        Tree tree = net.getTree();
        Set<Arc> treeArcs = tree.getArcs();
        for (Arc arc : net.getArcs().values()) {
            if (arc.getPeriod() == period) {
                nodes.add(arc.getBeginNode());
                nodes.add(arc.getEndNode());
                if (treeArcs.contains(arc)) {
                    supportArcs.add(arc);
                } else {
                    noSupportArcs.add(arc);
                }
            }
        }

        int delta = nodeCountPerPeriod - supportArcs.size();
        for (int i = 0; i < delta; i++) {
            Arc arc = noSupportArcs.remove(0);
            supportArcs.add(arc);
        }

        Map<Integer, Integer> nodeNumbers = new HashMap<>();
        int i = 0;
        for (Node node : nodes) {
            nodeNumbers.put(node.getNumber(), i++);
        }

        return new Support(supportArcs, noSupportArcs, Collections.<Arc>emptyList(), Collections.<Node>emptyList(),
                Collections.<Node, Arc>emptyMap(), nodeNumbers);
    }

    private static double[] getNewL(double[] l, int nodeCountPerPeriod) {
        if (l.length == nodeCountPerPeriod) {
            return l;
        }
        double[] newL = new double[nodeCountPerPeriod];
        System.arraycopy(l, 1, newL, 0, nodeCountPerPeriod);
        return newL;
    }

    private static double[] getIntensities(AbstractNet net, Support support, int nodeCountPerPeriod, int period) {
        double[] intensities = new double[nodeCountPerPeriod];
        for (Node node : net.getNodes().values()) {
            if (node.getPeriod() == period) {
                intensities[support.getIndex(node)] = node.getIntensity();
            }
        }
        return intensities;
    }

    private static void fillDirections(Support support, double[] l, double[] noSupportL, double[] result) {
        for (int i = 0; i < l.length; i++) {
            l[i] = 0;
        }

        List<Node> supportNodes = support.getSupportNodes();
        Map<Node, Arc> supportNodableArcs = support.getSupportNodableArcs();
        int index = 0;
        for (int i = 0; i < supportNodableArcs.size(); i++, index++) {
            Node node = supportNodes.get(i);
            l[support.getIndex(node)] = result[index];
            Arc arc = supportNodableArcs.get(node);
            if (arc != null) {
                arc.setDirection(result[index] - arc.getFlow());  //изменено
            }
        }

        List<Arc> supportArcs = support.getSupportArcs();
        for (int i = 0; i < supportArcs.size(); i++, index++) {
            supportArcs.get(i).setDirection(result[index] - supportArcs.get(i).getFlow()); //изменено
        }

        for (Arc arc : support.getIntermediateNoSupportArcs()) {
            Node node = arc.getBeginNode();
            arc.setDirection(noSupportL[support.getIndex(node)] - arc.getFlow()); // изменено
        }
    }

    private static int getNodeCountPerPeriod(AbstractNet net, int period) {
        int count = 0;
        for (Node node : net.getNodes().values()) {
            if (node.getPeriod() == period) {
                count++;
            }
        }
        return count;
    }

    private static double[][] getMatrixA(Support support, int nodeCountPerPeriod) {
        double A[][] = new double[nodeCountPerPeriod][];

        for (int i = 0; i < nodeCountPerPeriod; i++) {
            A[i] = new double[nodeCountPerPeriod];
        }

        List<Node> supportNodes = support.getSupportNodes();
        int column = 0;
        for (int i = 0; i < supportNodes.size(); i++, column++) {
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

    private static double[][] getMatrixPreF(Support support, int nodeCountPerPeriod) {
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
            double direction;
            if (ArcUtils.hasZeroEstimate(arc)) {
                direction = 0;
            } else {
                //                direction = arc.getEstimate() > 0 ? arc.getCapacity() - arc.getFlow() : -arc
                // .getFlow();
                direction = arc.getEstimate() > 0 ? arc.getCapacity() : 0;  // изменено
            }
            noSupportV.add(direction);
            arc.setDirection(direction - arc.getFlow());
        }

        double[] result = new double[noSupportV.size()];
        for (int i = 0; i < noSupportV.size(); i++) {
            result[i] = noSupportV.get(i);
        }
        return result;
    }

    private static double[] getNoSupportL(Support support, int nodeCountPerPeriod) {
        double[] result = new double[nodeCountPerPeriod];
        for (Arc arc : support.getIntermediateNoSupportArcs()) {
            int index = support.getIndex(arc.getBeginNode());
            if (arc.getLeap() > 0) {
                //               result[index] = arc.getCapacity() - arc.getFlow();
                result[index] = arc.getCapacity();
            } else if (arc.getLeap() < 0) {
                result[index] = 0;
            }
        }
        return result;
    }

    private static Support getSupport(AbstractNet net, int period, int nodeCountPerPeriod) {
        List<Arc> supportArcs = new ArrayList<>();
        List<Node> supportNodes = new ArrayList<>();
        List<Node> supportArcableNodes = new ArrayList<>();
        Set<Node> nodes = new TreeSet<>(new NumObjComparator());

        Tree tree = net.getTree();
        int intermediatePeriod = -period - 1;
        for (Arc arc : tree.getArcs()) {
            if (arc.getPeriod() == period) {
                supportArcs.add(arc);
                nodes.add(arc.getBeginNode());
                nodes.add(arc.getEndNode());
            } else if (arc.getPeriod() == intermediatePeriod) {
                supportNodes.add(arc.getBeginNode());
                supportArcableNodes.add(arc.getBeginNode());
            }
        }

        for (Node node : net.getNodes().values()) {
            if (node.getPeriod() == period && !nodes.contains(node) && !supportNodes.contains(node)) {
                supportNodes.add(node);
            }
        }

        int delta = nodeCountPerPeriod - supportArcs.size() - supportNodes.size();
        for (int i = 0; i < delta; i++) {
            supportNodes.add(supportArcs.get(i).getEndNode());
        }
        for (int i = -delta; i > 0; i--) {
            Node node = supportArcableNodes.remove(0);
            supportNodes.remove(node);
            Arc arc = ArcUtils.getIntermediateArc(node);
            if (arc != null) {
                arc.setDirection(-arc.getFlow());
            }
        }

        List<Arc> noSupportArcs = new ArrayList<>();
        List<Arc> artificialNoSupportArcs = new ArrayList<>();
        for (Arc arc : net.getArcs().values()) {
            if (arc.getPeriod() == period && !supportArcs.contains(arc)) {
                noSupportArcs.add(arc);
                nodes.add(arc.getBeginNode());
                nodes.add(arc.getEndNode());
            } else if (arc.getPeriod() == intermediatePeriod && !tree.getArcs().contains(arc)) {
                artificialNoSupportArcs.add(arc);
            }
        }

        Map<Node, Arc> supportNodableArcs = new HashMap<>();
        for (Node node : supportNodes) {
            nodes.add(node);
            supportNodableArcs.put(node, ArcUtils.getIntermediateArc(node));
        }

        Map<Integer, Integer> nodeNumbers = new HashMap<>();
        int i = 0;
        for (Node node : nodes) {
            nodeNumbers.put(node.getNumber(), i++);
        }

        return new Support(supportArcs, noSupportArcs, artificialNoSupportArcs, supportNodes, supportNodableArcs,
                nodeNumbers);
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
            if (isNewMinArc(minArc, minStep, arc, step)) {
                minStep = step;
                minArc = arc;
            }
        }
        if (minArc == null) {
            return findMinArc(arcs);
        }
        return minArc;
    }

    private static Arc findMinArc(Set<Arc> arcs) {
        for (Arc arc : arcs) {
            if (arc.getNumber() < 0) {
                return arc;
            }
        }
        return arcs.iterator().next();
    }

    private static boolean isNewMinArc(Arc minArc, double minStep, Arc arc, double step) {
        if (minStep > step) {
            return true;
        }
        if (minStep < step) {
            return false;
        }
        if (minArc == null) {
            return true;
        }
        if (minArc.getNumber() > 0 && arc.getNumber() < 0) {
            return true;
        }
        if (minArc.getCost() < arc.getCost()) {
            return true;
        }
        return false;
    }

    public static void changeSupport(AbstractNet net, Arc minArc) {
        setupCostAliases(net.getTree(), minArc);
        calcPseudoCostAliases(net.getTree());
        calcPotentialAliases(net);
        Arc minArcAlias = getMinArcAlias(net.getArcs().values(), net.getTree().getArcs());
        minArcAlias = checkMinArcAlias(net, minArc, minArcAlias);
        changeTree(net, minArc, minArcAlias);
    }

    private static Arc checkMinArcAlias(AbstractNet net, Arc minArc, Arc minArcAlias) {
        if (minArcAlias.getNumber() >= 0) {
            return minArcAlias;
        }

        class ArcDescriptor implements Comparable<ArcDescriptor> {
            int number;
            double stepAlias;

            ArcDescriptor(int number, double stepAlias) {
                this.number = number;
                this.stepAlias = stepAlias;
            }

            @Override public int compareTo(ArcDescriptor o) {
                double delta = stepAlias - o.stepAlias;
                if (delta < 0) {
                    return -1;
                } else if (delta > 0) {
                    return 1;
                }
                return 0;
            }
        }

        Set<ArcDescriptor> arcDescriptors = new TreeSet<>();
        for (Arc arc : net.getArcs().values()) {
            if (arc != minArc && arc != minArcAlias && arc.getNumber() >= 0) {
                arcDescriptors.add(new ArcDescriptor(arc.getNumber(), arc.getStepAlias()));
            }
        }

        Set<Arc> treeArcs = new HashSet<>(net.getTree().getArcs());
        treeArcs.remove(minArc);
        for (ArcDescriptor arcDescriptor : arcDescriptors) {
            Arc arc = net.getArcs().get(arcDescriptor.number);
            treeArcs.add(arc);
            if (isTree(treeArcs)) {
                System.out.println("minArcAlias replaced on " + arc);
                return arc;
            }
            treeArcs.remove(arc);
        }

        return minArcAlias;
    }

    private static boolean isTree(Set<Arc> treeArcs) {
        Set<Arc> arcs = new HashSet<>(treeArcs);
        Set<Node> markedNodes = new HashSet<>();
        Set<Node> nodes = new HashSet<>();
        nodes.add(treeArcs.iterator().next().getBeginNode());
        while (!nodes.isEmpty()) {
            Iterator<Node> iterator = nodes.iterator();
            Node node = iterator.next();
            iterator.remove();

            for (Arc arc : node.getExitArcs()) {
                if (arcs.contains(arc)) {
                    arcs.remove(arc);
                    Node node1 = arc.getEndNode();
                    if (markedNodes.contains(node1)) {
                        return false;
                    }
                    markedNodes.add(node1);
                    nodes.add(node1);
                }
            }

            for (Arc arc : node.getIncomingArcs()) {
                if (arcs.contains(arc)) {
                    arcs.remove(arc);
                    Node node1 = arc.getBeginNode();
                    if (markedNodes.contains(node1)) {
                        return false;
                    }
                    markedNodes.add(node1);
                    nodes.add(node1);
                }
            }
        }
        return true;
    }

    private static void setupCostAliases(Tree tree, Arc minArc) {
        for (Arc arc : tree.getArcs()) {
            arc.setCostAlias(0);
        }
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

    public static void calcPotentialAliases(AbstractNet net) {
        for (Node node : net.getNodes().values()) {
            node.setPotentialAlias(null);
        }

        Tree tree = net.getTree();
        int periodCount = net.getPeriodCount();
        Set<Node> roots = tree.getRoots(periodCount - 1);
        roots.addAll(getRootByFakeArcs(tree, periodCount - 1));
        for (Node root : roots) {
            root.setPotentialAlias(0.0);
            calcPotentialAliases(root, tree, net.getNodeCount());
        }
    }

    private static void calcPotentialAliases(Node node, Tree tree, int nodeCount) {
        for (Node child : node.getChildren()) {
            if (child.getPotentialAlias() == null && NodeUtils.isNotMinusIntermediate(child, node, nodeCount)) {
                calcChildPotentialAlias(child, node, tree);
                calcPotentialAliases(child, tree, nodeCount);
            }
        }

        Node parent = node.getParent();
        if (parent != null && parent.getPotentialAlias() == null && NodeUtils
                .isNotMinusIntermediate(node, parent, nodeCount)) {
            calcParentPotentialAlias(parent, node, tree);
            calcPotentialAliases(parent, tree, nodeCount);
        }

        if (node.getSign() != Node.Sign.NONE) {
            if (node.getSign() == Node.Sign.PLUS) {
                Set<Node> children = NodeUtils.getEndNodes(tree.getFakeArcs(), node);
                for (Node child : children) {
                    if (child.getPotentialAlias() == null) {
                        calcChildPotentialAlias(child, node, tree);
                        calcPotentialAliases(child, tree, nodeCount);
                    }
                }
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

            arc.setStepAlias(stepAlias);

            if (isNewMinArcAlias(minArcAlias, minStepAlias, arc, stepAlias)) {
                minStepAlias = stepAlias;
                minArcAlias = arc;
            }
        }
        return minArcAlias;
    }

    private static boolean isNewMinArcAlias(Arc minArcAlias, double minStepAlias, Arc arc, double stepAlias) {
        if (minStepAlias > stepAlias) {
            return true;
        }
        if (minStepAlias < stepAlias) {
            return false;
        }
        if (minArcAlias == null) {
            return true;
        }
        if (minArcAlias.getNumber() < 0 && arc.getNumber() > 0) {
            return true;
        }
        if (minArcAlias.getCost() > arc.getCost()) {
            return true;
        }
        return false;
    }

    private static void changeTree(AbstractNet net, Arc minArc, Arc minArcAlias) {
        //        if (minArc.getNumber() < 0) {
        //            net.getArcs().remove(minArc.getNumber());
        //            removeArc(net, minArc);
        //        }

        System.out.println(
                "Remove from tree " + minArc.getBeginNode().getNumber() + "->" + minArc.getEndNode().getNumber());
        net.getTree().getArcs().remove(minArc);
        System.out.println(
                "Add to tree " + minArcAlias.getBeginNode().getNumber() + "->" + minArcAlias.getEndNode().getNumber());
        net.getTree().getArcs().add(minArcAlias);

        //        Iterator<Arc> iterator = net.getArcs().values().iterator();
        //        while (iterator.hasNext()) {
        //            Arc arc = iterator.next();
        //            if (arc.getNumber() < 0 && arc.getFlow() <= 0.00000001) {
        //                iterator.remove();
        //                net.getTree().getArcs().remove(arc);
        //                removeArc(net, arc);
        //            }
        //        }

        recalcParentsAndChildren(net.getTree());
    }

    private static void recalcParentsAndChildren(Tree tree) {
        Set<Arc> arcs = tree.getArcs();
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            nodes.add(arc.getBeginNode());
            nodes.add(arc.getEndNode());
        }

        while (!nodes.isEmpty()) {
            Node root = nodes.iterator().next();
            nodes.remove(root);
            root.setParent(null);
            root.getChildren().clear();

            Set<Node> nextLevel = new HashSet<>();
            nextLevel.add(root);
            while (!nextLevel.isEmpty()) {
                root = nextLevel.iterator().next();
                nextLevel.remove(root);
                root.getChildren().clear();

                for (Arc arc : root.getExitArcs()) {
                    Node endNode = arc.getEndNode();
                    if (arcs.contains(arc) && nodes.contains(endNode)) {
                        setupParentChildLink(root, endNode, nodes, nextLevel);
                    }
                }

                for (Arc arc : root.getIncomingArcs()) {
                    Node beginNode = arc.getBeginNode();
                    if (arcs.contains(arc) && nodes.contains(beginNode)) {
                        setupParentChildLink(root, beginNode, nodes, nextLevel);
                    }
                }
            }
        }
    }

    private static void setupParentChildLink(Node parent, Node child, Set<Node> source, Set<Node> target) {
        child.setParent(parent);
        parent.addChild(child);
        source.remove(child);
        target.add(child);
    }

    private static void removeArc(AbstractNet net, Arc arc) {
        System.out.println("Remove from net " + arc.getBeginNode().getNumber() + "->" + arc.getEndNode().getNumber());

        net.setArcCount(net.getArcCount() - 1);

        Node beginNode = arc.getBeginNode();
        beginNode.getExitArcs().remove(arc);
        if (beginNode.getIncomingArcs().isEmpty() && beginNode.getExitArcs().isEmpty()) {
            removeFromParentAndChildren(beginNode);
            net.getNodes().remove(beginNode.getNumber());
            net.setNodeCount(net.getNodeCount() - 1);
        }

        Node endNode = arc.getEndNode();
        endNode.getIncomingArcs().remove(arc);
        if (endNode.getIncomingArcs().isEmpty() && endNode.getExitArcs().isEmpty()) {
            net.getNodes().remove(endNode.getNumber());
            net.setNodeCount(net.getNodeCount() - 1);
        }
    }

    private static void removeFromParentAndChildren(Node node) {
        Node parent = node.getParent();
        if (parent == null) {
            for (Node child : node.getChildren()) {
                child.setParent(null);
            }
        } else {
            parent.getChildren().remove(node);
        }
    }

    public static void recalcPlan(Collection<Arc> arcs, double step) {
        for (Arc arc : arcs) {
            arc.setFlow(arc.getFlow() + step * arc.getDirection());
        }
    }
}
