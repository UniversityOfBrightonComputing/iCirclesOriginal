package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdual.AbstractDualEdge;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import icircles.decomposition.Decomposer;
import icircles.decomposition.DecompositionStep;
import icircles.recomposition.Recomposer;
import icircles.recomposition.RecompositionData;
import icircles.recomposition.RecompositionStep;
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

            List<AbstractDualEdge> path = graph.findShortestPath(nodeStart, nodeTarget);

            path.forEach(e -> {
                log.debug(e.from + " -> " + e.to);
                graph.removeEdge(e);
            });

            path = graph.findShortestPath(nodeStart, nodeTarget);

            log.debug("Cycle?:");

            path.forEach(e -> {
                log.debug(e.from + " -> " + e.to);
            });
        }

        return iCirclesDiagram;
    }
}
