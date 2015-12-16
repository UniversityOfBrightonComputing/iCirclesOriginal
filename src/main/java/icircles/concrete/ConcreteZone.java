package icircles.concrete;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

import icircles.abstractdescription.AbstractBasicRegion;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class ConcreteZone {

    private AbstractBasicRegion abr;
    private List<CircleContour> containingCircles;
    private List<CircleContour> excludingCircles;
    private Area shape;
    private Shape shapeFX;

    public ConcreteZone(AbstractBasicRegion abr,
            List<CircleContour> containingCircles,
            List<CircleContour> excludingCircles) {
        this.abr = abr;
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
}
