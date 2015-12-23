package icircles.abstractdual;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbstractDualGraph {

    private static final Logger log = LogManager.getLogger(AbstractDualGraph.class);

    List<AbstractDualNode> nodes;
    List<AbstractDualEdge> edges;

    public AbstractDualGraph(List<AbstractBasicRegion> abrs) {
        nodes = new ArrayList<AbstractDualNode>();
        edges = new ArrayList<AbstractDualEdge>();
        // Each abr becomes a node.
        // Neighbouring abrs get edges added between them.
        for (AbstractBasicRegion abr : abrs) {
            nodes.add(new AbstractDualNode(abr));
        }
        for (AbstractDualNode n : nodes) {
            boolean found_node_again = false;
            for (AbstractDualNode n2 : nodes) {
                if (!found_node_again) {
                    if (n2 == n) {
                        found_node_again = true;
                    }
                } else {
                    n.abr.getStraddledContour(n2.abr).ifPresent(curve -> add_edge(n, n2, curve));
                }
            }
        }
    }

    private void add_edge(AbstractDualNode n, AbstractDualNode n2,
            AbstractCurve straddlingCurve) {
        AbstractDualEdge e = new AbstractDualEdge(n, n2, straddlingCurve);
        n.incidentEdges.add(e);
        n2.incidentEdges.add(e);
        edges.add(e);
    }

    public void remove(AbstractDualEdge e) {
        e.from.removeEdge(e);
        e.to.removeEdge(e);
        edges.remove(e);
    }

    public void remove(AbstractDualNode n) {
        while (n.incidentEdges.size() != 0) {
            remove(n.incidentEdges.get(0));
        }
        nodes.remove(n);
    }

    public AbstractDualEdge getLowDegreeEdge() {
        // find a lowest-degree vertex, and from that,
        // choose the edge to its lowest-degree neighbour
        log.trace("Graph: " + this.debug());

        int lowestDegree = Integer.MAX_VALUE;
        AbstractDualNode lowestDegreeNode = null;
        for (AbstractDualNode n : nodes) {
            int thisDegree = n.degree();

            if (thisDegree == 0) {
                continue; // ignore isolated nodes when picking a low-degree edge
            }
            if (thisDegree < lowestDegree) {
                lowestDegreeNode = n;
                lowestDegree = thisDegree;
            }
        }
        if (lowestDegreeNode == null) {
            return null;
        }

        lowestDegree = Integer.MAX_VALUE;
        AbstractDualEdge result = null;
        for (AbstractDualEdge e : lowestDegreeNode.incidentEdges) {
            AbstractDualNode otherNode;
            if (e.from == lowestDegreeNode) {
                otherNode = e.to;
            } else {
                if (e.to != lowestDegreeNode)
                    throw new RuntimeException("Inconsistent graph nodes");

                otherNode = e.from;
            }
            int otherDegree = otherNode.degree();
            if (otherDegree < lowestDegree) {
                lowestDegree = otherDegree;
                result = e;
            }
        }
        return result;
    }

    public int getNumEdges() {
        return edges.size();
    }

    public Iterator<AbstractDualNode> getNodeIterator() {
        return nodes.iterator();
    }

    public String debug() {
        String result = "nodes : ";
        boolean isFirst = true;
        for (AbstractDualNode n : nodes) {
            if (!isFirst) {
                result += ",";
            } else {
                isFirst = false;
            }
            result += n.abr.toString();
        }
        result += " edges : ";
        isFirst = true;
        for (AbstractDualEdge e : edges) {
            if (!isFirst) {
                result += ",";
            } else {
                isFirst = false;
            }
            result += e.from.abr.toString();
            result += "->";
            result += e.to.abr.toString();
        }
        return result;
    }

    public ArrayList<AbstractDualNode> getFourTuple() {

        for (AbstractDualNode n : nodes) {
            for (AbstractDualEdge e : n.incidentEdges) {
                if (e.from != n) {
                    continue;
                }
                AbstractDualNode n2 = e.to;
                for (AbstractDualEdge e2 : n2.incidentEdges) {
                    if (e2.from != n2) {
                        continue;
                    }

                    // we have edges e and e2 - are these part of a square?
                    log.trace("Edges: " + e.from.abr + "->" + e.to.abr + " and " + e2.from.abr + "->" + e2.to.abr);

                    // look for an edge from n with the same label as e2
                    for (AbstractDualEdge e3 : n.incidentEdges) {
                        if (e3.label == e2.label) {
                            // found a square
                            ArrayList<AbstractDualNode> result = new ArrayList<AbstractDualNode>();
                            result.add(n);
                            result.add(n2);
                            result.add(e3.to);
                            result.add(e2.to);
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }
}
