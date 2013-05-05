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
        nodeCount = scanner.nextInt();
        arcCount = scanner.nextInt();
        builder.setNodeCount(nodeCount).setArcCount(arcCount).setEps(scanner.nextDouble());

        // Body
        int time = 0;
        while (scanner.hasNext()) {
            // Prepare to parse net #time
            int nodeBase = time * nodeCount;
            int arcBase = time > 0 ? (time - 1) * (arcCount + nodeCount) + arcCount : 0;
            int nodeNumber = nodeBase;
            int arcNumber = arcBase + 1;
            while (nodeNumber < nodeBase + nodeCount) {
                Node node = new Node(++nodeNumber);
                nodes.put(nodeNumber, node);
                builder.addNode(node);
            }
            if (time > 0) {
                while (arcNumber <= arcBase + nodeCount) {
                    Arc arc = new Arc(arcNumber);
                    arcs.put(arcNumber++, arc);
                    builder.addArc(arc);
                }
            }
            for (int i = 0; i < arcCount; i++) {
                Arc arc = new Arc(arcNumber);
                arcs.put(arcNumber++, arc);
                builder.addArc(arc);
            }

            // Parse net #time
            String mark = scanner.next();

            // Parse Node
            nodeNumber = nodeBase;
            while ("n".equals(mark)) {
                Node node = nodes.get(++nodeNumber);
                node.setProductivity(scanner.nextInt());
                node.setCapacity(scanner.nextInt());
                node.setCost(scanner.nextInt());
                mark = scanner.next();
            }

            // Build intermediate arcs
            arcNumber = arcBase + 1;
            if (time > 0) {
                for (int i = 1; i <= nodeCount; i++) {
                    Arc arc = arcs.get(arcNumber++);
                    Node beginNode = nodes.get(nodeBase - nodeCount + i);
                    arc.setBeginNode(beginNode);
                    arc.setEndNode(nodes.get(nodeBase + i));
                    arc.setCapacity(beginNode.getCapacity());
                    arc.setCost(beginNode.getCost());
                }
            }

            // Parse Arc
            while ("a".equals(mark)) {
                Arc arc = arcs.get(arcNumber++);
                arc.setBeginNode(nodes.get(nodeBase + scanner.nextInt()));
                arc.setEndNode(nodes.get(nodeBase + scanner.nextInt()));
                arc.setCapacity(scanner.nextDouble());
                arc.setCost(scanner.nextDouble());
                if (scanner.hasNext()) {
                    mark = scanner.next();
                } else {
                    break;
                }
            }

            // Prepare to parse next net
            time++;
        }

        scanner.close();
        return builder.build();
    }
}
