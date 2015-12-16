package icircles.recomposition;

import java.util.ArrayList;

import icircles.util.DEB;

import icircles.abstractdescription.AbstractBasicRegion;

public abstract class RecompositionStrategy {

    public abstract ArrayList<Cluster> makeClusters(ArrayList<AbstractBasicRegion> zones_to_split);
}
