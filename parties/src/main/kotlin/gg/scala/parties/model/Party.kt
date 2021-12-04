package gg.scala.parties.model

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.parties.service.PartyService
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
data class Party(
    val uniqueId: UUID = UUID.randomUUID(),
    var leader: PartyMember
)
{
    val members = mutableMapOf<UUID, PartyMember>()
    val settings = mutableMapOf<PartySetting, Boolean>()

    fun saveAndUpdateParty(): CompletableFuture<Void>
    {
        return PartyService.service.saveEntry(
            uniqueId.toString(), this
        ).thenRun {
            RedisHandler.buildMessage(
                "party-update",
                "uniqueId" to uniqueId.toString()
            ).dispatch(Lemon.instance.banana)
        }
    }
}
