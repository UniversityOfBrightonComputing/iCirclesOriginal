package icircles.decomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DecompositionStrategyInnermost extends DecompositionStrategy {

    List<AbstractCurve> getContoursToRemove(AbstractDescription ad) {
        List<AbstractCurve> result = new ArrayList<>();

        // an innermost abstract contour has the fewest abstract basic regions inside
        int best_num_zones = ad.getNumZones() + 1;
        AbstractCurve best_contour = null;

        for (AbstractCurve curve : ad.getCurvesUnmodifiable()) {
            int numZones = 0;

            for (AbstractBasicRegion zone : ad.getZonesUnmodifiable()) {
                if (zone.contains(curve)) {
                    numZones++;
                }
            }

            if (numZones < best_num_zones) {
                best_num_zones = numZones;
                best_contour = curve;
            }
        }

        result.add(best_contour);

        return result;
    }
}
