package icircles.guiswing;

import icircles.concrete.CircleContour;
import icircles.concrete.ConcreteDiagram;
import icircles.concrete.ConcreteZone;
import icircles.geometry.Rectangle;
import icircles.gui.Renderer;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class SwingRenderer implements Renderer {

    private Graphics2D g;

    public SwingRenderer(Graphics2D g) {
        this.g = g;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    @Override
    public void draw(ConcreteDiagram diagram) {
        Rectangle2D.Double bbox = toSwingRectangle(diagram.getBoundingBox());

        for (ConcreteZone zone : diagram.getShadedZones())
            drawShadedZone(zone, bbox);
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

    private Rectangle2D.Double toSwingRectangle(Rectangle rectangle) {
        return new Rectangle2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    private Ellipse2D.Double makeEllipse(double x, double y, double r) {
        return new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
    }
}
