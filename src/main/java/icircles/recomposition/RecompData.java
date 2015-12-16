package icircles.recomposition;

import java.util.List;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;

public class RecompData {

    public RecompData(AbstractCurve newCont,
            List<AbstractBasicRegion> splitZones,
            List<AbstractBasicRegion> newZones) {
        added_curve = newCont;
        split_zones = splitZones;
        new_zones = newZones;
    }
    public AbstractCurve added_curve;
    public List<AbstractBasicRegion> split_zones; // in "from"
    public List<AbstractBasicRegion> new_zones; // in "to"
}
