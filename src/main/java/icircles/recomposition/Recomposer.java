package icircles.recomposition;

import java.util.*;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractBasicRegion;

import icircles.decomposition.DecompositionStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Recomposer {

    private static final Logger log = LogManager.getLogger(Recomposer.class);

    private RecompositionStrategy strategy;

    public Recomposer(RecompositionType type) {
        strategy = type.strategy();
        log.info("Using strategy: " + type.getUiName());
    }

    public List<RecompositionStep> recompose(List<DecompositionStep> decompSteps) {
        Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones = new TreeMap<>(AbstractBasicRegion::compareTo);

        int numSteps = decompSteps.size();

        List<RecompositionStep> result = new ArrayList<>(numSteps);

        for (int i = numSteps - 1; i >= 0; i--) {
            if (i < numSteps - 1) {
                result.add(recomposeStep(decompSteps.get(i), result.get(numSteps - 2 - i), matchedZones));
            } else {
                result.add(recomposeFirstStep(decompSteps.get(i), matchedZones));
            }
        }

        log.info("Recomposition begin");
        result.forEach(log::trace);
        log.info("Recomposition end");

        return result;
    }

    /**
     * Recompose first step, which is also the last decomposition step.
     *
     * @param decompStep last decomposition step
     * @param matchedZones matched zones
     * @return first recomposition step
     */
    private RecompositionStep recomposeFirstStep(DecompositionStep decompStep,
                                                 Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones) {

        AbstractCurve was_removed = decompStep.removed();
        List<RecompositionData> added_contour_data = new ArrayList<>();

        // make a new Abstract Description
        Set<AbstractCurve> contours = new TreeSet<>();
        AbstractBasicRegion outside_zone = AbstractBasicRegion.get(contours);

        List<AbstractBasicRegion> split_zone = new ArrayList<>();
        List<AbstractBasicRegion> added_zone = new ArrayList<>();
        split_zone.add(outside_zone);
        added_contour_data.add(new RecompositionData(was_removed, split_zone, added_zone));

        contours.add(was_removed);
        AbstractBasicRegion new_zone = AbstractBasicRegion.get(contours);
        Set<AbstractBasicRegion> new_zones = new TreeSet<>();
        new_zones.add(new_zone);
        new_zones.add(outside_zone);
        added_zone.add(new_zone);

        matchedZones.put(outside_zone, outside_zone);
        matchedZones.put(new_zone, new_zone);

        AbstractDescription from = decompStep.to();
        AbstractDescription to = new AbstractDescription(contours, new_zones);

        return new RecompositionStep(from, to, added_contour_data);
    }

    /**
     *
     * @param decompStep decomposition step
     * @param previous previous recomposition step
     * @param matchedZones matched zones
     * @return recomposition step
     */
    private RecompositionStep recomposeStep(
            DecompositionStep decompStep,
            RecompositionStep previous,
            Map<AbstractBasicRegion, AbstractBasicRegion> matchedZones) {

        AbstractCurve was_removed = decompStep.removed();
        List<RecompositionData> added_contour_data = new ArrayList<>();

        AbstractDescription from = previous.to();

        // find the resulting zones in the previous step got to
        ArrayList<AbstractBasicRegion> zones_to_split = new ArrayList<>();

        Map<AbstractBasicRegion, AbstractBasicRegion> zones_moved_during_decomp = decompStep.zonesMoved();
        Collection<AbstractBasicRegion> zones_after_moved = zones_moved_during_decomp.values();

        Map<AbstractBasicRegion, AbstractBasicRegion> matched_inverse = new HashMap<>();
        Iterator<AbstractBasicRegion> moved_it = zones_after_moved.iterator();
        while (moved_it.hasNext()) {
            AbstractBasicRegion moved = moved_it.next();
            AbstractBasicRegion to_split = matchedZones.get(moved);

            matched_inverse.put(to_split, moved);
            if (to_split != null) {
                zones_to_split.add(to_split);
            } else {
                throw new RuntimeException("match not found");
            }
        }

        // Partition zones_to_split
        List<Cluster> clusters = strategy.makeClusters(zones_to_split);

        clusters.forEach(c -> log.trace("Cluster for recomposition: " + c));

        Set<AbstractBasicRegion> new_zone_set = from.getCopyOfZones();
        Set<AbstractCurve> new_cont_set = from.getCopyOfContours();

        // for each cluster, make a Contour with label
        for (Cluster cluster : clusters) {
            AbstractCurve new_cont = new AbstractCurve(was_removed);
            List<AbstractBasicRegion> split_zones = new ArrayList<>();
            List<AbstractBasicRegion> added_zones = new ArrayList<>();
            new_cont_set.add(new_cont);

            for (AbstractBasicRegion z : cluster.zones()) {
                split_zones.add(z);
                AbstractBasicRegion new_zone = z.moved_in(new_cont);
                new_zone_set.add(new_zone);
                added_zones.add(new_zone);

                AbstractBasicRegion decomp_z = matched_inverse.get(z);
                matchedZones.put(decomp_z.moved_in(was_removed), new_zone);
            }

            added_contour_data.add(new RecompositionData(new_cont, split_zones, added_zones));
        }

        AbstractDescription to = new AbstractDescription(new_cont_set, new_zone_set);

        return new RecompositionStep(from, to, added_contour_data);
    }
}
