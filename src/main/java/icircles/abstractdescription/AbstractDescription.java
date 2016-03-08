package icircles.abstractdescription;

import java.util.*;
import java.util.stream.Collectors;

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
 *
 * <p>
 *     <b>Immutable.</b>
 * </p>
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
        // unmodifiable collections are read-through,
        // so we wrap given sets with our own to ensure immutability
        this.curves = Collections.unmodifiableSortedSet(new TreeSet<>(curves));
        this.zones = Collections.unmodifiableSortedSet(new TreeSet<>(zones));

        //validate();
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
        SortedSet<AbstractBasicRegion> tmpZones = new TreeSet<>();
        //tmpZones.add(AbstractBasicRegion.OUTSIDE);

        Map<String, AbstractCurve> curves = new HashMap<>();

        for (String zoneName : informalDescription.split(" +")) {
            Set<AbstractCurve> zoneCurves = new TreeSet<>();

            for (char c : zoneName.toCharArray()) {
                String label = String.valueOf(c);

                // we have to do this because of curve id equality
                if (!curves.containsKey(label)) {
                    curves.put(label, new AbstractCurve(label));
                }
                zoneCurves.add(curves.get(label));
            }

            //tmpZones.add(AbstractBasicRegion.get(zoneCurves));
        }
        
        this.curves = Collections.unmodifiableSortedSet(new TreeSet<>(curves.values()));
        this.zones = Collections.unmodifiableSortedSet(tmpZones);

        //validate();
    }

    private void validate() {
        // Condition 1
//        for (AbstractBasicRegion zone : zones) {
//            for (AbstractCurve curve : zone.getCurvesUnmodifiable()) {
//                if (!curves.contains(curve)) {
//                    throw new IllegalArgumentException("Invalid AbstractDescription (Condition1): " + toDebugString());
//                }
//            }
//        }
//
//        // Condition 2
//        if (!zones.contains(AbstractBasicRegion.OUTSIDE))
//            throw new IllegalArgumentException("Invalid AbstractDescription (Condition2): " + toDebugString());
//
//        // Condition 3
//        curveLoop:
//        for (AbstractCurve curve : curves) {
//            for (AbstractBasicRegion zone : zones) {
//                if (zone.contains(curve))
//                    continue curveLoop;
//            }
//
//            throw new IllegalArgumentException("Invalid AbstractDescription (Condition3): " + toDebugString());
//        }
    }

    // these are needed for alphabetic / reverse decomposition
    // TODO: do we need those strategies?
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
//        for (AbstractBasicRegion zone : zones) {
//            for (AbstractCurve curve : zone.getCurvesUnmodifiable()) {
//                sb.append(curve.getLabel());
//            }
//
//            sb.append(" ");
//        }

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
     * @param curve the curve
     * @return sorted set of zones the given curve passes through
     */
    public Set<AbstractBasicRegion> getZonesIn(AbstractCurve curve) {
        return zones.stream().filter(z -> z.contains(curve)).sorted().collect(Collectors.toSet());
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

    public boolean includesLabel(String label) {
        for (AbstractCurve curve : curves) {
            if (curve.hasLabel(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * In the original abstract description there can only be 1 curve with given label.
     * Hence if the label exists, the correct curve is returned.
     * This is NOT true for the generated (actual) abstract description,
     * because of split curves we have more than 1 curve with same label.
     *
     * @param label curve label
     * @return curve or {@link Optional#empty()} if no such label is in description
     */
    public Optional<AbstractCurve> getCurveByLabel(String label) {
        for (AbstractCurve curve : curves) {
            if (curve.hasLabel(label)) {
                return Optional.of(curve);
            }
        }

        return Optional.empty();
    }

    public boolean hasLabelEquivalentZone(AbstractBasicRegion z) {
//        for (AbstractBasicRegion zone : zones) {
//            if (zone.isLabelEquivalent(z)) {
//                return true;
//            }
//        }
        return false;
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder("AD[curves=");
        //curves.forEach(curve -> sb.append(curve.toDebugString()).append(","));

        int lastIndex = sb.lastIndexOf(",");
        if (lastIndex != -1) {
            sb.deleteCharAt(lastIndex);
        }

        return sb.append(",zones=").append(zones).append("]").toString();
    }

    @Override
    public String toString() {
        List<String> zoneLabels = zones.stream()
                .map(AbstractBasicRegion::toString)
                .collect(Collectors.toList());

        return String.join(",", zoneLabels);
    }

    // TODO: what do we mean by has same?
    // original and actual are different, so are informal and rest
    public boolean hasSameAbstractDescription(AbstractDescription description) {
        return toString().equals(description.toString());
    }
}
