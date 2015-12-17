package icircles.decomposition;

import java.util.List;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;

public abstract class DecompositionStrategy {

    abstract List<AbstractCurve> getContoursToRemove(AbstractDescription ad);
}
