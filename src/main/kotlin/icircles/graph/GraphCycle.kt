package icircles.graph

import icircles.abstractdescription.AbstractBasicRegion

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class GraphCycle<V, E>(val nodes: List<V>, val edges: List<E>) {

    fun length() = nodes.size

//    fun contains(zones: List<AbstractBasicRegion>): Boolean {
//        val mappedNodes = nodes.map { it.zone.abstractZone }
//
//        return mappedNodes.containsAll(zones)
//    }
}