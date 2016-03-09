package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import icircles.decomposition.DecompositionStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class BetterBasicRecomposer extends BasicRecomposer {

    private static final Logger log = LogManager.getLogger(BetterBasicRecomposer.class);

    public BetterBasicRecomposer(RecompositionStrategy strategy) {
        super(strategy);
    }

    @Override
    protected RecompositionStep recomposeStep(DecompositionStep decompStep, RecompositionStep previous,
                                              Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones) {

        log.trace("Matched Zones: " + matchedZones);

        // find the resulting zones in the previous step got to
        List<AbstractBasicRegion> zonesToSplit = new ArrayList<>();

        Map<AbstractBasicRegion, AbstractBasicRegion> zones_moved_during_decomp = decompStep.zonesMoved();
        Collection<AbstractBasicRegion> zones_after_moved = zones_moved_during_decomp.values();

        Map<AbstractBasicRegion, AbstractBasicRegion> matched_inverse = new HashMap<>();

        Iterator<AbstractBasicRegion> moved_it = zones_after_moved.iterator();
        while (moved_it.hasNext()) {
            AbstractBasicRegion moved = moved_it.next();

            //System.out.println("Moved: " + moved);

            AbstractBasicRegion to_split = matchedZones.get(moved);

            matched_inverse.put(to_split, moved);

            if (to_split != null) {
                zonesToSplit.add(to_split);
            } else {
                throw new RuntimeException("match not found");
                //zonesToSplit.add(moved);
            }
        }

        log.trace("Matched Inverse: " + matched_inverse);


        AbstractDescription from = previous.to();

        // zonesToSplit, from

        log.debug("Zones to split (ORIGINAL): " + zonesToSplit);

        if (zonesToSplit.size() >= 2 && !zonesToSplit.get(0).getStraddledContour(zonesToSplit.get(1)).isPresent()
                /*|| zonesToSplit.size() == 3*/) {
            AbstractDualGraph graph = new AbstractDualGraph(new ArrayList<>(from.getZones()));

            List<AbstractDualNode> nodesToSplit = new ArrayList<>();

            for (int i = 0; i < zonesToSplit.size(); i++) {
                int j = i + 1 < zonesToSplit.size() ? i + 1 : 0;

                AbstractBasicRegion zone1 = zonesToSplit.get(i);
                AbstractBasicRegion zone2 = zonesToSplit.get(j);

                // TODO: this fixes 3 zones
//                if (i == 0 && zone1.equals(AbstractBasicRegion.OUTSIDE)) {
//                    nodesToSplit.add(graph.getNodeByZone(zonesToSplit.get(i)));
//                    graph.removeNode(graph.getNodeByZone(zonesToSplit.get(i)));
//                    continue;
//                } else if (j == 0 && zone2.equals(AbstractBasicRegion.OUTSIDE)) {
//                    break;
//                }


                log.debug("Searching path zones: " + zonesToSplit.get(i) + " " + zonesToSplit.get(j));

                AbstractDualNode node1 = graph.getNodeByZone(zonesToSplit.get(i));
                AbstractDualNode node2 = graph.getNodeByZone(zonesToSplit.get(j));

//                if (graph.isAdjacent(node1, node2)) {
//                    nodesToSplit.add(node1);
//                    //nodesToSplit.add(node2);
//
//                    graph.removeEdge(graph.getEdge(node1, node2));
//
//                    if (i != 0) {
//                        graph.removeNode(node1);
//                    }
//                    continue;
//                }

                try {
                    List<AbstractDualNode> nodePath = graph.findShortestVertexPath(node1, node2);

                    log.debug("Found path between " + node1 + " and " + node2 + " Path: " + nodePath);

                    nodesToSplit.addAll(nodePath);
                    nodesToSplit.remove(node2);

                    // if first, we keep it
                    if (i == 0) {
                        nodePath.remove(node1);
                    }

                    nodePath.remove(node2);

                    // remove visited edges and nodes
                    graph.findShortestEdgePath(node1, node2).forEach(graph::removeEdge);
                    nodePath.forEach(graph::removeNode);
                } catch (Exception e) {

                    if (j == 0) {
                        // we know nodes are connected with a single chain, NOT a cycle
                        if (isMultiPiercing(nodesToSplit)) {

                            nodesToSplit.add(node1);

                            break;
                        }
                    } else {
                        throw new RuntimeException(e.getMessage() + " Failed to chain: " + nodesToSplit);
                    }
                }
            }


            zonesToSplit = nodesToSplit.stream()
                    .map(AbstractDualNode::getZone)
                    .collect(Collectors.toList());
        }

        log.debug("Zones to split (FIXED): " + zonesToSplit);




        List<Cluster> clusters = new ArrayList<>();
        clusters.add(new Cluster(zonesToSplit.toArray(new AbstractBasicRegion[0])));

        //System.out.println(clusters);



        Set<AbstractBasicRegion> newZoneSet = new TreeSet<>(from.getZones());
        Set<AbstractCurve> newCurveSet = new TreeSet<>(from.getCurves());

        AbstractCurve removedCurve = decompStep.removed();
        List<RecompositionData> addedContourData = new ArrayList<>();

        // for each cluster, make a curve with label
        for (Cluster cluster : clusters) {

            List<AbstractBasicRegion> splitZones = new ArrayList<>();
            List<AbstractBasicRegion> addedZones = new ArrayList<>();

            AbstractCurve newCurve = new AbstractCurve(removedCurve.getLabel());
            newCurveSet.add(newCurve);

            for (AbstractBasicRegion z : cluster.zones()) {
                splitZones.add(z);
                AbstractBasicRegion new_zone = z.moveInside(newCurve);

                newZoneSet.add(new_zone);
                addedZones.add(new_zone);

                AbstractBasicRegion decomp_z = matched_inverse.get(z);

                // TODO: adhoc solves problem but what does it do?
                if (decomp_z == null) {
                    decomp_z = z;
                }

                matchedZones.put(decomp_z.moveInside(removedCurve), new_zone);
            }

            addedContourData.add(new RecompositionData(newCurve, splitZones, addedZones));
        }

        AbstractDescription to = new AbstractDescription(newCurveSet, newZoneSet);

        return new RecompositionStep(from, to, addedContourData);
    }

    private boolean isMultiPiercing(List<AbstractDualNode> nodes) {
        // TODO: essentially check if one zone is nested in another recursively
        return true;
    }
}