package icircles.decomposition;

import java.util.ArrayList;

import icircles.util.DEB;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;

public class DecompositionStrategyUseSortOrder extends DecompositionStrategy {

    // TODO: add param
    boolean m_natural_order = true;

    DecompositionStrategyUseSortOrder() {

        if (DEB.level > 1) {
            System.out.println("decomposition strategy is alphabetic");
            if (m_natural_order) {
                System.out.println("natural order");
            } else {
                System.out.println("reversed order");
            }
        }
        //m_natural_order = natural_order;
    }

    void getContoursToRemove(AbstractDescription ad, ArrayList<AbstractCurve> toRemove) {
        toRemove.clear();
        if (m_natural_order) {
            toRemove.add(ad.getFirstContour());
        } else {
            toRemove.add(ad.getLastContour());
        }
    }
}
