package gg.scala.cgs.common.environment

import java.lang.reflect.Field

/**
 * @author GrowlyX
 * @since 12/22/2021
 */
data class EditableFieldEntry(
    val field: Field,
    val instance: Any
)
