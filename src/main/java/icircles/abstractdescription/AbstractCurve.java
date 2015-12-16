package icircles.abstractdescription;

import icircles.util.DEB;

public class AbstractCurve implements Comparable<AbstractCurve> {

    private static int uniqueId = 0;
    private CurveLabel label;
    private int id;

    public AbstractCurve(CurveLabel label) {
        uniqueId++;
        id = uniqueId;
        this.label = label;
    }

    public AbstractCurve(AbstractCurve curve) {
        this(curve.label);
    }

    public CurveLabel getLabel() {
        return label;
    }

    @Override
    public int compareTo(AbstractCurve o) {
        int tmp = label.compareTo(o.label);
        if (tmp != 0) {
            return tmp;
        }
        int this_id = id;
        int other_id = o.id;
        return (this_id < other_id) ? -1 : (this_id == other_id) ? 0 : 1;
    }

    public String debug() {
        if (DEB.level == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean deb_level_was_high = false;
        if (DEB.level > 4) {
            sb.append("contour(");
            deb_level_was_high = true;
            DEB.level--;
        }
        sb.append(label.debug());
        if (deb_level_was_high) {
            DEB.level++;
            sb.append("_" + id + ")@");
            sb.append(hashCode());
        }
        return sb.toString();
    }

    public boolean matches_label(AbstractCurve c) {
        return label == c.label;
    }

    public String debugWithId() {
        return debug() + "_" + id;
    }

    public double checksum() {
        if (DEB.level == 2) {
            System.out.println("build checksum from " + label + " and " + id + "\n");
        }
        return label.checksum() * id;
    }

    public static void resetIdCounter() {
        uniqueId = 0;
    }

    @Override
    public String toString() {
        return label.getLabel();
    }
}
