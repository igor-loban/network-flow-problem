package by.bsu.fpmi.cnfp.main.net;

import by.bsu.fpmi.cnfp.main.AlgoUtils;
import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.model.Flow;
import by.bsu.fpmi.cnfp.main.model.Node;
import by.bsu.fpmi.cnfp.main.model.Tree;
import by.bsu.fpmi.cnfp.main.model.factory.ArcFactory;
import by.bsu.fpmi.cnfp.main.model.factory.NodeFactory;

import java.util.Map;

/**
 * @author Igor Loban
 */
public class Net extends AbstractNet {
    protected double eps;
    private double subOptVal = 0; // TODO: think about theses values
    private double step = 0;

    public Net(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, double eps, int nodeCount, int arcCount,
               int periodCount) {
        super(nodes, arcs, nodeCount, arcCount, periodCount);
        this.eps = eps;
    }

    public FirstPhaseNet createFirstPhaseNet() {
        Map<Integer, Node> newNodes = AlgoUtils.createStubs(nodes, NodeFactory.getInstance());
        Map<Integer, Arc> newArcs = AlgoUtils.createStubs(arcs, ArcFactory.getInstance());
        AlgoUtils.fillStubs(newNodes, nodes, arcs, NodeFactory.getInstance());
        AlgoUtils.fillStubs(newArcs, arcs, nodes, ArcFactory.getInstance());
        AlgoUtils.addArtificialNodes(newNodes, newArcs, periodCount);
        return new FirstPhaseNet(newNodes, newArcs, nodeCount, arcCount, periodCount);
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
