package gg.scala.parties.receiver

import gg.scala.banana.BananaBuilder
import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.options.BananaOptions
import gg.scala.banana.subscribe.marker.BananaHandler
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Service
object PartyReceiverHandler : BananaHandler
{
    val banana = BananaBuilder()
        .options(
            BananaOptions(
                channel = "party:backbone"
            )
        )
        .credentials(
            Lemon.instance.credentials
        )
        .build()

    @Configure
    fun configure()
    {
        banana.registerClass(this)
        banana.subscribe()
    }

    @Subscribe("network-disconnect")
    fun onNetworkDisconnect(message: Message)
    {
        val uniqueId = UUID.fromString(
            message["uniqueId"]
        )

        val party = PartyService
            .loadPartyOfPlayerIfAbsent(uniqueId)
            .join() ?: return

        if (uniqueId == party.leader.uniqueId)
        {
            party.gracefullyForget().join()
        } else
        {
            PartyService
                .handlePartyLeave(uniqueId)
                .join()
        }
    }

    @Subscribe("party-warp")
    fun onPartyWarp(message: Message)
    {
        val uniqueId = UUID.fromString(
            message["uniqueId"]
        )

        val party = PartyService
            .loadedParties.values
            .firstOrNull {
                it.uniqueId == uniqueId
            }
            ?: return

        val server = message["server"]!!

        for (uuid in party.members.keys)
        {
            val bukkitPlayer = Bukkit
                .getPlayer(uuid)
                ?: continue

            bukkitPlayer.sendMessage("${CC.SEC}You're being warped to ${CC.PRI}$server${CC.SEC}...")

            BungeeUtil.sendToServer(
                bukkitPlayer, server
            )
        }
    }

    @Subscribe("party-update")
    fun onPartyUpdate(message: Message)
    {
        val uniqueId = UUID.fromString(
            message["uniqueId"]
        )

        PartyService.reloadPartyByUniqueId(uniqueId)
    }

    @Subscribe("party-forget")
    fun onPartyForget(message: Message)
    {
        val uniqueId = UUID.fromString(
            message["uniqueId"]
        )

        PartyService.loadedParties
            .remove(uniqueId)
    }
}
