package gg.scala.parties.service

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyMember
import gg.scala.parties.model.PartyRole
import gg.scala.parties.prefix
import gg.scala.parties.stream.PartyMessageStream
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
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
    val loadedParties = ConcurrentHashMap<UUID, Party>()

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

    fun handlePartyKick(player: Player, target: UUID): CompletableFuture<Void>
    {
        return loadPartyOfPlayerIfAbsent(target)
            .thenCompose {
                if (it == null)
                {
                    throw ConditionFailedException("The party you tried to kick a player from no longer exists!")
                }

                it.members.remove(target)

                it.saveAndUpdateParty()
                    .thenAccept { _ ->
                        val message = FancyMessage().apply {
                            withMessage("$prefix${CC.RED}${target.username()}${CC.SEC} was kicked from the party!")
                        }

                        PartyMessageStream.pushToStream(it, message)
                    }
            }
    }

    fun handlePartyLeave(player: Player): CompletableFuture<Void>
    {
        return loadPartyOfPlayerIfAbsent(player.uniqueId)
            .thenCompose {
                if (it == null)
                {
                    throw ConditionFailedException("The party you tried to leave no longer exists!")
                }

                it.members.remove(player.uniqueId)

                it.saveAndUpdateParty()
                    .thenAccept { _ ->
                        val message = FancyMessage().apply {
                            withMessage("$prefix${CC.RED}${player.name}${CC.SEC} left the party!")
                        }

                        PartyMessageStream.pushToStream(it, message)
                    }
            }
    }

    fun handlePartyJoin(player: Player, partyId: UUID): CompletableFuture<Void>
    {
        return loadPartyIfAbsent(partyId)
            .thenCompose {
                if (it == null)
                {
                    throw ConditionFailedException("The party you tried to join no longer exists!")
                }

                it.members[player.uniqueId] =
                    PartyMember(player.uniqueId, PartyRole.MEMBER)

                it.saveAndUpdateParty()
                    .thenAccept { _ ->
                        val message = FancyMessage().apply {
                            withMessage("$prefix${CC.GREEN}${player.name}${CC.SEC} joined the party!")
                        }

                        PartyMessageStream.pushToStream(it, message)
                    }
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

    fun loadPartyIfAbsent(uniqueId: UUID): CompletableFuture<Party?>
    {
        val loadedParty = loadedParties[uniqueId]

        if (loadedParty != null)
        {
            return CompletableFuture
                .completedFuture(loadedParty)
        }

        return service
            .load(uniqueId, DataStoreStorageType.REDIS)
            .thenApply {
                if (it != null)
                {
                    kotlin.run {
                        loadedParties[it.uniqueId] = it
                    }
                }

                return@thenApply it
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
