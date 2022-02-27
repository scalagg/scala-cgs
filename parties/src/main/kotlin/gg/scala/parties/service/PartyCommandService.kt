package gg.scala.parties.service

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.parties.PartySpigotPlugin
import gg.scala.parties.command.PartyCommand
import gg.scala.parties.command.TogglePartyInvitesCommand
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.command.manager.CubedCommandManager
import org.bukkit.ChatColor
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/25/2022
 */
@Service
object PartyCommandService
{
    @Inject
    lateinit var plugin: PartySpigotPlugin

    @Configure
    fun configure()
    {
        val manager = CubedCommandManager(
            plugin = plugin,
            primary = ChatColor.valueOf(Lemon.instance.lemonWebData.primary),
            secondary = ChatColor.valueOf(Lemon.instance.lemonWebData.secondary)
        )

        manager.registerCommand(PartyCommand)

        // proxy command call to PartyCommand
        manager.registerCommand(object : BaseCommand() {
            @CommandAlias("pc")
            fun onPartyChat(player: Player, message: String)
            {
                PartyCommand.onPartyChat(player, message)
            }
        })

        manager.registerCommand(TogglePartyInvitesCommand)
    }
}
