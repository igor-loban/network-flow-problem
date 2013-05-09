package by.bsu.fpmi.cnfp.main.net;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Flow;
import by.bsu.fpmi.cnfp.main.model.Node;
import by.bsu.fpmi.cnfp.main.model.Tree;

import java.util.Map;

/**
 * @author Igor Loban
 */
public abstract class AbstractNet {
    protected Map<Integer, Node> nodes;
    protected Map<Integer, Arc> arcs;
    protected Tree tree;
    protected Flow flow;
    protected int nodeCount;
    protected int arcCount;

    protected AbstractNet(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount, int arcCount) {
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
