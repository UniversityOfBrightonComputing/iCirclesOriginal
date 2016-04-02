package icircles.concrete

import icircles.abstractdescription.AbstractBasicRegion
import icircles.abstractdescription.AbstractCurve
import icircles.abstractdescription.AbstractDescription
import icircles.decomposition.DecomposerFactory
import icircles.decomposition.DecompositionStrategyType
import icircles.graph.EulerDualGraph
import icircles.recomposition.RecomposerFactory
import icircles.recomposition.RecompositionStrategyType
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.stream.Collectors

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class TwoStepDiagramCreator : DiagramCreator(
        DecomposerFactory.newDecomposer(DecompositionStrategyType.INNERMOST),
        RecomposerFactory.newRecomposer(RecompositionStrategyType.DOUBLY_PIERCED)) {

    private val log = LogManager.getLogger(javaClass)

    private fun removeCurveFromDiagram(curve: AbstractCurve, diagram: ConcreteDiagram, size: Int): ConcreteDiagram {
        // generate a concrete diagram with the removed curve
        val newCurves = TreeSet(diagram.actualDescription.curves)
        run {
            val it = newCurves.iterator()
            while (it.hasNext()) {
                if (it.next().matchesLabel(curve)) {
                    it.remove()
                }
            }
        }

        val newZones = TreeSet(diagram.actualDescription.zones)
        val it = newZones.iterator()
        while (it.hasNext()) {
            val zone = it.next()
            if (zone.contains(curve)) {
                it.remove()
            }
        }

        val actual = AbstractDescription(newCurves, newZones)

        diagram.circles.removeAll { it.curve.matchesLabel(curve) }

        val contours = ArrayList(diagram.contours)

        val concreteDiagram = ConcreteDiagram(diagram.originalDescription, actual,
                diagram.circles, diagram.curveToContour, size, *contours.toTypedArray())
        return concreteDiagram
    }

    override fun createDiagram(description: AbstractDescription, size: Int): ConcreteDiagram {
        initial = description
        val diagram0 = super.createDiagram(description, size)

        val duplicates = diagram0.findDuplicateContours()
        if (duplicates.isEmpty())
            return diagram0

        var d = diagram0

        for (curve in duplicates.keys) {
            val iCirclesDiagramNew = removeCurveFromDiagram(curve, d, size)

            val ad = d.getActualDescription()

            log.debug("Actual Description: " + ad)

            var zones = ad.zones.filter({ z -> z.contains(curve) })

            log.debug("Zones in " + curve + ":" + zones.toString())

            zones = zones.map({ z -> z.moveOutside(curve) })

            log.debug("Zones that will be in " + curve + ":" + zones.toString())

            val graph = EulerDualGraph(iCirclesDiagramNew)

            graph.computeCycle(zones).ifPresent {
                println("Found approriate: $it")

                // create new contour
                val contour = PathContour(curve, it.path)

                val newCurves = TreeSet(iCirclesDiagramNew.actualDescription.curves)
                run {
                    val iter = newCurves.iterator()
                    while (iter.hasNext()) {
                        if (iter.next().matchesLabel(curve)) {
                            iter.remove()
                        }
                    }
                }

                newCurves.add(curve)

                // GENERATE ACTUAL DESC
                val newZones = TreeSet(iCirclesDiagramNew.actualDescription.zones)
                val iter = newZones.iterator()
                while (iter.hasNext()) {
                    val zone = iter.next()
                    if (zone.contains(curve)) {
                        iter.remove()
                    }
                }

                for (zone in it.nodes.map { it.zone.abstractZone }) {
                    newZones.add(zone.moveInside(curve))
                }

                val actual = AbstractDescription(newCurves, newZones)

                println("New actual: $actual")

                // put mapping from abstract to conrete curve
                iCirclesDiagramNew.curveToContour.put(curve, contour)

                val contours = ArrayList(iCirclesDiagramNew.contours)
                contours.add(contour)

                d = ConcreteDiagram(iCirclesDiagramNew.originalDescription, actual,
                        iCirclesDiagramNew.circles, iCirclesDiagramNew.curveToContour, size, *contours.toTypedArray())
            }
        }















        println("Generating for: ${d.actualDescription}")
        dSteps = null
        rSteps = null
        return super.createDiagram(d.actualDescription, size)
    }

    private lateinit var initial: AbstractDescription

    override fun getInitialDiagram(): AbstractDescription {
        return initial
    }
}