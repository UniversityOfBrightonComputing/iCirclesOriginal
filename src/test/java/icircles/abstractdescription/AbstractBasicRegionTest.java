package icircles.abstractdescription;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractBasicRegionTest {

    private AbstractBasicRegion zone1;
    private AbstractBasicRegion zone2;
    private AbstractBasicRegion zone3;

    @Before
    public void setUp() {
        zone1 = new AbstractBasicRegion(new AbstractCurve("a"));
        zone2 = new AbstractBasicRegion(new AbstractCurve("a"), new AbstractCurve("b"));
        zone3 = new AbstractBasicRegion(new AbstractCurve("a"));
    }

    @Test
    public void testGetCopyOfContours() throws Exception {

    }

    @Test
    public void testGetNumContours() throws Exception {
        assertEquals(1, zone1.getNumContours());
        assertEquals(2, zone2.getNumContours());
        assertEquals(1, zone3.getNumContours());
    }

    @Test
    public void testContains() throws Exception {
        assertTrue(zone1.containsCurveWithLabel("a"));
        assertTrue(zone2.containsCurveWithLabel("a"));
        assertTrue(zone3.containsCurveWithLabel("a"));

        assertFalse(zone1.containsCurveWithLabel("b"));
        assertTrue(zone2.containsCurveWithLabel("a"));
        assertFalse(zone3.containsCurveWithLabel("b"));
    }

    @Test
    public void testIsLabelEquivalent() throws Exception {
        assertTrue(zone1.isLabelEquivalent(zone3));
        assertTrue(zone3.isLabelEquivalent(zone1));

        assertFalse(zone1.isLabelEquivalent(zone2));
        assertFalse(zone2.isLabelEquivalent(zone1));

        assertFalse(zone2.isLabelEquivalent(zone3));
    }

    @Test
    public void testToString() throws Exception {
        assertNotEquals(zone1.toString(), zone2.toString());
        assertEquals(zone1.toString(), zone3.toString());

        assertEquals("{a}", zone1.toString());
        assertEquals("{a,b}", zone2.toString());
    }
}