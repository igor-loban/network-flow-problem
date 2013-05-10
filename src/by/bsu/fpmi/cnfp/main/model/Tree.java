package by.bsu.fpmi.cnfp.main.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Igor Loban
 */
public class Tree {
    private Node root;
    private Set<Arc> arcs = new HashSet<>();

    public Tree() {
    }

    public Tree(Node root) {
        this.root = root;
    }

    public Set<Arc> populate(Set<Arc> newArcs) {
        Set<Arc> populatedArcs = new HashSet<>();
        for (Arc arc : newArcs) {
            if (!arcs.contains(arc)) {
                arcs.add(arc);
                populatedArcs.add(arc);
            }
        }
        return populatedArcs;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Set<Arc> getArcs() {
        return arcs;
    }
}
