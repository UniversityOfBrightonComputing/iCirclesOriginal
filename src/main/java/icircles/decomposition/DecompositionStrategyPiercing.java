package icircles.decomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class DecompositionStrategyPiercing extends DecompositionStrategy {

    List<AbstractCurve> getContoursToRemove(AbstractDescription ad) {
        List<AbstractCurve> result = new ArrayList<>();

        int bestNZ = Integer.MAX_VALUE;

        for (AbstractCurve curve : ad.getCurvesUnmodifiable()) {
            if (isPiercingCurve(curve, ad)) {
                int nz = ad.getNumZonesIn(curve);
                if (nz < bestNZ) {
                    result.clear();
                    result.add(curve);
                    bestNZ = nz;
                } else if (nz == bestNZ) {
                    result.add(curve);
                }
            }
        }

        if (result.isEmpty()) {
            for (AbstractCurve curve : ad.getCurvesUnmodifiable()) {
                int nz = ad.getNumZonesIn(curve);
                if (nz < bestNZ) {
                    result.clear();
                    result.add(curve);
                    bestNZ = nz;
                } else if (nz == bestNZ) {
                    result.add(curve);
                }
            }
        }

        return result;
    }

    private boolean isPiercingCurve(AbstractCurve ac, AbstractDescription ad) {
        // every abstract basic region in ad which is in ac
        // must have a corresponding abr which is not in ac
        ArrayList<AbstractBasicRegion> zonesInContour = new ArrayList<>();

        abrLoop:
        for (AbstractBasicRegion zone : ad.getZonesUnmodifiable()) {
            if (zone.contains(ac)) {
                zonesInContour.add(zone);

                // look for a partner zone
                for (AbstractBasicRegion zone2 : ad.getZonesUnmodifiable()) {
                    if (zone.getStraddledContour(zone2).orElse(null) == ac) {
                        continue abrLoop;
                    }
                }

                // never found a partner zone
                return false;
            }
        }

        // check that the zones in C form a cluster - we need 2^n zones
        int power = powerOfTwo(zonesInContour.size());
        if (power < 0) {
            return false;
        }

        // find the smallest zone (one in fewest contours)
        int zoneSize = Integer.MAX_VALUE;
        AbstractBasicRegion smallestZone = null;
        Iterator<AbstractBasicRegion> abrit = zonesInContour.iterator();
        while (abrit.hasNext()) {
            AbstractBasicRegion abr = abrit.next();
            int numCs = abr.getNumContours();
            if (numCs < zoneSize) {
                zoneSize = numCs;
                smallestZone = abr;
            }
        }

        // every other zone in ac must be a superset of that zone
        abrit = zonesInContour.iterator();
        while (abrit.hasNext()) {
            AbstractBasicRegion abr = abrit.next();
            Iterator<AbstractCurve> acIt = smallestZone.getContourIterator();
            while (acIt.hasNext()) {
                AbstractCurve ac2 = acIt.next();
                if (!abr.contains(ac2)) {
                    return false;
                }
            }
        }

        // We have 2^n zones which are all supersets of smallestZone.
        // Check that they use exactly n contours from smallestZone.
        TreeSet<AbstractCurve> addedContours = new TreeSet<AbstractCurve>();
        abrit = zonesInContour.iterator();
        while (abrit.hasNext()) {
            AbstractBasicRegion abr = abrit.next();
            Iterator<AbstractCurve> acIt = abr.getContourIterator();
            while (acIt.hasNext()) {
                AbstractCurve ac2 = acIt.next();
                if (!smallestZone.contains(ac2)) {
                    addedContours.add(ac2);
                    if (addedContours.size() > power) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     *
     * @param n
     * @return result where  n = 2^(result)
     */
    private int powerOfTwo(int n) {
        int result = 0;
        while (n % 2 == 0) {
            result++;
            n /= 2;
        }
        if (n != 1) {
            return -1;
        } else {
            return result;
        }
    }
}
