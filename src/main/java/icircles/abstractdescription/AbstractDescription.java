package icircles.abstractdescription;

import java.util.*;

/**
 * An AbstractDescription encapsulates the elements of a diagram, with no drawn information.
 * A diagram comprises a set of AbstractCurves (the curves) and
 * a set of AbstractBasicRegions (zones which must be present).
 * <p>
 * An AbstractDescription is consistent if
 *
 * <ol>
 *     <li>The curves in each of the AbstractBasicRegions match those in the curves set.</li>
 *     <li>Every valid diagram includes the "outside" zone ({@link AbstractBasicRegion#OUTSIDE}).</li>
 *     <li>Every curve must have a zone inside it (label).</li>
 * </ol>
 */
public class AbstractDescription {

    private final SortedSet<AbstractCurve> curves;
    private final SortedSet<AbstractBasicRegion> zones;

    /**
     * Constructs abstract description from the set of curves and set of zones.
     *
     * @param curves the curves
     * @param zones the zones
     */
    public AbstractDescription(Set<AbstractCurve> curves, Set<AbstractBasicRegion> zones) {
        this.curves = Collections.unmodifiableSortedSet(new TreeSet<>(curves));
        this.zones = Collections.unmodifiableSortedSet(new TreeSet<>(zones));

        validate();
    }

    /**
     * Constructs abstract description from informal description in the string form.
     * Empty set (the outside zone) is always implicitly present.
     * <p>
     *     Example:
     *     "a b c ab ac bc abc" is Venn3
     * </p>
     *
     * @param informalDescription abstract description in informal form
     */
    public AbstractDescription(String informalDescription) {
        TreeSet<AbstractBasicRegion> ad_zones = new TreeSet<>();
        ad_zones.add(AbstractBasicRegion.OUTSIDE);

        StringTokenizer st = new StringTokenizer(informalDescription);
        Map<String, AbstractCurve> contours = new HashMap<>();

        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            TreeSet<AbstractCurve> zoneContours = new TreeSet<>();

            for (int i = 0; i < word.length(); i++) {
                String label = "" + word.charAt(i);
                if (!contours.containsKey(label)) {
                    contours.put(label, new AbstractCurve(label));
                }
                zoneContours.add(contours.get(label));
            }
            ad_zones.add(AbstractBasicRegion.get(zoneContours));
        }

        this.curves = Collections.unmodifiableSortedSet(new TreeSet<>(contours.values()));
        this.zones = Collections.unmodifiableSortedSet(new TreeSet<>(ad_zones));

        validate();
    }

    private void validate() {
        // Condition 1
        for (AbstractBasicRegion zone : zones) {
            for (AbstractCurve curve : zone.getCurvesUnmodifiable()) {
                if (!curves.contains(curve)) {
                    throw new IllegalArgumentException("Invalid AbstractDescription (Condition1): " + toDebugString());
                }
            }
        }

        // Condition 2
        if (!zones.contains(AbstractBasicRegion.OUTSIDE))
            throw new IllegalArgumentException("Invalid AbstractDescription (Condition2): " + toDebugString());

        // Condition 3
        curveLoop:
        for (AbstractCurve curve : curves) {
            for (AbstractBasicRegion zone : zones) {
                if (zone.contains(curve))
                    continue curveLoop;
            }

            throw new IllegalArgumentException("Invalid AbstractDescription (Condition3): " + toDebugString());
        }
    }

    public AbstractCurve getFirstContour() {
        if (curves.size() == 0) {
            return null;
        }
        return curves.first();
    }

    public AbstractCurve getLastContour() {
        if (curves.size() == 0) {
            return null;
        }
        return curves.last();
    }

    /**
     * @return abstract description in informal string form
     */
    public String getInformalDescription() {
        StringBuilder sb = new StringBuilder();
        for (AbstractBasicRegion zone : zones) {
            for (AbstractCurve curve : zone.getCurvesUnmodifiable()) {
                sb.append(curve.getLabel());
            }

            sb.append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Returns unmodifiable set of zones of this abstract description.
     * The returned set is read-only. Use this to query/iterate over zones.
     * Note: the zones themselves are still mutable.
     *
     * @return unmodifiable set of zones
     */
    public Set<AbstractBasicRegion> getZonesUnmodifiable() {
        return zones;
    }

    /**
     * @return number of abstract zones, including the outside zone
     */
    public int getNumZones() {
        return zones.size();
    }

    /**
     * @param curve the curve
     * @return number of zones the given curves passes through
     */
    public int getNumZonesIn(AbstractCurve curve) {
        return (int) zones.stream().filter(z -> z.contains(curve)).count();
    }

    /**
     * Returns unmodifiable set of abstract curves of this abstract description.
     * The returned set is read-only. Use this to query/iterate over curves.
     *
     * @return unmodifiable set of abstract curves
     */
    public Set<AbstractCurve> getCurvesUnmodifiable() {
        return curves;
    }

    /**
     * @return number of abstract curves (curves)
     */
    public int getNumContours() {
        return curves.size();
    }

    public double checksum() {
        double scaling = 2.1;
        double result = 0.0;
        for (AbstractCurve c : curves) {
            result += c.checksum() * scaling;
            scaling += 0.07;
            scaling += 0.05;
            for (AbstractBasicRegion z : zones) {
                if (z.contains(c)) {
                    result += z.checksum() * scaling;
                    scaling += 0.09;
                }
            }
        }
        return result;
    }

    public boolean includesLabel(String label) {
        for (AbstractCurve curve : curves) {
            if (curve.hasLabel(label)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLabelEquivalentZone(AbstractBasicRegion z) {
        for (AbstractBasicRegion zone : zones) {
            if (zone.isLabelEquivalent(z)) {
                return true;
            }
        }
        return false;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder("AD[curves=");
        curves.forEach(curve -> sb.append(curve.toDebugString()).append(","));

        int lastIndex = sb.lastIndexOf(",");
        if (lastIndex != -1) {
            sb.deleteCharAt(lastIndex);
        }

        return sb.append(",zones=").append(zones).append("]").toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        zones.forEach(zone -> sb.append(zone).append(","));

        int lastIndex = sb.lastIndexOf(",");
        if (lastIndex != -1) {
            sb.deleteCharAt(lastIndex);
        }

        return sb.toString();
    }

    public boolean hasSameAbstractDescription(AbstractDescription description) {
        return toString().equals(description.toString());
    }
}
