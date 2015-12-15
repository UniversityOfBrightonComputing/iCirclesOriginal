package icircles.recomposition;

import java.util.ArrayList;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;

public class RecompData {

    public RecompData(AbstractCurve newCont,
            ArrayList<AbstractBasicRegion> splitZones,
            ArrayList<AbstractBasicRegion> newZones) {
        added_curve = newCont;
        split_zones = splitZones;
        new_zones = newZones;
    }
    public AbstractCurve added_curve;
    public ArrayList<AbstractBasicRegion> split_zones; // in "from"
    public ArrayList<AbstractBasicRegion> new_zones; // in "to"
}
