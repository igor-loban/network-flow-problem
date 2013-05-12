package by.bsu.fpmi.dnfp.main.util;

import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Igor Loban
 */
public final class NodeUtils {
    private NodeUtils() {
    }

    public static Set<Node> getNodes(Set<Arc> arcs) {
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            nodes.add(arc.getBeginNode());
            nodes.add(arc.getEndNode());
        }
        return nodes;
    }

    public static Set<Node> getBeginNodes(Set<Arc> arcs) {
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            nodes.add(arc.getBeginNode());
        }
        return nodes;
    }

    public static Set<Node> getEndNodes(Set<Arc> arcs) {
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            nodes.add(arc.getEndNode());
        }
        return nodes;
    }

    public static Set<Node> getEndNodes(Set<Arc> arcs, Node beginNode) {
        Set<Node> nodes = new HashSet<>();
        for (Arc arc : arcs) {
            if (arc.getBeginNode() == beginNode) {
                nodes.add(arc.getEndNode());
            }
        }
        return nodes;
    }

    public static boolean hasArcFromSet(Node node, Set<Arc> arcs) {
        for (Arc arc : arcs) {
            if (arc.getBeginNode() == node || arc.getEndNode() == node) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotMinusIntermediate(Node child, Node parent, int nodeCount) {
        return (child.getNumber() != parent.getNumber() + nodeCount) || (child.getSign() != Node.Sign.MINUS);
    }
}
