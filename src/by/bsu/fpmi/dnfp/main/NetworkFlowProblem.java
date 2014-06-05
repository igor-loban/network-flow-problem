package by.bsu.fpmi.dnfp.main;

import by.bsu.fpmi.dnfp.io.InputData;
import by.bsu.fpmi.dnfp.io.OutputData;
import by.bsu.fpmi.dnfp.main.net.AbstractNet;
import by.bsu.fpmi.dnfp.main.net.FirstPhaseNet;
import by.bsu.fpmi.dnfp.main.net.Net;

/**
 * @author Igor Loban
 */
public final class NetworkFlowProblem {
    private NetworkFlowProblem() {
    }

    public static void solve(InputData inputData, OutputData outputData) {
        long startTime = System.nanoTime();
        long duration;
        try {
            Net net = inputData.parse();
            FirstPhaseNet firstPhaseNet = net.createFirstPhaseNet();
            doFirstPhase(firstPhaseNet);
            System.out.println("Solution (flow): " + firstPhaseNet.getFlow());
            outputData.write(firstPhaseNet);
        } catch (Exception e) {
            e.printStackTrace();
            outputData.writeError(e);
        } finally {
            duration = System.nanoTime() - startTime;
        }
        System.out.println("Duration: " + (double) duration / 1000000 + " ms");
    }

    private static void doFirstPhase(FirstPhaseNet firstPhaseNet) {
        solveProblem(firstPhaseNet);
    }

    private static void solveProblem(AbstractNet net) {
        int iteration = 1;
        net.prepare();
        System.out.println("Net prepared.");
        if (!net.isViolated()) {
            System.out.println("Net isn't violated.");
            net.recalcPlan();
            System.out.println("Iteration " + iteration++ + ".");
            System.out.println("Plan recalculated.");
            while (!net.isOptimized()) {
                System.out.println("Plan isn't optimize.");
                net.changeSupport();
                System.out.println("Support changed.");
                net.recalcPlan();
                System.out.println("Iteration " + iteration++ + ".");
                System.out.println("Plan recalculated.");
            }
        }
        System.out.println("Problem solved.");
    }
}
