package by.bsu.fpmi.cnfp.main.net;

import java.util.List;

/**
 * @author Igor Loban
 */
public class Node {
    private int number;
    private double potential;
    private Arc exitArc;
    private Arc incomingArc;
    private List<Node> descendants;
    private Node ancestor;
    private int depth;
    private int productivity; // TODO: исток/сток

    public Node() {
    }

    public Node(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getPotential() {
        return potential;
    }

    public void setPotential(double potential) {
        this.potential = potential;
    }

    public Arc getExitArc() {
        return exitArc;
    }

    public void setExitArc(Arc exitArc) {
        this.exitArc = exitArc;
    }

    public Arc getIncomingArc() {
        return incomingArc;
    }

    public void setIncomingArc(Arc incomingArc) {
        this.incomingArc = incomingArc;
    }

    public List<Node> getDescendants() {
        return descendants;
    }

    public void setDescendants(List<Node> descendants) {
        this.descendants = descendants;
    }

    public Node getAncestor() {
        return ancestor;
    }

    public void setAncestor(Node ancestor) {
        this.ancestor = ancestor;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getProductivity() {
        return productivity;
    }

    public void setProductivity(int productivity) {
        this.productivity = productivity;
    }
}
