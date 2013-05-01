package by.bsu.fpmi.cnfp.main.net;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Svetlana Kostyukovich
 */
public class NetBuilder {
    private List<Node> nodes = new ArrayList<>();
    private List<Arc> arcs = new ArrayList<>();
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
        nodes.add(node);
        return this;
    }

    public NetBuilder addArc(Arc arc) {
        arcs.add(arc);
        return this;
    }
}
