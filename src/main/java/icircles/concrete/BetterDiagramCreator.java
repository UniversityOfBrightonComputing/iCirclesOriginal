package icircles.concrete;

import icircles.decomposition.DecompositionStep;
import icircles.recomposition.RecompositionStep;

import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class BetterDiagramCreator extends DiagramCreator {
    public BetterDiagramCreator(List<DecompositionStep> d_steps, List<RecompositionStep> r_steps) {
        super(d_steps, r_steps);

        // this is a specific solution to "a b ab ac bc abc" which originally results in a split curve
        // can we generify the solution?

//        RecompositionStep step1 = rSteps.get(1);
//
//        List<AbstractBasicRegion> splitZones = new AbstractDescription("a b ab").getCopyOfZones().stream().collect(Collectors.toList());
//        List<AbstractBasicRegion> newZones = new AbstractDescription("c ac bc abc").getCopyOfZones().stream().filter(z -> !z.getCopyOfContours().isEmpty()).collect(Collectors.toList());
//
//        RecompositionData data = new RecompositionData(new AbstractCurve(CurveLabel.get("c")), splitZones, newZones);
//
//        rSteps.remove(2);
//        RecompositionStep step2 = new RecompositionStep(step1.to(), new AbstractDescription("a b ab ac bc abc c"),
//                Arrays.asList(data));
//        rSteps.add(step2);
//
//        System.out.println(step2);
    }
}
