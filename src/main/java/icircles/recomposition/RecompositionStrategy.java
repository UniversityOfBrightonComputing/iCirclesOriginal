package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;

import java.util.List;

public interface RecompositionStrategy {

    List<Cluster> makeClusters(List<AbstractBasicRegion> zonesToSplit);
}
