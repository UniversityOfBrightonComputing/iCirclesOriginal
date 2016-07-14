package icircles.concrete

import icircles.abstractdescription.AbstractBasicRegion
import icircles.abstractdescription.AbstractCurve
import icircles.abstractdescription.AbstractDescription
import icircles.decomposition.DecomposerFactory
import icircles.decomposition.DecompositionStrategyType
import icircles.graph.EulerDualEdge
import icircles.graph.EulerDualNode
import icircles.graph.MED
import icircles.recomposition.BetterBasicRecomposer
import icircles.recomposition.RecomposerFactory
import icircles.recomposition.RecompositionStrategyType
import javafx.collections.FXCollections
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.QuadCurve
import javafx.scene.shape.Shape
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class HamiltonianDiagramCreator
: DiagramCreator(DecomposerFactory.newDecomposer(DecompositionStrategyType.INNERMOST),
        //RecomposerFactory.newRecomposer(RecompositionStrategyType.DOUBLY_PIERCED_EXTRA_ZONES)
        BetterBasicRecomposer(null)) {

    val curveToContour = FXCollections.observableMap(LinkedHashMap<AbstractCurve, Contour>())

    private val abstractZones = ArrayList<AbstractBasicRegion>()

    lateinit var modifiedDual: MED

    private var firstCurve = true

    companion object {
        @JvmField val BASE_CURVE_RADIUS = 1000.0

        private val MED_RADIUS = 500.0

        private val OFFSET = 1000.0

        private val CONTROL_POINT_STEP = BASE_CURVE_RADIUS / 20
    }

    override fun createDiagram(description: AbstractDescription, size: Int): ConcreteDiagram? {

        dSteps = decomposer.decompose(description)
        rSteps = recomposer.recompose(dSteps)

        var i = 0
        for (step in rSteps) {
            // no duplicates, so just single data
            val data = step.addedContourData[0]

            if (i == 0) {

                // BASE CASE
                val contour = CircleContour(BASE_CURVE_RADIUS + OFFSET, BASE_CURVE_RADIUS + OFFSET, BASE_CURVE_RADIUS, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else if (i == 1) {

                // BASE CASE
                val contour = CircleContour((BASE_CURVE_RADIUS + 0) * 2 + OFFSET, BASE_CURVE_RADIUS + OFFSET, BASE_CURVE_RADIUS, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else {

                // MED
                createMED()

                val cycle = modifiedDual.computeCycle(
                        data.splitZones
                ).orElseThrow { Exception("Failed to find cycle") }

                // AP cycle USING QUAD CURVES
                var contour = PathContour(data.addedCurve, cycle.path)

                // SMOOTHED AP CYCLE THRU NODE POINTS

                // TODO: THIS FAILS WHEN THE CYCLE IS A LINE
                if (cycle.nodes.map { it.zone.abstractZone.toString() }.none { it == "{}" }) {
                    contour = PathContour(data.addedCurve, BezierApproximation.pathThruPoints(cycle.nodes.map { it.point }.toMutableList()))
                }

                curveToContour[data.addedCurve] = contour

                // ADD NEW ZONES
                abstractZones.addAll(cycle.nodes.map { it.zone.abstractZone.moveInside(data.addedCurve) })
            }

            // embed curve

            i++
        }

        createMED()

        return null
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
        cz.bbox = javafx.scene.shape.Rectangle(5000.0, 5000.0)

        return cz
    }

    /**
     * Needs to be generated every time because contours change zones.
     */
    private fun createMED() {

        val concreteZones = abstractZones.map { makeConcreteZone(it) }

        val bounds = concreteZones.map { it.shape.layoutBounds }

        val minX = bounds.map { it.minX }.min()
        val minY = bounds.map { it.minY }.min()
        val maxX = bounds.map { it.maxX }.max()
        val maxY = bounds.map { it.maxY }.max()

        val center = Point2D((minX!! + maxX!!) / 2, (minY!! + maxY!!) / 2)
        val radius = Math.max(maxX - minX, maxY - minY) / 2 + MED_RADIUS   // how much bigger is the MED

        //println(center)
        //println(radius)

        val boundingCircle = Circle(center.x, center.y, radius, null)
        boundingCircle.stroke = Color.GREEN

        modifiedDual = MED(concreteZones, curveToContour.values.toList(), boundingCircle)

        val outside = makeConcreteZone(AbstractBasicRegion.OUTSIDE)

        val nodesMED = ArrayList<EulerDualNode>()

        modifiedDual.nodes
                .filter { it.zone.isTopologicallyAdjacent(outside) }
                .forEach {
                    val vectorToMED = it.zone.center.subtract(center)
                    val length = radius - vectorToMED.magnitude()

                    // from zone center to closest point on MED
                    val vector = vectorToMED.normalize().multiply(length)

                    val p1 = it.zone.center
                    val p2 = it.zone.center.add(vector)

                    val q = QuadCurve()
                    q.fill = null
                    q.stroke = Color.BLACK
                    q.startX = p1.x
                    q.startY = p1.y
                    q.endX = p2.x
                    q.endY = p2.y

                    q.controlX = p1.midpoint(p2).x
                    q.controlY = p1.midpoint(p2).y

                    // make "distinct" nodes so that jgrapht doesnt think it's a loop
                    val node = EulerDualNode(makeConcreteZone(AbstractBasicRegion.OUTSIDE), p2)
                    nodesMED.add(node)
                    modifiedDual.edges.add(EulerDualEdge(it, node, q))
                }

        modifiedDual.nodes.addAll(nodesMED)

        // SORT NODES ALONG THE MED RING
        Collections.sort(nodesMED, { node1, node2 ->
            val v1 = node1.point.subtract(center)
            val angle1 = -Math.toDegrees(Math.atan2(v1.y, v1.x))

            val v2 = node2.point.subtract(center)
            val angle2 = -Math.toDegrees(Math.atan2(v2.y, v2.x))

            (angle1 - angle2).toInt()
        })

        // ADD EDGES
        for (i in nodesMED.indices) {
            val node1 = nodesMED[i]
            val node2 = if (i == nodesMED.size - 1) nodesMED[0] else nodesMED[i+1]

            val p1 = node1.point
            val p2 = node2.point

            val q = QuadCurve()
            q.fill = null
            q.stroke = Color.BLACK
            q.startX = p1.x
            q.startY = p1.y
            q.endX = p2.x
            q.endY = p2.y

            q.controlX = p1.midpoint(p2).x
            q.controlY = p1.midpoint(p2).y

            // ATTEMPT TO ROUTE THE EDGE
            val x = q.controlX
            val y = q.controlY

            var step = CONTROL_POINT_STEP
            var safetyCount = 0

            var delta = Point2D(step.toDouble(), 0.0)
            var s = 0

            // the new curve segment must pass through the straddled curve
            // and only through that curve
            //val curve = node1.zone.abstractZone.getStraddledContour(node2.zone.abstractZone).get()

            //log.trace("Searching ${node1.zone} - ${node2.zone} : $curve")

            while (!isOKMED(q) && safetyCount < 500) {
                q.controlX = x + delta.x
                q.controlY = y + delta.y

                s++

                when (s) {
                    1 -> delta = Point2D(step.toDouble(), step.toDouble())
                    2 -> delta = Point2D(0.0, step.toDouble())
                    3 -> delta = Point2D((-step).toDouble(), step.toDouble())
                    4 -> delta = Point2D((-step).toDouble(), 0.0)
                    5 -> delta = Point2D((-step).toDouble(), (-step).toDouble())
                    6 -> delta = Point2D(0.0, (-step).toDouble())
                    7 -> delta = Point2D(step.toDouble(), (-step).toDouble())
                }

                if (s == 8) {
                    s = 0
                    delta = Point2D(step.toDouble(), 0.0)
                    step *= 2
                }

                safetyCount++
            }

            if (safetyCount == 500) {
                println("FAILED, NO CONTROL POINT")
            }

            modifiedDual.edges.add(EulerDualEdge(node1, node2, q))
        }

        // ASK MED TO GEN CYCLES
        modifiedDual.initCycles()
    }

    fun isOKMED(q: QuadCurve): Boolean {
        val list = curveToContour.values.filter {
            val s = it.shape
            s.fill = null
            s.stroke = Color.BROWN

            !Shape.intersect(s, q).getLayoutBounds().isEmpty()
        }

        return list.isEmpty()
    }
}