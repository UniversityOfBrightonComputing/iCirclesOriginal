package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.geometry.Point2D;
import icircles.geometry.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

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
    private final List<Contour> containingContours;

    /**
     * Contours outside of this zone.
     */
    private final List<Contour> excludingContours;

    /**
     * Constructs a concrete zone from abstract zone given containing and excluding contours.
     *
     * @param zone abstract zone
     * @param containingContours containing contours
     * @param excludingContours   excluding contours
     */
    public ConcreteZone(AbstractBasicRegion zone, List<Contour> containingContours, List<Contour> excludingContours) {
        this.zone = zone;
        this.containingContours = containingContours;
        this.excludingContours = excludingContours;
    }

    public AbstractBasicRegion getAbstractZone() {
        return zone;
    }

    /**
     * @return contours within this zone
     */
    public List<Contour> getContainingContours() {
        return containingContours;
    }

    /**
     * @return contours outside of this zone
     */
    public List<Contour> getExcludingContours() {
        return excludingContours;
    }

//    public Rectangle getBoundingBox() {
//        double minX = containingContours.stream()
//                .mapToDouble(CircleContour::getMinX)
//                .min()
//                .orElse(0);
//
//        double minY = containingContours.stream()
//                .mapToDouble(CircleContour::getMinY)
//                .min()
//                .orElse(0);
//
//        double maxX = containingContours.stream()
//                .mapToDouble(CircleContour::getMaxX)
//                .max()
//                .orElse(0);
//
//        double maxY = containingContours.stream()
//                .mapToDouble(CircleContour::getMaxY)
//                .max()
//                .orElse(0);
//
//        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
//    }

    public Shape getShape() {
        Shape shape = new javafx.scene.shape.Rectangle(1000, 1000);

        for (Contour contour : getContainingContours()) {
            shape = Shape.intersect(shape, contour.getShape());
        }

        for (Contour contour : getExcludingContours()) {
            shape = Shape.subtract(shape, contour.getShape());
        }

        return shape;
    }

    /**
     * TODO: computation is based on BBOX but shape can be non-regular.
     * Hence point may not be within zone at all.
     *
     * @return center point of this concrete zone in 2D space
     */
    public Point2D getCenter() {
        Shape shape = getShape();

        double minX = shape.getLayoutBounds().getMinX();
        double minY = shape.getLayoutBounds().getMinY();
        double width = shape.getLayoutBounds().getWidth();
        double height = shape.getLayoutBounds().getHeight();

        return new Point2D(minX + width / 2, minY + height / 2);
    }

    public String toDebugString() {
        return "ConcreteZone:[zone=" + zone + "\n"
                + "containing: " + containingContours.toString() + "\n"
                + "excluding:  " + excludingContours.toString() + "]";
    }

    @Override
    public String toString() {
        return zone.toString();
    }
}
