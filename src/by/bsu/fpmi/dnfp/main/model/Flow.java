package by.bsu.fpmi.dnfp.main.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Loban
 */
public class Flow {
    private Map<Integer, Double> flowValues = new HashMap<>();

    public void put(int arcNumber, double flowValue) {
        flowValues.put(arcNumber, flowValue);
    }

    public double get(int arcNumber) {
        return flowValues.get(arcNumber);
    }
}
