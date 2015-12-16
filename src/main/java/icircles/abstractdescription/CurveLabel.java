package icircles.abstractdescription;

import java.util.Set;
import java.util.TreeSet;

import icircles.util.DEB;

public class CurveLabel implements Comparable<CurveLabel> {

    private String label;
    private static Set<CurveLabel> library = new TreeSet<>();

    public static void clearLibrary() {
        library.clear();
    }

    private CurveLabel(String label) {
        this.label = label;
    }

    public static CurveLabel get(String label) {
        for (CurveLabel alreadyThere : library) {
            if (alreadyThere.label.equals(label)) {
                return alreadyThere;
            }
        }

        CurveLabel result = new CurveLabel(label);
        library.add(result);
        return result;
    }

    public String debug() {
        if (DEB.level == 0) {
            return "";
        } else {
            return label;
        }
    }

    public int compareTo(CurveLabel other) {
        return label.compareTo(other.label);
    }

    public double checksum() {
        double result = 0.0;
        double scaling = 1.1;
        for (int i = 0; i < label.length(); i++) {
            result += (int) (label.charAt(i)) * scaling;
            scaling += 0.01;
        }
        return result;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
