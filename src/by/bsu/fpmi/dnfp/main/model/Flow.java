package by.bsu.fpmi.dnfp.main.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Loban
 */
public final class Flow {
    private final Map<Arc, Double> flowValues = new HashMap<>();

    public void put(Arc arc, double flowValue) {
        flowValues.put(arc, flowValue);
    }

    @Override
    public String toString() {
        return "Flow{flowValues=" + flowValues + '}';
    }
}
