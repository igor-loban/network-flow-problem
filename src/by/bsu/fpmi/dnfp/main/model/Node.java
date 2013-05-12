package by.bsu.fpmi.dnfp.main.model;

import java.util.HashSet;
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
    private Sign sign = Sign.NONE;
    private Node ancestor;
    private Set<Node> descendants = new HashSet<>();
    private Set<Arc> incomingArcs = new HashSet<>();
    private Set<Arc> exitArcs = new HashSet<>();

    public Node() {
    }

    public Node(int number, int period) {
        super(number, period);
    }

    public void addDescendant(Node node) {
        descendants.add(node);
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

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }

    public Set<Arc> getIncomingArcs() {
        return incomingArcs;
    }

    public Set<Arc> getExitArcs() {
        return exitArcs;
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

    public static enum Sign {
        NONE, PLUS, MINUS
    }
}
