package by.bsu.fpmi.dnfp.test;

import by.bsu.fpmi.dnfp.io.InputData;
import by.bsu.fpmi.dnfp.io.PlainTextInputData;
import by.bsu.fpmi.dnfp.main.NetworkFlowProblem;
import by.bsu.fpmi.dnfp.io.OutputData;
import by.bsu.fpmi.dnfp.io.PlainTextOutputData;

import java.io.FileNotFoundException;

/**
 * @author Igor Loban
 */
public class TestLauncher {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Test has started.");

        InputData inputData = new PlainTextInputData("input2.txt");
        OutputData outputData = new PlainTextOutputData("output.txt");
        NetworkFlowProblem.solve(inputData, outputData);

        System.out.println("Test has finished.");
    }
}
