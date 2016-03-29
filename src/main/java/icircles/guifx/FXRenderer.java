package icircles.guifx;

import icircles.concrete.*;
import icircles.graph.EulerDualGraph;
import icircles.gui.Renderer;
import javafx.geometry.Point2D;
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
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class FXRenderer extends Pane implements Renderer {

    private Pane rootShadedZones = new Pane();
    private Canvas canvas = new Canvas();

    private Pane freeRoot = new Pane();

    private GraphicsContext g;

    public FXRenderer() {
        canvas.setMouseTransparent(true);
        g = canvas.getGraphicsContext2D();

        freeRoot.setMouseTransparent(true);

        getChildren().addAll(rootShadedZones, freeRoot, canvas);
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
        //normalZones.removeIf(z -> z.getContainingContours().isEmpty());

        for (ConcreteZone zone : normalZones)
            drawNormalZone(zone, bbox);

        for (CircleContour contour : diagram.getCircles())
            drawCircleContour(contour);

//        for (PolygonContour contour : diagram.getContours())
//            drawPolygonContour(contour);






        freeRoot.getChildren().clear();

//        for (Shape shape : diagram.shapes) {
//            freeRoot.getChildren().addAll(shape);
//        }

        for (PathContour contour : diagram.getContours()) {
            freeRoot.getChildren().addAll(contour.getShape());
        }


        EulerDualGraph dual = new EulerDualGraph(diagram);
        drawPoints(dual.getNodes().stream().map(n -> n.getZone().getCenter()).collect(Collectors.toList()));

        dual.getEdges().forEach(q -> freeRoot.getChildren().addAll(q));
    }

    private void drawShadedZone(ConcreteZone zone, Rectangle bbox) {
        Shape shape = bbox;

        for (Contour contour : zone.getContainingContours()) {
            shape = Shape.intersect(shape, contour.getShape());
        }

        for (Contour contour : zone.getExcludingContours()) {
            shape = Shape.subtract(shape, contour.getShape());
        }

        Tooltip.install(shape, new Tooltip(zone.toDebugString()));
        shape.setUserData(zone);
        shape.setFill(Color.LIGHTGREY);
        rootShadedZones.getChildren().add(shape);
    }

    private void drawNormalZone(ConcreteZone zone, Rectangle bbox) {
        //System.out.println(zone.toDebugString());

        Shape shape = bbox;

        for (Contour contour : zone.getContainingContours()) {
            shape = Shape.intersect(shape, contour.getShape());
        }

        for (Contour contour : zone.getExcludingContours()) {
            shape = Shape.subtract(shape, contour.getShape());
        }

        Tooltip.install(shape, new Tooltip(zone.toDebugString()));
        shape.setUserData(zone);
        shape.setFill(Color.TRANSPARENT);
        rootShadedZones.getChildren().add(shape);
    }

    private void drawCircleContour(CircleContour contour) {
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

    private void drawPolygonContour(PolygonContour contour) {
        g.setStroke(Color.BLUE);

        double[] xPoints = new double[contour.getCriticalPoints().size()];
        double[] yPoints = new double[contour.getCriticalPoints().size()];

        int i = 0;
        for (Point2D p : contour.getCriticalPoints()) {
            xPoints[i] = p.getX();
            yPoints[i] = p.getY();
            i++;

            //System.out.println("Points: " + p.x + " " + p.y);
        };

        g.strokePolygon(xPoints, yPoints, xPoints.length);
    }

    private void drawPoints(List<Point2D> points) {
        points.forEach(p -> {
            g.fillOval(p.getX() - 5, p.getY() - 5, 10, 10);
        });
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
