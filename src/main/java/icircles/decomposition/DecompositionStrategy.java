package icircles.decomposition;

import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;

import java.util.List;

public abstract class DecompositionStrategy {

    abstract List<AbstractCurve> getContoursToRemove(AbstractDescription ad);
}
