package icircles.decomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;

import java.util.List;
import java.util.Map;

public class DecompositionStep {

    private AbstractDescription from;
    private AbstractDescription to;
    private Map<AbstractBasicRegion, AbstractBasicRegion> zonesMoved;

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
     * @param zonesMoved maps zones before to zones after (only contains altered zones)
     * @param removed the curve that was removed in this step
     */
    public DecompositionStep(
            AbstractDescription from,
            AbstractDescription to,
            Map<AbstractBasicRegion, AbstractBasicRegion> zonesMoved, // could be derived from from + to
            AbstractCurve removed) // could be derived from from + to
    {
        this.from = from;
        this.to = to;
        this.zonesMoved = zonesMoved;
        this.removed = removed;
    }

    public AbstractDescription from() {
        return from;
    }

    public AbstractDescription to() {
        return to;
    }

    public Map<AbstractBasicRegion, AbstractBasicRegion> zonesMoved() {
        return zonesMoved;
    }

    public AbstractCurve removed() {
        return removed;
    }

    private double checksum() {
        return 1.1 * from.checksum() + 1.3 * to.checksum();
    }

    public static double checksum(List<DecompositionStep> d_steps) {
        double scaling = 1.11;
        double result = 0.0;
        for (DecompositionStep step : d_steps) {
            result += step.checksum() * scaling;
            scaling += 0.1;
        }
        return result;
    }

    @Override
    public String toString() {
        return "D_Step[Removed=" + removed + ". From=" + from + " To=" + to +
                ". Zones Moved=" + zonesMoved.toString().replace("=", "->") + "]";
    }
}
