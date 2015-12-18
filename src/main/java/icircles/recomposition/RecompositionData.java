package icircles.recomposition;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;

import java.util.List;

/**
 * Holds data for a single recomposition step.
 */
public class RecompositionData {

    /**
     * The curve added at this step.
     */
    public AbstractCurve addedCurve;

    /**
     * The zones we split at this step. The zones are
     * in the "from" abstract description.
     */
    public List<AbstractBasicRegion> splitZones;

    /**
     * The zones we created at this step. The zones are
     * in the "to" abstract description.
     */
    public List<AbstractBasicRegion> newZones;

    public RecompositionData(AbstractCurve addedCurve, List<AbstractBasicRegion> splitZones, List<AbstractBasicRegion> newZones) {
        this.addedCurve = addedCurve;
        this.splitZones = splitZones;
        this.newZones = newZones;
    }

    /**
     * Returns true if we split just 1 zone, so a nested curve.
     *
     * @return true iff the added curve is nested
     */
    public boolean isNested() {
        return splitZones.size() == 1;
    }

    /**
     * Returns true if we split 2 zones, so a single piercing.
     *
     * @return true iff the added curve is a single piercing
     */
    public boolean isSinglePiercing() {
        return splitZones.size() == 2;
    }

    @Override
    public String toString() {
        return "R_Data[added=" + addedCurve + ",split=" + splitZones + ",new=" + newZones + "]";
    }
}
