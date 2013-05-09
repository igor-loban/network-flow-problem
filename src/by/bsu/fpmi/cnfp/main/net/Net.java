package by.bsu.fpmi.cnfp.main.net;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Flow;
import by.bsu.fpmi.cnfp.main.model.Node;
import by.bsu.fpmi.cnfp.main.model.NumerableObject;
import by.bsu.fpmi.cnfp.main.model.Tree;
import by.bsu.fpmi.cnfp.main.model.factory.ArcFactory;
import by.bsu.fpmi.cnfp.main.model.factory.NodeFactory;
import by.bsu.fpmi.cnfp.main.model.factory.NumerableObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Loban
 */
public class Net extends AbstractNet {
    protected double eps;
    private double subOptVal = 0; // TODO: think about theses values
    private double step = 0;

    public Net(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, double eps, int nodeCount, int arcCount) {
        super(nodes, arcs, nodeCount, arcCount);
        this.eps = eps;
    }

    /**
     * Ввести искусственные вершины, из которых выходят источники и нейтральные узлы и входят стоки (на каждом уровне)
     * Искусственные вершины соединяются дугами из уровня в уровень. Присвоить искусственным дугам стоимость M, где M -
     * большое число (штраф)
     *
     * @return net for first phase
     */
    public FirstPhaseNet createFirstPhaseNet() {
        Map<Integer, Node> newNodes = createStubs(nodes, NodeFactory.getInstance());
        Map<Integer, Arc> newArcs = createStubs(arcs, ArcFactory.getInstance());
        fillStubs(newNodes, nodes, arcs, NodeFactory.getInstance());
        fillStubs(newArcs, arcs, nodes, ArcFactory.getInstance());

        // TODO: add artificial nodes

        return new FirstPhaseNet(newNodes, newArcs, nodeCount, arcCount);
    }

    private <T extends NumerableObject, S extends NumerableObject> Map<Integer, T> createStubs(Map<Integer, T> sourcePool, NumerableObjectFactory<T, S> factory) {
        Map<Integer, T> stubs = new HashMap<>();
        for (T source : sourcePool.values()) {
            T stub = factory.createStub(source);
            stubs.put(stub.getNumber(), stub);
        }
        return stubs;
    }

    private <T extends NumerableObject, S extends NumerableObject> void fillStubs(Map<Integer, T> stubs, Map<Integer, T> sourcePool, Map<Integer, S> addingPool, NumerableObjectFactory<T, S> factory) {
        for (T stub : stubs.values()) {
            T source = sourcePool.get(stub.getNumber());
            factory.fillStub(stub, source, sourcePool, addingPool);
        }
    }

    public void setInitialFlow(Tree tree, Flow flow) {
        // TODO: implement initialization
    }

    @Override
    public boolean hasSolution() {
        return tree != null && flow != null;
    }

    public void prepare() {
        // Построить динамич опору Qr(op)
        // Посчитать псевдо-c(ij)
        // Посчитать потенциалы psi и ksi
    }

    public boolean isViolated() {
        return false;
    }

    public void recalcPlan() {
        // Посчитать оценки delta
        // Посчитать v и l
        // Посчитать шаг theta
        // Пересчет плана
    }

    public boolean isOptimized() {
        // Пересчет оценки субоптимальности beta
        return (1 - step) * subOptVal <= eps;
    }

    public void changeSupport() {
        // Замена опоры
        // Пересчет потенциалов
    }

    // TODO: think about this method
    public double calcEstimates(int index) {
        return arcs.get(index).getBeginNode().getPotential() -
                arcs.get(index).getEndNode().getPotential() - arcs.get(index).getCost();
    }
}
