package icircles.abstractdual;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the (there can only be one at abstract level) abstract dual graph of an Euler diagram.
 */
public class AbstractDualGraph {

    private static final Logger log = LogManager.getLogger(AbstractDualGraph.class);

    // we do not specify the edge factory because we instantiate edges ourselves
    private final UndirectedGraph<AbstractDualNode, AbstractDualEdge> graph = new SimpleGraph<>(AbstractDualEdge.class);

    /**
     * Constructs a dual graph from given zones.
     *
     * @param zones the zones
     */
    public AbstractDualGraph(List<AbstractBasicRegion> zones) {
        // each zone becomes a node. We do not add directly to graph
        // because lists are easier to obtain powersets from for edges
        List<AbstractDualNode> nodes = zones.stream()
                .map(AbstractDualNode::new)
                .collect(Collectors.toList());

        // add nodes to the underlying graph
        nodes.forEach(graph::addVertex);

        // go through all pairs and see if they are neighbors.
        // Neighboring zones get edges added between them.
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                AbstractDualNode n = nodes.get(i);
                AbstractDualNode n2 = nodes.get(j);

                n.getZone().getStraddledContour(n2.getZone()).ifPresent(curve -> {
                    AbstractDualEdge e = new AbstractDualEdge(n, n2, curve);

                    graph.addEdge(e.from, e.to, e);
                });
            }
        }
    }

    /**
     * @return number of edges in this graph
     */
    public int getNumEdges() {
        return graph.edgeSet().size();
    }

    /**
     * @return set of nodes of this graph
     */
    public Set<AbstractDualNode> getNodes() {
        return graph.vertexSet();
    }

    /**
     * Removes edge from the graph.
     *
     * @param edge to remove
     */
    public void removeEdge(AbstractDualEdge edge) {
        graph.removeEdge(edge);
    }

    /**
     * Removes node from the graph.
     * Edges incident with the node are also removed.
     *
     * @param node to remove
     */
    public void removeNode(AbstractDualNode node) {
        graph.removeVertex(node);
    }

    /**
     * Returns edge from lowest degree node to its lowest degree neighbor.
     * Note this ignores isolated vertices since they do not have incident edges.
     *
     * @return edge
     */
    public AbstractDualEdge getLowDegreeEdge() {
        log.trace("Graph: " + graph);

        // find a lowest-degree vertex, and from that ...
        Optional<AbstractDualNode> lowestDegreeNode = graph.vertexSet()
                .stream()
                .filter(node -> graph.degreeOf(node) != 0)  // ignore isolated nodes when picking a low-degree edge
                .reduce((node1, node2) -> graph.degreeOf(node2) < graph.degreeOf(node1) ? node2 : node1);

        if (!lowestDegreeNode.isPresent())
            return null;

        // choose the edge to its lowest-degree neighbour
        AbstractDualNode node = lowestDegreeNode.get();

        Optional<AbstractDualEdge> lowestDegreeEdge = graph.edgesOf(node)
                .stream()
                .map(edge -> edge.from == node ? edge.to : edge.from)
                .reduce((node1, node2) -> graph.degreeOf(node2) < graph.degreeOf(node1) ? node2 : node1)
                .map(n -> graph.getEdge(n, node));

        return lowestDegreeEdge.get();
    }

    /**
     * @return 4 nodes that form a square with their edges
     */
    public List<AbstractDualNode> getFourTuple() {
        for (AbstractDualNode n : graph.vertexSet()) {
            for (AbstractDualEdge e : graph.edgesOf(n)) {
                if (e.from != n) {
                    continue;
                }

                AbstractDualNode n2 = e.to;
                for (AbstractDualEdge e2 : graph.edgesOf(n2)) {
                    if (e2.from != n2) {
                        continue;
                    }

                    // we have edges e and e2 - are these part of a square?
                    log.trace("Edges: " + e.from.getZone() + "->" + e.to.getZone() + " and " + e2.from.getZone() + "->" + e2.to.getZone());

                    // look for an edge from n with the same label (curve) as e2
                    for (AbstractDualEdge e3 : graph.edgesOf(n)) {
                        if (e3.curve == e2.curve) {
                            // found a square
                            ArrayList<AbstractDualNode> result = new ArrayList<>();
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

    // EXPERIMENTAL BEGIN
    public Optional<AbstractBasicRegion> getMissingZone(Set<AbstractBasicRegion> zones) {
//        AbstractDualNode node1 = nodes.get(0);
//        AbstractDualNode node2 = nodes.get(1);
//        AbstractDualNode node3 = nodes.get(2);
//
//        if (node2.isAdjacent(node1) && node2.isAdjacent(node3)) {
//            return getMissingZone(node1.abr, node3.abr, node2.abr, zones);
//        }
//
//        if (node1.isAdjacent(node2) && node1.isAdjacent(node3)) {
//            return getMissingZone(node2.abr, node3.abr, node1.abr, zones);
//        }
//
//        if (node3.isAdjacent(node1) && node3.isAdjacent(node2)) {
//            return getMissingZone(node1.abr, node2.abr, node3.abr, zones);
//        }

        return Optional.empty();
    }

    private Optional<AbstractBasicRegion> getMissingZone(AbstractBasicRegion zone1, AbstractBasicRegion zone2, AbstractBasicRegion sameZone, Set<AbstractBasicRegion> zones) {
        log.trace("CALLLING with " + zone1 + " " + zone2);

        for (AbstractBasicRegion zone : zones) {
            if (zone.isLabelEquivalent(sameZone))
                continue;

            Optional<AbstractCurve> curve1 = zone.getStraddledContour(zone1);
            Optional<AbstractCurve> curve2 = zone.getStraddledContour(zone2);

            if (curve1.isPresent() && curve2.isPresent()) {
                return Optional.of(zone);
            }
        }

        return Optional.empty();
    }
    // EXPERIMENTAL END

    @Override
    public String toString() {
        return graph.toString();
    }
}
