package by.bsu.fpmi.cnfp.main.net;

import java.util.List;

/**
 * @author Igor Loban
 */
public class Net extends AbstractNet {
    protected double eps;
    private double subOptVal = 0; // TODO: think about theses values
    private double step = 0;

    public Net(List<Node> nodes, List<Arc> arcs, double eps, int nodeCount, int arcCount) {
        super(nodes, arcs, nodeCount, arcCount);
        this.eps = eps;
    }

    public FirstPhaseNet createFirstPhaseNet() {
        // TODO: create net for first phase
        //      Ввести искусственные вершины, из которых выходят источники и нейтральные узлы и входят стоки (на каждом
        //      уровне)
        //      Искусственные вершины соединяются дугами из уровня в уровень
        //      Присвоить искусственным дугам стоимость M, где M - большое число (штраф)
        return null;
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
