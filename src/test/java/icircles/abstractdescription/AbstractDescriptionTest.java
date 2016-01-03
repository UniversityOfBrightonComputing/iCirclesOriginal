package icircles.abstractdescription;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class AbstractDescriptionTest {

    private AbstractDescription ad1, ad2, ad3;

    @Before
    public void setUp() {
        AbstractCurve.resetIdCounter();
        AbstractBasicRegion.clearLibrary();
    }

    private void manualSetUp() {
        ad1 = new AbstractDescription("a ab abc bc ac");
        ad2 = new AbstractDescription("abc bc ab ac a");
        ad3 = new AbstractDescription("a ad abc bc ac");
    }

    @Test
    public void testConstructorCondition1Valid() {
        Set<AbstractCurve> curves = new TreeSet<>();
        Set<AbstractBasicRegion> zones = new TreeSet<>();

        AbstractCurve curve1 = new AbstractCurve("a");
        AbstractCurve curve2 = new AbstractCurve("b");

        curves.add(curve1);
        curves.add(curve2);

        zones.add(AbstractBasicRegion.get(curves));
        zones.add(AbstractBasicRegion.OUTSIDE);

        // Condition 1 holds
        //curves.remove(curve2);

        new AbstractDescription(curves, zones);
    }

    @Test
    public void testConstructorCondition1Invalid() {
        Set<AbstractCurve> curves = new TreeSet<>();
        Set<AbstractBasicRegion> zones = new TreeSet<>();

        AbstractCurve curve1 = new AbstractCurve("a");
        AbstractCurve curve2 = new AbstractCurve("b");

        curves.add(curve1);
        curves.add(curve2);

        zones.add(AbstractBasicRegion.get(curves));

        // Condition 1 fails because we have zones with curves not in the curve set
        curves.remove(curve2);

        String error = "";
        try {
            new AbstractDescription(curves, zones);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        assertThat(error, containsString("Condition1"));
    }

    @Test
    public void testConstructorCondition2Invalid() {
        Set<AbstractCurve> curves = new TreeSet<>();
        Set<AbstractBasicRegion> zones = new TreeSet<>();

        AbstractCurve curve1 = new AbstractCurve("a");
        AbstractCurve curve2 = new AbstractCurve("b");

        curves.add(curve1);
        curves.add(curve2);

        zones.add(AbstractBasicRegion.get(curves));

        // Condition 2 fails because we have no outside zones
        //zones.add(AbstractBasicRegion.OUTSIDE);

        String error = "";
        try {
            new AbstractDescription(curves, zones);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        assertThat(error, containsString("Condition2"));
    }

    @Test
    public void testConstructorCondition3Invalid() {
        Set<AbstractCurve> curves = new TreeSet<>();
        Set<AbstractBasicRegion> zones = new TreeSet<>();

        AbstractCurve curve1 = new AbstractCurve("a");
        AbstractCurve curve2 = new AbstractCurve("b");
        AbstractCurve curve3 = new AbstractCurve("c");

        curves.add(curve1);
        curves.add(curve2);

        zones.add(AbstractBasicRegion.get(curves));
        zones.add(AbstractBasicRegion.OUTSIDE);

        // Condition 3 fails because we have curves but no corresponding zones
        curves.add(curve3);

        String error = "";
        try {
            new AbstractDescription(curves, zones);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        assertThat(error, containsString("Condition3"));
    }

    @Test
    public void testGetInformalDescription() {
        manualSetUp();
        assertEquals("a ab ac bc abc", ad1.getInformalDescription());
        assertEquals("a ab ac bc abc", ad2.getInformalDescription());
        assertEquals("a ac ad bc abc", ad3.getInformalDescription());
    }

    @Test
    public void testToString() {
        manualSetUp();
        assertEquals(ad1.toString(), ad2.toString());
        assertNotEquals(ad1.toString(), ad3.toString());

        assertEquals("{},{a},{a,b},{a,c},{b,c},{a,b,c}", ad1.toString());
        assertEquals("{},{a},{a,b},{a,c},{b,c},{a,b,c}", ad2.toString());
        assertEquals("{},{a},{a,c},{a,d},{b,c},{a,b,c}", ad3.toString());
    }

    @Test
    public void testNumZonesIn() {
        manualSetUp();
        assertEquals(4, ad1.getNumZonesIn(getCurve(ad1, "a")));
        assertEquals(3, ad1.getNumZonesIn(getCurve(ad1, "b")));
        assertEquals(3, ad1.getNumZonesIn(getCurve(ad1, "c")));
    }

    @Test
    public void testZonesIn() {
        manualSetUp();

        // A
        Set<AbstractBasicRegion> actualZonesInA = ad1.getZonesIn(getCurve(ad1, "a"));

        Set<AbstractBasicRegion> expectedZonesInA = new TreeSet<>();
        expectedZonesInA.add(getZone(ad1, "a"));
        expectedZonesInA.add(getZone(ad1, "ab"));
        expectedZonesInA.add(getZone(ad1, "ac"));
        expectedZonesInA.add(getZone(ad1, "abc"));

        assertEquals(expectedZonesInA, actualZonesInA);
        assertEquals(4, actualZonesInA.size());

        // B
        Set<AbstractBasicRegion> actualZonesInB = ad1.getZonesIn(getCurve(ad1, "b"));

        Set<AbstractBasicRegion> expectedZonesInB = new TreeSet<>();
        expectedZonesInB.add(getZone(ad1, "ab"));
        expectedZonesInB.add(getZone(ad1, "bc"));
        expectedZonesInB.add(getZone(ad1, "abc"));

        assertEquals(expectedZonesInB, actualZonesInB);
        assertEquals(3, actualZonesInB.size());

        // C
        Set<AbstractBasicRegion> actualZonesInC = ad1.getZonesIn(getCurve(ad1, "c"));

        Set<AbstractBasicRegion> expectedZonesInC = new TreeSet<>();
        expectedZonesInC.add(getZone(ad1, "ac"));
        expectedZonesInC.add(getZone(ad1, "bc"));
        expectedZonesInC.add(getZone(ad1, "abc"));

        assertEquals(expectedZonesInC, actualZonesInC);
        assertEquals(3, actualZonesInC.size());
    }

    private AbstractCurve getCurve(AbstractDescription description, String label) {
        for (AbstractCurve curve : description.getCurvesUnmodifiable()) {
            if (curve.hasLabel(label)) {
                return curve;
            }
        }

        throw new IllegalArgumentException("No curve with label: " + label + " in "
            + description);
    }

    private AbstractBasicRegion getZone(AbstractDescription description, String zoneLabel) {
        Set<AbstractCurve> curves = Arrays.stream(zoneLabel.split(""))
                .map(String::valueOf)
                .map(curveLabel -> getCurve(description, curveLabel))
                .sorted()
                .collect(Collectors.toSet());

        return AbstractBasicRegion.get(curves);
    }

    // Existing test, TODO: refactor as junit test

    /*
    public static void main(String args[])
    {

    CurveLabel a = CurveLabel.get("a");
    CurveLabel a2 = CurveLabel.get("a");

    Debug.level = 2;
    System.out.println("contour labels equal? "+a.toDebugString()+","+a2.toDebugString());
    System.out.println("contour labels equal? "+(a==a2));

    AbstractCurve ca1 = new AbstractCurve(a);
    AbstractCurve ca2 = new AbstractCurve(a);

    System.out.println("contours equal? "+a.toDebugString()+","+a2.toDebugString());
    System.out.println("contours equal? "+(a==a2));

    TreeSet<AbstractCurve> ts = new TreeSet<AbstractCurve>();
    AbstractBasicRegion z0 = AbstractBasicRegion.get(ts);
    System.out.println("outside zone "+z0.toDebugString());

    ts.add(ca1);
    AbstractBasicRegion za = AbstractBasicRegion.get(ts);

    AbstractBasicRegion za2;
    {
    TreeSet<AbstractCurve> ts2 = new TreeSet<AbstractCurve>();
    ts2.add(ca2);
    za2 = AbstractBasicRegion.get(ts2);
    System.out.println("za==za2 ?" + (za == za2));
    }

    System.out.println("zone in a "+za.toDebugString());
    System.out.println("zone in a "+za2.toDebugString());


    CurveLabel b = CurveLabel.get("b");
    AbstractCurve cb = new AbstractCurve(b);
    ts.add(cb);
    AbstractBasicRegion zab = AbstractBasicRegion.get(ts);
    System.out.println("zone in ab "+zab.toDebugString());

    ts.remove(ca1);
    AbstractBasicRegion zb = AbstractBasicRegion.get(ts);
    System.out.println("zone in b "+zb.toDebugString());

    ts.add(ca1);
    AbstractBasicRegion zab2 = AbstractBasicRegion.get(ts);
    System.out.println("zone2 in ab "+zab2.toDebugString());

    System.out.println("zab==zab2 ?" + (zab == zab2));

    ts.clear();
    TreeSet<AbstractBasicRegion> tsz = new TreeSet<AbstractBasicRegion>();

    debug_abstract_description(ts, tsz);

    ts.add(ca1);
    debug_abstract_description(ts, tsz);

    ts.add(ca1);
    debug_abstract_description(ts, tsz);

    ts.add(ca2);
    debug_abstract_description(ts, tsz);

    ts.add(cb);
    debug_abstract_description(ts, tsz);

    tsz.add(z0);
    debug_abstract_description(ts, tsz);

    tsz.add(za);
    debug_abstract_description(ts, tsz);

    tsz.add(zab);
    debug_abstract_description(ts, tsz);

    tsz.add(zb);
    debug_abstract_description(ts, tsz);

    //ContourLabel c = ContourLabel.get("c");
    //ContourLabel d = ContourLabel.get("d");
    //ContourLabel e = ContourLabel.get("e");

    System.out.println("\"\" is " + makeForTesting("").toDebugString());
    System.out.println("\"a\" is " + makeForTesting("a").toDebugString());
    System.out.println("\"a a\" is " + makeForTesting("a a").toDebugString());
    System.out.println("\"a ab\" is " + makeForTesting("a ab").toDebugString());

    }
    private static void debug_abstract_description(
    TreeSet<AbstractCurve> ts, TreeSet<AbstractBasicRegion> tsz)
    {
    AbstractDescription ad = new AbstractDescription(ts, tsz);
    System.out.println("ad is " + ad.toDebugString());
    }
     */
}
