package icircles.concrete

import icircles.abstractdescription.AbstractBasicRegion
import icircles.abstractdescription.AbstractCurve
import icircles.abstractdescription.AbstractDescription
import icircles.decomposition.DecomposerFactory
import icircles.decomposition.DecompositionStrategyType
import icircles.graph.EulerDualEdge
import icircles.graph.EulerDualNode
import icircles.graph.MED
import icircles.recomposition.RecomposerFactory
import icircles.recomposition.RecompositionStrategyType
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.QuadCurve
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class HamiltonianDiagramCreator
: DiagramCreator(DecomposerFactory.newDecomposer(DecompositionStrategyType.INNERMOST),
        RecomposerFactory.newRecomposer(RecompositionStrategyType.DOUBLY_PIERCED_EXTRA_ZONES)) {

    val curveToContour = LinkedHashMap<AbstractCurve, Contour>()

    private val abstractZones = ArrayList<AbstractBasicRegion>()

    lateinit var modifiedDual: MED

    private var firstCurve = true

    override fun createDiagram(description: AbstractDescription, size: Int): ConcreteDiagram? {

        dSteps = decomposer.decompose(description)
        rSteps = recomposer.recompose(dSteps)

        var i = 0
        for (step in rSteps) {
            // no duplicates, so just single data
            val data = step.addedContourData[0]

            if (i == 0) {

                // BASE CASE
                val contour = CircleContour(100.0, 100.0, 100.0, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else if (i == 1) {

                // BASE CASE
                val contour = CircleContour(200.0, 100.0, 100.0, data.addedCurve)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)

            } else {

                // MED
                createMED()

                val cycle = modifiedDual.computeCycle(
                        data.splitZones
                ).orElseThrow { Exception("Failed to find cycle") }

                // AP cycle
                val contour = PathContour(data.addedCurve, cycle.path)
                curveToContour[data.addedCurve] = contour

                abstractZones.addAll(data.newZones)
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
        val radius = Math.max(maxX - minX, maxY - minY) / 2 + 100   // how much bigger is the MED

        println(center)
        println(radius)

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

        // TODO: order outside nodes using vectors ?
        // TODO: add edges between outside nodes

        // HARDCODED

        if (firstCurve) {

            val p1 = nodesMED[0].point
            val p2 = nodesMED[1].point

            val q = QuadCurve()
            q.fill = null
            q.stroke = Color.BLACK
            q.startX = p1.x
            q.startY = p1.y
            q.endX = p2.x
            q.endY = p2.y

            q.controlX = p1.midpoint(p2).x
            q.controlY = p1.midpoint(p2).y + 500

            modifiedDual.edges.add(EulerDualEdge(nodesMED[0], nodesMED[1], q))

            modifiedDual.initCycles()

            firstCurve = false
        } else {
            modifiedDual.initCycles()
        }
    }
}