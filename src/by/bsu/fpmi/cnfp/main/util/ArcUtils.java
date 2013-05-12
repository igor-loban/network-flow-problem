package by.bsu.fpmi.cnfp.main.util;

import by.bsu.fpmi.cnfp.exception.LogicalFailException;
import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Igor Loban
 */
public final class ArcUtils {
    private ArcUtils() {
    }

    public static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, Node artificialNode, Node node) {
        createArtificialArc(arcNumber, arcs, node.getIntensity(), artificialNode, node);
    }

    public static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, double capacity, Node beginNode,
                                           Node endNode) {
        Arc artificialArc = new Arc(arcNumber, getArcPeriod(beginNode, endNode));
        arcs.put(artificialArc.getNumber(), artificialArc);
        artificialArc.setCapacity(Math.abs(capacity));
        artificialArc.setCost(Double.MAX_VALUE);
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
