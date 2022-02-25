package gg.scala.parties.receiver

import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.subscribe.marker.BananaHandler
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.parties.service.PartyService
import java.util.*

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Service
object PartyReceiverHandler : BananaHandler
{
    @Configure
    fun configure()
    {

    }

    @Subscribe("party-update")
    fun onPartyUpdate(message: Message)
    {
        val uniqueId = UUID.fromString(
            message["uniqueId"]
        )

        PartyService.reloadPartyByUniqueId(uniqueId)
    }
}
