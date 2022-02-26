package gg.scala.parties.receiver

import gg.scala.banana.Banana
import gg.scala.banana.BananaBuilder
import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.options.BananaOptions
import gg.scala.banana.subscribe.marker.BananaHandler
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
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

        banana.registerClass(this)
        banana.subscribe()
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
