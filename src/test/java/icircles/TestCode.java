package icircles;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.DiagramCreator;
import icircles.decomposition.*;
import icircles.recomposition.*;
import icircles.util.CannotDrawException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestCode {

    @Test
    public void runAllTests() {
        for (int i = 0; i < TestData.test_data.length; i++) {
            runTest(i);
        }
    }

    public void runTest(int testNumber) {
        TestDatum datum = TestData.test_data[testNumber];

        AbstractCurve.resetIdCounter();
        AbstractBasicRegion.clearLibrary();

        try {
            ArrayList<DecompositionStep> d_steps = new ArrayList<>();
            ArrayList<RecompositionStep> r_steps = new ArrayList<>();

            ConcreteDiagram cd = getDiagram(testNumber, d_steps, r_steps, 100); // fixed size for checksumming
            List<CircleContour> circles = cd.getCircles();

            double actualChecksum = DecompositionStep.checksum(d_steps)
                    + RecompositionStep.checksum(r_steps)
                    + ConcreteDiagram.checksum(circles);

            assertEquals("Test: " + testNumber, datum.expectedChecksum, actualChecksum, 0.0001);
        } catch (CannotDrawException e) {
            fail(e.getMessage());
        }
    }

    private static ConcreteDiagram getDiagram(int test_num,
            ArrayList<DecompositionStep> d_steps,
            ArrayList<RecompositionStep> r_steps,
            int size) throws CannotDrawException {
        DecompositionStrategyType decomp_strategy = TestData.test_data[test_num].decomp_strategy;
        RecompositionStrategyType recomp_strategy = TestData.test_data[test_num].recomp_strategy;

        Decomposer d = DecomposerFactory.newDecomposer(decomp_strategy);
        Recomposer r = RecomposerFactory.newRecomposer(recomp_strategy);

        DiagramCreator dc = new DiagramCreator(d, r);
        ConcreteDiagram diagram = dc.createDiagram(new AbstractDescription(TestData.test_data[test_num].description), size);

        d_steps.addAll(dc.getDSteps());
        r_steps.addAll(dc.getRSteps());

        return diagram;
    }
}
