package icircles.concrete;

import icircles.abstractdescription.AbstractDescription;
import icircles.decomposition.Decomposer;
import icircles.decomposition.DecompositionStep;
import icircles.decomposition.DecompositionType;
import icircles.recomposition.Recomposer;
import icircles.recomposition.RecompositionStep;
import icircles.recomposition.RecompositionType;
import icircles.util.CannotDrawException;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

public class ConcreteDiagram {

    private Rectangle2D.Double box;
    private List<CircleContour> circles;
    private List<ConcreteZone> shadedZones;

    public ConcreteDiagram(Rectangle2D.Double box,
            List<CircleContour> circles,
            List<ConcreteZone> shadedZones) {
        this.box = box;
        this.circles = circles;
        this.shadedZones = shadedZones;
    }

    public Rectangle2D.Double getBox() {
        return box;
    }

    public List<CircleContour> getCircles() {
        return circles;
    }

    public List<ConcreteZone> getShadedZones() {
        return shadedZones;
    }

    public static double checksum(List<CircleContour> circles) {
        double result = 0.0;
        if (circles == null) {
            return result;
        }

        Iterator<CircleContour> cIt = circles.iterator();
        while (cIt.hasNext()) {
            CircleContour c = cIt.next();
            result += c.cx * 0.345 + c.cy * 0.456 + c.radius * 0.567 + c.ac.checksum() * 0.555;
            result *= 1.2;
        }
        return result;
    }

    /**
     * This can be used to obtain a drawing of an abstract diagram.
     * @param dType decomposition type
     * @param rType recomposition type
     * @param ad the description to be drawn
     * @param size the size of the drawing panel
     * @return concrete diagram
     * @throws CannotDrawException
     */
    public static ConcreteDiagram makeConcreteDiagram(DecompositionType dType,
                                                      RecompositionType rType,
                                                      AbstractDescription ad, int size) throws CannotDrawException {
        Decomposer d = new Decomposer(dType);
        List<DecompositionStep> d_steps = d.decompose(ad);

        Recomposer r = new Recomposer(rType);
        List<RecompositionStep> r_steps = r.recompose(d_steps);

        DiagramCreator dc = new DiagramCreator(d_steps, r_steps);
        return dc.createDiagram(size);
    }
}
