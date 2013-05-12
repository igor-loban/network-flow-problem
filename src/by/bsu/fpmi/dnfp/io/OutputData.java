package by.bsu.fpmi.dnfp.io;

import by.bsu.fpmi.dnfp.main.net.Net;

/**
 * @author Igor Loban
 */
public interface OutputData {
    void write(Net net);

    void writeError(Exception e);
}
