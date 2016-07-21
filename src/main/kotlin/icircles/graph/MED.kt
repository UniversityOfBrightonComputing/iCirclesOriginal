package icircles.graph

import icircles.abstractdescription.AbstractBasicRegion
import icircles.abstractdescription.AbstractCurve
import icircles.concrete.ConcreteDiagram
import icircles.concrete.ConcreteZone
import icircles.concrete.Contour
import icircles.graph.cycles.CycleFinder
import icircles.util.CannotDrawException
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.*
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
    lateinit var cycles: List<GraphCycle<EulerDualNode, EulerDualEdge>>

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

                        throw CannotDrawException("Failed to add EGD edge: ${node1.zone} - ${node2.zone}")

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

    /**
     * Compute all valid cycles.
     * A cycle is valid if it can be used to embed a curve.
     */
    private fun computeValidCycles(): List<GraphCycle<EulerDualNode, EulerDualEdge>> {
        val graph = CycleFinder<EulerDualNode, EulerDualEdge>(EulerDualEdge::class.java)
        nodes.forEach { graph.addVertex(it) }
        edges.forEach { graph.addEdge(it.v1, it.v2, it) }

        return graph.computeCycles().filter { cycle ->

            //println("Checking cycle: $cycle")

            // this ensures that we do not allow same vertices in the cycle
            // unless it's the outside vertex
            cycle.nodes.groupBy { it.zone.abstractZone.toString() }.forEach {
                if (it.key != "{}" && it.value.size > 1) {
                    //log.debug("Discarding cycle because ${it.key} is present ${it.value.size} times")
                    return@filter false
                }
            }

            val path = Path()
            val moveTo = MoveTo(cycle.nodes.get(0).point.x, cycle.nodes.get(0).point.y)
            path.elements.addAll(moveTo)

            tmpPoint = cycle.nodes.get(0).point

            cycle.edges.map { it.curve }.forEach { q ->

                when(q) {
                    is QuadCurve -> {
                        val quadCurveTo = QuadCurveTo()

                        // we do this coz source and end vertex might be swapped
                        if (tmpPoint == Point2D(q.startX, q.startY)) {
                            quadCurveTo.x = q.endX
                            quadCurveTo.y = q.endY
                        } else {
                            quadCurveTo.x = q.startX
                            quadCurveTo.y = q.startY
                        }

                        tmpPoint = Point2D(quadCurveTo.x, quadCurveTo.y)

                        quadCurveTo.controlX = q.controlX
                        quadCurveTo.controlY = q.controlY

                        path.elements.addAll(quadCurveTo)
                    }

                    is Arc -> {

                        //println(q.startAngle)

                        val p1 = (q.userData as Pair<Point2D, Point2D>).first
                        val p2 = (q.userData as Pair<Point2D, Point2D>).second

                        // a b c d ab ac bc bd cd abc bcd
                        // a b c d ab ac bc bd cd ce abc ace bcd bce abce

                        val arcTo = ArcTo()
                        arcTo.radiusX = q.radiusX
                        arcTo.radiusY = q.radiusY
                        arcTo.xAxisRotation = q.startAngle

                        val arcCenter = Point2D(q.centerX, q.centerY)

                        // p1 is start then
                        if (tmpPoint == p1) {
                            arcTo.x = p2.x
                            arcTo.y = p2.y

//                            val vector = p1.subtract(arcCenter)
//
//                            if (vector.x < 0) {
//
//                                if (vector.y < 0) {
//
//                                } else {
//
//                                }
//
//                            } else {
//
//                            }

                        } else {
                            arcTo.x = p1.x
                            arcTo.y = p1.y
                        }

                        arcTo.isSweepFlag = true

                        tmpPoint = Point2D(arcTo.x, arcTo.y)

                        path.elements.add(arcTo)
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown edge shape: $q")
                    }
                }
            }

            path.elements.add(ClosePath())
            path.fill = Color.TRANSPARENT

            cycle.path = path

            // we filter those vertices that are not part of the cycle
            // then we check if filtered vertices are inside the cycle
            nodes.filter {

                // we do not need to check for ouside zone right?
                !cycle.contains(it)
                // fails for some reason
                //!cycle.nodes.contains(it)

            }.forEach {

                //println("Checking vertex $it")

                if (path.contains(it.point)) {
                    //log.debug("Discarding cycle because of inside vertex: ${it.point}")
                    return@filter false
                }
            }

            //log.debug("Cycle is valid")
            return@filter true
        }
    }

    fun initCycles() {
        cycles = computeValidCycles()
        log.debug("Valid cycles: $cycles")
    }

    fun computeCycle(zonesToSplit: List<AbstractBasicRegion>): Optional<GraphCycle<EulerDualNode, EulerDualEdge>> {
        return Optional.ofNullable(cycles.filter { it.nodes.map { it.zone.abstractZone }.containsAll(zonesToSplit) }.firstOrNull())
        //return Optional.ofNullable(cycles.filter { containsAll(it.nodes.map { it.zone.abstractZone }, zonesToSplit) }.firstOrNull())
    }
}