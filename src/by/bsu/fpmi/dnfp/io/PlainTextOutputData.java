package by.bsu.fpmi.dnfp.io;

import by.bsu.fpmi.dnfp.main.net.Net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

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
    public void write(Net net) {
        PrintWriter writer = new PrintWriter(outputStream);

        if (net.hasSolution()) {
            // TODO: print the solution
            writer.println("Solution: ...");
        } else {
            writer.println("Problem has no solution.");
            writer.println("Reason: constraints maybe are antithetical.");
        }

        writer.close();
    }

    @Override
    public void writeError(Exception e) {
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("Problem has no solution.");
        writer.println("Reason: " + e.getMessage());
        writer.close();
    }
}
