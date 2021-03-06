package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.abstractdual.AbstractDualEdge;
import icircles.abstractdual.AbstractDualGraph;
import icircles.abstractdual.AbstractDualNode;
import icircles.decomposition.*;
import icircles.recomposition.BetterBasicRecomposer;
import icircles.recomposition.Recomposer;
import icircles.recomposition.RecompositionStep;
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



        // for finding good inbetween point

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



        // a b c ab bc abd bcd
        // a b c ab ag ah bh cf cg agy cgy
        // a b c ab ag ah ax az bh cf cg yz cgx
        // a b c ab bc bd cd abe bcd bce

        // t at bt ct ft abt agt aht bht cft cgt tyz agty atyz cftx cgtx cgty ctyz

        // Examples where wrong

        // a b c d ab ac bc bd be bf cd cf de abc - 2pierce disjoint

        // y Ac Af bc bd bj cf cl de fh hi hq ik iy ky Acf Acl abc bfg fhz hiz - encloses region it shouldnt
        // b c g h q ab ag ah aq adg adh adq

        // a b c d e f ac ae bc be ce acf bce bcf bde - not enough covered

        // a b d ac bc ce bcd bde - when both paths are short need to choose better one
        // a c ab bc cd cf df abc abf ace cde
        // a b c ab ag ah bh cf cg hz yz agy cgy
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

        List<PathContour> contours = new ArrayList<>(diagram.getContours());

        ConcreteDiagram concreteDiagram = new ConcreteDiagram(diagram.getOriginalDescription(), actual,
                diagram.getCircles(), diagram.getCurveToContour(), size, contours.toArray(new PathContour[0]));
        return concreteDiagram;
    }

    private AbstractDescription original;

    @Override
    protected AbstractDescription getInitialDiagram() {
        return original;
    }

    // a b c d e ab bc cd de , af ef  - new algorithm case

    @Override
    public ConcreteDiagram createDiagram(AbstractDescription description, int size) throws CannotDrawException {
        original = description;

        Decomposer decomposer = DecomposerFactory.newDecomposer(DecompositionStrategyType.PIERCED_FIRST);

        Recomposer recomposer = new BetterBasicRecomposer(null);
        List<RecompositionStep> rs = recomposer.recompose(decomposer.decompose(description));

        return super.createDiagram(rs.get(rs.size() - 1).to(), size);
        //return createDiagramConcrete(description, size);
    }

    public ConcreteDiagram createDiagramConcrete(AbstractDescription description, int size) throws CannotDrawException {
        //AbstractCurve.resetIdCounter();
        //AbstractBasicRegion.clearLibrary();

        ConcreteDiagram iCirclesDiagramOriginal = super.createDiagram(description, size);

        Map<AbstractCurve, List<CircleContour> > duplicates = iCirclesDiagramOriginal.findDuplicateContours();
        if (duplicates.isEmpty())
            return iCirclesDiagramOriginal;

        ConcreteDiagram cd = iCirclesDiagramOriginal;

        for (AbstractCurve curve : duplicates.keySet()) {
            ConcreteDiagram iCirclesDiagramNew = removeCurveFromDiagram(curve, cd, size);


            AbstractDescription ad = cd.getActualDescription();
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

            //graph.removeNode(graph.getNodeByZone(AbstractBasicRegion.OUTSIDE));

            List<AbstractDualNode> nodesToSplit = new ArrayList<>();

            for (int i = 0; i < zones.size(); i++) {
                int j = i + 1 < zones.size() ? i + 1 : 0;

                AbstractDualNode node1 = graph.getNodeByZone(zones.get(i));
                AbstractDualNode node2 = graph.getNodeByZone(zones.get(j));

                List<AbstractDualNode> nodePath = graph.findShortestVertexPath(node1, node2);

                nodesToSplit.addAll(nodePath);
                nodesToSplit.remove(node2);

                // if first, we keep it
                if (i == 0) {
                    nodePath.remove(node1);
                }

                nodePath.remove(node2);

                // remove visited edges and nodes
                graph.findShortestEdgePath(node1, node2).forEach(graph::removeEdge);
                nodePath.forEach(graph::removeNode);
            }


//            AbstractBasicRegion zoneStart = zones.get(0);
//            AbstractBasicRegion zoneTarget = zones.get(1);
//
//            AbstractDualNode nodeStart = null, nodeTarget = null;
//
//            for (AbstractDualNode node : graph.getNodes()) {
//                if (node.getZone() == zoneStart) {
//                    nodeStart = node;
//                } else if (node.getZone() == zoneTarget) {
//                    nodeTarget = node;
//                }
//            }

//            List<AbstractBasicRegion> zonesToSplit = graph.findCycle(nodeStart, nodeTarget)
//                    .stream()
//                    .map(AbstractDualNode::getZone)
//                    .collect(Collectors.toList());

            List<AbstractBasicRegion> zonesToSplit = nodesToSplit.stream()
                    .map(AbstractDualNode::getZone)
                    .collect(Collectors.toList());

            log.debug("Zones to split: " + zonesToSplit);

            // convert abstract to concrete zones
            List<ConcreteZone> concreteZones = zonesToSplit.stream()
                    .map(zone -> {
                        for (ConcreteZone cz : iCirclesDiagramNew.getAllZones()) {
                            if (cz.getAbstractZone() == zone) {
                                return cz;
                            }
                        }

                        log.trace("No concrete zone for zone: " + zone);

                        return null;
                    })
                    .collect(Collectors.toList());

            // find 'center' points of each zone
            List<Point2D> points = concreteZones.stream()
                    .map(ConcreteZone::getCenter)
                    .collect(Collectors.toList());

            Path path = new Path();
            MoveTo moveTo = new MoveTo(points.get(0).getX(), points.get(0).getY());
            path.getElements().addAll(moveTo);

            for (int i = 0; i < concreteZones.size(); i++) {
                int second = i + 1 < points.size() ? i + 1 : 0;

                Point2D p1 = points.get(i);
                Point2D p2 = points.get(second);

                QuadCurve q = new QuadCurve();
                q.setFill(null);
                q.setStroke(Color.BLACK);
                q.setStartX(p1.getX());
                q.setStartY(p1.getY());
                q.setEndX(p2.getX());
                q.setEndY(p2.getY());
                q.setControlX((p1.getX() + p2.getX()) / 2);
                q.setControlY((p1.getY() + p2.getY()) / 2);

                double x = (p1.getX() + p2.getX()) / 2;
                double y = (p1.getY() + p2.getY()) / 2;

                int step = 10;
                int safetyCount = 0;

                Point2D delta = new Point2D(step, 0);
                int j = 0;

                while (!isOK(q, concreteZones.get(i), concreteZones.get(second), iCirclesDiagramNew.getNormalZones()) && safetyCount < 100) {
                    q.setControlX(x + delta.getX());
                    q.setControlY(y + delta.getY());

                    j++;

                    switch (j) {
                        case 1:
                            delta = new Point2D(step, step);
                            break;
                        case 2:
                            delta = new Point2D(0, step);
                            break;
                        case 3:
                            delta = new Point2D(-step, step);
                            break;
                        case 4:
                            delta = new Point2D(-step, 0);
                            break;
                        case 5:
                            delta = new Point2D(-step, -step);
                            break;
                        case 6:
                            delta = new Point2D(0, -step);
                            break;
                        case 7:
                            delta = new Point2D(step, -step);
                            break;
                    }

                    if (j == 8) {
                        j = 0;
                        delta = new Point2D(step, 0);
                        step *= 2;
                    }

                    safetyCount++;
                }

                // we failed to find the correct spot
                if (safetyCount == 100) {
                    log.trace("Failed to find correct control point");
                    q.setControlX(x);
                    q.setControlY(y);
                }

                QuadCurveTo quadCurveTo = new QuadCurveTo();
                quadCurveTo.setX(q.getEndX());
                quadCurveTo.setY(q.getEndY());
                quadCurveTo.setControlX(q.getControlX());
                quadCurveTo.setControlY(q.getControlY());

                path.getElements().addAll(quadCurveTo);
            }

            // create new contour
            PathContour contour = new PathContour(curve, path);

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

            // put mapping from abstract to conrete curve
            iCirclesDiagramNew.getCurveToContour().put(curve, contour);

            List<PathContour> contours = new ArrayList<>(iCirclesDiagramNew.getContours());
            contours.add(contour);

            cd = new ConcreteDiagram(iCirclesDiagramNew.getOriginalDescription(), actual,
                    iCirclesDiagramNew.getCircles(), iCirclesDiagramNew.getCurveToContour(), size, contours.toArray(new PathContour[0]));
        }

        return cd;
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
