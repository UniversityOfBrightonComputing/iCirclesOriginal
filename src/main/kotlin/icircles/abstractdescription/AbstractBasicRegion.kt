package icircles.abstractdescription

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class AbstractBasicRegion(val inSet: Set<AbstractCurve>) {

    companion object {
        @JvmField val OUTSIDE = AbstractBasicRegion(TreeSet())
    }

    fun getNumCurves() = inSet.size

    fun contains(curve: AbstractCurve) = inSet.contains(curve)

    fun moveInside(curve: AbstractCurve) = AbstractBasicRegion(inSet.plus(curve))

    fun moveOutside(curve: AbstractCurve) = AbstractBasicRegion(inSet.minus(curve))

    fun getStraddledContour(otherRegion: AbstractBasicRegion): Optional<AbstractCurve> {
        val biggerSet = if (inSet.size >= otherRegion.inSet.size) inSet else otherRegion.inSet
        val smallerSet = if (inSet == biggerSet) otherRegion.inSet else inSet


        val difference = biggerSet.minus(smallerSet)
        return if (difference.size != 1) Optional.empty() else Optional.of(difference.first())
    }

    override fun toString() = inSet.map { it.label }.joinToString(",", "{", "}")
}