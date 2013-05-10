package by.bsu.fpmi.cnfp.io;

import by.bsu.fpmi.cnfp.main.model.Arc;
import by.bsu.fpmi.cnfp.main.net.Net;
import by.bsu.fpmi.cnfp.main.net.NetBuilder;
import by.bsu.fpmi.cnfp.main.model.Node;

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
        Map<Integer, Node> nodes = new HashMap<>();
        Map<Integer, Arc> arcs = new HashMap<>();

        // Header
        int nodeCount = scanner.nextInt();
        int arcCount = scanner.nextInt();
        builder.setNodeCount(nodeCount).setArcCount(arcCount).setEps(scanner.nextDouble());

        // Body
        int periodCount = 0;
        while (scanner.hasNext()) {
            int nodeBase = periodCount * nodeCount;
            int arcBase = periodCount > 0 ? (periodCount - 1) * (arcCount + nodeCount) + arcCount : 0;
            createNodes(nodes, builder, nodeBase, nodeCount);
            createArcs(arcs, builder, arcBase, periodCount > 0 ? arcCount + nodeCount : arcCount);

            for (int nodeNumber = nodeBase + 1; nodeNumber <= nodeBase + nodeCount; nodeNumber++) {
                scanner.next();
                parseNode(nodes.get(nodeNumber), scanner);
            }

            if (periodCount > 0) {
                int arcNumber = arcBase;
                int nodeNumber = nodeBase;
                for (int i = 0; i < nodeCount; i++) {
                    initIntermediateArc(arcs.get(++arcNumber), nodes.get(++nodeNumber),
                            nodes.get(nodeNumber - nodeCount - 1));
                }
            }

            int arcNumber = periodCount > 0 ? arcBase + nodeCount : arcBase;
            for (int i = 0; i < arcCount; i++) {
                scanner.next();
                parseArc(arcs.get(++arcNumber), nodes, nodeBase, scanner);
            }

            // TODO: add incoming/exit arcs if needed

            periodCount++;
            if (scanner.hasNext()) {
                scanner.next();
            }
        }

        scanner.close();
        return builder.setPeriodCount(periodCount).build();
    }

    private void createNodes(Map<Integer, Node> nodes, NetBuilder builder, int nodeBase, int nodeCount) {
        int nodeNumber = nodeBase;
        while (nodeNumber < nodeBase + nodeCount) {
            Node node = new Node(++nodeNumber);
            nodes.put(nodeNumber, node);
            builder.addNode(node);
        }
    }

    private void createArcs(Map<Integer, Arc> arcs, NetBuilder builder, int arcBase, int arcCount) {
        int arcNumber = arcBase;
        while (arcNumber < arcBase + arcCount) {
            Arc arc = new Arc(++arcNumber);
            arcs.put(arcNumber, arc);
            builder.addArc(arc);
        }
    }

    private void parseNode(Node node, Scanner scanner) {
        node.setIntensity(scanner.nextInt());
        node.setCapacity(scanner.nextInt());
        node.setCost(scanner.nextInt());
    }

    private void parseArc(Arc arc, Map<Integer, Node> nodes, int nodeBase, Scanner scanner) {
        arc.setBeginNode(nodes.get(nodeBase + scanner.nextInt()));
        arc.setEndNode(nodes.get(nodeBase + scanner.nextInt()));
        arc.setCapacity(scanner.nextDouble());
        arc.setCost(scanner.nextDouble());
    }

    private void initIntermediateArc(Arc arc, Node beginNode, Node endNode) {
        arc.setBeginNode(beginNode);
        arc.setEndNode(endNode);
        arc.setCapacity(beginNode.getCapacity());
        arc.setCost(beginNode.getCost());
    }
}
