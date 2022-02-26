package gg.scala.parties

import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.flavor.Flavor
import gg.scala.flavor.FlavorOptions
import gg.scala.parties.command.PartyCommand
import gg.scala.parties.receiver.PartyReceiverHandler
import gg.scala.parties.service.PartyCommandService
import gg.scala.parties.service.PartyInviteService
import gg.scala.parties.service.PartyService
import gg.scala.parties.stream.PartyMessageStream
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
        val flavor = Flavor.create<PartySpigotPlugin>(
            FlavorOptions(this.logger)
        )

        flavor.bind<PartySpigotPlugin>() to this

        // temp - use flavor.startup in prod
        flavor.inject(PartyService)
        flavor.inject(PartyMessageStream)
        flavor.inject(PartyCommandService)
        flavor.inject(PartyInviteService)
        flavor.inject(PartyReceiverHandler)

        CloudSyncDiscoveryService
            .discoverable.assets
            .add("gg.scala.cgs:parties:cgs-parties")
    }
}
