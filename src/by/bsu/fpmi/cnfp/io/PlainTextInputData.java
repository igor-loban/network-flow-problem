package by.bsu.fpmi.cnfp.io;

import by.bsu.fpmi.cnfp.main.net.Arc;
import by.bsu.fpmi.cnfp.main.net.Net;
import by.bsu.fpmi.cnfp.main.net.NetBuilder;
import by.bsu.fpmi.cnfp.main.net.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Igor Loban
 */
public class PlainTextInputData implements InputData {
    private InputStream inputStream;

    public PlainTextInputData(String inputFileName) throws FileNotFoundException {
        this(new File(inputFileName));
    }

    public PlainTextInputData(File inputFile) throws FileNotFoundException {
        this(new FileInputStream(inputFile));
    }

    public PlainTextInputData(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Net parse() {
        NetBuilder builder = new NetBuilder();
        Scanner scanner = new Scanner(inputStream);

        int nodeCount;
        int arcCount;
        Map<Integer, Node> nodes = new HashMap<>();
        Map<Integer, Arc> arcs = new HashMap<>();

        // Header
        builder.setEps(scanner.nextDouble());
        scanner.next();
        scanner.next();
        nodeCount = scanner.nextInt();
        arcCount = scanner.nextInt();
        builder.setNodeCount(nodeCount).setArcCount(arcCount);

        // Body
        int time = 0;
        while (scanner.hasNext()) {
            // Prepare to parse net #time
            int nodeBase = time * nodeCount;
            int arcBase = time * arcCount;
            for (int i = 1; i <= nodeCount; i++) {
                int number = nodeBase + i;
                Node node = new Node(number);
                nodes.put(number, node);
                builder.addNode(node);
            }
            for (int i = 1; i <= arcCount; i++) {
                int number = arcBase + i;
                Arc arc = new Arc(number);
                arcs.put(number, arc);
                builder.addArc(arc);
            }

            // Parse net #time
            String mark = scanner.next();

            // Parse Node
            while ("n".equals(mark)) {
                int number = nodeBase + scanner.nextInt();
                Node node = nodes.get(number);
                node.setProductivity(scanner.nextInt());
                mark = scanner.next();
            }

            // Parse Arc
            int arcNumber = arcBase + 1;
            while ("a".equals(mark)) {
                Arc arc = arcs.get(arcNumber);
                arc.setBeginNode(nodes.get(nodeBase + scanner.nextInt()));
                arc.setEndNode(nodes.get(nodeBase + scanner.nextInt()));
                scanner.next();
                arc.setCapacity(scanner.nextDouble());
                arc.setCost(scanner.nextDouble());
                arcNumber++;
                if (scanner.hasNext()) {
                    mark = scanner.next();
                } else {
                    break;
                }
            }

            //

            // Prepare to parse next net
            time++;
        }

        scanner.close();
        return builder.build();
    }
}
