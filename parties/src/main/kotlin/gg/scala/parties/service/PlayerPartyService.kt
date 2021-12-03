package gg.scala.parties.service

import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import gg.scala.lemon.Lemon
import gg.scala.parties.model.PlayerParty
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object PlayerPartyService
{
    var service by Delegates.notNull<RedisStorageLayer<PlayerParty>>()

    private val loadedParties = mutableMapOf<UUID, PlayerParty>()

    fun initialLoad()
    {
        service = RedisStorageLayer(
            Lemon.instance.redisConnection,
            "cgs:parties", PlayerParty::class.java
        )
    }

    fun findPartyByUniqueId(player: Player): PlayerParty?
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

    fun loadPartyOfPlayerIfAbsent(player: Player): CompletableFuture<PlayerParty?>
    {
        val loadedParty = findPartyByUniqueId(player)

        if (loadedParty != null)
        {
            val completable = CompletableFuture<PlayerParty?>()
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
