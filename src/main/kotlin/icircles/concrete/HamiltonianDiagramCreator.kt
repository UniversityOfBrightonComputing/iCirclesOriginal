package icircles.concrete

import icircles.abstractdescription.AbstractBasicRegion
import icircles.abstractdescription.AbstractCurve
import icircles.abstractdescription.AbstractDescription
import icircles.decomposition.DecomposerFactory
import icircles.graph.EulerDualEdge
import icircles.graph.EulerDualNode
import icircles.graph.MED
import icircles.guifx.SettingsController
import icircles.recomposition.BetterBasicRecomposer
import icircles.util.CannotDrawException
import icircles.util.Profiler
import javafx.collections.FXCollections
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.*
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * Diagram creator that uses Hamiltonian cycles to ensure
 * that any diagram description is drawable.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class HamiltonianDiagramCreator(val settings: SettingsController) {

    companion object {
        private val log = LogManager.getLogger(HamiltonianDiagramCreator::class.java)

        @JvmField val BASE_CURVE_RADIUS = 1500.0
    }

    /**
     * Maps abstract curve to its concrete version.
     * This is a 1 to 1 map since there are no duplicates.
     */
    val curveToContour = FXCollections.observableMap(LinkedHashMap<AbstractCurve, Contour>())

    /**
     * Abstract zones we **currently** occupy.
     */
    private val abstractZones = ArrayList<AbstractBasicRegion>()
    val concreteShadedZones = ArrayList<ConcreteZone>()

    lateinit var modifiedDual: MED

    val debugPoints = ArrayList<Point2D>()

    fun createDiagram(description: AbstractDescription) {

        // all we need is decomposition; recomposition is almost no-op
        val dSteps = DecomposerFactory.newDecomposer(settings.decompType).decompose(description)
        val rSteps = BetterBasicRecomposer(null).recompose(dSteps)

        for (i in rSteps.indices) {
            // no duplicates, so just single data
            val data = rSteps[i].addedContourData[0]

            if (i == 0) {

                // base case 1 curve
                val contour = CircleContour(BASE_CURVE_RADIUS, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else if (i == 1) {

                // base case 2 curves
                val contour = CircleContour((BASE_CURVE_RADIUS + 0) * 2, BASE_CURVE_RADIUS, BASE_CURVE_RADIUS, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else if (i == 2) {

                // base case 3 curves
                val contour = CircleContour((BASE_CURVE_RADIUS + 0) * 1.5, BASE_CURVE_RADIUS * 2, BASE_CURVE_RADIUS, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else {    // evaluating 4th+ curve

                createMED()

                log.trace("Searching cycle with zones: ${data.splitZones}")

                val cycle = modifiedDual.computeCycle(
                        data.splitZones
                )
                        // if the rest of the app worked properly, this will never happen because there is >= 1 Hamiltonian cycles
                .orElseThrow { CannotDrawException("Failed to find cycle") }

                var contour: Contour = PathContour(data.addedCurve, cycle.path)

                // smooth curves if required
                if (settings.useSmooth()) {

                    Profiler.start("Smoothing")

                    val pathSegments = BezierApproximation.pathThruPoints(cycle.nodes.map { it.point }.toMutableList(), settings.smoothFactor)

                    val newPath = Path()

                    // add moveTo
                    newPath.elements.add(cycle.path.elements[0])

                    for (j in cycle.nodes.indices) {
                        val node1 = cycle.nodes[j]
                        val node2 = if (j == cycle.nodes.size - 1) cycle.nodes[0] else cycle.nodes[j + 1]

                        // check if this is the MED ring segment
                        if (node1.zone.abstractZone == AbstractBasicRegion.OUTSIDE && node2.zone.abstractZone == AbstractBasicRegion.OUTSIDE) {
                            // j + 1 because we skip the first moveTo
                            newPath.elements.addAll(cycle.path.elements[j + 1])
                            continue
                        }

                        // the new curve segment must pass through the straddled curve
                        // and only through that curve
                        val curve = node1.zone.abstractZone.getStraddledContour(node2.zone.abstractZone).get()

                        if (isOK(pathSegments[j], curve, curveToContour.values.toList())) {
                            // remove first moveTo
                            pathSegments[j].elements.removeAt(0)

                            // add to new path
                            newPath.elements.addAll(pathSegments[j].elements)
                        } else {
                            // j + 1 because we skip the first moveTo
                            newPath.elements.addAll(cycle.path.elements[j + 1])
                        }
                    }

                    newPath.fill = Color.TRANSPARENT
                    newPath.elements.add(ClosePath())

                    contour = PathContour(data.addedCurve, newPath)

                    Profiler.end("Smoothing")
                }

                curveToContour[data.addedCurve] = contour

                // we might've used more zones to get a cycle, so we make sure we capture all of the used ones
                // we also call distinct() to ensure we don't reuse the outside zone more than once
                abstractZones.addAll(cycle.nodes.map { it.zone.abstractZone.moveInside(data.addedCurve) }.distinct())
            }
        }

        // create MED for final diagram if required
        if (settings.showMED())
            createMED()

        log.trace("Generating shaded zones")

        val shaded = abstractZones.minus(description.zones)

        concreteShadedZones.addAll(shaded.map { makeConcreteZone(it) })
    }

    /**
     * Creates a concrete zone out of an abstract zone.
     *
     * @param zone the abstract zone
     * @return the concrete zone
     */
    private fun makeConcreteZone(zone: AbstractBasicRegion): ConcreteZone {
        val includingCircles = ArrayList<Contour>()
        val excludingCircles = ArrayList<Contour>(curveToContour.values)

        for (curve in zone.inSet) {
            val contour = curveToContour[curve]

            excludingCircles.remove(contour)
            includingCircles.add(contour!!)
        }

        val cz = ConcreteZone(zone, includingCircles, excludingCircles)

        // TODO: make global bbox
        cz.bbox = javafx.scene.shape.Rectangle(10000.0, 10000.0)
        cz.bbox.translateX = -3000.0
        cz.bbox.translateY = -3000.0

        return cz
    }

    /**
     * Needs to be generated every time because contours change zones.
     *
     * TODO: we could potentially only compute zones that have been changed by the curve
     */
    private fun createMED() {
        log.trace("Creating MED")

        Profiler.start("Creating MED")

        val concreteZones = abstractZones.map { makeConcreteZone(it) }

        val bounds = concreteZones.map { it.shape.layoutBounds }

        val minX = bounds.map { it.minX }.min()
        val minY = bounds.map { it.minY }.min()
        val maxX = bounds.map { it.maxX }.max()
        val maxY = bounds.map { it.maxY }.max()

        val center = Point2D((minX!! + maxX!!) / 2, (minY!! + maxY!!) / 2)

        val w = (maxX - minX) / 2
        val h = (maxY - minY) / 2

        // half diagonal of the bounds rectangle
        val radius = Math.sqrt(w*w + h*h) + settings.medSize  // how much bigger is the MED

        modifiedDual = MED(concreteZones, curveToContour.values.toList())

        log.trace("MED is constructed")

        val outside = makeConcreteZone(AbstractBasicRegion.OUTSIDE)

        val nodesMED = ArrayList<EulerDualNode>()

        log.trace("Adding extra nodes")

        modifiedDual.nodes
                .filter { it.zone.isTopologicallyAdjacent(outside) }
                .forEach {
                    val vectorToMED = it.zone.center.subtract(center)
                    val length = radius - vectorToMED.magnitude()

                    // from zone center to closest point on MED
                    val vector = vectorToMED.normalize().multiply(length)

                    val p1 = it.zone.center
                    val p2 = it.zone.center.add(vector)

                    // TODO: this can be replaced with a line

                    val q = QuadCurve()
                    q.fill = null
                    q.stroke = Color.BLACK
                    q.startX = p1.x
                    q.startY = p1.y
                    q.endX = p2.x
                    q.endY = p2.y

                    q.controlX = p1.midpoint(p2).x
                    q.controlY = p1.midpoint(p2).y

                    // make "distinct" nodes so that jgrapht doesn't think it's a loop
                    val node = EulerDualNode(makeConcreteZone(AbstractBasicRegion.OUTSIDE), p2)
                    nodesMED.add(node)
                    modifiedDual.edges.add(EulerDualEdge(it, node, q))
                }

        modifiedDual.nodes.addAll(nodesMED)

        // sort nodes along the MED ring
        // sorting is CCW from 0 (right) to 360
        Collections.sort(nodesMED, { node1, node2 ->
            val v1 = node1.point.subtract(center)
            val angle1 = vectorToAngle(v1)

            val v2 = node2.point.subtract(center)
            val angle2 = vectorToAngle(v2)

            (angle1 - angle2).toInt()
        })

        log.trace("Adding extra edges")

        for (i in nodesMED.indices) {
            val node1 = nodesMED[i]
            val node2 = if (i == nodesMED.size - 1) nodesMED[0] else nodesMED[i+1]

            val p1 = node1.point
            val p2 = node2.point

            val v1 = node1.point.subtract(center)
            val angle1 = vectorToAngle(v1)

            val v2 = node2.point.subtract(center)
            val angle2 = vectorToAngle(v2)

            println("Angle1: $angle1")

            // extent of arc in degrees
            var extent = angle2 - angle1

            if (extent < 0) {
                extent += 360
            }

            val arc = Arc(center.x, center.y, radius, radius, angle1, extent)

            with(arc) {
                fill = null
                stroke = Color.BLACK

                userData = p1.to(p2)
                properties["sweep"] = angle1 < angle2
            }

            modifiedDual.edges.add(EulerDualEdge(node1, node2, arc))
        }

        Profiler.end("Creating MED")

        log.trace("Generating cycles")

        modifiedDual.initCycles()
    }

    /**
     * @return angle in [0..360]
     */
    private fun vectorToAngle(v: Point2D): Double {
        var angle = -Math.toDegrees(Math.atan2(v.y, v.x))

        if (angle < 0) {
            val delta = 180 - (-angle)
            angle = delta + 180
        }

        return angle
    }

    /**
     * Does curve segment [q] only pass through [actual] curve.
     */
    fun isOK(q: Shape, actual: AbstractCurve, curves: List<Contour>): Boolean {
        val list = curves.filter {
            val s = it.shape
            s.fill = null
            s.stroke = Color.BROWN

            !Shape.intersect(s, q).getLayoutBounds().isEmpty()
        }

        if (list.size != 1)
            return false

        return list.get(0).curve == actual
    }







    // 4 nodes can (maybe?) make a nice circle

    // a 4 node cluster is currently a minimum, since 2 nodes not a cycle
    //                if (cycle.nodes.size == 4) {
    //
    //                    // scale to make it larger to check intersection
    //                    val bounds = cycle.nodes
    //                            .map { it.zone.shape }
    //                            .map {
    //                                it.scaleX = 1.2
    //                                it.scaleY = 1.2
    //                                it
    //                            }
    //                            .reduceRight { s1, s2 -> Shape.intersect(s1, s2) }
    //                            .layoutBounds
    //
    //                    // scale back
    //                    cycle.nodes.map { it.zone.shape }.forEach {
    //                        it.scaleX = 1.0
    //                        it.scaleY = 1.0
    //                    }
    //
    //                    val center = Point2D(bounds.minX + bounds.width / 2, bounds.minY + bounds.height / 2)
    //
    //                    //val minRadius = BASE_CURVE_RADIUS / 5
    //
    //                    val minRadius = Math.min(bounds.width, bounds.height) / 2
    //
    //                    //println(center)
    //                    //debugPoints.add(center)
    //
    //                    contour = CircleContour(center.x, center.y, minRadius, data.addedCurve)
    //                }
}