package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;

import java.util.List;

public abstract class RecompositionStrategy {

    public abstract List<Cluster> makeClusters(List<AbstractBasicRegion> zones_to_split);
}
