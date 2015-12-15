package icircles.recomposition;

import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdescription.CurveLabel;
import icircles.decomposition.Decomposer;
import icircles.decomposition.DecompositionStep;
import icircles.util.DEB;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class RecomposerTest {

    private Recomposer recomposer;

    @Before
    public void setUp() {
        recomposer = new Recomposer(RecompositionStrategy.RECOMPOSE_DOUBLY_PIERCED);
    }

    @Test
    public void recompose() {
        ArrayList<DecompositionStep> decompositionSteps = new Decomposer().decompose(AbstractDescription.makeForTesting("a b ab"));
        List<RecompositionStep> steps = recomposer.recompose(decompositionSteps);

        // 0 + b -> b
        // b + a -> a b ab
        assertEquals(2, steps.size());

        RecompositionStep step1 = steps.get(0);
        assertTrue(step1.to().hasSameAbstractDescription(AbstractDescription.makeForTesting("b")));

        RecompositionStep step2 = steps.get(1);
        assertTrue(step2.to().hasSameAbstractDescription(AbstractDescription.makeForTesting("a b ab")));
    }
}
