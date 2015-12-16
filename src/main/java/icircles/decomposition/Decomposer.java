package icircles.decomposition;

import java.util.*;

import icircles.abstractdescription.CurveLabel;
import icircles.util.DEB;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractBasicRegion;

public class Decomposer {

    private DecompositionStrategy s;
    private ArrayList<AbstractCurve> toRemove = new ArrayList<AbstractCurve>(); // some utility data

    public Decomposer(int decompStrategy) {
        s = DecompositionStrategy.getDecompositionStrategy(decompStrategy);
    }

    public Decomposer() {
        s = DecompositionStrategy.getDecompositionStrategy();
    }

    private DecompositionStep take_step(AbstractDescription ad, AbstractCurve c) {
        if (c == null) {
            return null;
        }

        // otherwise, make a new AbstractDescription
        TreeSet<AbstractCurve> contours = ad.getCopyOfContours();
        contours.remove(c);

        Iterator<AbstractBasicRegion> zoneIt = ad.getZoneIterator();
        TreeSet<AbstractBasicRegion> zones = new TreeSet<AbstractBasicRegion>();
        TreeMap<AbstractBasicRegion, AbstractBasicRegion> zones_moved = new TreeMap<AbstractBasicRegion, AbstractBasicRegion>();
        while (zoneIt.hasNext()) {
            AbstractBasicRegion z = zoneIt.next();
            AbstractBasicRegion znew = z.moveOutside(c);
            zones.add(znew);
            if (z != znew) {
                zones_moved.put(z, znew);
            }
        }
        AbstractDescription target_ad = new AbstractDescription(contours, zones);
        DecompositionStep result = new DecompositionStep(
                ad, target_ad, zones_moved, c);
        return result;
    }

    public List<DecompositionStep> decompose(AbstractDescription ad) {
        if (!ad.getZoneIterator().hasNext()) {
            throw new Error("decompose empty description?");
        }

        ArrayList<DecompositionStep> result = new ArrayList<DecompositionStep>();
        while_loop:
        while (true) {
            s.getContoursToRemove(ad, toRemove);

            if (toRemove.size() == 0) {
                break while_loop;
            }

            for (AbstractCurve c : toRemove) {
                DecompositionStep step = take_step(ad, c);
                if (step == null) {
                    break while_loop;
                }
                result.add(step);
                ad = step.to();
            }
        }
        if (DEB.level >= 1) {
            System.out.println("decomposition begin : ");
            for (DecompositionStep step : result) {
                System.out.println("step : " + step.debug());
            }
            System.out.println("decomposition end ");
        }
        return result;
    }
}
