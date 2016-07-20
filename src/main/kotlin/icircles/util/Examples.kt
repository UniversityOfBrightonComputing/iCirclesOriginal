package icircles.util

import icircles.abstractdescription.AbstractDescription
import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
object Examples {

    val list = ArrayList<Pair<String, AbstractDescription> >()

    init {
        add("Venn-3", "a b c abc ab ac bc")
        add("Venn-4", "a b c d ab ac ad bc bd cd abc abd acd bcd abcd")
    }

    private fun add(name: String, description: String) {
        list.add(name.to(AbstractDescription.from(description)))
    }
}