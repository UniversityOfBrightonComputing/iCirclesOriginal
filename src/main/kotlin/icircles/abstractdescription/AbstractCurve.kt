package icircles.abstractdescription

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class AbstractCurve(val label: String) {

    fun hasLabel(label: String) = this.label == label

    fun split() = AbstractCurve("$label'")
}