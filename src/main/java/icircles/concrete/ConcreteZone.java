package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.geometry.Rectangle;
import javafx.geometry.Point2D;
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
        //Shape shape = new javafx.scene.shape.Rectangle(1000, 1000);
        Shape shape = new javafx.scene.shape.Rectangle(600, 600);

        for (Contour contour : getContainingContours()) {
            shape = Shape.intersect(shape, contour.getShape());
        }

        for (Contour contour : getExcludingContours()) {
            shape = Shape.subtract(shape, contour.getShape());
        }

        return shape;
    }

    public boolean intersects(Shape shape) {
        return !Shape.intersect(getShape(), shape).getLayoutBounds().isEmpty();
    }

    public boolean intersects(ConcreteZone other) {
        return intersects(other.getShape());
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

        Point2D center = new Point2D(minX + width / 2, minY + height / 2);
        Point2D newCenter = center;

        // this regulates preciseness
        int step = 1;

        Point2D delta = new Point2D(step, 0);
        int i = 0;

        int safetyCount = 0;

        while (!shape.contains(newCenter) && safetyCount < 100) {
            newCenter = center.add(delta);
            i++;

            switch (i) {
                case 1:
                    delta = new Point2D(step, step);
                    break;
                case 2:
                    delta = new Point2D(0, step);
                    break;
                case 3:
                    delta = new Point2D(-step, step);
                    break;
                case 4:
                    delta = new Point2D(-step, 0);
                    break;
                case 5:
                    delta = new Point2D(-step, -step);
                    break;
                case 6:
                    delta = new Point2D(0, -step);
                    break;
                case 7:
                    delta = new Point2D(step, -step);
                    break;
            }

            if (i == 8) {
                i = 0;
                delta = new Point2D(step, 0);
                step *= 2;
            }

            safetyCount++;
        }

        if (safetyCount == 100) {
            System.out.println("Failed to find center");
            return center;
        }

        return newCenter;
    }

    public boolean isTopologicallyAdjacent(ConcreteZone other) {
        if (zone.getStraddledContour(other.zone).isPresent()) {
            Shape shape2 = other.getShape();

            shape2.setTranslateX(shape2.getTranslateX() - 5);

            if (intersects(shape2)) {
                return true;
            }

            shape2.setTranslateX(shape2.getTranslateX() + 10);

            if (intersects(shape2)) {
                return true;
            }

            shape2.setTranslateX(shape2.getTranslateX() - 5);
            shape2.setTranslateY(shape2.getTranslateY() - 5);

            if (intersects(shape2)) {
                return true;
            }

            shape2.setTranslateY(shape2.getTranslateY() + 10);

            if (intersects(shape2)) {
                return true;
            }
        }

        return false;
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
