package icircles.decomposition;

import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;

import java.util.ArrayList;
import java.util.List;

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
