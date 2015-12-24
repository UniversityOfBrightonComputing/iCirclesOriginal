package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.decomposition.*;
import icircles.geometry.Rectangle;
import icircles.recomposition.*;
import icircles.util.CannotDrawException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Represents a diagram at the concrete level.
 * Technically, this is a concrete form of AbstractDescription.
 */
public class ConcreteDiagram {

    private final Rectangle box;
    private final List<CircleContour> circles;
    private final List<ConcreteZone> shadedZones, allZones;

    private AbstractDescription original, actual;

    public ConcreteDiagram(AbstractDescription original, AbstractDescription actual,
                           Rectangle box, List<CircleContour> circles,
                           List<ConcreteZone> allZones, List<ConcreteZone> shadedZones) {
        this.original = original;
        this.actual = actual;
        this.box = box;
        this.circles = circles;
        this.shadedZones = shadedZones;
        this.allZones = allZones;
    }

    /**
     * Constructs a concrete form of an abstract diagram.
     *
     * @param description the description to be drawn
     * @param size the size of the concrete diagram
     * @param dType decomposition type
     * @param rType recomposition type
     * @throws CannotDrawException if diagram cannot be drawn with given parameters
     */
    public ConcreteDiagram(AbstractDescription description, int size,
                           DecompositionStrategyType dType, RecompositionStrategyType rType) throws CannotDrawException {

        Decomposer d = DecomposerFactory.newDecomposer(dType);
        List<DecompositionStep> dSteps = d.decompose(description);

        Recomposer r = RecomposerFactory.newRecomposer(rType);
        List<RecompositionStep> rSteps = r.recompose(dSteps);

        DiagramCreator dc = new DiagramCreator(dSteps, rSteps);
        ConcreteDiagram diagram = dc.createDiagram(size);

        this.box = diagram.box;
        this.circles = diagram.circles;
        this.shadedZones = diagram.shadedZones;
        this.original = diagram.original;
        this.actual = diagram.actual;
        this.allZones = diagram.allZones;
    }

    /**
     * @return bounding box of the whole diagram
     */
    public Rectangle getBoundingBox() {
        return box;
    }

    /**
     * @return diagram contours
     */
    public List<CircleContour> getCircles() {
        return circles;
    }

    /**
     * @return extra zones
     */
    public List<ConcreteZone> getShadedZones() {
        return shadedZones;
    }

    /**
     * Returns original abstract description, i.e. the one that was requested.
     *
     * @return original abstract description
     */
    public AbstractDescription getOriginalDescription() {
        return original;
    }

    /**
     * Returns actual abstract description, i.e. the one that was generated.
     *
     * @return actual abstract description
     */
    public AbstractDescription getActualDescription() {
        return actual;
    }

    /**
     * @return all zones this concrete diagram has
     */
    public List<ConcreteZone> getAllZones() {
        return allZones;
    }

    public void setSize(int size) {
        // work out a suitable size
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CircleContour cc : circles) {
            if (cc.getMinX() < minX) {
                minX = cc.getMinX();
            }
            if (cc.getMinY() < minY) {
                minY = cc.getMinY();
            }
            if (cc.getMaxX() > maxX) {
                maxX = cc.getMaxX();
            }
            if (cc.getMaxY() > maxY) {
                maxY = cc.getMaxY();
            }
        }

        double midX = (minX + maxX) * 0.5;
        double midY = (minY + maxY) * 0.5;
        for (CircleContour cc : circles) {
            cc.shift(-midX, -midY);
        }

        double width = maxX - minX;
        double height = maxY - minY;
        double biggest_HW = Math.max(height, width);
        double scale = (size * 0.95) / biggest_HW;
        for (CircleContour cc : circles) {
            cc.scaleAboutZero(scale);
        }

        for (CircleContour cc : circles) {
            cc.shift(size * 0.5, size * 0.5);
        }
    }

    /**
     * Returns a map, where keys are abstract curves that map to all concrete contours
     * for that curve. The map only contains duplicates, i.e. it won't contain a curve
     * which only maps to a single contour.
     *
     * @return duplicate contours
     */
    public Map<AbstractCurve, List<CircleContour> > findDuplicateContours() {
        Map<String, List<CircleContour> > groups = circles.stream()
                .collect(Collectors.groupingBy(contour -> contour.getCurve().getLabel()));

        Map<AbstractCurve, List<CircleContour> > duplicates = new TreeMap<>();
        groups.forEach((label, contours) -> {
            if (contours.size() > 1)
                duplicates.put(new AbstractCurve(label), contours);
        });

        return duplicates;
    }

    /**
     * Returns zones in the drawn diagram that contain the given contour.
     *
     * @param contour the contour
     * @return zones containing contour
     */
    public List<ConcreteZone> getZonesContainingContour(CircleContour contour) {
        return allZones.stream()
                .filter(zone -> zone.getContainingCircles().contains(contour))
                .collect(Collectors.toList());
    }

    public static double checksum(List<CircleContour> circles) {
        double result = 0.0;
        if (circles == null) {
            return result;
        }

        Iterator<CircleContour> cIt = circles.iterator();
        while (cIt.hasNext()) {
            CircleContour c = cIt.next();
            result += c.centerX * 0.345 + c.centerY * 0.456 + c.radius * 0.567 + c.getCurve().checksum() * 0.555;
            result *= 1.2;
        }
        return result;
    }

    public String toDebugString() {
        return "ConcreteDiagram[box=" + box + "\n"
                + "contours: " + circles + "\n"
                + "shaded zones: " + shadedZones + "]";
    }

    @Override
    public String toString() {
        return toDebugString();
    }
}
