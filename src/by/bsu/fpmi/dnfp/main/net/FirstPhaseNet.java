package by.bsu.fpmi.dnfp.main.net;

import by.bsu.fpmi.dnfp.exception.IterationLimitException;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.util.AlgoUtils;

import java.util.Map;

/**
 * @author Igor Loban
 */
public final class FirstPhaseNet extends AbstractNet {
    protected static final int ITERATION_LIMIT = 10;

    private double beta;
    private Arc minArc;

    public FirstPhaseNet(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount, int arcCount, int periodCount,
                         double eps) {
        super(nodes, arcs, nodeCount, arcCount, periodCount, eps);
    }

    @Override
    public boolean hasSolution() {
        for (Arc arc : tree.getArcs()) { // Check containing of artificial arcs in tree
            if (arc.getNumber() < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void prepare() {
        tree = AlgoUtils.createInitialTree(nodes); // Построить дерево
        AlgoUtils.createInitialFlow(arcs.values()); // Построить начальный поток
        AlgoUtils.createDynamicSupport(tree, periodCount); // Построить динамич опору Qr(op)
        AlgoUtils.calcPseudoCost(tree); // Посчитать псевдо-c(ij)
        AlgoUtils.calcPotentials(tree, nodeCount, periodCount); // Посчитать потенциалы psi
        AlgoUtils.calcLeaps(arcs.values()); // Посчитать потенциалы ksi
        AlgoUtils.calcEstimates(arcs.values()); // Посчитать оценки delta
    }

    public boolean isViolated() {
        double epsU = AlgoUtils.calcEpsU(arcs.values());
        double epsX = AlgoUtils.calcEpsX(arcs.values());
        beta = epsU + epsX;
        System.out.println("Beta = " + beta);
        return beta < eps;
    }

    public void recalcPlan() {
        AlgoUtils.calcEstimates(arcs.values()); // Посчитать оценки delta
        AlgoUtils.calcDirections(this); // Посчитать направления v и l
        minArc = AlgoUtils.calcSteps(tree.getArcs()); // Посчитать шаг theta
        AlgoUtils.recalcPlan(arcs.values(), minArc.getStep()); // Пересчитать поток

    }

    public boolean isOptimized() {
        beta *= (1 - minArc.getStep()); // Пересчет оценки субоптимальности beta
        if (beta < eps) {
            return true;
        }
        checkIterationLimit();
        return false;
    }

    public void changeSupport() {
        AlgoUtils.changeSupport(this, minArc); // Замена опоры
        AlgoUtils.calcPotentials(tree, nodeCount, periodCount); // Пересчет потенциалов psi
    }

    @Override
    protected void checkIterationLimit() {
        if (++iterationCount >= ITERATION_LIMIT) {
            throw new IterationLimitException(
                    "iteration limit exception happened in the first phase (limit equals " + ITERATION_LIMIT + ").");
        }
    }
}
