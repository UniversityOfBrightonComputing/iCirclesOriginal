package icircles.abstractdescription;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Represents a zone (basic region) at an abstract level.
 * Holds curves that pass through or are inside this zone.
 */
public class AbstractBasicRegion implements Comparable<AbstractBasicRegion> {

    private static final Logger log = LogManager.getLogger(AbstractBasicRegion.class);

    private Set<AbstractCurve> theInSet;
    private static Set<AbstractBasicRegion> library = new TreeSet<>();

    private AbstractBasicRegion(SortedSet<AbstractCurve> in_set) {
        theInSet = Collections.unmodifiableSortedSet(in_set);
    }

    // TODO: test this to become the normal ctor
    public AbstractBasicRegion(AbstractCurve... curves) {
        this(new TreeSet<>(Arrays.asList(curves)));
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

        AbstractBasicRegion result = new AbstractBasicRegion(new TreeSet<>(in_set));
        library.add(result);
        return result;
    }

    public static final AbstractBasicRegion OUTSIDE = get(new TreeSet<>());

    public AbstractBasicRegion moveInside(AbstractCurve newCont) {
        TreeSet<AbstractCurve> conts = new TreeSet<>(theInSet);
        conts.add(newCont);
        return get(conts);
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

    /**
     * @return unmodifiable set of curves ('in' set of this zone)
     */
    public Set<AbstractCurve> getCurvesUnmodifiable() {
        return theInSet;
    }

    /**
     * @return number of curves within this zone
     */
    public int getNumCurves() {
        return theInSet.size();
    }

    /**
     * Returns true if this zone contains the curve.
     *
     * @param curve the curve
     * @return true iff the curve is within this zone
     */
    public boolean contains(AbstractCurve curve) {
        return theInSet.contains(curve);
    }

    /**
     * @param label the label
     * @return true if the zone contains the curve with given label.
     */
    public boolean containsCurveWithLabel(String label) {
        for (AbstractCurve curve : theInSet) {
            if (curve.hasLabel(label)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the other zone is topologically adjacent to this zone.
     * If that is the case the difference curve is returned else {@link Optional#empty()}.
     *
     * @param other the other zone
     * @return curve if zones are a cluster else {@link Optional#empty()}.
     */
    public Optional<AbstractCurve> getStraddledContour(AbstractBasicRegion other) {
        int nc = getNumCurves();
        int othernc = other.getNumCurves();

        if (Math.abs(nc - othernc) != 1) {
            return Optional.empty();
        } else if (nc < othernc) {
            // delegate the computation to the other since it has 1 more contour
            return other.getStraddledContour(this);
        } else {
            // we have one more contour than other - are we neighbours?
            AbstractCurve result = null;
            for (AbstractCurve curve : theInSet) {
                if (!other.contains(curve)) {
                    if (result == null) {
                        // found first curve not in other
                        result = curve;
                    } else {
                        // found second curve not in other, so we are not topologically adjacent
                        return Optional.empty();
                    }
                }
            }

            log.trace("straddle: " + this + "->" + other + "=" + result);

            // we have 1 more contour than other, so there is at least 1 contour not in other
            // therefore we can guarantee that result != null
            return Optional.of(result);
        }
    }

    public boolean isLabelEquivalent(AbstractBasicRegion other) {
        // TODO: if we didn't have id for curve we couldve used more natural
        //return theInSet.equals(other.theInSet);

        if (getNumCurves() == other.getNumCurves()) {
            // TODO: we could check for this == OUTSIDE, would be clearer
            if (other.getNumCurves() == 0) {
                return true;
            } else {

                outerLoop:
                for (AbstractCurve curve1 : theInSet) {
                    for (AbstractCurve curve2 : other.theInSet) {
                        if (curve1.matchesLabel(curve2)) {
                            continue outerLoop;
                        }
                    }

                    return false;
                }

                return true;
            }
        }
        return false;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof AbstractBasicRegion))
            return false;

        AbstractBasicRegion other = (AbstractBasicRegion) obj;
        return isLabelEquivalent(other);
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
