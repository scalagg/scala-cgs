package gg.scala.cgs.game.command

import gg.scala.cgs.common.environment.EditableFieldService
import gg.scala.cgs.common.environment.editor.EnvironmentEditorService
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Single
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
@AutoRegister
object EditCommand : ScalaCommand()
{
    @CommandAlias("edit")
    @CommandCompletion("@fields")
    @CommandPermission("cgs.command.edit")
    fun onEdit(
        sender: CommandSender,
        @Single field: String,
        @Single value: String
    )
    {
        val entry = EnvironmentEditorService.editable
            .firstOrNull { it.field.name == field }
            ?: throw ConditionFailedException(
                "There is no field with the name $field."
            )

        val current = entry.field
            .get(entry.instance)

        entry.field.set(
            entry.instance,
            EditableFieldService
                .castFancy(entry, value)
        )

        sender.sendMessage("${CC.SEC}You set ${CC.PRI}${
            entry.field.name
        }${CC.SEC} to ${CC.WHITE}${
            entry.field.get(
                entry.instance
            )
        }${CC.SEC}. ${CC.GRAY}(previously $current)")
    }
}
