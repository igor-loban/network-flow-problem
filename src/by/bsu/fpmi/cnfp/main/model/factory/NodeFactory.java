package by.bsu.fpmi.cnfp.main.model.factory;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;

import java.util.Map;

/**
 * @author Igor Loban
 */
public final class NodeFactory implements NumerableObjectFactory<Node, Arc> {
    private static final NodeFactory nodeFactory = new NodeFactory();

    private NodeFactory() {
    }

    public static NodeFactory getInstance() {
        return nodeFactory;
    }

    public Node createStub(Node source) {
        Node stub = new Node(source.getNumber());
        stub.setPotential(source.getPotential());
        stub.setDepth(source.getDepth());
        stub.setIntensity(source.getIntensity());
        stub.setCapacity(source.getCapacity());
        stub.setCost(source.getCost());
        return stub;
    }

    public void fillStub(Node stub, Node source, Map<Integer, Node> nodes, Map<Integer, Arc> arcs) {
        for (Node node : source.getDescendants()) {
            stub.addDescendant(nodes.get(node.getNumber()));
        }
        for (Arc arc : source.getIncomingArcs()) {
            stub.addIncomingArc(arcs.get(arc.getNumber()));
        }
        for (Arc arc : source.getExitArcs()) {
            stub.addExitArc(arcs.get(arc.getNumber()));
        }
    }
}
