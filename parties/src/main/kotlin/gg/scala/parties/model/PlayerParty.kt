package gg.scala.parties.model

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.parties.service.PlayerPartyService
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
data class PlayerParty(
    val uniqueId: UUID = UUID.randomUUID(),
    var leader: PlayerPartyMember
)
{
    val members = mutableMapOf<UUID, PlayerPartyMember>()
    val settings = mutableMapOf<PlayerPartySetting, Boolean>()

    fun saveAndUpdateParty(): CompletableFuture<Void>
    {
        return PlayerPartyService.service.saveEntry(
            uniqueId.toString(), this
        ).thenRun {
            RedisHandler.buildMessage(
                "party-update",
                "uniqueId" to uniqueId.toString()
            ).dispatch(Lemon.instance.banana)
        }
    }
}
