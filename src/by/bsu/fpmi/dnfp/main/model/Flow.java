package by.bsu.fpmi.dnfp.main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Loban
 */
public final class Flow {
    private final Map<Arc, Double> flowValues = new HashMap<>();

    public void put(Arc arc, double flowValue) {
        flowValues.put(arc, flowValue);
    }

    public Map<Arc, Double> asMap() {
        List<Arc> arcs = new ArrayList<>(flowValues.keySet());
        Collections.sort(arcs, new Comparator<Arc>() {
            @Override public int compare(Arc o1, Arc o2) {
                return o1.getNumber() - o2.getNumber();
            }
        });
        Map<Arc, Double> view = new LinkedHashMap<>();
        for (Arc arc : arcs) {
            view.put(arc, flowValues.get(arc));
        }
        return view;
    }

    @Override
    public String toString() {
        return "Flow{flowValues=" + flowValues + '}';
    }
}
