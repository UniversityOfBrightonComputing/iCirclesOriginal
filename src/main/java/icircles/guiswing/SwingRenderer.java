package icircles.guiswing;

import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;
import icircles.geometry.Rectangle;
import icircles.gui.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class SwingRenderer extends JPanel implements Renderer {

    private Graphics2D g;

    private ConcreteDiagram lastDrawnDiagram;

    public SwingRenderer() {
        setBackground(Color.WHITE);
    }

    @Override
    public void paint(Graphics g) {
        this.g = (Graphics2D)g;
        this.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.g.setStroke(new BasicStroke(2));

        super.paint(g);
        drawImpl(lastDrawnDiagram);
    }

    @Override
    public void draw(ConcreteDiagram diagram) {
        lastDrawnDiagram = diagram;
        Rectangle2D.Double bbox = toSwingRectangle(diagram.getBoundingBox());

        setPreferredSize(new Dimension((int)bbox.getWidth() + 5, (int)bbox.getHeight() + 5));

        repaint();
    }

    private void drawImpl(ConcreteDiagram diagram) {
        Rectangle2D.Double bbox = toSwingRectangle(diagram.getBoundingBox());

        for (ConcreteZone zone : diagram.getShadedZones())
            drawShadedZone(zone, bbox);

        for (CircleContour contour : diagram.getCircles())
            drawContour(contour);
    }

    private void drawShadedZone(ConcreteZone zone, Rectangle2D.Double bbox) {
        Area area = new Area(bbox);

        for (CircleContour c : zone.getContainingCircles()) {
            area.intersect(new Area(makeEllipse(c.getCenterX(), c.getCenterY(), c.getBigRadius())));
        }

        for (CircleContour c : zone.getExcludingCircles()) {
            area.subtract(new Area(makeEllipse(c.getCenterX(), c.getCenterY(), c.getSmallRadius())));
        }

        g.setColor(Color.LIGHT_GRAY);
        g.fill(area);
    }

    private void drawContour(CircleContour contour) {
        g.setColor(Color.BLUE);

        double radius = contour.getRadius();
        double x = contour.getCenterX() - radius;
        double y = contour.getCenterY() - radius;
        double w = 2 * radius;
        double h = 2 * radius;

        Ellipse2D.Double circle = new Ellipse2D.Double(x, y, w, h);

        g.draw(circle);

        // draw label
        g.setColor(Color.BLACK);
        g.drawString(contour.ac.getLabel().getLabel(), (int) contour.getLabelXPosition(), (int) contour.getLabelYPosition());
    }

    private Rectangle2D.Double toSwingRectangle(Rectangle rectangle) {
        return new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    private Ellipse2D.Double makeEllipse(double x, double y, double r) {
        return new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
}
