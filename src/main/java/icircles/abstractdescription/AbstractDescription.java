package icircles.abstractdescription;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import icircles.util.DEB;

/**
 * An AbstractDescription encapsulates the elements of a diagram, with no drawn information.
 * A diagram comprises a set of AbstractCurves (the contours).
 * A set of AbstractBasicRegions is given (zones which must be present.
 * <p>
 * An AbstractDiagram is consistent if 
 * <p>1. the contours in each of the AbstractBasicRegions match those
 * in contours.
 * <p>2. every valid diagram includes the "outside" zone. 
 * TODO add a coherence check on these internal checks.
 */
public class AbstractDescription {

    // TODO: immutable data structure?
    private TreeSet<AbstractCurve> contours;
    private TreeSet<AbstractBasicRegion> zones;

    public AbstractDescription(Set<AbstractCurve> contours, Set<AbstractBasicRegion> zones) {
        this.contours = new TreeSet<>(contours);
        this.zones = new TreeSet<>(zones);
    }

    public AbstractCurve getFirstContour() {
        if (contours.size() == 0) {
            return null;
        }
        return contours.first();
    }

    public AbstractCurve getLastContour() {
        if (contours.size() == 0) {
            return null;
        }
        return contours.last();
    }

    public Iterator<AbstractCurve> getContourIterator() {
        return contours.iterator();
    }

    public int getNumContours() {
        return contours.size();
    }

    public Iterator<AbstractBasicRegion> getZoneIterator() {
        return zones.iterator();
    }

    public TreeSet<AbstractCurve> getCopyOfContours() {
        return new TreeSet<>(contours);
    }

    public TreeSet<AbstractBasicRegion> getCopyOfZones() {
        return new TreeSet<>(zones);
    }

    public static AbstractDescription makeForTesting(String s) {
        TreeSet<AbstractBasicRegion> ad_zones = new TreeSet<>();
        ad_zones.add(AbstractBasicRegion.get(new TreeSet<>()));

        StringTokenizer st = new StringTokenizer(s);
        HashMap<CurveLabel, AbstractCurve> contours = new HashMap<>();

        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            TreeSet<AbstractCurve> zoneContours = new TreeSet<>();
            for (int i = 0; i < word.length(); i++) {
                String character = "" + word.charAt(i);
                CurveLabel cl = CurveLabel.get(character);
                if (!contours.containsKey(cl)) {
                    contours.put(cl, new AbstractCurve(cl));
                }
                zoneContours.add(contours.get(cl));
            }
            ad_zones.add(AbstractBasicRegion.get(zoneContours));
        }

        TreeSet<AbstractCurve> ad_contours = new TreeSet<>(contours.values());
        return new AbstractDescription(ad_contours, ad_zones);
    }

    public String debug() {
        if (DEB.level == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append("labels:");
        boolean first = true;
        if (DEB.level > 1) {
            b.append("{");
        }
        for (AbstractCurve c : contours) {
            if (!first) {
                b.append(",");
            }
            b.append(c);
            first = false;
        }
        if (DEB.level > 1) {
            b.append("}");
        }
        b.append("\n");
        b.append("zones:");
        if (DEB.level > 1) {
            b.append("{");
        }
        first = true;
        for (AbstractBasicRegion z : zones) {
            if (!first) {
                b.append(",");
            }
            if (DEB.level > 1) {
                b.append("\n");
            }
            b.append(z.debug());
            first = false;
        }
        if (DEB.level > 1) {
            b.append("}");
        }
        b.append("\n");

        return b.toString();
    }

    public String debugAsSentence() {
        HashMap<AbstractCurve, String> printable = new HashMap<AbstractCurve, String>();
        for (AbstractCurve c : contours) {
            printable.put(c, print_contour(c));
        }
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (AbstractBasicRegion z : zones) {
            if (!first) {
                b.append(",");
            }
            Iterator<AbstractCurve> c_it = z.getContourIterator();
            boolean printed_something = false;
            while (c_it.hasNext()) {
                AbstractCurve c = c_it.next();
                b.append(printable.get(c));
                printed_something = true;
            }
            if (!printed_something) {
                b.append("0");
            }
            first = false;
        }
        return b.toString();
    }

    public String print_contour(AbstractCurve c) {
        if (one_of_multiple_instances(c)) {
            return c.toString();
        } else {
            return c.toString();
        }
    }

    boolean one_of_multiple_instances(AbstractCurve c) {
        for (AbstractCurve cc : contours) {
            if (cc != c && cc.matches_label(c)) {
                return true;
            }
        }
        return false;
    }

    public int getNumZones() {
        return zones.size();
    }

    public double checksum() {
        double scaling = 2.1;
        double result = 0.0;
        for (AbstractCurve c : contours) {
            result += c.checksum() * scaling;
            scaling += 0.07;
            scaling += 0.05;
            for (AbstractBasicRegion z : zones) {
                if (z.contains(c)) {
                    result += z.checksum() * scaling;
                    scaling += 0.09;
                }
            }
        }
        return result;
    }

    public boolean includesLabel(CurveLabel l) {
        for (AbstractCurve c : contours) {
            if (c.getLabel() == l) {
                return true;
            }
        }
        return false;
    }

    public boolean hasLabelEquivalentZone(AbstractBasicRegion z) {
        for (AbstractBasicRegion zone : zones) {
            if (zone.isLabelEquivalent(z)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        zones.forEach(zone -> sb.append(zone).append(","));

        int lastIndex = sb.lastIndexOf(",");
        if (lastIndex != -1) {
            sb.deleteCharAt(lastIndex);
        }

        return sb.toString();
    }

    public boolean hasSameAbstractDescription(AbstractDescription description) {
        return toString().equals(description.toString());
    }
}
