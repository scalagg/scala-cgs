package gg.scala.parties.service

import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.parties.command.PartyCommand
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Service
object PartyInviteService
{
    fun hasOutgoingInvite(party: UUID, target: UUID): CompletableFuture<Boolean>
    {
        return CompletableFuture.supplyAsync {
            var exists: Boolean

            PartyCommand.connection.apply {
                exists = this.sync().hget("parties:invites:$target:$party", party.toString()) != null
            }

            return@supplyAsync exists
        }
    }
}
