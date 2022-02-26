package gg.scala.parties.stream

import gg.scala.banana.BananaBuilder
import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.options.BananaOptions
import gg.scala.banana.subscribe.marker.BananaHandler
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.parties.model.Party
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 12/31/2021
 */
object PartyMessageStream : BananaHandler
{
    fun pushToStream(
        party: Party, message: FancyMessage
    )
    {
        party.members.values.toMutableList()
            .apply { add(party.leader) }
            .forEach {
                it.sendMessage(message)
            }
    }
}
