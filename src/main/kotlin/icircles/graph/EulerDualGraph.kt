package icircles.graph

import icircles.abstractdescription.AbstractCurve
import icircles.concrete.CircleContour
import icircles.concrete.ConcreteDiagram
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.QuadCurve
import javafx.scene.shape.Shape
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class EulerDualGraph(val diagram: ConcreteDiagram) {

    val nodes = ArrayList<EulerDualNode>()
    val edges = ArrayList<QuadCurve>()

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

                    //println("End Searching")

                    // we failed to find the correct spot
                    if (safetyCount == 100) {
                        println("Failed to find correct control point: ${node1.zone} - ${node2.zone}")
                        q.controlX = x
                        q.controlY = y
                    }


                    edges.add(q)
                } else {
                    //println("NOT ADJ")
                }

                j++
            }
        }
    }

    fun isOK(q: QuadCurve, actual: AbstractCurve, curves: List<CircleContour>): Boolean {
        val list = curves.filter {
            val s = it.shape
            s.fill = null
            s.stroke = Color.BROWN

            !Shape.intersect(s, q).getLayoutBounds().isEmpty()
        }

        //println(list)

        if (list.size != 1)
            return false

        return list.get(0).curve == actual
    }
}