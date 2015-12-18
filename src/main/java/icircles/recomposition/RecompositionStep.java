package icircles.recomposition;

import java.util.Iterator;
import java.util.List;

import icircles.abstractdescription.CurveLabel;
import icircles.abstractdescription.AbstractDescription;

public class RecompositionStep {

    private AbstractDescription from;
    private AbstractDescription to;
    private List<RecompositionData> addedContourData;

    public RecompositionStep(AbstractDescription from,
            AbstractDescription to,
            List<RecompositionData> added_contour_data) {
        this.from = from;
        this.to = to;
        addedContourData = added_contour_data;

        if (addedContourData.isEmpty()) {
            throw new IllegalArgumentException("No added curve in recomp");
        }

        CurveLabel cl = added_contour_data.get(0).addedCurve.getLabel();
        for (RecompositionData rp : added_contour_data) {
            if (rp.addedCurve.getLabel() != cl)
                throw new IllegalArgumentException("Mixed curves added in recomp");
        }

        if (from.includesLabel(cl))
            throw new IllegalArgumentException("Added curve already present");
        if (!to.includesLabel(cl))
            throw new IllegalArgumentException("Added curve not present in next description");
    }

    public AbstractDescription to() {
        return to;
    }

    public Iterator<RecompositionData> getRecompIterator() {
        return addedContourData.iterator();
    }

    public static double checksum(List<RecompositionStep> rSteps) {
        double scaling = 11.23;
        double result = 0.0;
        for (RecompositionStep step : rSteps) {
            result += step.checksum() * scaling;
            scaling += 0.1;
        }
        return result;
    }

    private double checksum() {
        return 7.1 * from.checksum() + 7.3 * to.checksum();
    }

    @Override
    public String toString() {
        return "R_Step[From=" + from + " To=" + to + " Data=" + addedContourData + "]";
    }
}
