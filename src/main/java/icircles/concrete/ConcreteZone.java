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

    public AbstractBasicRegion getAbstractZone() {
        return zone;
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

    public Rectangle getBoundingBox() {
        double minX = containingCircles.stream()
                .mapToDouble(CircleContour::getMinX)
                .min()
                .orElse(0);

        double minY = containingCircles.stream()
                .mapToDouble(CircleContour::getMinY)
                .min()
                .orElse(0);

        double maxX = containingCircles.stream()
                .mapToDouble(CircleContour::getMaxX)
                .max()
                .orElse(0);

        double maxY = containingCircles.stream()
                .mapToDouble(CircleContour::getMaxY)
                .max()
                .orElse(0);

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public Shape getShape() {
        Shape shape = new javafx.scene.shape.Rectangle(1000, 1000);

        for (CircleContour contour : getContainingCircles()) {
            shape = Shape.intersect(shape, new Circle(contour.getCenterX(), contour.getCenterY(), contour.getBigRadius()));
        }

        for (CircleContour contour : getExcludingCircles()) {
            shape = Shape.subtract(shape, new Circle(contour.getCenterX(), contour.getCenterY(), contour.getSmallRadius()));
        }


        return shape;
    }

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
                + "containing: " + containingCircles.toString() + "\n"
                + "excluding:  " + excludingCircles.toString() + "]";
    }

    @Override
    public String toString() {
        return zone.toString();
    }
}
