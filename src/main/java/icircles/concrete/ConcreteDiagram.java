package icircles.concrete;

import icircles.abstractdescription.AbstractDescription;
import icircles.decomposition.Decomposer;
import icircles.decomposition.DecompositionStep;
import icircles.decomposition.DecompositionStrategy;
import icircles.gui.CirclesPanel;
import icircles.recomposition.Recomposer;
import icircles.recomposition.RecompositionStep;
import icircles.recomposition.RecompositionStrategy;
import icircles.util.CannotDrawException;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

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
     * @param ad the description to be drawn
     * @param size the size of the drawing panel
     * @return concrete diagram
     * @throws CannotDrawException
     */
    public static ConcreteDiagram makeConcreteDiagram(AbstractDescription ad, int size) throws CannotDrawException {
        ArrayList<DecompositionStep> d_steps = new ArrayList<>();
        ArrayList<RecompositionStep> r_steps = new ArrayList<>();
        Decomposer d = new Decomposer(DecompositionStrategy.PIERCEDFIRST);
        d_steps.addAll(d.decompose(ad));

        Recomposer r = new Recomposer(RecompositionStrategy.RECOMPOSE_DOUBLY_PIERCED);
        r_steps.addAll(r.recompose(d_steps));
        DiagramCreator dc = new DiagramCreator(d_steps, r_steps, size);
        return dc.createDiagram(size);
    }
}
