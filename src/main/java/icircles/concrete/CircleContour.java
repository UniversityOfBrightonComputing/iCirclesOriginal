package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete form of AbstractCurve.
 */
public class CircleContour {

    private Ellipse2D.Double circle;
    double centerX;
    double centerY;
    double radius;
    double nudge = 0.1;

    /**
     * Abstract representation of this concrete contour.
     */
    public AbstractCurve ac;

    /**
     * Constructs a contour from abstract curve and geometric values.
     *
     * @param centerX center x coordinate of the contour
     * @param centerY center y coordinate of the contour
     * @param radius contour radius
     * @param ac abstract curve
     */
    public CircleContour(double centerX, double centerY, double radius, AbstractCurve ac) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.ac = ac;
        circle = makeEllipse(centerX, centerY, radius);
    }

    /**
     * Copy constructor.
     *
     * @param contour other contour
     */
    public CircleContour(CircleContour contour) {
        this(contour.centerX, contour.centerY, contour.radius, contour.ac);
	}

    /**
     * @return center x
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * @return center y
     */
    public double getCenterY() {
        return centerY;
    }

    public double getRadius() {
        return radius;
    }

    public double getSmallRadius() {
        return radius - nudge;
    }

    public double getBigRadius() {
        return radius + nudge;
    }

    private void shift(double x, double y) {
        centerX += x;
        centerY += y;
        circle = makeEllipse(centerX, centerY, radius);
    }

    private void scaleAboutZero(double scale) {
        centerX *= scale;
        centerY *= scale;
        radius *= scale;
        circle = makeEllipse(centerX, centerY, radius);
    }

    private Ellipse2D.Double makeEllipse(double x, double y, double r) {
        return new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }

    public Ellipse2D.Double getCircle() {
        return circle;
    }

    public Area getBigInterior() {
        return new Area(makeEllipse(centerX, centerY, radius + nudge));
    }

    public javafx.scene.shape.Shape getBigInteriorFX() {
        return new Circle(centerX, centerY, radius + nudge);
    }

    public Area getSmallInterior() {
        return new Area(makeEllipse(centerX, centerY, radius - nudge));
    }

    public javafx.scene.shape.Shape getSmallInteriorFX() {
        return new Circle(centerX, centerY, radius - nudge);
    }

    public Shape getFatInterior(double fatter) {
        return new Area(makeEllipse(centerX, centerY, radius + fatter));
    }

    public double getLabelXPosition() {
        return centerX + 0.8 * radius;
    }

    public double getLabelYPosition() {
        return centerY - 0.8 * radius;
    }

    public int getMinX() {
        return (int) (centerX - radius);
    }

    public int getMaxX() {
        return (int) (centerX + radius) + 1;
    }

    public int getMinY() {
        return (int) (centerY - radius);
    }

    public int getMaxY() {
        return (int) (centerY + radius) + 1;
    }

    public String toDebugString() {
        return String.format("CircleCountour[center=(%.0f,%.0f),radius=%.0f,curve=%s]",
                centerX, centerY, radius, ac);
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
