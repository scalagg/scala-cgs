package gg.scala.parties.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.acf.annotation.CommandAlias
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 5/7/2022
 */
@AutoRegister
object PartyChatCommand : ScalaCommand()
{
    @CommandAlias("pc|partychat|pchat")
    fun onPartyChat(player: Player, message: String)
    {
        PartyCommand.onPartyChat(player, message)
    }
}
