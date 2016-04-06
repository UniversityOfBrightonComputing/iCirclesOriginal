package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Concrete form of AbstractBasicRegion.
 */
public class ConcreteZone {

    private static final Logger log = LogManager.getLogger(ConcreteZone.class);

    private static final int RADIUS_STEP = 5;

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

    public Shape getShape() {
        //Shape shape = new javafx.scene.shape.Rectangle(600, 600);
        Shape shape = new javafx.scene.shape.Rectangle(1000, 1000);

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

    // TODO: cache shape
    //private Shape shape = null;
    private Point2D center = null;

    /**
     * @return center point of this concrete zone in 2D space
     */
    public Point2D getCenter() {
        if (center == null) {
            center = computeCenter();
        }

        return center;
    }

    /**
     * Scans the zone using a smaller radius circle each time
     * until the circle is completely within the zone.
     * Once found, returns the center of that circle.
     *
     * @return zone center
     */
    private Point2D computeCenter() {
        Shape shape = getShape();
        shape.setFill(Color.RED);

        double minX = shape.getLayoutBounds().getMinX();
        double minY = shape.getLayoutBounds().getMinY();
        double width = shape.getLayoutBounds().getWidth();
        double height = shape.getLayoutBounds().getHeight();

        // TODO: width < height ? width : height;
        double radius = 100;

        while (true) {
            // TODO: replace with circle
            javafx.scene.shape.Rectangle circle = new javafx.scene.shape.Rectangle(radius*2, radius*2);
            //Circle circle = new Circle(radius, radius, radius);
            circle.setStroke(Color.BLACK);

            for (int y = (int) minY; y < minY + height - radius; y += 5) {
                circle.setY(y);

                for (int x = (int) minX; x < minX + width - radius; x += 5) {
                    circle.setX(x);

                    // if circle is completely enclosed by this zone
                    if (Shape.subtract(circle, shape).getLayoutBounds().isEmpty()) {
                        return new Point2D(x + radius, y + radius);
                    }
                }
            }

            radius -= RADIUS_STEP;

            if (radius <= 0) {
                throw new RuntimeException("Cannot find zone center: " + zone);
            }
        }
    }

//    private Point2D getCenterOld() {
//        Shape shape = getShape();
//
//        double minX = shape.getLayoutBounds().getMinX();
//        double minY = shape.getLayoutBounds().getMinY();
//        double width = shape.getLayoutBounds().getWidth();
//        double height = shape.getLayoutBounds().getHeight();
//
//        Point2D center = new Point2D(minX + width / 2, minY + height / 2);
//        Point2D newCenter = center;
//
//        // this regulates preciseness
//        int step = 5;
//
//        Point2D delta = new Point2D(step, 0);
//        int i = 0;
//
//        int safetyCount = 0;
//
//        while (!shape.contains(newCenter) && safetyCount < 100) {
//            newCenter = center.add(delta);
//            i++;
//
//            switch (i) {
//                case 1:
//                    delta = new Point2D(step, step);
//                    break;
//                case 2:
//                    delta = new Point2D(0, step);
//                    break;
//                case 3:
//                    delta = new Point2D(-step, step);
//                    break;
//                case 4:
//                    delta = new Point2D(-step, 0);
//                    break;
//                case 5:
//                    delta = new Point2D(-step, -step);
//                    break;
//                case 6:
//                    delta = new Point2D(0, -step);
//                    break;
//                case 7:
//                    delta = new Point2D(step, -step);
//                    break;
//            }
//
//            if (i == 8) {
//                i = 0;
//                delta = new Point2D(step, 0);
//                step *= 2;
//            }
//
//            safetyCount++;
//        }
//
//        if (safetyCount == 100) {
//            System.out.println("Failed to find center");
//            return center;
//        } else {
//            delta = newCenter.subtract(center);
//
//            if (shape.contains(newCenter.add(delta.multiply(3)))) {
//                //System.out.println("returning new");
//                //return newCenter.add(delta.multiply(3));
//            }
//        }
//
//        return newCenter;
//    }

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
