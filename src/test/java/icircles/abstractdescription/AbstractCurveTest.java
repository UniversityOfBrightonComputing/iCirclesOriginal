package icircles.abstractdescription;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class AbstractCurveTest {

    @Test
    public void testToString() {
        AbstractCurve curve1 = new AbstractCurve("a");
        AbstractCurve curve2 = new AbstractCurve("b");
        AbstractCurve curve3 = new AbstractCurve("a");

        assertNotEquals(curve1.toString(), curve2.toString());
        assertEquals(curve1.toString(), curve3.toString());
    }

    @Test
    public void sameLabel() {
        AbstractCurve curve1 = new AbstractCurve("a");
        AbstractCurve curve2 = new AbstractCurve("b");
        AbstractCurve curve3 = new AbstractCurve("a");

        assertFalse(curve1.matchesLabel(curve2));
        assertTrue(curve1.matchesLabel(curve3));

        assertTrue(curve1.hasLabel("a"));
        assertFalse(curve2.hasLabel("a"));
        assertTrue(curve3.hasLabel("a"));

        assertEquals("a", curve1.getLabel());
        assertEquals(curve1.getLabel(), curve3.getLabel());
        assertNotEquals("a", curve2.getLabel());
    }
}
