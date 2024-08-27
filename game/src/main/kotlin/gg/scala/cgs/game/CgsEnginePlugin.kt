package gg.scala.cgs.game

import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.cgs.game.client.CgsLunarClientService
import gg.scala.cgs.game.engine.CgsEngineConfigurationService
import gg.scala.cgs.game.locator.CgsInstanceLocator
import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.annotations.container.flavor.LazyStartup
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
@Plugin(
    name = "ScGameFramework",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Parties"),
    PluginDependency("cloudsync"),
    PluginDependency("Grape", soft = true),
    PluginDependency("CoreGameExtensions", soft = true),
    PluginDependency("LunarClient-API", soft = true),
    PluginDependency("SlimeWorldManager", soft = true)
)
@LazyStartup
class CgsEnginePlugin : ExtendedScalaPlugin()
{
    companion object
    {
        @JvmStatic
        var LOADING_STRING = ""
    }

    @ContainerEnable
    fun containerEnable()
    {
        logger.info("*** Attempting to find CGS Game implementation! ***")

        server.scheduler.runTaskTimerAsynchronously(this,
            {
                LOADING_STRING = if (LOADING_STRING == "") "." else if (LOADING_STRING == ".") ".." else if (LOADING_STRING == "..") "..." else ""
            }, 0L, 10L
        )

        flavor().inject(CgsInstanceLocator)

        CgsInstanceLocator.configure {
            flavor().inject(CgsEngineConfigurationService)
            /*if (Bukkit.getPluginManager().getPlugin("LunarClient-API") != null)
            {
                flavor().inject(CgsLunarClientService)
            }*/
        }

        CloudSyncDiscoveryService
            .discoverable.assets
            .apply {
                add("gg.scala.cgs:game:cgs-game")
            }
    }

    @ContainerDisable
    fun containerDisable()
    {
        CgsGameArenaHandler.close()
    }
}
