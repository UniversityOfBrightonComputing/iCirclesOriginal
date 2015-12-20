package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private List<AbstractBasicRegion> zones;

    public Cluster(AbstractBasicRegion z) {
        zones = new ArrayList<>();
        zones.add(z);
    }

    public Cluster(AbstractBasicRegion z1,
            AbstractBasicRegion z2) {

        if (z1.getStraddledContour(z2) == null)
            throw new IllegalArgumentException("Non-adjacent cluster pair");

        zones = new ArrayList<>();
        zones.add(z1);
        zones.add(z2);
    }

    public Cluster(AbstractBasicRegion z1,
            AbstractBasicRegion z2,
            AbstractBasicRegion z3,
            AbstractBasicRegion z4) {

        if (z1.getStraddledContour(z2) == null)
            throw new IllegalArgumentException("Non-adjacent cluster pair");
        if (z1.getStraddledContour(z3) == null)
            throw new IllegalArgumentException("Non-adjacent cluster pair");

        if (z2.getStraddledContour(z4) != z1.getStraddledContour(z3))
            throw new IllegalArgumentException("Non-adjacent cluster pair");
        if (z3.getStraddledContour(z4) != z1.getStraddledContour(z2))
            throw new IllegalArgumentException("Non-adjacent cluster pair");

        zones = new ArrayList<>();
        zones.add(z1);
        zones.add(z2);
        zones.add(z3);
        zones.add(z4);
    }

    public List<AbstractBasicRegion> zones() {
        return zones;
    }

    @Override
    public String toString() {
        return zones.toString();
    }
}
