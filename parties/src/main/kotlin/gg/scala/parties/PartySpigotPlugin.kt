package gg.scala.parties

import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.commands.ManualRegister
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.parties.command.PartyCommand
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.command.manager.CubedCommandManager
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Plugin(
    name = "Parties",
    apiVersion = "1.18",
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("helper"),
        PluginDependency("Lemon"),
        PluginDependency("store-spigot"),
        PluginDependency("cloudsync"),
        PluginDependency("Cookie"),
    ]
)
class PartySpigotPlugin : ExtendedScalaPlugin()
{
    @ContainerEnable
    fun containerEnable()
    {
        CloudSyncDiscoveryService
            .discoverable.assets
            .add("gg.scala.cgs:parties:cgs-parties")
    }

    @ManualRegister
    fun manualRegister(
        commandManager: CubedCommandManager
    )
    {
        commandManager.registerCommand(object : BaseCommand() {
            @CommandAlias("pc|partychat|pchat")
            fun onPartyChat(player: Player, message: String)
            {
                PartyCommand.onPartyChat(player, message)
            }
        })
    }
}
