package icircles.abstractdescription;

/**
 * Represents a curve at an abstract level.
 * The curve has a label and a unique ID.
 */
public class AbstractCurve implements Comparable<AbstractCurve> {

    private static int uniqueId = 0;
    private final String label;
    private final int id;

    public static void resetIdCounter() {
        uniqueId = 0;
    }

    /**
     * Constructs an abstract curve with given label.
     *
     * @param label the curve label
     */
    public AbstractCurve(String label) {
        // TODO: check for bad labels

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
    public String getLabel() {
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

    /**
     * @param c other cure
     * @return true iff given curve's label is the same as this curve's
     */
    public boolean matchesLabel(AbstractCurve c) {
        return label.equals(c.label);
    }

    /**
     * @param label the label
     * @return true iff the curve has given label
     */
    public boolean hasLabel(String label) {
        return this.label.equals(label);
    }

    public double checksum() {
        double result = 0.0;
        double scaling = 1.1;
        for (int i = 0; i < label.length(); i++) {
            result += (int) (label.charAt(i)) * scaling;
            scaling += 0.01;
        }

        return result * id;
    }

//    /**
//     * Checks if the same object.
//     *
//     * @param obj other object
//     * @return true iff both are the same object
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof AbstractCurve))
//            return false;
//
//        AbstractCurve other = (AbstractCurve) obj;
//        return label.equals(other.label) && id == other.id;
//    }
//


//    @Override
//    public boolean equals(Object obj) {
//        return matchesLabel((AbstractCurve)(obj));
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(label);
//    }

    @Override
    public String toString() {
        return label;
    }

    public String toDebugString() {
        return "[id=" + id + ",label=" + label + "]";
    }
}
