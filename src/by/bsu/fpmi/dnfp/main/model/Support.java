package by.bsu.fpmi.dnfp.main.model;

import java.util.*;
import java.util.function.ToIntFunction;

public final class Support {
    private final List<Arc> supportArcs;
    private final List<Arc> noSupportArcs;
    private final List<Arc> intermediateNoSupportArcs;
    private final List<Node> supportNodes;
    private final Map<Node, Arc> supportNodableArcs;
    private final Map<Integer, Integer> nodeNumbers;

    public Support(List<Arc> supportArcs, List<Arc> noSupportArcs, List<Arc> intermediateNoSupportArcs,
                   List<Node> supportNodes, Map<Node, Arc> supportNodableArcs, Map<Integer, Integer> nodeNumbers) {
        this.supportNodableArcs = supportNodableArcs;
        this.nodeNumbers = nodeNumbers;

        List<Arc> tempArcs = new ArrayList<>(supportArcs);
        Comparator<NumerableObject> comparator = Comparator.comparingInt(new ToIntFunction<NumerableObject>() {
            @Override
            public int applyAsInt(NumerableObject object) {
                return object.getNumber();
            }
        });
        Collections.sort(tempArcs, comparator);
        this.supportArcs = Collections.unmodifiableList(tempArcs);

        List<Node> tempNodes = new ArrayList<>(supportNodes);
        Collections.sort(tempNodes, comparator);
        this.supportNodes = Collections.unmodifiableList(tempNodes);

        tempArcs = new ArrayList<>(noSupportArcs);
        Collections.sort(tempArcs, comparator);
        this.noSupportArcs = Collections.unmodifiableList(tempArcs);

        tempArcs = new ArrayList<>(intermediateNoSupportArcs);
        Collections.sort(tempArcs, comparator);
        this.intermediateNoSupportArcs = Collections.unmodifiableList(tempArcs);
    }

    public int getIndex(Node node) {
        return nodeNumbers.get(node.getNumber());
    }

    public List<Arc> getSupportArcs() {
        return supportArcs;
    }

    public List<Node> getSupportNodes() {
        return supportNodes;
    }

    public List<Arc> getNoSupportArcs() {
        return noSupportArcs;
    }

    public List<Arc> getIntermediateNoSupportArcs() {
        return intermediateNoSupportArcs;
    }

    public Map<Node, Arc> getSupportNodableArcs() {
        return supportNodableArcs;
    }
}
