package by.bsu.fpmi.cnfp.main.net;

import java.util.Map;

/**
 * @author Igor Loban
 */
public final class NodeFactory {
    private NodeFactory() {
    }

    public static Node createStubFrom(Node source) {
        Node stub = new Node(source.getNumber());
        stub.setPotential(source.getPotential());
        stub.setDepth(source.getDepth());
        stub.setProductivity(source.getProductivity());
        stub.setCapacity(source.getCapacity());
        stub.setCost(source.getCost());
        return stub;
    }

    public static void fillStub(Node stub, Node source, Map<Integer, Node> nodes, Map<Integer, Arc> arcs) {
        Arc exitArc = source.getExitArc();
        stub.setExitArc(exitArc == null ? null : arcs.get(exitArc.getNumber()));
        // TODO: complete coping of node
    }
}
