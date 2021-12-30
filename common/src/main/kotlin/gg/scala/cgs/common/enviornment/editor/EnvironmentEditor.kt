package gg.scala.cgs.common.enviornment.editor

import gg.scala.cgs.common.enviornment.EditableFieldEntry

/**
 * @author GrowlyX
 * @since 12/22/2021
 */
object EnvironmentEditor
{
    val editable = mutableListOf<EditableFieldEntry>()

    fun registerAllEditables(
        `object`: Any
    )
    {
        val fields = `object`.javaClass
            .declaredFields

        // Only @JvmFields will be registered,
        // so no checks are needed.
        for (field in fields)
        {
            // lmao
            if (!field.name.contains("_"))
                continue

            editable.add(
                EditableFieldEntry(
                    field, `object`
                )
            )
        }
    }
}
