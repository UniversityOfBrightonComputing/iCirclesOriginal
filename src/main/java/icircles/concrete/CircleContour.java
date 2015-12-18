package icircles.concrete;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import icircles.abstractdescription.AbstractCurve;
import javafx.scene.shape.Circle;

/**
 * Concrete form of AbstractCurve.
 */
public class CircleContour {

    private Ellipse2D.Double circle;
    double cx;
    double cy;
    double radius;
    double nudge = 0.1;

    /**
     * Abstract representation of this concrete contour.
     */
    public AbstractCurve ac;

    /**
     * Constructs a contour from abstract curve and geometric values.
     *
     * @param cx center x coordinate of the contour
     * @param cy center y coordinate of the contour
     * @param radius contour radius
     * @param ac abstract curve
     */
    public CircleContour(double cx, double cy, double radius, AbstractCurve ac) {
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.ac = ac;
        circle = makeEllipse(cx, cy, radius);
    }

    /**
     * Copy constructor.
     *
     * @param contour other contour
     */
    public CircleContour(CircleContour contour) {
        this(contour.cx, contour.cy, contour.radius, contour.ac);
	}

	private void shift(double x, double y) {
        cx += x;
        cy += y;
        circle = makeEllipse(cx, cy, radius);
    }

    private void scaleAboutZero(double scale) {
        cx *= scale;
        cy *= scale;
        radius *= scale;
        circle = makeEllipse(cx, cy, radius);
    }

    private Ellipse2D.Double makeEllipse(double x, double y, double r) {
        return new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }

    public Ellipse2D.Double getCircle() {
        return circle;
    }

    public Area getBigInterior() {
        return new Area(makeEllipse(cx, cy, radius + nudge));
    }

    public javafx.scene.shape.Shape getBigInteriorFX() {
        return new Circle(cx, cy, radius + nudge);
    }

    public Area getSmallInterior() {
        return new Area(makeEllipse(cx, cy, radius - nudge));
    }

    public javafx.scene.shape.Shape getSmallInteriorFX() {
        return new Circle(cx, cy, radius - nudge);
    }

    public Shape getFatInterior(double fatter) {
        return new Area(makeEllipse(cx, cy, radius + fatter));
    }

    public double getLabelXPosition() {
        return cx + 0.8 * radius;
    }

    public double getLabelYPosition() {
        return cy - 0.8 * radius;
    }

    public int getMinX() {
        return (int) (cx - radius);
    }

    public int getMaxX() {
        return (int) (cx + radius) + 1;
    }

    public int getMinY() {
        return (int) (cy - radius);
    }

    public int getMaxY() {
        return (int) (cy + radius) + 1;
    }

    public String toDebugString() {
        return String.format("CircleCountour[center=(%.0f,%.0f),radius=%.0f,curve=%s]",
                cx, cy, radius, ac);
    }

    @Override
    public String toString() {
        return ac.toString();
    }

    static void fitCirclesToSize(List<CircleContour> circles, int size) {
        // work out a suitable size
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CircleContour cc : circles) {
            if (cc.getMinX() < minX) {
                minX = cc.getMinX();
            }
            if (cc.getMinY() < minY) {
                minY = cc.getMinY();
            }
            if (cc.getMaxX() > maxX) {
                maxX = cc.getMaxX();
            }
            if (cc.getMaxY() > maxY) {
                maxY = cc.getMaxY();
            }
        }

        double midX = (minX + maxX) * 0.5;
        double midY = (minY + maxY) * 0.5;
        for (CircleContour cc : circles) {
            cc.shift(-midX, -midY);
        }

        double width = maxX - minX;
        double height = maxY - minY;
        double biggest_HW = Math.max(height, width);
        double scale = (size * 0.95) / biggest_HW;
        for (CircleContour cc : circles) {
            cc.scaleAboutZero(scale);
        }

        for (CircleContour cc : circles) {
            cc.shift(size * 0.5, size * 0.5);
        }
    }

    static Rectangle2D.Double makeBigOuterBox(List<CircleContour> circles) {
    	if (circles.isEmpty())
    		return new Rectangle2D.Double(0, 0, 1000, 1000);
    	
        // work out a suitable size
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CircleContour cc : circles) {
            if (cc.getMinX() < minX) {
                minX = cc.getMinX();
            }
            if (cc.getMinY() < minY) {
                minY = cc.getMinY();
            }
            if (cc.getMaxX() > maxX) {
                maxX = cc.getMaxX();
            }
            if (cc.getMaxY() > maxY) {
                maxY = cc.getMaxY();
            }
        }
        int width = maxX - minX;
        int height = maxY - minX;
        
        return new Rectangle2D.Double(minX - 2*width, minY - 2*height, 5*width, 5*height);
    }
}
