package icircles.recomposition;

import java.util.Comparator;

import icircles.abstractdescription.AbstractBasicRegion;

/**
 * Abstract Basic Region comparator.
 */
public class ABRComparator implements Comparator<AbstractBasicRegion> {

    @Override
    public int compare(AbstractBasicRegion o1, AbstractBasicRegion o2) {
        return o1.compareTo(o2);
    }
}
