package by.bsu.fpmi.cnfp.main;

import by.bsu.fpmi.cnfp.io.InputData;
import by.bsu.fpmi.cnfp.io.OutputData;
import by.bsu.fpmi.cnfp.main.net.Net;

/**
 * @author Igor Loban
 */
public class CNFP {
    public static void solve(InputData inputData, OutputData outputData) {
        Net net = inputData.parse();
        net.prepare();
        while (net.calcSuboptimality() > net.getEps()) {
            net.nextIteration();
        }
        outputData.write(net);
    }
}
