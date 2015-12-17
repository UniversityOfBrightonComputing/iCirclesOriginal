package icircles.recomposition;

import java.util.Iterator;
import java.util.List;

import icircles.util.DEB;
import icircles.abstractdescription.CurveLabel;
import icircles.abstractdescription.AbstractDescription;

public class RecompositionStep {

    private AbstractDescription from;
    private AbstractDescription to;
    private List<RecompData> addedContourData;

    public RecompositionStep(AbstractDescription from,
            AbstractDescription to,
            List<RecompData> added_contour_data) {
        this.from = from;
        this.to = to;
        addedContourData = added_contour_data;

        DEB.assertCondition(added_contour_data.size() > 0, "no added curve in recomp");
        CurveLabel cl = added_contour_data.get(0).added_curve.getLabel();
        for (RecompData rp : added_contour_data) {
            DEB.assertCondition(rp.added_curve.getLabel() == cl, "mixed curves added in recomp");
        }

        DEB.assertCondition(!from.includesLabel(cl), "added curve already present");
        DEB.assertCondition(to.includesLabel(cl), "added curve wasn't added");
    }

    public AbstractDescription to() {
        return to;
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

    public Iterator<RecompData> getRecompIterator() {
        return addedContourData.iterator();
    }

    @Override
    public String toString() {
        return "R_Step[From=" + from + " To=" + to + "]";
    }
}
