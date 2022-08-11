package gg.scala.cgs.game

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.information.arena.CgsGameArenaHandler
import gg.scala.cgs.game.engine.CgsEngineConfigurationService
import gg.scala.cgs.game.locator.CgsInstanceLocator
import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.annotations.container.flavor.LazyStartup
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
@Plugin(
    name = "CGS-Engine",
    apiVersion = "1.18",
    depends = [
        PluginDependency("scala-commons"),
        PluginDependency("Lemon"),
        PluginDependency("Parties"),
        PluginDependency("cloudsync"),
        PluginDependency("LunarClient-API", soft = true),
        PluginDependency("SlimeWorldManager", soft = true),
    ]
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
