package gg.scala.parties.command

import gg.scala.parties.service.PartyService
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.HelpCommand
import net.evilblock.cubed.acf.annotation.Subcommand
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/17/2021
 */
object PartyCommand : BaseCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("create")
    fun onCreate(player: Player)
    {
        val existing = PartyService
            .findPartyByUniqueId(player)

        if (existing != null)
        {

        }
    }
}
