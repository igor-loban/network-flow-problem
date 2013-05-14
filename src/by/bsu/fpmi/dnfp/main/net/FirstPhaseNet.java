    package by.bsu.fpmi.dnfp.main.net;

import by.bsu.fpmi.dnfp.exception.IterationLimitException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.util.AlgoUtils;

import java.util.Map;

/**
 * @author Igor Loban
 */
public class FirstPhaseNet extends AbstractNet {
    protected static final int ITERATION_LIMIT = 10;

    public FirstPhaseNet(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount, int arcCount,
                         int periodCount, double eps) {
        super(nodes, arcs, nodeCount, arcCount, periodCount, eps);
    }

    @Override
    public boolean hasSolution() {
        // TODO: check containing of artificial arcs in tree
        return false;
    }

    // TODO: think about overriding below methods
    public void prepare() {
        // Построить дерево и начальный поток
        tree = AlgoUtils.createInitialTree(nodes);
        AlgoUtils.createInitialFlow(arcs.values());
        // Построить динамич опору Qr(op)
        AlgoUtils.createDynamicSupport(tree, periodCount);
        // Посчитать псевдо-c(ij)
        AlgoUtils.calcPseudoCost(tree);
        // Посчитать потенциалы psi и ksi
        AlgoUtils.calcPotentials(tree, nodeCount, periodCount);
        AlgoUtils.calcLeaps(arcs.values());
        // Посчитать оценки delta
        AlgoUtils.calcEstimates(arcs.values());
    }

    public boolean isViolated() {
        double epsU = AlgoUtils.calcEpsU(arcs.values());
        double epsX = AlgoUtils.calcEpsX(arcs.values());
        return epsU + epsX > eps;
    }

    public void recalcPlan() {
        // Посчитать оценки delta
        AlgoUtils.calcEstimates(arcs.values());
        // Посчитать v и l


        // Посчитать шаг theta
        // Пересчет плана
    }

    public boolean isOptimized() {
        // Пересчет оценки субоптимальности beta

        checkIterationLimit();
        return false;
    }

    public void changeSupport() {
        // Замена опоры
        // Пересчет потенциалов
    }

    @Override
    protected void checkIterationLimit() {
        if (++iterationCount >= ITERATION_LIMIT) {
            throw new IterationLimitException("iteration limit exception happened in the first phase (limit equals "
                    + ITERATION_LIMIT + ").");
        }
    }
}
