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
    private double direction; // l
    private Double potential;
    private Sign sign = Sign.NONE;
    private Node parent;
    private Set<Node> children = new HashSet<>();
    private Set<Arc> incomingArcs = new HashSet<>();
    private Set<Arc> exitArcs = new HashSet<>();

    public Node() {
    }

    public Node(int number, int period) {
        super(number, period);
    }

    public void addChild(Node node) {
        children.add(node);
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

    public Double getPotential() {
        return potential;
    }

    public void setPotential(Double potential) {
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

    public Set<Node> getChildren() {
        return children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public static enum Sign {
        NONE, PLUS, MINUS
    }
}
