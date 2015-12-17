package icircles.abstractdescription;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class AbstractCurveTest {

    @Test
    public void testToString() {
        AbstractCurve curve1 = new AbstractCurve(CurveLabel.get("a"));
        AbstractCurve curve2 = new AbstractCurve(CurveLabel.get("b"));
        AbstractCurve curve3 = new AbstractCurve(CurveLabel.get("a"));

        assertNotEquals(curve1.toString(), curve2.toString());
        assertEquals(curve1.toString(), curve3.toString());
    }
}
