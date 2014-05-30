package by.bsu.fpmi.dnfp.main.model;

import java.util.Comparator;

public class NumObjComparator implements Comparator<NumerableObject> {
    @Override public int compare(NumerableObject o1, NumerableObject o2) {
        return o1.getNumber() - o2.getNumber();
    }
}
