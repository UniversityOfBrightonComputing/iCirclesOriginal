package icircles.graph

import icircles.concrete.ConcreteZone

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class EulerDualNode(val zone: ConcreteZone) {

    override fun toString(): String {
        return zone.toString()
    }
}