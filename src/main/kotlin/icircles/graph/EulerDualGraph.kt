package icircles.graph

import icircles.abstractdescription.AbstractCurve
import icircles.concrete.CircleContour
import icircles.concrete.ConcreteDiagram
import icircles.graph.elem.GraphHandling
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.*
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class EulerDualGraph(val diagram: ConcreteDiagram) {

    val nodes = ArrayList<EulerDualNode>()
    val edges = ArrayList<EulerDualEdge>()
    val cycles = ArrayList<ArrayList<EulerDualNode> >()

    init {
        diagram.allZones
            .forEach { nodes.add(EulerDualNode(it)) }

        for (i in nodes.indices) {
            var j = i + 1
            while (j < nodes.size) {
                //println("$i, $j vs ${nodes.size}")

                val node1 = nodes[i]
                val node2 = nodes[j]

                //println("Nodes: ${node1.zone} ${node2.zone}")

                if (node1.zone.isTopologicallyAdjacent(node2.zone)) {
                    //println("ADJ")

                    val p1 = node1.zone.center
                    val p2 = node2.zone.center

                    val q = QuadCurve()
                    q.fill = null
                    q.stroke = Color.BLACK
                    q.startX = p1.getX()
                    q.startY = p1.getY()
                    q.endX = p2.getX()
                    q.endY = p2.getY()
                    q.controlX = (p1.getX() + p2.getX()) / 2
                    q.controlY = (p1.getY() + p2.getY()) / 2


                    val x = (p1.x + p2.x) / 2
                    val y = (p1.y + p2.y) / 2

                    var step = 1
                    var safetyCount = 0

                    var delta = Point2D(step.toDouble(), 0.0)
                    var s = 0

                    val curve = node1.zone.abstractZone.getStraddledContour(node2.zone.abstractZone).get()

                    println("Searching ${node1.zone} - ${node2.zone} : $curve")

                    while (!isOK(q, curve, diagram.circles) && safetyCount < 500) {
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

                    // a b c ab ac bc abc bd bcd
                    println("End Searching with $safetyCount tries")

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

                        //edges.add(q)
                        // TODO: fix cubic curve
                        //edges.add(EulerDualEdge(node1, node2, c))
                    } else {
                        edges.add(EulerDualEdge(node1, node2, q))
                    }
                } else {
                    //println("NOT ADJ")
                }

                j++
            }
        }

        // ENUMERATE ALL VALID CYCLES

        val graph = GraphHandling()
        nodes.forEach { graph.addVertex(it) }
        edges.forEach { graph.addEdge(it.v1, it.v2, it) }

        graph.computeCycles().filter { cycle ->
            val path = Path()
            val moveTo = MoveTo(cycle.nodes.get(0).zone.center.x, cycle.nodes.get(0).zone.center.y)
            path.elements.addAll(moveTo)

            tmpPoint = cycle.nodes.get(0).zone.center

            cycle.edges.map { it.curve }.forEach { q ->
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

                quadCurveTo.controlX = q.getControlX()
                quadCurveTo.controlY = q.getControlY()

                path.elements.addAll(quadCurveTo)
            }

            path.elements.add(ClosePath())
            path.fill = Color.TRANSPARENT


            return@filter nodes.filter { !cycle.nodes.contains(it) }.none { path.contains(it.zone.center) }
        }
        .forEach { println(it) }
    }

    private var tmpPoint = Point2D.ZERO

    fun isOK(q: QuadCurve, actual: AbstractCurve, curves: List<CircleContour>): Boolean {
        val list = curves.filter {
            val s = it.shape
            s.fill = null
            s.stroke = Color.BROWN

            !Shape.intersect(s, q).getLayoutBounds().isEmpty()
        }



        if (list.size != 1)
            return false

        println("Found: $list")

        return list.get(0).curve == actual
    }
}