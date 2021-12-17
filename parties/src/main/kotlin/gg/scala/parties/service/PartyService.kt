package gg.scala.parties.service

import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import gg.scala.lemon.Lemon
import gg.scala.parties.model.Party
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object PartyService
{
    var service by Delegates.notNull<RedisStorageLayer<Party>>()

    private val loadedParties = mutableMapOf<UUID, Party>()

    fun initialLoad()
    {
        service = RedisStorageLayer(
            Lemon.instance.redisConnection,
            "cgs:parties", Party::class.java
        )
    }

    fun findPartyByLeader(uniqueId: UUID): Party?
    {
        return loadedParties.values.firstOrNull {
            it.leader.uniqueId == uniqueId
        }
    }

    fun findPartyByUniqueId(uniqueId: UUID): Party?
    {
        return loadedParties.values.firstOrNull {
            it.members.containsKey(uniqueId)
        }
    }

    fun findPartyByUniqueId(player: Player): Party?
    {
        return loadedParties.values.firstOrNull {
            it.members.containsKey(player.uniqueId)
        }
    }

    fun reloadPartyByUniqueId(uniqueId: UUID)
    {
        if (loadedParties[uniqueId] == null)
            return

        service.fetchAllEntries().thenAccept {
            val found = it.values.firstOrNull { playerParty ->
                playerParty.uniqueId == uniqueId
            }

            if (found != null)
            {
                kotlin.run {
                    loadedParties[found.uniqueId] = found
                }
            }
        }
    }

    fun loadPartyOfPlayerIfAbsent(player: Player): CompletableFuture<Party?>
    {
        val loadedParty = findPartyByUniqueId(player)

        if (loadedParty != null)
        {
            val completable = CompletableFuture<Party?>()
            completable.complete(loadedParty)

            return completable
        } else
        {
            return service.fetchAllEntries().thenApply {
                val found = it.values.firstOrNull { playerParty ->
                    playerParty.members.containsKey(player.uniqueId)
                }

                if (found != null)
                {
                    loadedParties[found.uniqueId] = found
                }

                return@thenApply found
            }
        }
    }
}
