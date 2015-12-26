package icircles.concrete;

import icircles.decomposition.DecompositionStep;
import icircles.recomposition.RecompositionStep;

import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
//public class BetterDiagramCreator extends DiagramCreator {
//    public BetterDiagramCreator(List<DecompositionStep> d_steps, List<RecompositionStep> r_steps) {
//        super();
//
//        // this is a specific solution to "a b ab ac bc abc" which originally results in a split curve
//        // can we generify the solution?
//
////        RecompositionStep step1 = rSteps.get(1);
////
////        List<AbstractBasicRegion> splitZones = new AbstractDescription("a b ab").getZonesShallowCopy().stream().collect(Collectors.toList());
////        List<AbstractBasicRegion> newZones = new AbstractDescription("c ac bc abc").getZonesShallowCopy().stream().filter(z -> !z.getCopyOfContours().isEmpty()).collect(Collectors.toList());
////
////        RecompositionData data = new RecompositionData(new AbstractCurve(CurveLabel.get("c")), splitZones, newZones);
////
////        rSteps.remove(2);
////        RecompositionStep step2 = new RecompositionStep(step1.to(), new AbstractDescription("a b ab ac bc abc c"),
////                Arrays.asList(data));
////        rSteps.add(step2);
////
////        System.out.println(step2);
//
//
//
//
//        // solution for a ab abc ac bc abd ad
//        // but iCircles currently doesn't know how to add with TP (triple point) so it adds like a 2-piercing
//
////        RecompositionStep step1 = rSteps.get(1);
////
////        List<AbstractBasicRegion> splitZones = new AbstractDescription("a c ac").getZonesShallowCopy().stream().collect(Collectors.toList());
////        List<AbstractBasicRegion> newZones = new AbstractDescription("ab bc abc").getZonesShallowCopy().stream().filter(z -> !z.getCopyOfContours().isEmpty()).collect(Collectors.toList());
////
////        RecompositionData data = new RecompositionData(new AbstractCurve("b"), splitZones, newZones);
////
////        RecompositionStep step2 = new RecompositionStep(step1.to(), new AbstractDescription("a c ac ab bc abc"),
////                Arrays.asList(data));
////        rSteps.set(2, step2);
//
//
//
//    }
//}
