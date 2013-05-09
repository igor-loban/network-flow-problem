package by.bsu.fpmi.cnfp.main.net;

import java.util.List;

/**
 * @author Igor Loban
 */
public abstract class AbstractNet {
    protected List<Node> nodes;
    protected List<Arc> arcs;
    protected Tree tree;
    protected Flow flow;
    protected int nodeCount;
    protected int arcCount;

    protected AbstractNet(List<Node> nodes, List<Arc> arcs, int nodeCount, int arcCount) {
        this.nodes = nodes;
        this.arcs = arcs;
        this.nodeCount = nodeCount;
        this.arcCount = arcCount;
    }

    public abstract boolean hasSolution();

    public abstract void prepare();

    public abstract boolean isViolated();

    public abstract void recalcPlan();

    public abstract boolean isOptimized();

    public abstract void changeSupport();

    public Tree getTree() {
        return tree;
    }

    public Flow getFlow() {
        return flow;
    }
}
