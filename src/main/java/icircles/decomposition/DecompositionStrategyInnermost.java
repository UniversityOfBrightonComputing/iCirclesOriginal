package icircles.decomposition;

import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * An innermost abstract contour has the fewest abstract basic regions inside
 */
public class DecompositionStrategyInnermost extends DecompositionStrategy {

    List<AbstractCurve> getContoursToRemove(AbstractDescription ad) {
        List<AbstractCurve> result = new ArrayList<>();

        ad.getCurvesUnmodifiable()
                .stream()
                .reduce((curve1, curve2) -> ad.getNumZonesIn(curve1) <= ad.getNumZonesIn(curve2) ? curve1 : curve2)
                .ifPresent(result::add);

        return result;
    }
}
