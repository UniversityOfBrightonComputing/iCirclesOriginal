package icircles.recomposition;

import java.util.ArrayList;
import java.util.List;

import icircles.util.DEB;

import icircles.abstractdescription.AbstractBasicRegion;

public abstract class RecompositionStrategy {

    public abstract List<Cluster> makeClusters(List<AbstractBasicRegion> zones_to_split);
}
