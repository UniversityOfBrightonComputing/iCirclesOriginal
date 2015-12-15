package icircles.concrete;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import icircles.abstractdescription.AbstractBasicRegion;

public class ConcreteZone {

    private AbstractBasicRegion abr;
    private ArrayList<CircleContour> containingCircles;
    private ArrayList<CircleContour> excludingCircles;
    private Area shape;

    public ConcreteZone(AbstractBasicRegion abr,
            ArrayList<CircleContour> containingCircles,
            ArrayList<CircleContour> excludingCircles) {
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
