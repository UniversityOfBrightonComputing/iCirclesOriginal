package icircles.abstractdescription

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
data class AbstractCurve(val label: String) : Comparable<AbstractCurve> {

    fun hasLabel(label: String) = this.label == label

    fun matchesLabel(other: AbstractCurve) = clean(label).equals(clean(other.label))

    fun split() = AbstractCurve("$label'")

    private fun clean(label: String) = label.replace("'", "")

    override fun compareTo(other: AbstractCurve) = this.label.compareTo(other.label)

    override fun toString() = label

//    override fun equals(other: Any?): Boolean {
//        return matchesLabel(other as AbstractCurve)
//    }


}