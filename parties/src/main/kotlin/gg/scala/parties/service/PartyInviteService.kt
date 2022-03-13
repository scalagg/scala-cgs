package gg.scala.parties.service

import gg.scala.lemon.Lemon
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object PartyInviteService
{
    fun hasOutgoingInvite(party: UUID, target: UUID): CompletableFuture<Boolean>
    {
        return CompletableFuture.supplyAsync {
            var exists: Boolean

            Lemon.instance.aware.publishConnection.apply {
                exists = this.sync().hget("parties:invites:$target:$party", party.toString()) != null
            }

            return@supplyAsync exists
        }
    }
}
