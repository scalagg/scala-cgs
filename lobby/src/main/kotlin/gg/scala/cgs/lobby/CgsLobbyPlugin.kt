package gg.scala.cgs.lobby

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.locator.CgsInstanceLocator
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.annotations.container.flavor.LazyStartup
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
@Plugin(
    name = "CGS-Lobby",
    apiVersion = "1.18",
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("helper"),
        PluginDependency("Lemon"),
        PluginDependency("Tangerine"),
        PluginDependency("cloudsync")
    ]
)
@LazyStartup
class CgsLobbyPlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var INSTANCE by Delegates.notNull<CgsLobbyPlugin>()
    }

    @ContainerEnable
    fun containerEnable()
    {
        INSTANCE = this

        CgsInstanceLocator.configure {
            CgsGameLobby.INSTANCE.configureResources(this)
        }
    }
}
