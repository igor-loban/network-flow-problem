package by.bsu.fpmi.dnfp.main.net;

import by.bsu.fpmi.dnfp.main.util.AlgoUtils;
import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;

import java.util.Map;

/**
 * @author Igor Loban
 */
public class FirstPhaseNet extends AbstractNet {
    public FirstPhaseNet(Map<Integer, Node> nodes, Map<Integer, Arc> arcs, int nodeCount, int arcCount,
                         int periodCount) {
        super(nodes, arcs, nodeCount, arcCount, periodCount);
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
        AlgoUtils.createInitialFlow(arcs);
        // Построить динамич опору Qr(op)
        AlgoUtils.createDynamicSupport(tree, periodCount);
        // Посчитать псевдо-c(ij)
        AlgoUtils.calcPseudoCost(tree);
        // Посчитать потенциалы psi и ksi
        AlgoUtils.calcPotentials(tree, nodeCount, periodCount);
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
        return false;
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
