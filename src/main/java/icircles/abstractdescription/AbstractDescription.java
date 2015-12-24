package icircles.abstractdescription;

import java.util.*;

/**
 * An AbstractDescription encapsulates the elements of a diagram, with no drawn information.
 * A diagram comprises a set of AbstractCurves (the contours).
 * A set of AbstractBasicRegions is given (zones which must be present).
 * <p>
 * An AbstractDiagram is consistent if 
 * <p>1. the contours in each of the AbstractBasicRegions match those
 * in contours.
 * <p>2. every valid diagram includes the "outside" zone. 
 * TODO add a coherence check on these internal checks.
 */
public class AbstractDescription {

    private final String informalDescription;

    private final SortedSet<AbstractCurve> contours;
    private final SortedSet<AbstractBasicRegion> zones;

    /**
     * Constructs abstract description from the set of contours and set of zones.
     *
     * @param contours the contours
     * @param zones the zones
     */
    public AbstractDescription(Set<AbstractCurve> contours, Set<AbstractBasicRegion> zones) {
        this.contours = Collections.unmodifiableSortedSet(new TreeSet<>(contours));
        this.zones = Collections.unmodifiableSortedSet(new TreeSet<>(zones));

        StringBuilder sb = new StringBuilder();
        for (AbstractBasicRegion zone : zones) {
            for (AbstractCurve curve : zone.getCopyOfContours()) {
                sb.append(curve.getLabel());
            }

            sb.append(" ");
        }

        informalDescription = sb.toString().trim();
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
        // TODO: this description is not sorted as in the above ctor
        this.informalDescription = informalDescription;

        TreeSet<AbstractBasicRegion> ad_zones = new TreeSet<>();

        // add the outside zone
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

        this.contours = Collections.unmodifiableSortedSet(new TreeSet<>(contours.values()));
        this.zones = Collections.unmodifiableSortedSet(new TreeSet<>(ad_zones));
    }

    public AbstractCurve getFirstContour() {
        if (contours.size() == 0) {
            return null;
        }
        return contours.first();
    }

    public AbstractCurve getLastContour() {
        if (contours.size() == 0) {
            return null;
        }
        return contours.last();
    }

    /**
     * @return abstract description in informal string form
     */
    public String getInformalDescription() {
        return informalDescription;
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
        return contours;
    }

    /**
     * @return number of abstract contours (curves)
     */
    public int getNumContours() {
        return contours.size();
    }

    public double checksum() {
        double scaling = 2.1;
        double result = 0.0;
        for (AbstractCurve c : contours) {
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
        for (AbstractCurve curve : contours) {
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
        contours.forEach(curve -> sb.append(curve.toDebugString()).append(","));

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
