package by.bsu.fpmi.cnfp.main.model.factory;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;

import java.util.Map;

/**
 * @author Igor Loban
 */
public final class ArcFactory implements NumerableObjectFactory<Arc, Node> {
    private static final ArcFactory arcFactory = new ArcFactory();

    private ArcFactory() {
    }

    public static ArcFactory getInstance() {
        return arcFactory;
    }

    public Arc createStub(Arc source) {
        Arc stub = new Arc(source.getNumber());
        stub.setCapacity(source.getCapacity());
        stub.setCost(source.getCost());
        stub.setFlow(source.getFlow());
        stub.setEstimate(source.getEstimate());
        return stub;
    }

    public void fillStub(Arc stub, Arc source, Map<Integer, Arc> arcs, Map<Integer, Node> nodes) {
        // TODO: complete coping of arc
    }
}
