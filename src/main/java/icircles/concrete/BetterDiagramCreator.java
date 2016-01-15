package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdual.AbstractDualGraph;
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

        // this is a specific solution to "a b ab ac bc abc" which originally results in a split curve
        // can we generify the solution?

//        RecompositionStep step1 = rSteps.get(1);
//
//        List<AbstractBasicRegion> splitZones = new AbstractDescription("a b ab").getZonesShallowCopy().stream().collect(Collectors.toList());
//        List<AbstractBasicRegion> newZones = new AbstractDescription("c ac bc abc").getZonesShallowCopy().stream().filter(z -> !z.getCopyOfContours().isEmpty()).collect(Collectors.toList());
//
//        RecompositionData data = new RecompositionData(new AbstractCurve(CurveLabel.get("c")), splitZones, newZones);
//
//        rSteps.remove(2);
//        RecompositionStep step2 = new RecompositionStep(step1.to(), new AbstractDescription("a b ab ac bc abc c"),
//                Arrays.asList(data));
//        rSteps.add(step2);
//
//        System.out.println(step2);




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



    }

    @Override
    public ConcreteDiagram createDiagram(AbstractDescription description, int size) throws CannotDrawException {
        AbstractBasicRegion.clearLibrary();

        ConcreteDiagram iCirclesDiagram = super.createDiagram(description, size);

        Map<AbstractCurve, List<CircleContour> > duplicates = iCirclesDiagram.findDuplicateContours();
        if (duplicates.isEmpty())
            return iCirclesDiagram;

        for (AbstractCurve curve : duplicates.keySet()) {
            // TODO: maybe actual, not original?
            AbstractDescription original = iCirclesDiagram.getOriginalDescription();

            Set<AbstractBasicRegion> zones = original.getZonesIn(curve);
            if (zones.size() != 3) { // >= 4
                // if the curve runs over 4 or more zones we give up for now...
                continue;
            }

            List<AbstractBasicRegion> newZones = new ArrayList<>(zones);

            zones = zones.stream().map(zone -> zone.moveOutside(curve)).collect(Collectors.toSet());
            List<AbstractBasicRegion> splitZones = new ArrayList<>(zones);


            log.trace("Cluster zones: " + zones.toString());

            AbstractDualGraph graph = new AbstractDualGraph(new ArrayList<>(zones));

            graph.getMissingZone(original.getZonesUnmodifiable()).ifPresent(zone -> {
                splitZones.add(zone);
                newZones.add(zone.moveInside(curve));

                log.debug("4 Cluster is present: " + zone);
            });


            if (splitZones.size() == 4) {
                int index = 0;

                RecompositionStep newStep = null;

                loop:
                for (RecompositionStep step : getRSteps()) {


                    for (RecompositionData data : step.getAddedContourData()) {
                        if (data.addedCurve.matchesLabel(curve)) {

                            Collections.sort(splitZones);

                            log.debug("SPLIT ZONES: " + splitZones);
                            log.debug("NEW ZONES:   " + newZones);

                            RecompositionData newData = new RecompositionData(curve, splitZones, newZones);

                            AbstractDescription from = getRSteps().get(index - 1).to();
                            Set<AbstractBasicRegion> zz = new TreeSet<>(from.getZonesUnmodifiable());
                            //zz.removeAll(splitZones);
                            zz.addAll(newZones);

                            if (!zz.contains(AbstractBasicRegion.OUTSIDE)) {
                                zz.add(AbstractBasicRegion.OUTSIDE);
                            }

                            Set<AbstractCurve> contours = new TreeSet<>(from.getCurvesUnmodifiable());
                            contours.add(curve);

                            AbstractDescription to = new AbstractDescription(contours, zz);

                            newStep = new RecompositionStep(from, to, Collections.singletonList(newData));

                            if (index + 1 < getRSteps().size()) {
                                RecompositionStep oldStep = getRSteps().get(index + 1);

                                getRSteps().set(index + 1, new RecompositionStep(to, oldStep.to(), oldStep.getAddedContourData()));
                            }

                            log.debug("From: " + from + " TO: " + to);

                            break loop;
                        }
                    }

                    index++;
                }


                List<RecompositionStep> newSteps = new ArrayList<>(getRSteps());
                newSteps.set(index, newStep);

                log.debug("CREATING NEW DIAGRAM");

                return new DiagramCreator(getDSteps(), newSteps).createDiagram(description, size);
            }
        }

        return iCirclesDiagram;
    }
}
