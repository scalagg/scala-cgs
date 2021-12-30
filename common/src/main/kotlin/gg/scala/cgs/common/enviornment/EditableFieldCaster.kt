package gg.scala.cgs.common.enviornment

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object EditableFieldCaster
{
    private val casters = mutableMapOf<Class<*>, (String) -> Any>()

    fun initialLoad()
    {
        casters[Int::class.java] = { it.toInt() }
        casters[Boolean::class.java] = { it.toBoolean() }
        casters[Double::class.java] = { it.toDouble() }
    }

    fun castFancy(
        entry: EditableFieldEntry,
        input: String
    ): Any
    {
        val caster = casters[entry.field.type]
            ?: return input

        return caster.invoke(input)
    }
}
