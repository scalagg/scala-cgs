package gg.scala.cgs.game.command

import gg.scala.cgs.common.teams.CgsGameTeamService
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.util.QuickAccess.username
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 12/20/2021
 */
@AutoRegister
object DebugCommand : ScalaCommand()
{
    @CommandAlias("debug")
    @CommandPermission("cgs.command.debug")
    fun onDebug(sender: CommandSender)
    {
        CgsGameTeamService.teams
            .values
            .filter { it.participants.isNotEmpty() }
            .forEach {
                sender.sendMessage("=== ${it.id}")
                it.participants
                    .forEach { username ->
                        sender.sendMessage(username.username())
                    }
                sender.sendMessage("===")
                it.eliminated
                    .forEach { username ->
                        sender.sendMessage(username.username())
                    }
            }
    }
}
