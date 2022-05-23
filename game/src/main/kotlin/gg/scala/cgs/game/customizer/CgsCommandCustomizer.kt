package gg.scala.cgs.game.customizer

import gg.scala.cgs.common.environment.editor.EnvironmentEditorService
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager

/**
 * @author GrowlyX
 * @since 4/17/2022
 */
object CgsCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        manager: ScalaCommandManager
    )
    {
        manager.commandCompletions
            .registerAsyncCompletion("fields") {
                return@registerAsyncCompletion EnvironmentEditorService
                    .editable.map { it.field.name }
            }
    }
}
