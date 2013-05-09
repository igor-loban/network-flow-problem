package by.bsu.fpmi.cnfp.main.net;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Svetlana Kostyukovich
 */
public class NetBuilder {
    private Map<Integer, Node> nodes = new HashMap<>();
    private Map<Integer, Arc> arcs = new HashMap<>();
    private double eps;
    private int nodeCount;
    private int arcCount;

    public Net build() {
        return new Net(nodes, arcs, eps, nodeCount, arcCount);
    }

    public NetBuilder setEps(double eps) {
        this.eps = eps;
        return this;
    }

    public NetBuilder setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
        return this;
    }

    public NetBuilder setArcCount(int arcCount) {
        this.arcCount = arcCount;
        return this;
    }

    public NetBuilder addNode(Node node) {
        nodes.put(node.getNumber(), node);
        return this;
    }

    public NetBuilder addArc(Arc arc) {
        arcs.put(arc.getNumber(), arc);
        return this;
    }
}
