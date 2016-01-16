package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractDescription;

import java.util.List;

public interface RecompositionStrategy {

    /**
     *
     * @param zonesToSplit zones needed to split by the curve
     * @param description abstract description so far
     * @return clusters
     */
    List<Cluster> makeClusters(List<AbstractBasicRegion> zonesToSplit, AbstractDescription description);
}
