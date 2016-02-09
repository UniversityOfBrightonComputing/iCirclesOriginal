package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdual.AbstractDualEdge;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import icircles.decomposition.Decomposer;
import icircles.recomposition.Recomposer;
import icircles.util.CannotDrawException;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class BetterDiagramCreator extends DiagramCreator {

    private static final Logger log = LogManager.getLogger(DiagramCreator.class);

    public BetterDiagramCreator(Decomposer decomposer, Recomposer recomposer) {
        super(decomposer, recomposer);

        // solution for a ab abc ac bc abd ad
        // but iCircles currently doesn't know how to add with TP (triple point) so it adds like a 2-piercing

//        RecompositionStep step1 = rSteps.get(1);
//
//        List<AbstractBasicRegion> splitZones = new AbstractDescription("a c ac").getZonesShallowCopy().stream().collect(Collectors.toList());
//        List<AbstractBasicRegion> newZones = new AbstractDescription("ab bc abc").getZonesShallowCopy().stream().filter(z -> !z.getCopyOfContours().isEmpty()).collect(Collectors.toList());
//
//        RecompositionData data = new RecompositionData(new AbstractCurve("b"), splitZones, newZones);
//
//        RecompositionStep step2 = new RecompositionStep(step1.to(), new AbstractDescription("a c ac ab bc abc"),
//                Arrays.asList(data));
//        rSteps.set(2, step2);

        // a b c ab bc abd bcd
        // a b c ab bc bd cd abe bcd bce
        // p q pr ps qr qs rs rt prt qrt rst
    }

    private ConcreteDiagram removeCurveFromDiagram(AbstractCurve curve, ConcreteDiagram diagram, int size) {
        // generate a concrete diagram with the removed curve
        Set<AbstractCurve> newCurves = new TreeSet<>(diagram.getActualDescription().getCurvesUnmodifiable());
        for (Iterator<AbstractCurve> it = newCurves.iterator(); it.hasNext(); ) {
            if (it.next().matchesLabel(curve)) {
                it.remove();
            }
        }

        Set<AbstractBasicRegion> newZones = new TreeSet<>(diagram.getActualDescription().getZonesUnmodifiable());
        for (Iterator<AbstractBasicRegion> it = newZones.iterator(); it.hasNext(); ) {
            AbstractBasicRegion zone = it.next();
            if (zone.containsCurveWithLabel(curve.getLabel())) {
                it.remove();
            }
        }

        AbstractDescription actual = new AbstractDescription(newCurves, newZones);

        diagram.getCircles().removeIf(contour -> {
            return contour.getCurve().matchesLabel(curve);
        });

        ConcreteDiagram concreteDiagram = new ConcreteDiagram(diagram.getOriginalDescription(), actual,
                diagram.getCircles(), diagram.getCurveToContour(), size);
        return concreteDiagram;
    }

    @Override
    public ConcreteDiagram createDiagram(AbstractDescription description, int size) throws CannotDrawException {
        //AbstractCurve.resetIdCounter();
        //AbstractBasicRegion.clearLibrary();

        ConcreteDiagram iCirclesDiagram2 = super.createDiagram(description, size);

        Map<AbstractCurve, List<CircleContour> > duplicates = iCirclesDiagram2.findDuplicateContours();
        if (duplicates.isEmpty())
            return iCirclesDiagram2;

        for (AbstractCurve curve : duplicates.keySet()) {
            ConcreteDiagram iCirclesDiagramNew = removeCurveFromDiagram(curve, iCirclesDiagram2, size);

















            AbstractDescription ad = iCirclesDiagram2.getActualDescription();
            List<AbstractBasicRegion> zones = ad.getZonesUnmodifiable().stream()
                    .filter(z -> z.containsCurveWithLabel(curve.getLabel()))
                    .collect(Collectors.toList());

            log.debug("Zones in " + curve + ":" + zones.toString());

            zones = zones.stream()
                    .map(z -> z.moveOutsideLabel(curve.getLabel()))
                    .collect(Collectors.toList());

            log.debug("Zones that will be in " + curve + ":" + zones.toString());

            AbstractDualGraph graph = new AbstractDualGraph(new ArrayList<>(ad.getZonesUnmodifiable()));

            // TODO: let's assume we found nodes which need to be connected to get a connected graph

            AbstractBasicRegion zoneStart = zones.get(0);
            AbstractBasicRegion zoneTarget = zones.get(1);

            AbstractDualNode nodeStart = null, nodeTarget = null;

            for (AbstractDualNode node : graph.getNodes()) {
                if (node.getZone() == zoneStart) {
                    nodeStart = node;
                } else if (node.getZone() == zoneTarget) {
                    nodeTarget = node;
                }
            }

            List<AbstractBasicRegion> zonesToSplit = graph.findCycle(nodeStart, nodeTarget)
                    .stream()
                    .map(AbstractDualNode::getZone)
                    .collect(Collectors.toList());

            log.debug("Zones to split: " + zonesToSplit);

            List<ConcreteZone> concreteZones = zonesToSplit.stream()
                    .map(zone -> {
                        for (ConcreteZone cz : iCirclesDiagramNew.getAllZones()) {
                            if (cz.getAbstractZone() == zone) {
                                return cz;
                            }
                        }

                        return null;
                    })
                    .collect(Collectors.toList());


//            Point2D p1 = null, p2 = null;
//
//            for (int i = 0; i < concreteZones.size(); i++) {
//                Point2D p = concreteZones.get(i).getCenter();
//
//                if (p.getX() == 500 && p.getY() == 500) {
//                    if (i - 1 >= 0)
//                        p1 = concreteZones.get(i - 1).getCenter();
//                    else
//                        p1 = concreteZones.get(concreteZones.size() - 1).getCenter();
//
//                    if (i + 1 < concreteZones.size())
//                        p2 = concreteZones.get(i + 1).getCenter();
//                    else
//                        p2 = concreteZones.get(0).getCenter();
//
//                    break;
//                }
//            }
//
//            Point2D center = new Point2D(0, 0);
//
//            if (p1 != null && p2 != null) {
//                log.trace(p1.toString());
//                log.trace(p2.toString());
//
//                // if y values are closer than x values then do x / 2
//
//                center = new Point2D((p1.getX() + p2.getX()) / 2, p2.getY());
//
//
//                ConcreteZone outside = iCirclesDiagram.getOutsideZone();
//
//                Shape outsideShape = outside.getShape();
//
//                log.trace(outsideShape.contains(center) + " " + outsideShape.contains(new Point2D(center.getX(), 550)));
//
//            }


            List<Point2D> points = concreteZones.stream()
                    .map(zone -> zone.getCenter())
                    .collect(Collectors.toList());

            List<Shape> shapes = new ArrayList<>();
            out:
            for (int i = 0; i < concreteZones.size(); i++) {
                Point2D p1 = points.get(i);

                int second = i + 1 < points.size() ? i + 1 : 0;

                Point2D p2 = i + 1 < points.size() ? points.get(i + 1) : points.get(0);

                Line line = new Line();
                line.setStartX(p1.getX());
                line.setStartY(p1.getY());
                line.setEndX(p2.getX());
                line.setEndY(p2.getY());

                QuadCurve q = new QuadCurve();
                q.setFill(null);
                q.setStroke(Color.BLACK);
                q.setStartX(p1.getX());
                q.setStartY(p1.getY());
                q.setEndX(p2.getX());
                q.setEndY(p2.getY());
                q.setControlX((p1.getX() + p2.getX()) / 2);
                q.setControlY((p1.getY() + p2.getY()) / 2);

//                for (ConcreteZone zone : iCirclesDiagramNew.getNormalZones()) {
//                    if (zone.intersects(q) && (zone != concreteZones.get(i) && zone != concreteZones.get(second))) {
//                        continue out;
//                    }
//                }

                double x = (p1.getX() + p2.getX()) / 2;
                double y = (p1.getY() + p2.getY()) / 2;

                while (!isOK(q, concreteZones.get(i), concreteZones.get(second), iCirclesDiagramNew.getNormalZones())) {
                    x -= 10;
                    y += 10;

                    q.setControlX(x);
                    q.setControlY(y);
                }

                shapes.add(q);
            }

            PolygonContour contour = new PolygonContour(curve, points);

            List<CircleContour> circles = iCirclesDiagramNew.getCircles();
            circles.removeAll(duplicates.get(curve));

            Set<AbstractCurve> newCurves = new TreeSet<>(iCirclesDiagramNew.getActualDescription().getCurvesUnmodifiable());
            for (Iterator<AbstractCurve> it = newCurves.iterator(); it.hasNext(); ) {
                if (it.next().matchesLabel(curve)) {
                    it.remove();
                }
            }

            newCurves.add(curve);

            // GENERATE ACTUAL DESC
            Set<AbstractBasicRegion> newZones = new TreeSet<>(iCirclesDiagramNew.getActualDescription().getZonesUnmodifiable());
            for (Iterator<AbstractBasicRegion> it = newZones.iterator(); it.hasNext(); ) {
                AbstractBasicRegion zone = it.next();
                if (zone.containsCurveWithLabel(curve.getLabel())) {
                    it.remove();
                }
            }

            // some of it is wrong due to different objects
            for (AbstractBasicRegion zone : zonesToSplit) {
                newZones.add(zone.moveInside(curve));
            }

            AbstractDescription actual = new AbstractDescription(newCurves, newZones);

            iCirclesDiagramNew.getCurveToContour().put(curve, contour);

            // TODO: actual desc is different
            ConcreteDiagram concreteDiagram = new ConcreteDiagram(iCirclesDiagramNew.getOriginalDescription(), actual,
                    circles, iCirclesDiagramNew.getCurveToContour(), size, contour);

            concreteDiagram.shapes.addAll(shapes);

//            if (p1 != null && p2 != null) {
//                Path pathShape = new Path();
//                //pathShape.setFill(Color.BLUEVIOLET);
//
//                QuadCurveTo q = new QuadCurveTo();
//                q.setX(p2.getX());
//                q.setY(p2.getY());
//                q.setControlX(center.getX());
//                q.setControlY(550);
//
//                ArcTo arcTo = new ArcTo();
//                arcTo.setX(p2.getX());
//
//
//
//                MoveTo moveTo = new MoveTo();
//                moveTo.setX(p1.getX());
//                moveTo.setY(p1.getY());
//
//                pathShape.getElements().addAll(moveTo, q);
//                concreteDiagram.shapes.add(pathShape);
//
//                iCirclesDiagram.getAllZones().forEach(zone -> {
//                    if (concreteZones.contains(zone)) {
//                        // CAN USE EMPTY TO CHECK FOR CONTAINMENT
//                        log.trace("True:  " + !Shape.intersect(zone.getShape(), pathShape).getLayoutBounds().isEmpty() + " " + zone.toString());
//                    } else {
//                        log.trace("False: " + !Shape.intersect(zone.getShape(), pathShape).getLayoutBounds().isEmpty() + " " + zone.toString());
//                    }
//                });
//            }

            return concreteDiagram;
        }

        return iCirclesDiagram2;
    }

    private boolean isOK(Shape shape, ConcreteZone zone1, ConcreteZone zone2, List<ConcreteZone> zones) {
        for (ConcreteZone zone : zones) {
            if (zone.intersects(shape) && (zone != zone1 && zone != zone2)) {
                return false;
            }
        }

        return true;
    }
}
