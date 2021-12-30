package gg.scala.cgs.game.command

import gg.scala.cgs.common.enviornment.EditableFieldCaster
import gg.scala.cgs.common.enviornment.editor.EnvironmentEditor
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Single
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object EditCommand : BaseCommand()
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
        val entry = EnvironmentEditor.editable
            .firstOrNull { it.field.name == field }
            ?: throw ConditionFailedException(
                "There is no field with the name $field."
            )

        val current = entry.field
            .get(entry.instance)

        entry.field.set(
            entry.instance,
            EditableFieldCaster
                .castFancy(entry, value)
        )

        sender.sendMessage("${CC.SEC}You set ${CC.PRI}${
            entry.field.name
        }${CC.SEC} from ${CC.PRI}${
            current
        }${CC.SEC} to ${
            entry.field.get(
                entry.instance
            )
        }")
    }
}
