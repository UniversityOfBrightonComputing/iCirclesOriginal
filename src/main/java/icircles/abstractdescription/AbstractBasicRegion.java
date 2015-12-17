package icircles.abstractdescription;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a zone (basic region) at an abstract level.
 * Holds curves that pass through or are inside this zone.
 */
public class AbstractBasicRegion implements Comparable<AbstractBasicRegion> {

    private static final Logger log = LogManager.getLogger(AbstractBasicRegion.class);

    private Set<AbstractCurve> theInSet;
    private static Set<AbstractBasicRegion> library = new TreeSet<>();

    private AbstractBasicRegion(Set<AbstractCurve> in_set) {
        theInSet = in_set;
    }

    public static void clearLibrary() {
        library.clear();
    }

    public static AbstractBasicRegion get(Set<AbstractCurve> in_set) {
        for (AbstractBasicRegion alreadyThere : library) {
            if (alreadyThere.theInSet.equals(in_set)) {
                return alreadyThere;
            }
        }

        Set<AbstractCurve> tmp = new TreeSet<>(in_set);
        AbstractBasicRegion result = new AbstractBasicRegion(tmp);
        library.add(result);
        return result;
    }

    public AbstractBasicRegion moveOutside(AbstractCurve c) {
        if (theInSet.contains(c)) {
            Set<AbstractCurve> contours = new TreeSet<>(theInSet);
            contours.remove(c);
            return get(contours);
        } else {
            return this;
        }
    }

    @Override
    public int compareTo(AbstractBasicRegion other) {
        if (other.theInSet.size() < theInSet.size()) {
            return 1;
        } else if (other.theInSet.size() > theInSet.size()) {
            return -1;
        }

        // same sized in_set
        Iterator<AbstractCurve> this_it = theInSet.iterator();
        Iterator<AbstractCurve> other_it = other.theInSet.iterator();

        while (this_it.hasNext()) {
            AbstractCurve this_c = this_it.next();
            AbstractCurve other_c = other_it.next();
            int comp = this_c.compareTo(other_c);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }

    public Iterator<AbstractCurve> getContourIterator() {
        return theInSet.iterator();
    }

    /**
     * @return number of contours within this zone
     */
    public int getNumContours() {
        return theInSet.size();
    }

    public AbstractCurve getStraddledContour(AbstractBasicRegion other) {
        int nc = getNumContours();
        int othernc = other.getNumContours();

        if (Math.abs(nc - othernc) != 1) {
            return null;
        } else if (nc < othernc) {
            return other.getStraddledContour(this);
        } else {
            // we have one more contour than other - are we neighbours?
            AbstractCurve result = null;
            Iterator<AbstractCurve> it = getContourIterator();
            while (it.hasNext()) {
                AbstractCurve ac = it.next();
                if (!other.contains(ac)) {
                    if (result != null) {
                        return null; // found two contours here absent from other
                    } else {
                        result = ac;
                    }
                }
            }

            log.trace("straddle: " + this + "->" + other + "=" + result);

            return result;
        }
    }

    public AbstractBasicRegion moved_in(AbstractCurve newCont) {
        TreeSet<AbstractCurve> conts = new TreeSet<>(theInSet);
        conts.add(newCont);
        return AbstractBasicRegion.get(conts);
    }

    /**
     * Returns true if this zone contains the curve.
     *
     * @param curve the curve
     * @return true iff the curve in within this zone
     */
    public boolean contains(AbstractCurve curve) {
        return theInSet.contains(curve);
    }

    public double checksum() {
        double result = 0.0;
        double scaling = 3.1;
        for (AbstractCurve c : theInSet) {
            result += c.checksum() * scaling;
            scaling += 0.09;
        }
        return result;
    }

    public boolean isLabelEquivalent(AbstractBasicRegion z) {
        if (getNumContours() == z.getNumContours()) {
            if (z.getNumContours() == 0) {
                return true;
            } else {
                //System.out.println(" compare zones "+toDebugString()+" and "+z.toDebugString());
                Iterator<AbstractCurve> acIt = getContourIterator();
                AcItLoop:
                while (acIt.hasNext()) {
                    AbstractCurve thisAC = acIt.next();
                    // look for an AbstractCurve in z with the same label
                    Iterator<AbstractCurve> acIt2 = z.getContourIterator();
                    while (acIt2.hasNext()) {
                        AbstractCurve thatAC = acIt2.next();
                        //System.out.println(" compare abstract contours "+thisAC.toDebugString()+" and "+thatAC.toDebugString());
                        if (thisAC.matches_label(thatAC)) {
                            //System.out.println(" got match ");
                            continue AcItLoop;
                        }
                    }
                    //System.out.println(" no match for "+thisAC.toDebugString());
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        theInSet.forEach(curve -> sb.append(curve).append(","));
        sb.append("}");

        int lastIndex = sb.lastIndexOf(",");
        if (lastIndex != -1) {
            sb.deleteCharAt(lastIndex);
        }

        return sb.toString();
    }
}
