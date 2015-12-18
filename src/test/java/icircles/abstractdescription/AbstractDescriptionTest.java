package icircles.abstractdescription;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class AbstractDescriptionTest {

    @Test
    public void testToString() {
        AbstractDescription ad1 = new AbstractDescription("a ab abc bc ac");
        AbstractDescription ad2 = new AbstractDescription("abc bc ab ac a");
        AbstractDescription ad3 = new AbstractDescription("a ad abc bc ac");

        assertEquals(ad1.toString(), ad2.toString());
        assertNotEquals(ad1.toString(), ad3.toString());
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
