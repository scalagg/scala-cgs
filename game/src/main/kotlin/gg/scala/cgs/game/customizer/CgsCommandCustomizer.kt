package gg.scala.cgs.game.customizer

import gg.scala.cgs.common.enviornment.editor.EnvironmentEditorService
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import net.evilblock.cubed.command.manager.CubedCommandManager

/**
 * @author GrowlyX
 * @since 4/17/2022
 */
object CgsCommandCustomizer
{
    @CommandManagerCustomizer
    fun customize(
        manager: CubedCommandManager
    )
    {
        manager.commandCompletions
            .registerAsyncCompletion("fields") {
                return@registerAsyncCompletion EnvironmentEditorService
                    .editable.map { it.field.name }
            }
    }
}