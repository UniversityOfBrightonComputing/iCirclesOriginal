package icircles.recomposition;

import java.util.Comparator;

import icircles.abstractdescription.AbstractBasicRegion;

public class ABRComparator implements Comparator<AbstractBasicRegion> {

    public int compare(AbstractBasicRegion o1, AbstractBasicRegion o2) {
        return o1.compareTo(o2);
    }
}
