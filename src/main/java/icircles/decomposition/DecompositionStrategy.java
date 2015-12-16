package icircles.decomposition;

import java.util.ArrayList;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;

public abstract class DecompositionStrategy {

    abstract void getContoursToRemove(AbstractDescription ad, ArrayList<AbstractCurve> toRemove);
}
