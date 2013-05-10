package by.bsu.fpmi.cnfp.main;

import by.bsu.fpmi.cnfp.exception.AntitheticalConstraintsException;
import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Node;
import by.bsu.fpmi.cnfp.main.model.NumerableObject;
import by.bsu.fpmi.cnfp.main.model.factory.NumerableObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Loban
 */
public final class AlgoUtils {
    private AlgoUtils() {
    }

    public static <T extends NumerableObject, S extends NumerableObject> Map<Integer, T> createStubs(
            Map<Integer, T> sourcePool, NumerableObjectFactory<T, S> factory) {
        Map<Integer, T> stubs = new HashMap<>();
        for (T source : sourcePool.values()) {
            T stub = factory.createStub(source);
            stubs.put(stub.getNumber(), stub);
        }
        return stubs;
    }

    public static <T extends NumerableObject, S extends NumerableObject> void fillStubs(Map<Integer, T> stubs,
                                                                                        Map<Integer, T> sourcePool,
                                                                                        Map<Integer, S> addingPool,
                                                                                        NumerableObjectFactory<T,
                                                                                                S> factory) {
        for (T stub : stubs.values()) {
            T source = sourcePool.get(stub.getNumber());
            factory.fillStub(stub, source, sourcePool, addingPool);
        }
    }

    /**
     * Ввести искусственные вершины, из которых выходят источники и нейтральные узлы и входят стоки (на каждом уровне)
     * Искусственные вершины соединяются дугами из уровня в уровень. Присвоить искусственным дугам стоимость M, где M -
     * большое число (штраф)
     */
    public static void addArtificialNodes(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int periodCount) {
        double totalIntensity = 0;
        for (int nodeNumber = -1, arcNumber = -1; nodeNumber >= -periodCount; nodeNumber--) {
            int period = -nodeNumber;
            Node artificialNode = new Node(nodeNumber);
            nodes.put(nodeNumber, artificialNode);

            if (nodeNumber < -1) {
                if (totalIntensity < 0) {
                    throw new AntitheticalConstraintsException(
                            "Total intensity of periods from 1 to " + period + " equals " + totalIntensity + ".");
                }
                Node previousArtificialNode = nodes.get(nodeNumber + 1);
                createArtificialArc(arcNumber--, arcs, totalIntensity, previousArtificialNode, artificialNode);
            }

            int maxNumber = period * periodCount;
            for (int number = (period - 1) * periodCount + 1; number <= maxNumber; number++) {
                Node node = nodes.get(number);
                createArtificialArc(arcNumber--, arcs, artificialNode, node);
                totalIntensity += node.getIntensity();
            }
        }
    }

    private static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, Node artificialNode, Node node) {
        createArtificialArc(arcNumber, arcs, node.getIntensity(), artificialNode, node);
    }

    private static void createArtificialArc(int arcNumber, Map<Integer, Arc> arcs, double capacity, Node beginNode,
                                            Node endNode) {
        Arc artificialArc = new Arc(arcNumber);
        arcs.put(artificialArc.getNumber(), artificialArc);
        artificialArc.setCapacity(Math.abs(capacity));
        artificialArc.setCost(Double.MAX_VALUE);
        if (capacity > 0 || capacity == 0) {
            setupLinks(beginNode, endNode, artificialArc);
        } else {
            setupLinks(endNode, beginNode, artificialArc);
        }
    }

    private static void setupLinks(Node beginNode, Node endNode, Arc arc) {
        arc.setBeginNode(beginNode);
        arc.setEndNode(endNode);
        beginNode.addExitArc(arc);
        endNode.addIncomingArc(arc);
    }
}
