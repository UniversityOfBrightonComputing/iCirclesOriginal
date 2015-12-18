package icircles.abstractdescription;

/**
 * Represents a curve at an abstract level.
 * The curve has a label and a unique ID.
 */
public class AbstractCurve implements Comparable<AbstractCurve> {

    private static int uniqueId = 0;
    private final CurveLabel label;
    private final int id;

    public static void resetIdCounter() {
        uniqueId = 0;
    }

    /**
     * Constructs an abstract curve with given label.
     *
     * @param label the curve label
     */
    public AbstractCurve(CurveLabel label) {
        uniqueId++;
        id = uniqueId;
        this.label = label;
    }

    /**
     * Copy constructor. The created abstract curve
     * will have same curve label but different id.
     *
     * @param curve other curve
     */
    public AbstractCurve(AbstractCurve curve) {
        this(curve.label);
    }

    /**
     * @return curve label
     */
    public CurveLabel getLabel() {
        return label;
    }

    @Override
    public int compareTo(AbstractCurve other) {
        int tmp = label.compareTo(other.label);
        if (tmp != 0) {
            return tmp;
        }

        return (id < other.id) ? -1 : (id == other.id) ? 0 : 1;
    }

    public boolean matches_label(AbstractCurve c) {
        return label == c.label;
    }

    public double checksum() {
        return label.checksum() * id;
    }

    @Override
    public String toString() {
        return label.getLabel();
    }

    public String toDebugString() {
        return "[id=" + id + ",label=" + label.getLabel() + "]";
    }
}
