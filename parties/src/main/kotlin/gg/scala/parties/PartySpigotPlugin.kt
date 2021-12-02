package gg.scala.parties

import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.parties.service.PlayerPartyService

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
class PartySpigotPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        invokeTrackedTask("party resources") {
            PlayerPartyService.initialLoad()
        }
    }
}
