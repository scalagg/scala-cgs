package gg.scala.parties.service

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.parties.model.Party
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
@Service
object PartyService
{
    var service by Delegates.notNull<DataStoreObjectController<Party>>()

    private val loadedParties = ConcurrentHashMap<UUID, Party>()

    @Configure
    fun configure()
    {
        service = DataStoreObjectControllerCache.create()
        service.provideCustomSerializer(Serializers.gson)

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java)
            .handler {
                this.loadPartyOfPlayerIfAbsent(it.uniqueId)
            }
    }

    fun findPartyByUniqueId(uniqueId: UUID): Party?
    {
        return loadedParties.values.firstOrNull {
            it.leader.uniqueId == uniqueId || it.members.containsKey(uniqueId)
        }
    }

    fun findPartyByUniqueId(player: Player): Party?
    {
        return findPartyByUniqueId(player.uniqueId)
    }

    fun reloadPartyByUniqueId(uniqueId: UUID)
    {
        if (loadedParties[uniqueId] == null)
            return

        service
            .loadAll(DataStoreStorageType.REDIS)
            .thenAccept {
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

    fun loadPartyOfPlayerIfAbsent(uniqueId: UUID): CompletableFuture<Party?>
    {
        val loadedParty = findPartyByUniqueId(uniqueId)

        if (loadedParty != null)
        {
            return CompletableFuture
                .completedFuture(loadedParty)
        }

        return service
            .loadAll(DataStoreStorageType.REDIS)
            .thenApply {
                val found = it.values.firstOrNull { party ->
                    party.members.containsKey(uniqueId) || party.leader.uniqueId == uniqueId
                }

                if (found != null)
                {
                    kotlin.run {
                        loadedParties[found.uniqueId] = found
                    }
                }

                return@thenApply found
            }
    }
}
