package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete form of AbstractBasicRegion.
 */
public class ConcreteZone {

    /**
     * The abstract basic region of this concrete zone.
     */
    private AbstractBasicRegion zone;

    /**
     * Contours within this zone.
     */
    private List<CircleContour> containingCircles;

    /**
     * Contours outside of this zone.
     */
    private List<CircleContour> excludingCircles;

    private Area shape;
    private Shape shapeFX;

    public ConcreteZone(AbstractBasicRegion zone, List<CircleContour> containingCircles, List<CircleContour> excludingCircles) {
        this.zone = zone;
        this.containingCircles = containingCircles;
        this.excludingCircles = excludingCircles;
    }

    public Area getShape(Rectangle2D.Double box) {
        if (shape != null) {
            return shape;
        }

        Area a = new Area(box);
        for (CircleContour c : containingCircles) {
            a.intersect(c.getBigInterior());
        }
        for (CircleContour c : excludingCircles) {
            a.subtract(c.getSmallInterior());
        }
        shape = a;
        return a;
    }

    public Shape getShapeFX(Rectangle2D.Double box) {
        if (shapeFX != null)
            return shapeFX;

        Shape a = new Rectangle(box.getX(), box.getY(), box.getWidth(), box.getHeight());
        for (CircleContour c : containingCircles) {
            a = Shape.intersect(a, c.getBigInteriorFX());
        }

        for (CircleContour c : excludingCircles) {
            a = Shape.subtract(a, c.getSmallInteriorFX());
        }

        shapeFX = a;
        return a;
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
