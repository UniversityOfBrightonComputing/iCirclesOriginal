package icircles.graph

import icircles.abstractdescription.AbstractCurve
import icircles.concrete.ConcreteDiagram
import icircles.concrete.ConcreteZone
import icircles.concrete.Contour
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.CubicCurve
import javafx.scene.shape.QuadCurve
import javafx.scene.shape.Shape
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class MED(allZones: List<ConcreteZone>, allContours: List<Contour>, val boundingCircle: Circle) {

    private val log = LogManager.getLogger(javaClass)

    private val CONTROL_POINT_STEP = 5

    val nodes: MutableList<EulerDualNode>
    val edges = ArrayList<EulerDualEdge>()
    //val cycles: List<GraphCycle<EulerDualNode, EulerDualEdge>>

    init {
        nodes = allZones.map { EulerDualNode(it, it.center) }.toMutableList()

        for (i in nodes.indices) {
            var j = i + 1
            while (j < nodes.size) {
                val node1 = nodes[i]
                val node2 = nodes[j]

                // if zones are topologically adjacent then there exists
                // a curve segment between zone centers
                if (node1.zone.isTopologicallyAdjacent(node2.zone)) {
                    log.trace("${node1.zone} and ${node2.zone} are adjacent")

                    val p1 = node1.zone.center
                    val p2 = node2.zone.center

                    val q = QuadCurve()
                    q.fill = null
                    q.stroke = Color.BLACK
                    q.startX = p1.x
                    q.startY = p1.y
                    q.endX = p2.x
                    q.endY = p2.y

                    q.controlX = p1.midpoint(p2).x
                    q.controlY = p1.midpoint(p2).y
                    //q.controlX = (p1.x + p2.x) / 2
                    //q.controlY = (p1.y + p2.y) / 2


                    val x = q.controlX
                    val y = q.controlY

                    var step = CONTROL_POINT_STEP
                    var safetyCount = 0

                    var delta = Point2D(step.toDouble(), 0.0)
                    var s = 0

                    // the new curve segment must pass through the straddled curve
                    // and only through that curve
                    val curve = node1.zone.abstractZone.getStraddledContour(node2.zone.abstractZone).get()

                    log.trace("Searching ${node1.zone} - ${node2.zone} : $curve")

                    while (!isOK(q, curve, allContours) && safetyCount < 500) {
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

                    log.trace("End Searching with $safetyCount tries")

                    // we failed to find the correct spot
                    if (safetyCount == 500) {
                        println("Failed to find correct control point: ${node1.zone} - ${node2.zone}")
                        q.controlX = x
                        q.controlY = y

                        val c = CubicCurve()
                        c.fill = null
                        c.stroke = Color.BROWN
                        c.startX = q.startX
                        c.startY = q.startY
                        c.endX = q.endX
                        c.endY = q.endY

                        val vector = p2.subtract(p1)
                        val perpen = Point2D(-vector.y, vector.x).multiply(-1.0).normalize()

                        val perpen2 = Point2D(-perpen.y, perpen.x).multiply(-1.0).normalize()

                        c.controlX1 = x + perpen.x * 300 + perpen2.x * 300
                        c.controlY1 = y + perpen.y * 300 + perpen2.y * 300

                        c.controlX2 = x + perpen.x * 300 - perpen2.x * 300
                        c.controlY2 = y + perpen.y * 300 - perpen2.y * 300

                        // TODO: find algorithm
                        c.controlX1 = 300.0
                        c.controlY1 = 0.0

                        c.controlX2 = 500.0
                        c.controlY2 = 50.0

                        //edges.add(EulerDualEdge(node1, node2, c))
                    } else {
                        edges.add(EulerDualEdge(node1, node2, q))
                    }
                }

                j++
            }
        }

        //cycles = computeValidCycles()
        //log.debug("Valid cycles: $cycles")
    }

    private var tmpPoint = Point2D.ZERO

    fun isOK(q: QuadCurve, actual: AbstractCurve, curves: List<Contour>): Boolean {
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
}