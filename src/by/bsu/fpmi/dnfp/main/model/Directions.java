package by.bsu.fpmi.dnfp.main.model;

import java.util.Collections;
import java.util.List;

public final class Directions {
    private final List<Double> v;
    private final List<Double> l;

    public Directions(List<Double> v, List<Double> l) {
        this.v = Collections.unmodifiableList(v);
        this.l = Collections.unmodifiableList(l);
    }

    public List<Double> getV() {
        return v;
    }

    public List<Double> getL() {
        return l;
    }
}
