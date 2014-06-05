package by.bsu.fpmi.dnfp.io;

import by.bsu.fpmi.dnfp.main.net.AbstractNet;

/**
 * @author Igor Loban
 */
public interface OutputData {
    void write(AbstractNet net);

    void writeError(Exception e);
}
