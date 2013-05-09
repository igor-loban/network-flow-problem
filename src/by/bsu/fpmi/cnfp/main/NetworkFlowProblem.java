package by.bsu.fpmi.cnfp.main;

import by.bsu.fpmi.cnfp.io.InputData;
import by.bsu.fpmi.cnfp.io.OutputData;
import by.bsu.fpmi.cnfp.main.net.FirstPhaseNet;
import by.bsu.fpmi.cnfp.main.net.Net;

/**
 * @author Igor Loban
 */
public final class NetworkFlowProblem {
    private NetworkFlowProblem() {
    }

    public static void solve(InputData inputData, OutputData outputData) {
        Net net = inputData.parse();
        FirstPhaseNet firstPhaseNet = net.createFirstPhaseNet();

        doFirstPhase(firstPhaseNet);

        // TODO: check condition of solving

        doSecondPhase(net);

        outputData.write(net);
    }

    private static void doFirstPhase(FirstPhaseNet firstPhaseNet) {

    }

    private static void doSecondPhase(Net net) {
        net.prepare();
        if (net.isViolated()) {
            net.recalcPlan();
            while (!net.isOptimized()) {
                net.changeSupport();
                net.recalcPlan();
            }
        }
    }
}
