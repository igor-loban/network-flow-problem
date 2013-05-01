package by.bsu.fpmi.cnfp.io;

import by.bsu.fpmi.cnfp.io.OutputData;
import by.bsu.fpmi.cnfp.main.net.Net;

import java.io.*;

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
        writer.close();
    }
}
