package gg.scala.cgs.lobby

import gg.scala.cgs.lobby.gamemode.CgsGameLobby
import gg.scala.cgs.lobby.locator.CgsInstanceLocator
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.annotations.container.flavor.LazyStartup
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
@Plugin(
    name = "ScGameLobby",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Tangerine"),
    PluginDependency("Parties"),
    PluginDependency("cloudsync")
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
