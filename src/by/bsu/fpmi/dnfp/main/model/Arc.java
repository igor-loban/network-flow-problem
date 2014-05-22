package by.bsu.fpmi.dnfp.main.model;

/**
 * @author Igor Loban
 */
public class Arc extends NumerableObject {
    private Node beginNode;
    private Node endNode;
    private boolean fake;
    private double capacity;
    private double cost;
    private double flow;
    private double estimate;
    private double leap;
    private double direction; // v or l
    private double step; // theta

    public Arc() {
    }

    public Arc(int number, int period) {
        super(number, period);
    }

    public Node getBeginNode() {
        return beginNode;
    }

    public void setBeginNode(Node beginNode) {
        this.beginNode = beginNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public boolean isFaked() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(double flow) {
        this.flow = Math.abs(flow);
    }

    public double getEstimate() {
        return estimate;
    }

    public void setEstimate(double estimate) {
        this.estimate = estimate;
    }

    public double getLeap() {
        return leap;
    }

    public void setLeap(double leap) {
        this.leap = leap;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }
}
