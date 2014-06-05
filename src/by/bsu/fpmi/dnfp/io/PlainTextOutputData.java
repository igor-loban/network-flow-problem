package by.bsu.fpmi.dnfp.io;

import by.bsu.fpmi.dnfp.main.model.Arc;
import by.bsu.fpmi.dnfp.main.model.Node;
import by.bsu.fpmi.dnfp.main.net.AbstractNet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author Igor Loban
 */
public class PlainTextOutputData implements OutputData {
    private OutputStream outputStream;

    public PlainTextOutputData(String outputFileName) throws FileNotFoundException {
        this(new File(outputFileName));
    }

    public PlainTextOutputData(File outputFile) throws FileNotFoundException {
        this(new FileOutputStream(outputFile));
    }

    public PlainTextOutputData(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(AbstractNet net) {
        PrintWriter writer = new PrintWriter(outputStream);

        //        if (net.hasSolution()) {
        int nodeCountPerPeriod = net.getNodeCount() / (net.getPeriodCount() + 1);
        for (Map.Entry<Arc, Double> entry : net.getFlow().asMap().entrySet()) {
            Arc arc = entry.getKey();
            writer.println(
                    "(" + getNumber(arc.getBeginNode(), nodeCountPerPeriod) + " -> " + getNumber(arc.getEndNode(),
                            nodeCountPerPeriod) + "): " + entry.getValue());
        }
        //        } else {
        //            writer.println("Problem has no solution.");
        //            writer.println("Reason: constraints maybe are antithetical.");
        //        }

        writer.close();
    }

    private String getNumber(Node node, int nodeCountPerPeriod) {
        return (node.getNumber() - nodeCountPerPeriod * node.getPeriod()) + "[" + node.getPeriod() + "]";
    }

    @Override
    public void writeError(Exception e) {
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("Problem has no solution.");
        writer.println("Reason: " + e.getMessage());
        writer.close();
    }
}
