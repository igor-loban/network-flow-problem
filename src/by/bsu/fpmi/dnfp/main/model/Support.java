package by.bsu.fpmi.dnfp.main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Support {
    private final int period;
    private final List<Arc> supportArcs;
    private final List<Node> supportNodes;

    public Support(int period, List<Arc> supportArcs, List<Node> supportNodes) {
        this.period = period;

        List<Arc> tempArcs = new ArrayList<>(supportArcs);
        Collections.sort(tempArcs, (arc1, arc2) -> arc1.getNumber() - arc2.getNumber());
        this.supportArcs = Collections.unmodifiableList(tempArcs);

        List<Node> tempNodes = new ArrayList<>(supportNodes);
        Collections.sort(tempNodes, (node1, node2) -> node1.getNumber() - node2.getNumber());
        this.supportNodes = Collections.unmodifiableList(tempNodes);
    }

    public int getSize() {
        return supportArcs.size() + supportNodes.size();
    }

    public int getPeriod() {
        return period;
    }

    public List<Arc> getSupportArcs() {
        return supportArcs;
    }

    public List<Node> getSupportNodes() {
        return supportNodes;
    }
}
