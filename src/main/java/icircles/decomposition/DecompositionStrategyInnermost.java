package icircles.decomposition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import icircles.util.DEB;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractBasicRegion;

public class DecompositionStrategyInnermost extends DecompositionStrategy {

    List<AbstractCurve> getContoursToRemove(AbstractDescription ad) {
        List<AbstractCurve> result = new ArrayList<>();

        // an innermost abstract contour has the fewest abstract basic regions inside
        int best_num_zones = ad.getNumZones() + 1;
        AbstractCurve best_contour = null;
        Iterator<AbstractCurve> c_it = ad.getContourIterator();
        while (c_it.hasNext()) {
            AbstractCurve c = c_it.next();
            int num_zones = 0;
            Iterator<AbstractBasicRegion> z_it = ad.getZoneIterator();
            while (z_it.hasNext()) {
                AbstractBasicRegion z = z_it.next();
                if (z.contains(c)) {
                    num_zones++;
                }
            }
            if (num_zones < best_num_zones) {
                best_num_zones = num_zones;
                best_contour = c;
            }
        }
        result.add(best_contour);

        return result;
    }
}
