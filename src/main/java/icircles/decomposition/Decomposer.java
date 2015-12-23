package icircles.decomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Decomposer {

    private static final Logger log = LogManager.getLogger(Decomposer.class);

    private DecompositionType type;

    public Decomposer(DecompositionType type) {
        this.type = type;
    }

    public List<DecompositionStep> decompose(AbstractDescription ad) {
        if (ad.getNumZones() <= 0) {
            throw new IllegalArgumentException("Abstraction description is empty: " + ad.toDebugString());
        }

        log.info("Using strategy: " + type.getUiName());
        DecompositionStrategy strategy = type.strategy();

        List<DecompositionStep> result = new ArrayList<>();

        while (true) {
            List<AbstractCurve> toRemove = strategy.getContoursToRemove(ad);

            // TODO: do we know that it doesn't contain null?
            if (toRemove.isEmpty() || toRemove.contains(null)) {
                break;
            }

            for (AbstractCurve curveToRemove : toRemove) {
                DecompositionStep step = takeStep(ad, curveToRemove);
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
        Set<AbstractCurve> contours = ad.getCopyOfContours();
        contours.remove(curve);

        Set<AbstractBasicRegion> zones = new TreeSet<>();
        Map<AbstractBasicRegion, AbstractBasicRegion> zonesMoved = new TreeMap<>();

        for (AbstractBasicRegion zone : ad.getZonesUnmodifiable()) {
            AbstractBasicRegion newZone = zone.moveOutside(curve);
            zones.add(newZone);
            if (!zone.isLabelEquivalent(newZone)) {
                zonesMoved.put(zone, newZone);
            }
        }

        AbstractDescription targetAD = new AbstractDescription(contours, zones);
        return new DecompositionStep(ad, targetAD, zonesMoved, curve);
    }
}
