package icircles.concrete;

import icircles.abstractdescription.AbstractBasicRegion;
import icircles.abstractdescription.AbstractCurve;
import icircles.abstractdescription.AbstractDescription;
import icircles.decomposition.DecompositionStep;
import icircles.gui.CirclesPanel;
import icircles.recomposition.RecompositionData;
import icircles.recomposition.RecompositionStep;
import icircles.util.CannotDrawException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DiagramCreator {

    private static final Logger log = LogManager.getLogger(DiagramCreator.class);

    private static final int SMALLEST_RADIUS = 3;

    private final List<DecompositionStep> d_steps;
    private final List<RecompositionStep> r_steps;

    private Map<AbstractBasicRegion, Double> zoneScores;
    private Map<AbstractCurve, Double> contourScores;

    /**
     * K - abstract curve
     * V - suggested radius for the curve
     */
    private Map<AbstractCurve, Double> guideSizes;

    /**
     * Holds the results so far.
     * K - AbstractCurve
     * V - CircleCountour representing AbstractCurve
     */
    private Map<AbstractCurve, CircleContour> map = new HashMap<>();

    /**
     * Contours for the drawn concrete diagram.
     */
    private List<CircleContour> circles;

    public DiagramCreator(List<DecompositionStep> d_steps, List<RecompositionStep> r_steps) {
        this.d_steps = d_steps;
        this.r_steps = r_steps;
    }

    public ConcreteDiagram createDiagram(int size) throws CannotDrawException {
        // also computes zone scores
        makeGuideSizes();

        circles = new ArrayList<>();
        createCircles();

        CircleContour.fitCirclesToSize(circles, size);

        List<ConcreteZone> shadedZones = createShadedZones();
        return new ConcreteDiagram(new Rectangle2D.Double(0, 0, size, size), circles, shadedZones);
    }

    private void makeGuideSizes() {
        guideSizes = new HashMap<>();
        if (r_steps.isEmpty()) {
            return;
        }

        AbstractDescription finalDiagram = getFinalDiagram();

        zoneScores = new HashMap<>();
        contourScores = new HashMap<>();
        double totalScore = 0.0;

        for (AbstractBasicRegion zone : finalDiagram.getCopyOfZones()) {
            double score = computeZoneScore(zone, finalDiagram);
            totalScore += score;
            zoneScores.put(zone, score);
        }

        for (AbstractCurve curve : finalDiagram.getCopyOfContours()) {
            double score = 0;

            for (AbstractBasicRegion zone : finalDiagram.getCopyOfZones()) {
                if (zone.contains(curve)) {
                    score += zoneScores.get(zone);
                }
            }

            contourScores.put(curve, score);
            double guideSize = Math.exp(0.75 * Math.log(score / totalScore)) * 200;
            guideSizes.put(curve, guideSize);
        }

        log.trace("Guide sizes: " + guideSizes);
    }

    private double computeZoneScore(AbstractBasicRegion abr, AbstractDescription context) {
        return 1.0;
    }

    /**
     * Creates shaded (extra) zones based on the difference
     * between the initial diagram and final diagram.
     * In other words, finds which zones in final diagram were not in initial diagram.
     *
     * @return list of shaded zones
     */
    private List<ConcreteZone> createShadedZones() {
        List<ConcreteZone> result = new ArrayList<>();
        if (d_steps.isEmpty()) {
            return result;
        }

        AbstractDescription initialDiagram = d_steps.get(0).from();
        AbstractDescription finalDiagram = getFinalDiagram();

        log.trace("Initial diagram zones: " + initialDiagram);
        log.trace("Final diagram zones:   " + finalDiagram);

        for (AbstractBasicRegion zone : finalDiagram.getCopyOfZones()) {
            if (!initialDiagram.hasLabelEquivalentZone(zone)) {
                log.trace("Extra zone: " + zone);
                result.add(makeConcreteZone(zone));
            }
        }

        return result;
    }

    /**
     * Creates a concrete zone out of an abstract zone.
     *
     * @param zone the abstract zone
     * @return the concrete zone
     */
    private ConcreteZone makeConcreteZone(AbstractBasicRegion zone) {
        List<CircleContour> includingCircles = new ArrayList<>();
        List<CircleContour> excludingCircles = new ArrayList<>(circles);

        for (AbstractCurve curve : zone.getCopyOfContours()) {
            CircleContour contour = map.get(curve);
            excludingCircles.remove(contour);
            includingCircles.add(contour);
        }

        return new ConcreteZone(zone, includingCircles, excludingCircles);
    }

    private BuildStep makeBuildSteps() {
        BuildStep head = null;
        BuildStep tail = null;

        for (RecompositionStep rs : r_steps) {
            Iterator<RecompositionData> it = rs.getRecompIterator();
            while (it.hasNext()) {
                RecompositionData rd = it.next();
                BuildStep newOne = new BuildStep(rd);
                if (head == null) {
                    head = newOne;
                    tail = newOne;
                } else {
                    tail.next = newOne;
                    tail = newOne;
                }
            }
        }

        return head;
    }

    /**
     *
     * @param step
     * @param outerBox
     * @return true if need to skip step loop
     * @throws CannotDrawException
     */
    private boolean addSymmetricalNestedContours(BuildStep step, Rectangle2D.Double outerBox) throws CannotDrawException {
        RecompositionData rd = step.recomp_data.get(0);
        AbstractBasicRegion zone = rd.splitZones.get(0);

        double suggestedRadius = guideSizes.get(rd.addedCurve);

        List<AbstractCurve> curves = step.recomp_data.stream()
                .map(d -> d.addedCurve)
                .collect(Collectors.toList());

        // put contours into a zone
        List<CircleContour> contours = findCircleContours(outerBox, SMALLEST_RADIUS, suggestedRadius,
                zone, getFinalDiagram(), curves);

        if (contours != null && !contours.isEmpty()) {
            if (contours.size() != step.recomp_data.size())
                throw new CannotDrawException("Not enough circles for rds");

            for (int i = 0; i < contours.size(); i++) {
                CircleContour contour = contours.get(i);
                AbstractCurve curve = step.recomp_data.get(i).addedCurve;

                if (contour.ac.getLabel() != curve.getLabel())
                    throw new CannotDrawException("Mismatched labels");

                map.put(curve, contour);
                addCircle(contour);
            }
            return true;
        }

        return false;
    }

    /**
     *
     * @param step
     * @param outerBox
     * @return true if skip step loop
     */
    private boolean addSymmetricalSinglePiercingContours(BuildStep step, Rectangle2D.Double outerBox) {
        // Look at the 1st 1-piercing
        RecompositionData rd0 = step.recomp_data.get(0);
        AbstractBasicRegion abr0 = rd0.splitZones.get(0);
        AbstractBasicRegion abr1 = rd0.splitZones.get(1);
        AbstractCurve piercingCurve = rd0.addedCurve;

        AbstractCurve pierced_ac = abr0.getStraddledContour(abr1);
        CircleContour pierced_cc = map.get(pierced_ac);
        ConcreteZone cz0 = makeConcreteZone(abr0);
        ConcreteZone cz1 = makeConcreteZone(abr1);
        Area a = new Area(cz0.getShape(outerBox));
        a.add(cz1.getShape(outerBox));

        double suggested_radius = guideSizes.get(piercingCurve);

        // We have made a piercing which is centred on the circumference of circle c.
        // but if the contents of rd.addedCurve are not equally balanced between
        // things inside c and things outside, we may end up squashing lots
        // into half of rd.addedCurve, leaving the other half looking empty.
        // See if we can nudge c outwards or inwards to accommodate
        // its contents.

        // iterate through zoneScores, looking for zones inside c,
        // then ask whether they are inside or outside cc.  If we
        // get a big score outside, then try to move c outwards.

        double score_in_c = 0.0;
        double score_out_of_c = 0.0;

        double center_of_circle_lies_on_rad = pierced_cc.radius;

        Set<AbstractBasicRegion> allZones = zoneScores.keySet();
        for (AbstractBasicRegion zone : allZones) {
            log.info("compare " + zone + " against " + piercingCurve);

            if (!zone.contains(piercingCurve))
                continue;

            log.info("OK. " + zone + " is in " + piercingCurve + ", so compare against " + pierced_ac);

            double zoneScore = zoneScores.get(zone);

            if (zone.contains(pierced_ac))
                score_in_c += zoneScore;
            else
                score_out_of_c += zoneScore;
        }

        log.trace("scores for " + piercingCurve + " are inside=" + score_in_c + " and outside=" + score_out_of_c);

        if (score_out_of_c > score_in_c) {
            double nudge = suggested_radius * 0.3;
            center_of_circle_lies_on_rad += nudge;
        } else if (score_out_of_c < score_in_c) {
            double nudge = Math.min(suggested_radius * 0.3, (pierced_cc.radius * 2 - suggested_radius) * 0.5) ;
            center_of_circle_lies_on_rad -= nudge;
        }

        double guide_radius = guideSizes.get(step.recomp_data.get(0).addedCurve);
        int sampleSize = (int) (Math.PI / Math.asin(guide_radius / pierced_cc.radius));
        if (sampleSize >= step.recomp_data.size()) {
            int num_ok = 0;
            for (int i = 0; i < sampleSize; i++) {
                double angle = i * Math.PI * 2.0 / sampleSize;
                double x = pierced_cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                double y = pierced_cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                if (a.contains(x, y)) {
                    CircleContour sample = new CircleContour(x, y, guide_radius, step.recomp_data.get(0).addedCurve);
                    if (containedIn(sample, a)) {
                        num_ok++;
                    }
                }
            }

            if (num_ok >= step.recomp_data.size()) {
                if (num_ok == sampleSize) {
                    // all OK.
                    for (int i = 0; i < step.recomp_data.size(); i++) {
                        double angle = 0.0 + i * Math.PI * 2.0 / step.recomp_data.size();
                        double x = pierced_cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                        double y = pierced_cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                        if (a.contains(x, y)) {
                            AbstractCurve added_curve = step.recomp_data.get(i).addedCurve;
                            CircleContour c = new CircleContour(x, y, guide_radius, added_curve);
                            abr0 = step.recomp_data.get(i).splitZones.get(0);
                            abr1 = step.recomp_data.get(i).splitZones.get(1);

                            map.put(added_curve, c);
                            addCircle(c);
                        }
                    }

                    return true;
                } else if (num_ok > sampleSize) {  // BUG?  Doesn't make sense
                    num_ok = 0;
                    for (int i = 0; i < sampleSize; i++) {
                        double angle = 0.0 + i * Math.PI * 2.0 / sampleSize;
                        double x = pierced_cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
                        double y = pierced_cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;
                        if (a.contains(x, y)) {
                            AbstractCurve added_curve = step.recomp_data.get(i).addedCurve;
                            CircleContour c = new CircleContour(x, y, guide_radius, added_curve);
                            if (containedIn(c, a)) {
                                abr0 = step.recomp_data.get(num_ok).splitZones.get(0);
                                abr1 = step.recomp_data.get(num_ok).splitZones.get(1);
                                map.put(added_curve, c);
                                addCircle(c);
                                num_ok++;
                                if (num_ok == step.recomp_data.size()) {
                                    break;
                                }
                            }
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private boolean willPierce(BuildStep bs, AbstractCurve ac) {
        BuildStep future_bs = bs.next;
        while (future_bs != null) {
            if (future_bs.recomp_data.get(0).splitZones.size() == 2) {
                AbstractBasicRegion abr0 = future_bs.recomp_data.get(0).splitZones.get(0);
                AbstractBasicRegion abr1 = future_bs.recomp_data.get(0).splitZones.get(1);
                AbstractCurve ac_future = abr0.getStraddledContour(abr1);
                if (ac_future == ac) {
                    return true;
                }
            }
            future_bs = future_bs.next;
        }

        return false;
    }

    private void addNestedContour(BuildStep bs, AbstractCurve ac, RecompositionData rd,
                                  Rectangle2D.Double outerBox) throws CannotDrawException {

        log.trace("Make a nested contour");

        // look ahead - are we going to add a piercing to this?
        // if so, push it to one side to make space
        boolean willPierce = willPierce(bs, ac);

        // make a circle inside containingCircles, outside excludingCirles.

        AbstractBasicRegion zone = rd.splitZones.get(0);

        double suggestedRadius = guideSizes.get(ac);

        // put contour into a zone
        CircleContour contour = findCircleContour(outerBox, SMALLEST_RADIUS, suggestedRadius,
                zone, getFinalDiagram(), ac);

        if (contour == null) {
            throw new CannotDrawException("Cannot place nested contour");
        }

        if (willPierce && rd.splitZones.get(0).getNumContours() > 0) {
            // nudge to the left
            contour.cx -= contour.radius * 0.5;

            ConcreteZone cz = makeConcreteZone(rd.splitZones.get(0));
            Area a = new Area(cz.getShape(outerBox));
            if (!containedIn(contour, a)) {
                contour.cx += contour.radius * 0.25;
                contour.radius *= 0.75;
            }
        }

        map.put(ac, contour);
        addCircle(contour);
    }

    private void addSinglePiercingContour(AbstractCurve ac, RecompositionData rd, double suggested_radius,
                                          Rectangle2D.Double outerBox) throws CannotDrawException {
        log.trace("Make a single-piercing contour");

        AbstractBasicRegion abr0 = rd.splitZones.get(0);
        AbstractBasicRegion abr1 = rd.splitZones.get(1);
        AbstractCurve c = abr0.getStraddledContour(abr1);
        CircleContour cc = map.get(c);
        ConcreteZone cz0 = makeConcreteZone(abr0);
        ConcreteZone cz1 = makeConcreteZone(abr1);
        Area a = new Area(cz0.getShape(outerBox));
        a.add(cz1.getShape(outerBox));

        // We have made a piercing which is centred on the circumference of circle c.
        // but if the contents of rd.addedCurve are not equally balanced between
        // things inside c and things outside, we may end up squashing lots
        // into half of rd.addedCurve, leaving the other half looking empty.
        // See if we can nudge c outwards or inwards to accommodate
        // its contents.

        // iterate through zoneScores, looking for zones inside c,
        // then ask whether they are inside or outside cc.  If we
        // get a big score outside, then try to move c outwards.

        double score_in_c = 0.0;
        double score_out_of_c = 0.0;

        Set<AbstractBasicRegion> allZones = zoneScores.keySet();
        for (AbstractBasicRegion zone : allZones) {
            log.info("compare " + zone + " against " + c);

            if (!zone.contains(rd.addedCurve))
                continue;

            log.info("OK. " + zone + " is in " + c + ", so compare against " + cc.toDebugString());

            double zoneScore = zoneScores.get(zone);

            if (zone.contains(c))
                score_in_c += zoneScore;
            else
                score_out_of_c += zoneScore;
        }

        log.trace("scores for " + c + " are inside=" + score_in_c + " and outside=" + score_out_of_c);

        double center_of_circle_lies_on_rad = cc.radius;
        double smallest_allowed_radius = SMALLEST_RADIUS;

        if (score_out_of_c > score_in_c) {
            double nudge = suggested_radius * 0.3;
            smallest_allowed_radius += nudge;
            center_of_circle_lies_on_rad += nudge;
        } else if(score_out_of_c < score_in_c) {
            double nudge = Math.min(suggested_radius * 0.3, (cc.radius * 2 - suggested_radius) * 0.5) ;
            smallest_allowed_radius += nudge;
            center_of_circle_lies_on_rad -= nudge;
        }

        // now place circles around cc, checking whether they fit into a
        CircleContour solution = null;
        // loop for different centre placement
        for (AngleIterator ai = new AngleIterator(); ai.hasNext(); ) {
            double angle = ai.nextAngle();
            double x = cc.cx + Math.cos(angle) * center_of_circle_lies_on_rad;
            double y = cc.cy + Math.sin(angle) * center_of_circle_lies_on_rad;

            //check that the centre is ok
            if (a.contains(x, y)) {
                // how big a circle can we make?
                double start_radius = SMALLEST_RADIUS + (solution != null ? solution.radius : 0);

                CircleContour attempt = growCircleContour(a, rd.addedCurve,
                        x, y,
                        suggested_radius,
                        start_radius,
                        smallest_allowed_radius);

                if (attempt != null) {
                    solution = attempt;
                    if (solution.radius == guideSizes.get(ac)) {
                        break; // no need to try any more
                    }
                }

            }
        }

        // no single piercing found which was OK
        if (solution == null) {
            throw new CannotDrawException("1-piercing no fit");
        } else {
            log.trace("Added a single piercing labelled " + solution.ac.getLabel());
            map.put(rd.addedCurve, solution);
            addCircle(solution);
        }
    }

    private void addDoublePiercingContour(RecompositionData rd, double suggested_radius,
                                          Rectangle2D.Double outerBox) throws CannotDrawException {
        log.trace("Make a double-piercing contour");

        AbstractBasicRegion abr0 = rd.splitZones.get(0);
        AbstractBasicRegion abr1 = rd.splitZones.get(1);
        AbstractBasicRegion abr2 = rd.splitZones.get(2);
        AbstractBasicRegion abr3 = rd.splitZones.get(3);
        AbstractCurve c1 = abr0.getStraddledContour(abr1);
        AbstractCurve c2 = abr0.getStraddledContour(abr2);
        CircleContour cc1 = map.get(c1);
        CircleContour cc2 = map.get(c2);

        double[][] intn_coords = intersect(cc1.cx, cc1.cy, cc1.radius,
                cc2.cx, cc2.cy, cc2.radius);
        if (intn_coords == null) {
            throw new CannotDrawException("Double piercing on non-intersecting circles");
        }

        ConcreteZone cz0 = makeConcreteZone(abr0);
        ConcreteZone cz1 = makeConcreteZone(abr1);
        ConcreteZone cz2 = makeConcreteZone(abr2);
        ConcreteZone cz3 = makeConcreteZone(abr3);
        Area a = new Area(cz0.getShape(outerBox));
        a.add(cz1.getShape(outerBox));
        a.add(cz2.getShape(outerBox));
        a.add(cz3.getShape(outerBox));

        double cx, cy;
        if (a.contains(intn_coords[0][0], intn_coords[0][1])) {
            log.trace("intn at (" + intn_coords[0][0] + "," + intn_coords[0][1] + ")");

            cx = intn_coords[0][0];
            cy = intn_coords[0][1];
        } else if (a.contains(intn_coords[1][0], intn_coords[1][1])) {
            log.trace("intn at (" + intn_coords[1][0] + "," + intn_coords[1][1] + ")");

            cx = intn_coords[1][0];
            cy = intn_coords[1][1];
        } else {
            log.trace("No suitable intn for double piercing");
            throw new CannotDrawException("2piercing + disjoint");
        }

        CircleContour solution = growCircleContour(a, rd.addedCurve, cx, cy,
                suggested_radius, SMALLEST_RADIUS, SMALLEST_RADIUS);

        // no double piercing found which was OK
        if (solution == null) {
            throw new CannotDrawException("2piercing no fit");
        } else {
            log.trace("Added a double piercing labelled " + solution.ac.getLabel());
            map.put(rd.addedCurve, solution);
            addCircle(solution);
        }
    }

    private void createCircles() throws CannotDrawException {
        BuildStep bs = makeBuildSteps();

        shuffle_and_combine(bs);

        BuildStep step = bs;
        while (step != null) {
            log.trace("Build step begin");
            Rectangle2D.Double outerBox = CircleContour.makeBigOuterBox(circles);
            
            // we need to add the new curves with regard to their placement
            // relative to the existing ones in the map
            if (step.recomp_data.size() > 1) {
                if (step.recomp_data.get(0).isNested()) {
                    // we have a symmetry of nested contours.
                    // try to add them together
                    boolean skip = addSymmetricalNestedContours(step, outerBox);
                    if (skip) {
                        step = step.next;
                        continue;
                    }
                } else if (step.recomp_data.get(0).isSinglePiercing()) {
                    // we have a symmetry of 1-piercings.
                    // try to add them together
                    boolean skip = addSymmetricalSinglePiercingContours(step, outerBox);
                    if (skip) {
                        step = step.next;
                        continue;
                    }
                }
            }

            // next RecompData in the BuildStep
            for (RecompositionData rd : step.recomp_data) {
                AbstractCurve ac = rd.addedCurve;
                double suggested_radius = guideSizes.get(ac);
                if (rd.isNested()) {
                    addNestedContour(bs, ac, rd, outerBox);
                } else if (rd.isSinglePiercing()) {
                    addSinglePiercingContour(ac, rd, suggested_radius, outerBox);
                } else {
                    addDoublePiercingContour(rd, suggested_radius, outerBox);
                }
            }

            step = step.next;

            log.info("Build step end: " + circles);
        }
    }

    /**
     * Collect together additions which are nested in the same zone.
     *
     * @param bs build steps
     */
    private void combineNestedContourSteps(BuildStep bs) {
        RecompositionData rd = bs.recomp_data.get(0);
        AbstractBasicRegion abr = rd.splitZones.get(0);

        // look ahead - are there other similar nested additions?
        // loop through futurebs's to see if we insert another
        BuildStep beforefuturebs = bs;
        while (beforefuturebs != null && beforefuturebs.next != null) {
            RecompositionData rd2 = beforefuturebs.next.recomp_data.get(0);
            if (rd2.splitZones.size() == 1) {
                AbstractBasicRegion abr2 = rd2.splitZones.get(0);
                if (abr.isLabelEquivalent(abr2)) {
                    log.trace("Found matching abrs " + abr + ", " + abr2);

                    // check scores match
                    double abrScore = contourScores.get(rd.addedCurve);
                    double abrScore2 = contourScores.get(rd2.addedCurve);

                    if (!(abrScore > 0 && abrScore2 > 0))
                        throw new RuntimeException("Zones must have score");

                    log.trace("Matched nestings " + abr + " and " + abr2 + " with scores " + abrScore + " and " + abrScore2);

                    if (abrScore == abrScore2) {
                        // unhook futurebs and insert into list after bs
                        BuildStep to_move = beforefuturebs.next;
                        beforefuturebs.next = to_move.next;

                        bs.recomp_data.add(to_move.recomp_data.get(0));
                    }
                }
            }
            beforefuturebs = beforefuturebs.next;
        }
    }

    /**
     * Collect together additions which are single-piercings with the same zones.
     *
     * @param bs build steps
     */
    private void combineSinglePiercingSteps(BuildStep bs) {
        RecompositionData rd = bs.recomp_data.get(0);
        AbstractBasicRegion abr1 = rd.splitZones.get(0);
        AbstractBasicRegion abr2 = rd.splitZones.get(1);

        // look ahead - are there other similar 1-piercings?
        // loop through futurebs's to see if we insert another
        BuildStep beforefuturebs = bs;
        while (beforefuturebs != null && beforefuturebs.next != null) {
            RecompositionData rd2 = beforefuturebs.next.recomp_data.get(0);
            if (rd2.splitZones.size() == 2) {
                AbstractBasicRegion abr3 = rd2.splitZones.get(0);
                AbstractBasicRegion abr4 = rd2.splitZones.get(1);
                if ((abr1.isLabelEquivalent(abr3) && abr2.isLabelEquivalent(abr4))
                        || (abr1.isLabelEquivalent(abr4) && abr2.isLabelEquivalent(abr3))) {

                    log.trace("Found matching abrs " + abr1 + ", " + abr2);

                    // check scores match
                    double abrScore = contourScores.get(rd.addedCurve);
                    double abrScore2 = contourScores.get(rd2.addedCurve);

                    if (!(abrScore > 0 && abrScore2 > 0))
                        throw new RuntimeException("Zones must have score");

                    log.trace("Matched piercings " + abr1 + " and " + abr2 + " with scores " + abrScore + " and " + abrScore2);

                    if (abrScore == abrScore2) {
                        // unhook futurebs and insert into list after bs
                        BuildStep to_move = beforefuturebs.next;
                        beforefuturebs.next = to_move.next;

                        bs.recomp_data.add(to_move.recomp_data.get(0));
                        continue;
                    }
                }
            }
            beforefuturebs = beforefuturebs.next;
        }
    }

    private void shuffle_and_combine(BuildStep steplist) {
        BuildStep bs = steplist;
        while (bs != null) {
            if (bs.recomp_data.size() != 1)
                throw new RuntimeException("Not ready for multistep");

            RecompositionData rd = bs.recomp_data.get(0);

            if (rd.splitZones.size() == 1) {
                // we are adding a nested contour
                combineNestedContourSteps(bs);
            }
            else if (rd.splitZones.size() == 2) {
                // we are adding a 1-piercing
                combineSinglePiercingSteps(bs);
            }

            bs = bs.next;
        }
    }

    private void addCircle(CircleContour c) {
        log.info("adding " + c.toDebugString());

        circles.add(c);
    }

    private CircleContour growCircleContour(Area a, AbstractCurve ac,
            double cx, double cy,
            double suggested_radius,
            double start_radius,
            double smallest_radius) {

        CircleContour attempt = new CircleContour(cx, cy, suggested_radius, ac);
        if (containedIn(attempt, a)) {
            return new CircleContour(cx, cy, suggested_radius, ac);
        }

        double good_radius = -1.0;
        double radius = start_radius;

        // loop for increasing radii
        while (true) {
            attempt = new CircleContour(cx, cy, radius, ac);
            if (containedIn(attempt, a)) {
                good_radius = radius;
                radius *= 1.5;
            } else {
                break;
            }
        }

        if (good_radius < 0.0) {
            return null;
        }

        return new CircleContour(cx, cy, good_radius, ac);
    }

    private CircleContour findCircleContour(Rectangle2D.Double outerBox,
            int smallest_rad,
            double guide_rad,
            AbstractBasicRegion zone,
            AbstractDescription last_diag,
            AbstractCurve ac) throws CannotDrawException {

        List<AbstractCurve> acs = new ArrayList<>();
        acs.add(ac);
        List<CircleContour> result = findCircleContours(outerBox,
                smallest_rad, guide_rad, zone, last_diag, acs);

        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    private List<CircleContour> findCircleContours(Rectangle2D.Double outerBox,
            int smallest_radius,
            double guide_rad,
            AbstractBasicRegion zone,
            AbstractDescription last_diag,
            List<AbstractCurve> acs) throws CannotDrawException {

        List<CircleContour> result = new ArrayList<>();

        // special case : our first contour
        boolean is_first_contour = !map.keySet().iterator().hasNext();
        if (is_first_contour) {
            int label_index = 0;
            for (AbstractCurve ac : acs) {
                result.add(new CircleContour(
                		outerBox.getCenterX() - 0.5 * (guide_rad * 3 * acs.size()) + 1.5 * guide_rad
                        + guide_rad * 3 * label_index,
                        outerBox.getCenterY(),
                        guide_rad, ac));
                label_index++;
            }

            log.info("added first contours into diagram, labelled " + acs.get(0).getLabel());

            return result;
        }

        if (zone.getNumContours() == 0) {
            // adding a contour outside everything else
            double minx = Double.MAX_VALUE;
            double maxx = Double.MIN_VALUE;
            double miny = Double.MAX_VALUE;
            double maxy = Double.MIN_VALUE;

            for (CircleContour c : circles) {
                if (c.getMinX() < minx) {
                    minx = c.getMinX();
                }
                if (c.getMaxX() > maxx) {
                    maxx = c.getMaxX();
                }
                if (c.getMinY() < miny) {
                    miny = c.getMinY();
                }
                if (c.getMaxY() > maxy) {
                    maxy = c.getMaxY();
                }
            }

            if (acs.size() == 1) {
                if (maxx - minx < maxy - miny) {// R
                    result.add(new CircleContour(
                            maxx + guide_rad * 1.5,
                            (miny + maxy) * 0.5,
                            guide_rad, acs.get(0)));
                } else {// B
                    result.add(new CircleContour(
                            (minx + maxx) * 0.5,
                            maxy + guide_rad * 1.5,
                            guide_rad, acs.get(0)));
                }
            } else if (acs.size() == 2) {
                if (maxx - minx < maxy - miny) {// R
                    result.add(new CircleContour(
                            maxx + guide_rad * 1.5,
                            (miny + maxy) * 0.5,
                            guide_rad, acs.get(0)));
                    result.add(new CircleContour(
                            minx - guide_rad * 1.5,
                            (miny + maxy) * 0.5,
                            guide_rad, acs.get(1)));
                } else {// T
                    result.add(new CircleContour(
                            (minx + maxx) * 0.5,
                            maxy + guide_rad * 1.5,
                            guide_rad, acs.get(0)));
                    result.add(new CircleContour(
                            (minx + maxx) * 0.5,
                            miny - guide_rad * 1.5,
                            guide_rad, acs.get(1)));
                }
            } else {
                if (maxx - minx < maxy - miny) {// R
                    double lowy = (miny + maxy) * 0.5 - 0.5 * acs.size() * guide_rad * 3 + guide_rad * 1.5;
                    for (int i = 0; i < acs.size(); i++) {
                        result.add(new CircleContour(
                                maxx + guide_rad * 1.5,
                                lowy + i * 3 * guide_rad,
                                guide_rad, acs.get(i)));
                    }
                } else {
                    double lowx = (minx + maxx) * 0.5 - 0.5 * acs.size() * guide_rad * 3 + guide_rad * 1.5;
                    for (int i = 0; i < acs.size(); i++) {
                        result.add(new CircleContour(
                                lowx + i * 3 * guide_rad,
                                maxy + guide_rad * 1.5,
                                guide_rad, acs.get(i)));
                    }
                }
            }
            return result;
        }

        ConcreteZone cz = makeConcreteZone(zone);
        Area a = new Area(cz.getShape(outerBox));
        if (a.isEmpty()) {
            throw new CannotDrawException("cannot put a nested contour into an empty region");
        }
        
        //DEB.show(4, a, "area for "+debug_index);

        // special case : one contour inside another with no other interference between
        // look at the final diagram - find the corresponding zone
        //DEB.out(2, "");
        if (zone.getNumContours() > 0 && acs.size() == 1) {
            //System.out.println("look for "+zone.toDebugString()+" in "+last_diag.toDebugString());
            // not the outside zone - locate the zone in the last diag
            AbstractBasicRegion zoneInLast = null;
            Iterator<AbstractBasicRegion> abrIt = last_diag.getZoneIterator();
            while (abrIt.hasNext() && zoneInLast == null) {
                AbstractBasicRegion abrInLast = abrIt.next();
                if (abrInLast.isLabelEquivalent(zone)) {
                    zoneInLast = abrInLast;
                }
            }

            //DEB.assertCondition(zoneInLast != null, "failed to locate zone in final diagram");

            // how many neighbouring abrs?
            abrIt = last_diag.getZoneIterator();
            List<AbstractCurve> nbring_curves = new ArrayList<>();
            while (abrIt.hasNext()) {
                AbstractBasicRegion abrInLast = abrIt.next();
                AbstractCurve ac = zoneInLast.getStraddledContour(abrInLast);
                if (ac != null) {
                    if (ac.getLabel() != acs.get(0).getLabel()) {
                        nbring_curves.add(ac);
                    }
                }
            }

            if (nbring_curves.size() == 1) {
                //  we should use concentric circles

                AbstractCurve acOutside = nbring_curves.get(0);
                // use the centre of the relevant contour

                //DEB.assertCondition(acOutside != null, "did not find containing contour");
                CircleContour ccOutside = map.get(acOutside);
                //DEB.assertCondition(ccOutside != null, "did not find containing circle");

                if (ccOutside != null) {
                    log.info("putting contour " + acs.get(0) + " inside " + acOutside.getLabel());
                    double rad = Math.min(guide_rad, ccOutside.radius - smallest_radius);
                    if (rad > 0.99 * smallest_radius) {
                        // build a co-centric contour
                        CircleContour attempt = new CircleContour(ccOutside.cx, ccOutside.cy, rad, acs.get(0));
                        if (containedIn(attempt, a)) {
                            // shrink the co-centric contour a bit
                            if (rad > 2 * smallest_radius) {
                                attempt = new CircleContour(ccOutside.cx, ccOutside.cy, rad - smallest_radius, acs.get(0));
                            }
                            result.add(attempt);
                            return result;
                        }
                    }
                } else {
                    System.out.println("warning : did not find expected containing circle...");
                }
            } else if (nbring_curves.size() == 2) {
                //  we should put a circle along the line between two existing centres
                AbstractCurve ac1 = nbring_curves.get(0);
                AbstractCurve ac2 = nbring_curves.get(1);

                CircleContour cc1 = map.get(ac1);
                CircleContour cc2 = map.get(ac2);

                if (cc1 != null && cc2 != null) {
                    boolean in1 = zone.contains(ac1);
                    boolean in2 = zone.contains(ac2);

                    double step_c1_c2_x = cc2.cx - cc1.cx;
                    double step_c1_c2_y = cc2.cy - cc1.cy;

                    double step_c1_c2_len = Math.sqrt(step_c1_c2_x * step_c1_c2_x + step_c1_c2_y * step_c1_c2_y);
                    double unit_c1_c2_x = 1.0;
                    double unit_c1_c2_y = 0.0;
                    if (step_c1_c2_len != 0.0) {
                        unit_c1_c2_x = step_c1_c2_x / step_c1_c2_len;
                        unit_c1_c2_y = step_c1_c2_y / step_c1_c2_len;
                    }

                    double p1x = cc1.cx + unit_c1_c2_x * cc1.radius * (in2 ? 1.0 : -1.0);
                    double p2x = cc2.cx + unit_c1_c2_x * cc2.radius * (in1 ? -1.0 : +1.0);
                    double cx = (p1x + p2x) * 0.5;
                    double max_radx = (p2x - p1x) * 0.5;
                    double p1y = cc1.cy + unit_c1_c2_y * cc1.radius * (in2 ? 1.0 : -1.0);
                    double p2y = cc2.cy + unit_c1_c2_y * cc2.radius * (in1 ? -1.0 : +1.0);
                    double cy = (p1y + p2y) * 0.5;
                    double max_rady = (p2y - p1y) * 0.5;
                    double max_rad = Math.sqrt(max_radx * max_radx + max_rady * max_rady);

                    // build a contour
                    CircleContour attempt = new CircleContour(cx, cy, max_rad - smallest_radius, acs.get(0));
                    if (containedIn(attempt, a)) {
                        // shrink the co-centric contour a bit
                        if (max_rad > 3 * smallest_radius) {
                            attempt = new CircleContour(cx, cy, max_rad - 2 * smallest_radius, acs.get(0));
                        } else if (max_rad > 2 * smallest_radius) { // shrink the co-centric contour a bit
                            attempt = new CircleContour(cx, cy, max_rad - smallest_radius, acs.get(0));
                        }
                        result.add(attempt);
                        return result;
                    }
                }
            }
        }

        // special case - inserting a nested contour into a part of a Venn2


        Rectangle bounds = a.getBounds();
        /*
        // try from the middle of the bounds.
        double cx = bounds.getCenterX();
        double cy = bounds.getCenterX();
        if(a.contains(cx, cy))
        {
        if(labels.size() == 1)
        {
        // go for a circle of the suggested size
        CircleContour attempt = new CircleContour(cx, cy, guide_rad, labels.get(0));
        if(containedIn(attempt, a))
        {
        result.add(attempt);
        return result;
        }
        }
        else
        {
        Rectangle box = new Rectangle(cx - guide_rad/2)
        }
        }
         */

        if (acs.get(0) == null)
        	log.info("putting unlabelled contour inside a zone - grid-style");
        else
        	log.info("putting contour " + acs.get(0).getLabel() + " inside a zone - grid-style");

        // Use a grid approach to search for a space for the contour(s)
        int ni = (int) (bounds.getWidth() / smallest_radius) + 1;
        int nj = (int) (bounds.getHeight() / smallest_radius) + 1;
        PotentialCentre contained[][] = new PotentialCentre[ni][nj];
        double basex = bounds.getMinX();
        double basey = bounds.getMinY();

        for (int i = 0; i < ni; i++) {
            double cx = basex + i * smallest_radius;

            for (int j = 0; j < nj; j++) {
                double cy = basey + j * smallest_radius;
                //System.out.println("check for ("+cx+","+cy+") in region");
                contained[i][j] = new PotentialCentre(cx, cy, a.contains(cx, cy));

//                if (DEB.level > 3) {
//                    if (contained[i][j].ok) {
//                        System.out.print("o");
//                    } else {
//                        System.out.print("x");
//                    }
//                }
            }
        }

        // look in contained[] for a large square

        int corneri = -1, cornerj = -1, size = -1;
        boolean isTall = true; // or isWide
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                // biggest possible square?
                int max_sq = Math.min(ni - i, nj - j);
                for (int sq = size + 1; sq < max_sq + 1; sq++) {
                    // scan a square from i, j
                    log.info("look for a box from (" + i + "," + j + ") size " + sq);

                    if (all_ok_in(i, i + (sq * acs.size()) + 1, j, j + sq + 1, contained, ni, nj)) {
                        log.info("found a wide box, corner at (" + i + "," + j + "), size " + sq);
                        corneri = i;
                        cornerj = j;
                        size = sq;
                        isTall = false;
                    } else if (acs.size() > 1 && all_ok_in(i, i + sq + 1, j, j + (sq * acs.size()) + 1, contained, ni, nj)) {
                        log.info("found a tall box, corner at (" + i + "," + j + "), size " + sq);
                        corneri = i;
                        cornerj = j;
                        size = sq;
                        isTall = true;
                    } else {
                        break; // neither wide nor tall worked - move onto next (x, y)
                    }
                }// loop for increasing sizes
            }// loop for j corner
        }// loop for i corner
        //System.out.println("best square is at corner ("+corneri+","+cornerj+"), of size "+size);


        if (size > 0) {
            PotentialCentre pc = contained[corneri][cornerj];
            double radius = size * smallest_radius * 0.5;
            double actualRad = radius;
            if (actualRad > 2 * smallest_radius) {
                actualRad -= smallest_radius;
            } else if (actualRad > smallest_radius) {
                actualRad = smallest_radius;
            }

            // have size, cx, cy
            log.info("corner at " + pc.x + "," + pc.y + ", size " + size);

            List<CircleContour> centredCircles = new ArrayList<>();

            double bx = bounds.getCenterX();
            double by = bounds.getCenterY();

            if (isTall) {
                by -= radius * (acs.size() - 1);
            } else {
                bx -= radius * (acs.size() - 1);
            }

            for (int labelIndex = 0; centredCircles != null && labelIndex < acs.size(); labelIndex++) {
                AbstractCurve ac = acs.get(labelIndex);
                double x = bx;
                double y = by;

                if (isTall) {
                    y += 2 * radius * labelIndex;
                } else {
                    x += 2 * radius * labelIndex;
                }

                CircleContour attempt = new CircleContour(x, y, Math.min(guide_rad, actualRad), ac);
                if (containedIn(attempt, a)) {
                    centredCircles.add(attempt);
                } else {
                    centredCircles = null;
                }
            }

            if (centredCircles != null) {
                result.addAll(centredCircles);
                return result;
            }

            for (int labelIndex = 0; labelIndex < acs.size(); labelIndex++) {
                AbstractCurve ac = acs.get(labelIndex);
                double x = pc.x + radius;
                double y = pc.y + radius;

                if (isTall) {
                    y += 2 * radius * labelIndex;
                } else {
                    x += 2 * radius * labelIndex;
                }

                CircleContour attempt = new CircleContour(x, y, Math.min(guide_rad, actualRad + smallest_radius), ac);
                if (containedIn(attempt, a)) {
                    result.add(attempt);
                } else {
                    result.add(new CircleContour(x, y, actualRad, ac));
                }
            }
            return result;
        } else {
            throw new CannotDrawException("cannot fit nested contour into region");
        }
    }

    private boolean all_ok_in(int lowi, int highi, int lowj, int highj,
                              PotentialCentre[][] ok_array, int Ni, int Nj) {
        boolean all_ok = true;
        for (int i = lowi; all_ok && i < highi + 1; i++) {
            for (int j = lowj; all_ok && j < highj + 1; j++) {
                if (i >= Ni || j >= Nj || !ok_array[i][j].ok) {
                    all_ok = false;
                }
            }
        }
        return all_ok;
    }

    private double[][] intersect(double c1x, double c1y, double rad1,
            double c2x, double c2y, double rad2) {

        double ret[][] = new double[2][2];
        double dx = c1x - c2x;
        double dy = c1y - c2y;
        double d2 = dx * dx + dy * dy;
        double d = Math.sqrt(d2);

        if (d > rad1 + rad2 || d < Math.abs(rad1 - rad2)) {
            return null; // no solution
        }

        double a = (rad1 * rad1 - rad2 * rad2 + d2) / (2 * d);
        double h = Math.sqrt(rad1 * rad1 - a * a);
        double x2 = c1x + a * (c2x - c1x) / d;
        double y2 = c1y + a * (c2y - c1y) / d;


        double paX = x2 + h * (c2y - c1y) / d;
        double paY = y2 - h * (c2x - c1x) / d;
        double pbX = x2 - h * (c2y - c1y) / d;
        double pbY = y2 + h * (c2x - c1x) / d;

        ret[0][0] = paX;
        ret[0][1] = paY;
        ret[1][0] = pbX;
        ret[1][1] = pbY;

        return ret;
    }

    private boolean containedIn(CircleContour c, Area a) {
        Area test = new Area(c.getFatInterior(SMALLEST_RADIUS));
        test.subtract(a);
        return test.isEmpty();
    }

    private void DEB_show_frame(int deb_level, int debug_frame_index, int size) {
//		if(DEB.level<deb_level)
//			return;
		
		// build a ConcreteDiagram for the current collection of circles
		ArrayList<ConcreteZone> shadedZones = new ArrayList<ConcreteZone>();
		
		ArrayList<CircleContour> circles_copy = new ArrayList<CircleContour>();
		for(CircleContour c : circles) {
			circles_copy.add(new CircleContour(c));
		}
        CircleContour.fitCirclesToSize(circles_copy, size);
		ConcreteDiagram cd = new ConcreteDiagram(new Rectangle2D.Double(0, 0, size, size),
	            circles_copy, shadedZones);
	    CirclesPanel cp = new CirclesPanel("toDebugString frame "+debug_frame_index, "no failure",
	    		cd, size, true);
	    //DEB.addFilmStripShot(cp);
    }

    /**
     * @return target abstract description of the last recomposition step
     */
    private AbstractDescription getFinalDiagram() {
        return r_steps.get(r_steps.size() - 1).to();
    }
}

/**
 * A class which will provide an angle between 0 and 2pi. For example, when
 * fitting a single-piercing around part of an already-drawn circle, we could
 * get an angle from this iterator and try putting the center of the piercing
 * circle at that position. To try more positions, add more potential angles to
 * this iterator. The order in which positions are attempted is determined by
 * the order in which this iterator generates possible angles.
 */
class AngleIterator {

    private int[] ints = { 0, 8, 4, 12, 2, 6, 10, 14, 1, 3, 5, 7, 9, 11, 13, 15 };
    private int index = -1;

    public AngleIterator() {
    }

    public boolean hasNext() {
        return index < ints.length - 1;
    }

    public double nextAngle() {
        index++;
        int modIndex = (index % ints.length);
        return Math.PI * 2 * ints[modIndex] / (1.0 * ints.length);
    }
}

class PotentialCentre {

    double x;
    double y;
    boolean ok;

    PotentialCentre(double x, double y, boolean ok) {
        this.x = x;
        this.y = y;
        this.ok = ok;
    }
}
