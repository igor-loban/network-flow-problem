package by.bsu.fpmi.cnfp.main.util;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;

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
}
