package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import icircles.geometry.Point2D;

import java.util.*;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class ArbitraryContour {

    private List<Point2D> criticalPoints;

    private AbstractCurve curve;

    public ArbitraryContour(AbstractCurve curve, List<Point2D> points) {
        this.curve = curve;
        this.criticalPoints = points;
    }

    public AbstractCurve getCurve() {
        return curve;
    }

    public List<Point2D> getCriticalPoints() {
        return criticalPoints;
    }
}
