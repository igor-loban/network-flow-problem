package by.bsu.fpmi.dnfp.main.util;

import by.bsu.fpmi.dnfp.exception.LogicalFailException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.model.Tree;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Igor Loban
 */
public final class ArcUtils {
    private ArcUtils() {
    }

    public static boolean isStraight(Arc arc, Node beginNode) {
        return arc.getBeginNode() == beginNode;
    }

    public static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, Node artificialNode, Node node) {
        createArtificialArc(arcNumber, arcs, node.getIntensity(), artificialNode, node);
    }

    public static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, double capacity, Node beginNode,
                                           Node endNode) {
        Arc artificialArc = new Arc(arcNumber, getArcPeriod(beginNode, endNode));
        arcs.put(artificialArc.getNumber(), artificialArc);
        artificialArc.setCapacity(Math.abs(capacity));
        artificialArc.setCost(AlgoUtils.BIG_COST);
        if (capacity > 0 || capacity == 0) {
            setupLinks(beginNode, endNode, artificialArc);
        } else {
            setupLinks(endNode, beginNode, artificialArc);
        }
    }

    public static Set<Arc> getArtificialArcs(Set<Arc> arcs) {
        Set<Arc> artificialArcs = new HashSet<>();
        for (Arc arc : arcs) {
            if (AlgoUtils.isArtificial(arc)) {
                artificialArcs.add(arc);
            }
        }
        return artificialArcs;
    }

    public static Set<Arc> getArcs(Set<Arc> arcs, int period) {
        Set<Arc> periodArcs = new HashSet<>();
        for (Arc arc : arcs) {
            if (arc.getPeriod() == period) {
                periodArcs.add(arc);
            }
        }
        return periodArcs;
    }

    public static Arc getArc(Set<Arc> arcs, Node endNode) {
        for (Arc arc : arcs) {
            if (arc.getEndNode().getNumber() == endNode.getNumber()) {
                return arc;
            }
        }
        throw new LogicalFailException("No arc between two nodes in tree.");
    }

    public static Arc getArc(Tree tree, int period, Node endNode) {
        Set<Arc> intermediateTreeArcs = getArcs(tree.getArcs(), -period);
        for (Arc arc : intermediateTreeArcs) {
            if (arc.getEndNode() == endNode) {
                return arc;
            }
        }
        throw new LogicalFailException("No arc between two nodes in tree.");
    }

    public static Arc getArc(Set<Arc> arcs, Node node1, Node node2) {
        for (Arc arc : arcs) {
            if ((arc.getBeginNode() == node1 && arc.getEndNode() == node2) || (arc.getBeginNode() == node2
                    && arc.getEndNode() == node1)) {
                return arc;
            }
        }
        return null;
    }

    private static int getArcPeriod(Node beginNode, Node endNode) {
        int beginPeriod = beginNode.getPeriod();
        int endPeriod = endNode.getPeriod();
        return beginPeriod == endPeriod ? endPeriod : -endPeriod;
    }

    private static void setupLinks(Node beginNode, Node endNode, Arc arc) {
        arc.setBeginNode(beginNode);
        arc.setEndNode(endNode);
        beginNode.addExitArc(arc);
        endNode.addIncomingArc(arc);
    }
}
