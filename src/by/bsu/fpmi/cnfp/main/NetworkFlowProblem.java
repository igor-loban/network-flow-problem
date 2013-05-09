package by.bsu.fpmi.cnfp.main;

import by.bsu.fpmi.cnfp.io.InputData;
import by.bsu.fpmi.cnfp.io.OutputData;
import by.bsu.fpmi.cnfp.main.net.AbstractNet;
import by.bsu.fpmi.cnfp.main.net.FirstPhaseNet;
import by.bsu.fpmi.cnfp.main.net.Flow;
import by.bsu.fpmi.cnfp.main.net.Net;
import by.bsu.fpmi.cnfp.main.net.Tree;

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

        if (firstPhaseNet.hasSolution()) {
            doSecondPhase(net, firstPhaseNet.getTree(), firstPhaseNet.getFlow());
        }

        outputData.write(net);
    }

    private static void doFirstPhase(FirstPhaseNet firstPhaseNet) {
        solve(firstPhaseNet);
    }

    private static void doSecondPhase(Net net, Tree tree, Flow flow) {
        net.setInitialFlow(tree, flow);
        solve(net);
    }

    private static void solve(AbstractNet net) {
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
