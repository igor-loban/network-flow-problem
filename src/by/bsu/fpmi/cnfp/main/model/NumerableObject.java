package by.bsu.fpmi.cnfp.main.model;

/**
 * @author Igor Loban
 */
public abstract class NumerableObject {
    private int number;
    private int period;

    protected NumerableObject() {
    }

    protected NumerableObject(int number, int period) {
        this.number = number;
        this.period = period;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
