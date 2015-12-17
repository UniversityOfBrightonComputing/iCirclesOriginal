package icircles.decomposition;

import java.util.ArrayList;
import java.util.List;

import icircles.util.DEB;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;

public class DecompositionStrategyUseSortOrder extends DecompositionStrategy {

    // TODO: add param
    boolean m_natural_order = true;

    DecompositionStrategyUseSortOrder() {
    }

    List<AbstractCurve> getContoursToRemove(AbstractDescription ad) {
        List<AbstractCurve> result = new ArrayList<>();

        if (m_natural_order) {
            result.add(ad.getFirstContour());
        } else {
            result.add(ad.getLastContour());
        }

        return result;
    }
}
