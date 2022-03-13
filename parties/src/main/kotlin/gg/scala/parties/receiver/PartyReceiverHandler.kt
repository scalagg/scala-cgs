package gg.scala.parties.receiver

import gg.scala.aware.AwareBuilder
import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.parties.PartySpigotPlugin
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
object PartyReceiverHandler
{
    @Inject
    lateinit var plugin: PartySpigotPlugin

    val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("party:backbone")
            .codec(AwareMessageCodec)
            .logger(plugin.logger)
            .build()
    }

    @Configure
    fun configure()
    {
        aware.listen(this)
        aware.connect()
    }

    @Close
    fun close()
    {
        aware.shutdown()
    }

    @Subscribe("network-disconnect")
    fun onNetworkDisconnect(message: AwareMessage)
    {
        // very temporary
        if (Lemon.instance.settings.id != "na-uml-1")
        {
            return
        }

        val uniqueId = message
            .retrieve<UUID>("uniqueId")

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
    fun onPartyWarp(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        val party = PartyService
            .loadedParties.values
            .firstOrNull {
                it.uniqueId == uniqueId
            }
            ?: return

        val server = message
            .retrieve<String>("server")

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
    fun onPartyUpdate(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        PartyService
            .reloadPartyByUniqueId(uniqueId)
    }

    @Subscribe("party-forget")
    fun onPartyForget(message: AwareMessage)
    {
        val uniqueId = message
            .retrieve<UUID>("uniqueId")

        PartyService.loadedParties
            .remove(uniqueId)
    }
}
