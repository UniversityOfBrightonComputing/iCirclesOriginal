package icircles.guifx;

import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;
import icircles.gui.Renderer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXRenderer extends Pane implements Renderer {

    private Pane rootShadedZones = new Pane();
    private Canvas canvas = new Canvas();

    private GraphicsContext g;

    public FXRenderer() {
        canvas.setMouseTransparent(true);
        g = canvas.getGraphicsContext2D();

        getChildren().addAll(rootShadedZones, canvas);
    }

    private void setCanvasSize(double w, double h) {
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    @Override
    public void draw(ConcreteDiagram diagram) {
        Rectangle bbox = toFXRectangle(diagram.getBoundingBox());
        setCanvasSize(bbox.getWidth(), bbox.getHeight());

        clearRenderer();

        for (ConcreteZone zone : diagram.getShadedZones())
            drawShadedZone(zone, bbox);

        List<ConcreteZone> normalZones = new ArrayList<>(diagram.getAllZones());
        normalZones.removeAll(diagram.getShadedZones());
        normalZones.removeIf(z -> z.getContainingCircles().isEmpty());

        for (ConcreteZone zone : normalZones)
            drawNormalZone(zone, bbox);

        for (CircleContour contour : diagram.getCircles())
            drawContour(contour);
    }

    private void drawShadedZone(ConcreteZone zone, Rectangle bbox) {
        Shape shape = bbox;

        for (CircleContour contour : zone.getContainingCircles()) {
            shape = Shape.intersect(shape, new Circle(contour.getCenterX(), contour.getCenterY(), contour.getBigRadius()));
        }

        for (CircleContour contour : zone.getExcludingCircles()) {
            shape = Shape.subtract(shape, new Circle(contour.getCenterX(), contour.getCenterY(), contour.getSmallRadius()));
        }

        Tooltip.install(shape, new Tooltip(zone.toDebugString()));
        shape.setUserData(zone);
        shape.setFill(Color.LIGHTGREY);
        rootShadedZones.getChildren().add(shape);
    }

    private void drawNormalZone(ConcreteZone zone, Rectangle bbox) {
        Shape shape = bbox;

        for (CircleContour contour : zone.getContainingCircles()) {
            shape = Shape.intersect(shape, new Circle(contour.getCenterX(), contour.getCenterY(), contour.getBigRadius()));
        }

        for (CircleContour contour : zone.getExcludingCircles()) {
            shape = Shape.subtract(shape, new Circle(contour.getCenterX(), contour.getCenterY(), contour.getSmallRadius()));
        }

        Tooltip.install(shape, new Tooltip(zone.toDebugString()));
        shape.setUserData(zone);
        shape.setFill(Color.TRANSPARENT);
        rootShadedZones.getChildren().add(shape);
    }

    private void drawContour(CircleContour contour) {
        g.setFill(Color.BLACK);
        g.setStroke(Color.BLUE);

        double radius = contour.getRadius();
        double x = contour.getCenterX() - radius;
        double y = contour.getCenterY() - radius;
        double w = 2 * radius;
        double h = 2 * radius;

        g.strokeOval(x, y, w, h);
        g.fillText(contour.getCurve().getLabel(), contour.getLabelXPosition(), contour.getLabelYPosition());
    }

    private void clearRenderer() {
        rootShadedZones.getChildren().clear();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private Rectangle toFXRectangle(icircles.geometry.Rectangle rectangle) {
        return new Rectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    public List<Node> getShadedZones() {
        return rootShadedZones.getChildrenUnmodifiable();
    }
}
