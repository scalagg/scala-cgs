package gg.scala.parties

import gg.scala.commons.ExtendedScalaPlugin
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Plugin(
    name = "Parties",
    apiVersion = "1.18",
    depends = [
        PluginDependency("scala-commons"),
        PluginDependency("Lemon"),
        PluginDependency("store-spigot"),
        PluginDependency("ScBasics", soft = true),
        PluginDependency("Cookie", soft = true),
        PluginDependency("cloudsync", soft = true)
    ]
)
class PartySpigotPlugin : ExtendedScalaPlugin()
