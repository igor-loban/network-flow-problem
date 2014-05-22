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

    public FirstPhaseNet(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount, int arcCount, int periodCount,
                         double eps) {
        super(nodes, arcs, nodeCount, arcCount, periodCount, eps);
    }

    @Override
    public boolean hasSolution() {
        // TODO: check containing of artificial arcs in tree
        return false;
    }

    // TODO: think about overriding below methods
    public void prepare() {
        tree = AlgoUtils.createInitialTree(nodes); // Построить дерево
        AlgoUtils.createInitialFlow(arcs.values()); // Построить начальный поток
        AlgoUtils.createDynamicSupport(tree, periodCount); // Построить динамич опору Qr(op)
        AlgoUtils.calcPseudoCost(tree); // Посчитать псевдо-c(ij)
        AlgoUtils.calcPotentials(tree, nodeCount, periodCount); // Посчитать потенциалы psi

        for (Node node : nodes.values()) {
            System.out.println("Period: " + node.getPeriod() + " number: " + node.getNumber() + " potential: " + node
                    .getPotential());
        }

        AlgoUtils.calcLeaps(arcs.values()); // Посчитать потенциалы ksi
        AlgoUtils.calcEstimates(arcs.values()); // Посчитать оценки delta

        for (Arc arc : arcs.values()) {
            System.out.println(
                    arc.getBeginNode().getNumber() + " -> " + arc.getEndNode().getNumber() + " period: " + arc
                            .getPeriod() + " number: " + arc.getNumber() + " leap: " + arc.getLeap() + " estimate: "
                            + arc.getEstimate()
            );
        }
    }

    public boolean isViolated() {
        double epsU = AlgoUtils.calcEpsU(arcs.values());
        double epsX = AlgoUtils.calcEpsX(arcs.values());
        return epsU + epsX > eps;
    }

    public void recalcPlan() {
        AlgoUtils.calcEstimates(arcs.values()); // Посчитать оценки delta
        AlgoUtils.calcDirections(this); // Посчитать направления v и l
        Arc minArc = AlgoUtils.calcSteps(tree.getArcs()); // Посчитать шаг theta
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
            throw new IterationLimitException(
                    "iteration limit exception happened in the first phase (limit equals " + ITERATION_LIMIT + ").");
        }
    }
}
