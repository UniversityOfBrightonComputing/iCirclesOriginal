package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import icircles.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.util.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class PolygonContour extends Contour {

    private List<Point2D> criticalPoints;

    public PolygonContour(AbstractCurve curve, List<Point2D> points) {
        super(curve);
        this.criticalPoints = points;
    }

    public List<Point2D> getCriticalPoints() {
        return criticalPoints;
    }

    @Override
    public Shape getShape() {
        double[] points = new double[getCriticalPoints().size() * 2];
        int i = 0;
        for (Point2D p : getCriticalPoints()) {
            points[i++] = p.x;
            points[i++] = p.y;
        }

        return new Polygon(points);
    }
}
