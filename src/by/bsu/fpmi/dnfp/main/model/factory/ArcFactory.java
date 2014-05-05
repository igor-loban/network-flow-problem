package by.bsu.fpmi.dnfp.main.model.factory;

import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;

import java.util.Map;

/**
 * @author Igor Loban
 */
public final class ArcFactory implements NumerableObjectFactory<Arc, Node> {
    private static final ArcFactory ARC_FACTORY = new ArcFactory();

    private ArcFactory() {
    }

    public static ArcFactory getInstance() {
        return ARC_FACTORY;
    }

    public Arc createStub(Arc source) {
        Arc stub = new Arc(source.getNumber(), source.getPeriod());
        stub.setCapacity(source.getCapacity());
        stub.setCost(source.getCost());
        stub.setFlow(source.getFlow());
        stub.setEstimate(source.getEstimate());
        return stub;
    }

    public void fillStub(Arc stub, Arc source, Map<Integer, Arc> arcs, Map<Integer, Node> nodes) {
        stub.setBeginNode(nodes.get(source.getBeginNode().getNumber()));
        stub.setEndNode(nodes.get(source.getEndNode().getNumber()));
    }
}
