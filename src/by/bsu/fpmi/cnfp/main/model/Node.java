package by.bsu.fpmi.cnfp.main.model;

import java.util.Set;

/**
 * @author Igor Loban
 */
public class Node extends NumerableObject {
    private int depth;
    private int intensity;
    private int capacity;
    private int cost;
    private double potential;
    private Set<Arc> incomingArcs;
    private Set<Arc> exitArcs;
    private Set<Node> descendants;
    private Node ancestor;

    public Node() {
    }

    public Node(int number) {
        super(number);
    }

    public void addIncomingArc(Arc arc) {
        incomingArcs.add(arc);
    }

    public void addExitArc(Arc arc) {
        exitArcs.add(arc);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
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

    public double getPotential() {
        return potential;
    }

    public void setPotential(double potential) {
        this.potential = potential;
    }

    public Set<Arc> getIncomingArcs() {
        return incomingArcs;
    }

    public void setIncomingArcs(Set<Arc> incomingArcs) {
        this.incomingArcs = incomingArcs;
    }

    public Set<Arc> getExitArcs() {
        return exitArcs;
    }

    public void setExitArcs(Set<Arc> exitArcs) {
        this.exitArcs = exitArcs;
    }

    public Set<Node> getDescendants() {
        return descendants;
    }

    public void setDescendants(Set<Node> descendants) {
        this.descendants = descendants;
    }

    public Node getAncestor() {
        return ancestor;
    }

    public void setAncestor(Node ancestor) {
        this.ancestor = ancestor;
    }
}
