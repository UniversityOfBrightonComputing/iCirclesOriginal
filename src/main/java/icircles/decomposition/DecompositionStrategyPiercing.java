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
        Iterator<AbstractCurve> acIt = ad.getContourIterator();
        while (acIt.hasNext()) {
            AbstractCurve ac = acIt.next();
            if (isPiercingCurve(ac, ad)) {
                int nz = numZonesInside(ac, ad);
                if (nz < bestNZ) {
                    result.clear();
                    result.add(ac);
                    bestNZ = nz;
                } else if (nz == bestNZ) {
                    result.add(ac);
                }
            }
        }

        if (result.isEmpty()) {
            acIt = ad.getContourIterator();
            while (acIt.hasNext()) {
                AbstractCurve ac = acIt.next();
                int nz = numZonesInside(ac, ad);
                if (nz < bestNZ) {
                    result.clear();
                    result.add(ac);
                    bestNZ = nz;
                } else if (nz == bestNZ) {
                    result.add(ac);
                }
            }
        }

        return result;
    }

    private int numZonesInside(AbstractCurve ac, AbstractDescription ad) {
        int nz = 0;

        Iterator<AbstractBasicRegion> abrit = ad.getZoneIterator();
        while (abrit.hasNext()) {
            AbstractBasicRegion abr = abrit.next();
            if (abr.contains(ac)) {
                nz++;
            }
        }
        return nz;
    }

    private boolean isPiercingCurve(AbstractCurve ac, AbstractDescription ad) {
        // every abstract basic region in ad which is in ac
        // must have a corresponding abr which is not in ac
        Iterator<AbstractBasicRegion> abrit = ad.getZoneIterator();
        ArrayList<AbstractBasicRegion> zonesInContour =
                new ArrayList<AbstractBasicRegion>();

        abrLoop:
        while (abrit.hasNext()) {
            AbstractBasicRegion abr = abrit.next();
            if (abr.contains(ac)) {
                zonesInContour.add(abr);
                // look for a partner zone
                Iterator<AbstractBasicRegion> abrit2 = ad.getZoneIterator();
                while (abrit2.hasNext()) {
                    AbstractBasicRegion abr2 = abrit2.next();
                    // TODO: be careful referential check, need it?
                    if (abr.getStraddledContour(abr2) == ac) {
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
        abrit = zonesInContour.iterator();
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
