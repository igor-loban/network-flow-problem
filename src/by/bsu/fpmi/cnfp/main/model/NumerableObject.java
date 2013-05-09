package by.bsu.fpmi.cnfp.main.model;

/**
 * @author Igor Loban
 */
public abstract class NumerableObject {
    private int number;

    protected NumerableObject() {
    }

    protected NumerableObject(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
