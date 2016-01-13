package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.geometry.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a diagram at the concrete level.
 * Technically, this is a concrete form of AbstractDescription.
 */
public class ConcreteDiagram {

    private static final Logger log = LogManager.getLogger(ConcreteDiagram.class);

    private final Rectangle box;
    private final List<CircleContour> circles;
    private final List<ConcreteZone> shadedZones, allZones;

    private final AbstractDescription original, actual;
    private final Map<AbstractCurve, CircleContour> curveToContour;

    ConcreteDiagram(AbstractDescription original, AbstractDescription actual,
                    List<CircleContour> circles,
                    Map<AbstractCurve, CircleContour> curveToContour, int size) {
        this.original = original;
        this.actual = actual;
        this.box = new Rectangle(0, 0, size, size);
        this.curveToContour = curveToContour;
        this.circles = circles;

        setSize(size);

        log.trace("Initial diagram: " + original);
        log.trace("Final diagram  : " + actual);

        this.shadedZones = createShadedZones();
        this.allZones = actual.getZonesUnmodifiable()
                .stream()
                .map(this::makeConcreteZone)
                .collect(Collectors.toList());

        Map<AbstractCurve, List<CircleContour> > duplicates = findDuplicateContours();

        log.trace("Duplicates: " + duplicates);
        duplicates.values().forEach(contours -> {
            for (CircleContour contour : contours) {
                log.trace("Contour " + contour + " is in " + getZonesContainingContour(contour));
            }
        });
    }

    /**
     * Creates shaded (extra) zones based on the difference
     * between the initial diagram and final diagram.
     * In other words, finds which zones in final diagram were not in initial diagram.
     *
     * @return list of shaded zones
     */
    private List<ConcreteZone> createShadedZones() {
        List<ConcreteZone> result = actual.getZonesUnmodifiable()
                .stream()
                .filter(zone -> !original.hasLabelEquivalentZone(zone))
                .map(this::makeConcreteZone)
                .collect(Collectors.toList());

        log.trace("Extra zones: " + result);

        return result;
    }

    /**
     * Creates a concrete zone out of an abstract zone.
     *
     * @param zone the abstract zone
     * @return the concrete zone
     */
    private ConcreteZone makeConcreteZone(AbstractBasicRegion zone) {
        List<CircleContour> includingCircles = new ArrayList<>();
        List<CircleContour> excludingCircles = new ArrayList<>(circles);

        for (AbstractCurve curve : zone.getCurvesUnmodifiable()) {
            CircleContour contour = curveToContour.get(curve);
            excludingCircles.remove(contour);
            includingCircles.add(contour);
        }

        return new ConcreteZone(zone, includingCircles, excludingCircles);
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
