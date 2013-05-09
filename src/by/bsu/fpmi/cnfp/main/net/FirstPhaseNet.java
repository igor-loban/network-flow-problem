package by.bsu.fpmi.cnfp.main.net;

import java.util.List;

/**
 * @author Igor Loban
 */
public class FirstPhaseNet extends AbstractNet {
    public FirstPhaseNet(List<Node> nodes, List<Arc> arcs, int nodeCount, int arcCount) {
        super(nodes, arcs, nodeCount, arcCount);
    }

    @Override
    public boolean hasSolution() {
        // TODO: check containing of artificial arcs in tree
        return false;
    }

    // TODO: think about overriding below methods
    public void prepare() {
        // Построить дерево и начальный поток
        //      Дерево будет состоять из искуственных дуг
        //      Поток равен абсолютным значениям интенсивностей узлов
        //      Решать обычным методом.
        //      Все искусственные дуги должны выйти из базиса
        //      Иначе задача не разрешима

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
