package gg.scala.parties

import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Plugin(
    name = "Parties",
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("helper"),
        PluginDependency("Lemon"),
        PluginDependency("store-spigot"),
        PluginDependency("cloudsync")
    ]
)
class PartySpigotPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        invokeTrackedTask("party resources") {
            val flavor = Flavor.create<PartySpigotPlugin>(
                FlavorOptions(this.logger)
            )

            flavor.bind<PartySpigotPlugin>() to this
            flavor.startup()
        }

        CloudSyncDiscoveryService
            .discoverable.assets
            .add("gg.scala.cgs:parties:cgs-parties")
    }
}
