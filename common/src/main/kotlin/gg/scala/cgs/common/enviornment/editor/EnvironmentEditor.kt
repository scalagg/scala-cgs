package gg.scala.cgs.common.enviornment.editor

import gg.scala.cgs.common.enviornment.EditableField
import gg.scala.cgs.common.enviornment.EditableFieldEntry

/**
 * @author GrowlyX
 * @since 12/22/2021
 */
object EnvironmentEditor
{
    private val editable = mutableListOf<EditableFieldEntry>()

    fun registerAllEditables(
        `object`: Any
    )
    {
        val fields = `object`.javaClass
            .declaredFields

        for (field in fields)
        {
            val annotation = field
                .getAnnotation(EditableField::class.java)
                ?: continue

            editable.add(
                EditableFieldEntry(
                    annotation, field, `object`
                )
            )
        }
    }

}
