package icircles.concrete;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

import icircles.abstractdescription.AbstractBasicRegion;

public class ConcreteZone {

    private AbstractBasicRegion abr;
    private List<CircleContour> containingCircles;
    private List<CircleContour> excludingCircles;
    private Area shape;

    public ConcreteZone(AbstractBasicRegion abr,
            List<CircleContour> containingCircles,
            List<CircleContour> excludingCircles) {
        this.abr = abr;
        this.containingCircles = containingCircles;
        this.excludingCircles = excludingCircles;
        shape = null;
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
}
