package by.bsu.fpmi.cnfp.main.net;

import java.util.List;

/**
 * @author Igor Loban
 */
public class Net {
    private List<Node> nodes;
    private List<Arc> arcs;
    private Tree tree;
    private Flow flow;
    private double eps;
    private int nodeCount;
    private int arcCount;

    public Net(List<Node> nodes, List<Arc> arcs, double eps, int nodeCount, int arcCount) {
        this.nodes = nodes;
        this.arcs = arcs;
        this.eps = eps;
        this.nodeCount = nodeCount;
        this.arcCount = arcCount;
    }

    public void prepare() {
    }

    public void nextIteration() {

    }

    public double calcSuboptimality() {
        return 0;
    }

    public double getEps() {
        return eps;
    }

    private class Tree {

    }

    private class Flow {

    }
}
