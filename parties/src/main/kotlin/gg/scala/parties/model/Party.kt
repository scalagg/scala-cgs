package gg.scala.parties.model

import gg.scala.aware.message.AwareMessage
import gg.scala.parties.event.PartyDisbandEvent
import gg.scala.parties.receiver.PartyReceiverHandler
import gg.scala.parties.service.PartyService
import gg.scala.parties.stream.PartyMessageStream
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
data class Party(
    val uniqueId: UUID = UUID.randomUUID(),
    var leader: PartyMember
) : IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    var password = ""
    var limit = 30
    var status = PartyStatus.PRIVATE

    val members = mutableMapOf<UUID, PartyMember>()

    private val settings = mutableMapOf<PartySetting, Boolean>()

    fun includedMembers(): List<UUID> =
        this.members.keys
            .toMutableList()
            .apply {
                add(leader.uniqueId)
            }

    fun includedMembersOnline(): List<UUID> =
        this.members.keys
            .filter {
                Bukkit.getPlayer(it) != null
            }
            .toMutableList()
            .apply {
                add(leader.uniqueId)
            }

    fun findMember(uniqueId: UUID): PartyMember?
    {
        if (leader.uniqueId == uniqueId)
            return leader

        return members[uniqueId]
    }

    fun isEnabled(setting: PartySetting): Boolean
    {
        return settings[setting] ?: false
    }

    fun update(
        setting: PartySetting, value: Boolean
    )
    {
        settings[setting] = value
        saveAndUpdateParty()
    }

    fun sendMessage(message: FancyMessage)
    {
        PartyMessageStream.pushToStream(this, message)
    }

    fun saveAndUpdateParty(): CompletableFuture<Void>
    {
        return PartyService.service.save(
            this, DataStoreStorageType.REDIS
        ).thenRun {
            AwareMessage.of(
                "party-update", PartyReceiverHandler.aware,
                "uniqueId" to uniqueId.toString()
            ).publish()
        }
    }

    fun gracefullyForget(): CompletableFuture<Void>
    {
        PartyMessageStream.pushToStream(
            this, FancyMessage()
                .withMessage("${CC.D_AQUA}[Party] ${CC.RED}Your party was disbanded!")
        )

        return this.forget()
            .thenRun {
                PartyDisbandEvent(this)
                    .call()
            }
    }

    private fun forget(): CompletableFuture<Void>
    {
        return PartyService.service.delete(
            this.uniqueId, DataStoreStorageType.REDIS
        ).thenRun {
            AwareMessage.of(
                "party-forget", PartyReceiverHandler.aware,
                "uniqueId" to uniqueId.toString()
            ).publish()
        }
    }
}
