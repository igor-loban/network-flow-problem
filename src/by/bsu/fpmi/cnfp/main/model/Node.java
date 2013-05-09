package by.bsu.fpmi.cnfp.main.model;

import java.util.List;

/**
 * @author Igor Loban
 */
public class Node extends NumerableObject {
    private double potential;
    private Arc exitArc; // TODO: add links with arcs
    private Arc incomingArc;
    private List<Node> descendants;
    private Node ancestor;
    private int depth;
    private int productivity;
    private int capacity;
    private int cost;

    public Node() {
    }

    public Node(int number) {
        super(number);
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
