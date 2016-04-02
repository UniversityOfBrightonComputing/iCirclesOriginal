package icircles.graph

import icircles.abstractdescription.AbstractBasicRegion
import javafx.scene.shape.Path


/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class GraphCycle<V, E>(val nodes: List<V>, val edges: List<E>) {

    lateinit var path: Path

    fun length() = nodes.size

//    fun contains(zones: List<AbstractBasicRegion>): Boolean {
//        val mappedNodes = nodes.map { it.zone.abstractZone }
//
//        return mappedNodes.containsAll(zones)
//    }
}