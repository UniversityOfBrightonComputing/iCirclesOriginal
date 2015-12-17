package icircles.decomposition;

import java.util.*;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractBasicRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Decomposer {

    private static final Logger log = LogManager.getLogger(Decomposer.class);

    private DecompositionStrategy strategy;
    private ArrayList<AbstractCurve> toRemove = new ArrayList<>();

    public Decomposer(DecompositionType type) {
        strategy = type.strategy();
    }

    public List<DecompositionStep> decompose(AbstractDescription ad) {
        if (!ad.getZoneIterator().hasNext()) {
            throw new IllegalArgumentException("Abstraction description is empty: " + ad.toDebugString());
        }

        List<DecompositionStep> result = new ArrayList<>();
        while_loop:
        while (true) {
            strategy.getContoursToRemove(ad, toRemove);

            if (toRemove.isEmpty()) {
                break while_loop;
            }

            for (AbstractCurve curveToRemove : toRemove) {
                DecompositionStep step = takeStep(ad, curveToRemove);
                if (step == null) {
                    break while_loop;
                }
                result.add(step);
                ad = step.to();
            }
        }

        log.info("Decomposition begin");
        result.forEach(log::trace);
        log.info("Decomposition end");

        return result;
    }

    private DecompositionStep takeStep(AbstractDescription ad, AbstractCurve curve) {
        if (curve == null) {
            return null;
        }

        // otherwise, make a new AbstractDescription
        Set<AbstractCurve> contours = ad.getCopyOfContours();
        contours.remove(curve);

        Set<AbstractBasicRegion> zones = new TreeSet<>();
        Map<AbstractBasicRegion, AbstractBasicRegion> zonesMoved = new TreeMap<>();

        for (AbstractBasicRegion zone : ad.getCopyOfZones()) {
            AbstractBasicRegion newZone = zone.moveOutside(curve);
            zones.add(newZone);
            if (zone != newZone) {
                zonesMoved.put(zone, newZone);
            }
        }

        AbstractDescription targetAD = new AbstractDescription(contours, zones);
        return new DecompositionStep(ad, targetAD, zonesMoved, curve);
    }
}
