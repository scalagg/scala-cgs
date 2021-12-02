package gg.scala.parties.receiver

import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.subscribe.marker.BananaHandler

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object PartyReceiverHandler : BananaHandler
{
    @Subscribe("party-update")
    fun onPartyUpdate(message: Message)
    {

    }
}
