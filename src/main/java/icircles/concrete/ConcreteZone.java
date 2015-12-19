package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;

import java.util.List;

/**
 * Concrete form of AbstractBasicRegion.
 */
public class ConcreteZone {

    /**
     * The abstract basic region of this concrete zone.
     */
    private final AbstractBasicRegion zone;

    /**
     * Contours within this zone.
     */
    private final List<CircleContour> containingCircles;

    /**
     * Contours outside of this zone.
     */
    private final List<CircleContour> excludingCircles;

    /**
     * Constructs a concrete zone from abstract zone given containing and excluding contours.
     *
     * @param zone abstract zone
     * @param containingCircles containing contours
     * @param excludingCircles   excluding contours
     */
    public ConcreteZone(AbstractBasicRegion zone, List<CircleContour> containingCircles, List<CircleContour> excludingCircles) {
        this.zone = zone;
        this.containingCircles = containingCircles;
        this.excludingCircles = excludingCircles;
    }

    /**
     * @return contours within this zone
     */
    public List<CircleContour> getContainingCircles() {
        return containingCircles;
    }

    /**
     * @return contours outside of this zone
     */
    public List<CircleContour> getExcludingCircles() {
        return excludingCircles;
    }

    public String toDebugString() {
        return "ConcreteZone:[zone=" + zone + "\n"
                + "containing: " + containingCircles.toString() + "\n"
                + "excluding:  " + excludingCircles.toString() + "]";
    }

    @Override
    public String toString() {
        return zone.toString();
    }
}
