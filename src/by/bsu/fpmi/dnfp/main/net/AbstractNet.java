package by.bsu.fpmi.dnfp.main.net;

import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Flow;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.model.Tree;

import java.util.Map;

/**
 * @author Igor Loban
 */
public abstract class AbstractNet {
    protected Map<Integer, Node> nodes;
    protected Map<Integer, Arc> arcs;
    protected Tree tree;
    protected int iterationCount;
    protected int nodeCount;
    protected int arcCount;
    protected int periodCount;
    protected double eps;

    protected AbstractNet(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount, int arcCount,
                          int periodCount, double eps) {
        this.nodes = nodes;
        this.arcs = arcs;
        this.nodeCount = nodeCount;
        this.arcCount = arcCount;
        this.periodCount = periodCount;
        this.eps = eps;
    }

    public abstract boolean hasSolution();

    public abstract void prepare();

    public abstract boolean isViolated();

    public abstract void recalcPlan();

    public abstract boolean isOptimized();

    public abstract void changeSupport();

    protected abstract void checkIterationLimit();

    public Tree getTree() {
        return tree;
    }

    public Flow getFlow() {
        Flow flow = new Flow();
        for (Arc arc : arcs.values()) {
            flow.put(arc.getNumber(), arc.getFlow());
        }
        return flow;
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public Map<Integer, Arc> getArcs() {
        return arcs;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getArcCount() {
        return arcCount;
    }

    public int getPeriodCount() {
        return periodCount;
    }

    public double getEps() {
        return eps;
    }
}
