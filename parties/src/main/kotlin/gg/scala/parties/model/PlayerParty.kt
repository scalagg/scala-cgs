package gg.scala.parties.model

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
        return CompletableFuture.runAsync {

        }
    }
}
