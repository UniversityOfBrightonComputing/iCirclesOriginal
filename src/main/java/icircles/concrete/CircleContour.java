package icircles.concrete;

import icircles.abstractdescription.AbstractCurve;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 * Concrete form of AbstractCurve.
 */
public class CircleContour extends Contour {
    double centerX;
    double centerY;
    double radius;
    private double nudge = 0.1;

    /**
     * Constructs a contour from abstract curve and geometric values.
     *
     * @param centerX center x coordinate of the contour
     * @param centerY center y coordinate of the contour
     * @param radius contour radius
     * @param curve abstract curve
     */
    public CircleContour(double centerX, double centerY, double radius, AbstractCurve curve) {
        super(curve);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
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

    public void shift(double x, double y) {
        centerX += x;
        centerY += y;
    }

    public void scaleAboutZero(double scale) {
        centerX *= scale;
        centerY *= scale;
        radius *= scale;
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

    @Override
    public Shape getShape() {
        // big for containing intersect
        // small for exluding subtract
        return new Circle(getCenterX(), getCenterY(), getBigRadius());
    }

    public String toDebugString() {
        return String.format("CircleCountour[center=(%.0f,%.0f),radius=%.0f,curve=%s]",
                centerX, centerY, radius, getCurve());
    }

    @Override
    public String toString() {
        return getCurve().toString();
    }
}
