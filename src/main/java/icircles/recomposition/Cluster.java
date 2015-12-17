package icircles.recomposition;

import java.util.ArrayList;

import icircles.util.DEB;

import icircles.abstractdescription.AbstractBasicRegion;

public class Cluster {

    private ArrayList<AbstractBasicRegion> zones;

    public Cluster(AbstractBasicRegion z) {
        zones = new ArrayList<>();
        zones.add(z);
    }

    public Cluster(AbstractBasicRegion z1,
            AbstractBasicRegion z2) {
        DEB.assertCondition(z1.getStraddledContour(z2) != null, "non-adjacent cluster pair");
        zones = new ArrayList<>();
        zones.add(z1);
        zones.add(z2);
    }

    public Cluster(AbstractBasicRegion z1,
            AbstractBasicRegion z2,
            AbstractBasicRegion z3,
            AbstractBasicRegion z4) {
        DEB.assertCondition(z1.getStraddledContour(z2) != null, "non-adjacent cluster pair");
        DEB.assertCondition(z1.getStraddledContour(z3) != null, "non-adjacent cluster pair");
        DEB.assertCondition(z2.getStraddledContour(z4) == z1.getStraddledContour(z3), "non-adjacent cluster pair");
        DEB.assertCondition(z3.getStraddledContour(z4) == z1.getStraddledContour(z2), "non-adjacent cluster pair");
        zones = new ArrayList<>();
        zones.add(z1);
        zones.add(z2);
        zones.add(z3);
        zones.add(z4);
    }

    public ArrayList<AbstractBasicRegion> zones() {
        return zones;
    }

    public String debug() {
        String result = "{";
        boolean firstOne = true;
        for (AbstractBasicRegion abr : zones) {
            if (!firstOne) {
                result += ",";
            }
            result = result + abr.toDebugString();
            firstOne = false;
        }
        result += "}";
        return result;
    }
}
