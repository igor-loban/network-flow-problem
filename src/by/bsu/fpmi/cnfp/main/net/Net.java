package by.bsu.fpmi.cnfp.main.net;

import java.util.List;

/**
 * @author Igor Loban
 */
public class Net {
    private List<Node> nodes;
    private List<Arc> arcs;
    private Tree tree;
    private Flow flow;
    private double eps;
    private double subOptVal = 0;
    private double step = 0;
    private int nodeCount;
    private int arcCount;


    public Net(List<Node> nodes, List<Arc> arcs, double eps, int nodeCount, int arcCount) {
        this.nodes = nodes;
        this.arcs = arcs;
        this.eps = eps;
        this.nodeCount = nodeCount;
        this.arcCount = arcCount;
    }

    public void prepare() {
        // Построить дерево и начальный поток
            // Ввести искусственные вершины, из которых выходят источники и нейтральные узлы и входят стоки (на каждом уровне)
            // Искусственные вершины соединяются дугами из уровня в уровень
            // Присвоить искусственным дугам стоимость M, где M - большое число (штраф)
            // Дерево будет состоять из искуственных дуг
            // Поток равен абсолютным значениям интенсивностей узлов
            // Решать обычным методом.
            // Все искусственные дуги должны выйти из базиса
            // Иначе задача не разрешима

        // Построить динамич опору Qr(op)
        // Посчитать псевдо-c(ij)
        // Посчитать потенциалы psi и ksi
    }

    public void recalcPlan() {
        // Посчитать оценки delta
        // Посчитать v и l
        // Посчитать шаг theta
        // Пересчет плана
    }

    public void changeSupport() {
        // Замена опоры
        // Пересчет потенциалов
    }

    public boolean isViolated() {
        return false;
    }

    public boolean isOptimized() {
        // Пересчет оценки субоптимальности beta
        if ((1 - step)*subOptVal <= eps)
            return true;
        else

        return false;
    }


    public double calcEstimates(int index) {
        return arcs.get(index).getBeginNode().getPotential() -
                arcs.get(index).getEndNode().getPotential() - arcs.get(index).getCost();
    }

    private class Tree {

    }

    private class Flow {

    }
}
