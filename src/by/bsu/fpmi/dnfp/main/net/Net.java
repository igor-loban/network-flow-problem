package by.bsu.fpmi.dnfp.main.net;

import by.bsu.fpmi.dnfp.exception.IterationLimitException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Flow;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.model.Tree;
import by.bsu.fpmi.dnfp.main.model.factory.ArcFactory;
import by.bsu.fpmi.dnfp.main.model.factory.NodeFactory;
import by.bsu.fpmi.dnfp.main.util.AlgoUtils;

import java.util.Map;

/**
 * @author Igor Loban
 */
public class Net extends AbstractNet {
    protected static final int ITERATION_LIMIT = 10;

    public Net(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, double eps, int nodeCount, int arcCount,
               int periodCount) {
        super(nodes, arcs, nodeCount, arcCount, periodCount, eps);
    }

    public FirstPhaseNet createFirstPhaseNet() {
        Map<Integer, Node> newNodes = AlgoUtils.createStubs(nodes, NodeFactory.getInstance());
        Map<Integer, Arc> newArcs = AlgoUtils.createStubs(arcs, ArcFactory.getInstance());
        AlgoUtils.fillStubs(newNodes, nodes, newArcs, NodeFactory.getInstance());
        AlgoUtils.fillStubs(newArcs, arcs, newNodes, ArcFactory.getInstance());
        AlgoUtils.addArtificialNodes(newNodes, newArcs, nodeCount, periodCount);
        return new FirstPhaseNet(newNodes, newArcs, newNodes.size(), newArcs.size(), periodCount, eps);
    }

    public void setInitialParams(Tree tree, Flow flow) {
        // TODO: implement initialization
    }

    @Override
    public boolean hasSolution() {
        return false;
    }

    public void prepare() {
        // Построить динамич опору Qr(op)
        // Посчитать псевдо-c(ij)
        // Посчитать потенциалы psi и ksi
    }

    public boolean isViolated() {
        // Проверка критерия
        return false;
    }

    public void recalcPlan() {
        // Посчитать оценки delta
        // Посчитать v и l
        // Посчитать шаг theta
        // Пересчет оценки субоптимальности beta
        // Пересчет плана
    }

    public boolean isOptimized() {
        // Проверка критерия
        return false; //(1 - step) * subOptVal <= eps;
    }

    public void changeSupport() {
        // Замена опоры
        // Пересчет потенциалов
    }

    @Override
    protected void checkIterationLimit() {
        if (++iterationCount >= ITERATION_LIMIT) {
            throw new IterationLimitException(
                    "iteration limit exception happened in the second phase (limit equals " + ITERATION_LIMIT + ").");
        }
    }
}
