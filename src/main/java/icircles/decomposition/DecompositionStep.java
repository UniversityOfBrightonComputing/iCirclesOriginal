package icircles.decomposition;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import icircles.util.DEB;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractBasicRegion;

public class DecompositionStep {

    private AbstractDescription from;
    private AbstractDescription to;
    private TreeMap<AbstractBasicRegion, AbstractBasicRegion> zonesMoved;

    /**
     * The curve that was removed in this step.
     * In other words it was in "from" but not in "to".
     */
    private AbstractCurve removed;

    /**
     * Constructs a new decomposition step.
     *
     * @param from the abstract description before this step
     * @param to   the abstract description after this step
     * @param zonesMoved ???
     * @param removed the curve that was removed in this step
     */
    public DecompositionStep(
            AbstractDescription from,
            AbstractDescription to,
            TreeMap<AbstractBasicRegion, AbstractBasicRegion> zonesMoved, // could be derived from from + to
            AbstractCurve removed) // could be derived from from + to
    {
        this.from = from;
        this.to = to;
        this.zonesMoved = zonesMoved;
        this.removed = removed;
    }

    public String debug() {
        if (DEB.level == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("remove ");
        sb.append(from.print_contour(removed));
        if (DEB.level > 1) {
            sb.append("\n");
        }
        sb.append(" from ");
        sb.append(from.debugAsSentence());
        if (DEB.level > 1) {
            sb.append("\n");
        }
        sb.append(" to ");
        sb.append(to.debugAsSentence());
        if (DEB.level > 1) {
            sb.append("\n");
        }
        sb.append(" zonesMoved: ");
        Set<Map.Entry<AbstractBasicRegion, AbstractBasicRegion>> entries = zonesMoved.entrySet();
        for (Map.Entry<AbstractBasicRegion, AbstractBasicRegion> z_map : entries) {
            sb.append("[");
            sb.append(z_map.getKey().debug());
            sb.append("->");
            sb.append(z_map.getValue().debug());
            sb.append("]");
        }
        return sb.toString();
    }

    public AbstractDescription to() {
        return to;
    }

    public AbstractDescription from() {
        return from;
    }

    public TreeMap<AbstractBasicRegion, AbstractBasicRegion> zonesMoved() {
        return zonesMoved;
    }

    public AbstractCurve removed() {
        return removed;
    }

    private double checksum() {
        return 1.1 * from.checksum() + 1.3 * to.checksum();
    }

    public static double checksum(ArrayList<DecompositionStep> d_steps) {
        double scaling = 1.11;
        double result = 0.0;
        for (DecompositionStep step : d_steps) {
            result += step.checksum() * scaling;
            scaling += 0.1;
        }
        return result;
    }
}
