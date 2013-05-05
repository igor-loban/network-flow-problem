package by.bsu.fpmi.cnfp.test;

import by.bsu.fpmi.cnfp.io.InputData;
import by.bsu.fpmi.cnfp.io.PlainTextInputData;
import by.bsu.fpmi.cnfp.main.NetworkFlowProblem;
import by.bsu.fpmi.cnfp.io.OutputData;
import by.bsu.fpmi.cnfp.io.PlainTextOutputData;

import java.io.FileNotFoundException;

/**
 * @author Igor Loban
 */
public class TestLauncher {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Test has started.");

        InputData inputData = new PlainTextInputData("input.txt");
        OutputData outputData = new PlainTextOutputData("output.txt");
        NetworkFlowProblem.solve(inputData, outputData);

        System.out.println("Test has finished.");
    }
}
