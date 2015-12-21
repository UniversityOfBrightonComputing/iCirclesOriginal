package icircles.abstractdescription;

import java.util.*;

/**
 * An AbstractDescription encapsulates the elements of a diagram, with no drawn information.
 * A diagram comprises a set of AbstractCurves (the contours).
 * A set of AbstractBasicRegions is given (zones which must be present.
 * <p>
 * An AbstractDiagram is consistent if 
 * <p>1. the contours in each of the AbstractBasicRegions match those
 * in contours.
 * <p>2. every valid diagram includes the "outside" zone. 
 * TODO add a coherence check on these internal checks.
 */
public class AbstractDescription {

    // TODO: immutable data structure?
    private TreeSet<AbstractCurve> contours;
    private TreeSet<AbstractBasicRegion> zones;

    /**
     * Constructs abstract description from the set of contours and set of zones.
     *
     * @param contours the contours
     * @param zones the zones
     */
    public AbstractDescription(Set<AbstractCurve> contours, Set<AbstractBasicRegion> zones) {
        this.contours = new TreeSet<>(contours);
        this.zones = new TreeSet<>(zones);
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

        // add the outside zone
        ad_zones.add(AbstractBasicRegion.get(new TreeSet<>()));

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

        this.contours = new TreeSet<>(contours.values());
        this.zones = new TreeSet<>(ad_zones);
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

    public Iterator<AbstractCurve> getContourIterator() {
        return contours.iterator();
    }

    public Iterator<AbstractBasicRegion> getZoneIterator() {
        return zones.iterator();
    }

    public TreeSet<AbstractCurve> getCopyOfContours() {
        return new TreeSet<>(contours);
    }

    public TreeSet<AbstractBasicRegion> getCopyOfZones() {
        return new TreeSet<>(zones);
    }

    public int getNumContours() {
        return contours.size();
    }

    public int getNumZones() {
        return zones.size();
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
