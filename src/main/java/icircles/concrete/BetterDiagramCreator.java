package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdual.AbstractDualEdge;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import icircles.decomposition.Decomposer;
import icircles.geometry.Point2D;
import icircles.recomposition.Recomposer;
import icircles.util.CannotDrawException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class BetterDiagramCreator extends DiagramCreator {

    private static final Logger log = LogManager.getLogger(DiagramCreator.class);

    public BetterDiagramCreator(Decomposer decomposer, Recomposer recomposer) {
        super(decomposer, recomposer);

        // solution for a ab abc ac bc abd ad
        // but iCircles currently doesn't know how to add with TP (triple point) so it adds like a 2-piercing

//        RecompositionStep step1 = rSteps.get(1);
//
//        List<AbstractBasicRegion> splitZones = new AbstractDescription("a c ac").getZonesShallowCopy().stream().collect(Collectors.toList());
//        List<AbstractBasicRegion> newZones = new AbstractDescription("ab bc abc").getZonesShallowCopy().stream().filter(z -> !z.getCopyOfContours().isEmpty()).collect(Collectors.toList());
//
//        RecompositionData data = new RecompositionData(new AbstractCurve("b"), splitZones, newZones);
//
//        RecompositionStep step2 = new RecompositionStep(step1.to(), new AbstractDescription("a c ac ab bc abc"),
//                Arrays.asList(data));
//        rSteps.set(2, step2);

        // a b c ab bc abd bcd
    }

    @Override
    public ConcreteDiagram createDiagram(AbstractDescription description, int size) throws CannotDrawException {
        //AbstractCurve.resetIdCounter();
        //AbstractBasicRegion.clearLibrary();

        ConcreteDiagram iCirclesDiagram = super.createDiagram(description, size);

        Map<AbstractCurve, List<CircleContour> > duplicates = iCirclesDiagram.findDuplicateContours();
        if (duplicates.isEmpty())
            return iCirclesDiagram;

        for (AbstractCurve curve : duplicates.keySet()) {
            AbstractDescription ad = iCirclesDiagram.getActualDescription();
            List<AbstractBasicRegion> zones = ad.getZonesUnmodifiable().stream()
                    .filter(z -> z.containsCurveWithLabel(curve.getLabel()))
                    .collect(Collectors.toList());

            log.debug("Zones in " + curve + ":" + zones.toString());

            zones = zones.stream()
                    .map(z -> z.moveOutsideLabel(curve.getLabel()))
                    .collect(Collectors.toList());

            log.debug("Zones that will be in " + curve + ":" + zones.toString());

            AbstractDualGraph graph = new AbstractDualGraph(new ArrayList<>(ad.getZonesUnmodifiable()));

            // let's assume we found nodes which need to be connected to get a connected graph

            AbstractBasicRegion zoneStart = zones.get(0);
            AbstractBasicRegion zoneTarget = zones.get(1);

            AbstractDualNode nodeStart = null, nodeTarget = null;

            for (AbstractDualNode node : graph.getNodes()) {
                if (node.getZone() == zoneStart) {
                    nodeStart = node;
                } else if (node.getZone() == zoneTarget) {
                    nodeTarget = node;
                }
            }

            List<AbstractDualEdge> path = graph.findShortestEdgePath(nodeStart, nodeTarget);

            // these are abstract zones we know the curve will go through now
//            Set<AbstractBasicRegion> zonesToSplit = new TreeSet<>();
//
//            path.forEach(e -> {
//                log.debug(e.from + " -> " + e.to);
//                zonesToSplit.add(e.from.getZone());
//                zonesToSplit.add(e.to.getZone());
//                graph.removeEdge(e);
//            });
//
//            path = graph.findShortestEdgePath(nodeStart, nodeTarget);
//
//            log.debug("Cycle?:");
//
//            path.forEach(e -> {
//                log.debug(e.from + " -> " + e.to);
//                zonesToSplit.add(e.from.getZone());
//                zonesToSplit.add(e.to.getZone());
//            });

            List<AbstractBasicRegion> zonesToSplit = graph.findShortestVertexPath(nodeStart, nodeTarget)
                    .stream()
                    .map(n -> n.getZone())
                    .collect(Collectors.toList());

            path.forEach(e -> graph.removeEdge(e));

            List<AbstractBasicRegion> zonesToSplit2 = graph.findShortestVertexPath(nodeTarget, nodeStart)
                    .stream()
                    .map(n -> n.getZone())
                    .collect(Collectors.toList());

            zonesToSplit2.remove(nodeStart.getZone());
            zonesToSplit2.remove(nodeTarget.getZone());

            zonesToSplit.addAll(zonesToSplit2);







            log.debug("Zones to split: " + zonesToSplit);


            List<ConcreteZone> concreteZones = zonesToSplit.stream()
                    .map(zone -> {
                        for (ConcreteZone cz : iCirclesDiagram.getAllZones()) {
                            if (cz.getAbstractZone() == zone) {
                                return cz;
                            }
                        }

                        return null;
                    })
                    .collect(Collectors.toList());

            List<Point2D> points = concreteZones.stream()
                    .map(zone -> zone.getCenter())
                    .collect(Collectors.toList());

            ArbitraryContour contour = new ArbitraryContour(curve, points);

            List<CircleContour> circles = iCirclesDiagram.getCircles();
            circles.removeAll(duplicates.get(curve));

            // TODO: actual desc is different
            return new ConcreteDiagram(iCirclesDiagram.getOriginalDescription(), iCirclesDiagram.getActualDescription(),
                    circles, iCirclesDiagram.getCurveToContour(), size, contour);
        }

        return iCirclesDiagram;
    }
}
