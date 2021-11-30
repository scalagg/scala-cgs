package gg.scala.cgs.common.handler

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.lemon.Lemon
import me.lucko.helper.Events
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsPlayerHandler
{
    lateinit var handle: MongoStorageLayer<CgsGamePlayer>
    private val players = ConcurrentHashMap<UUID, CgsGamePlayer>()

    fun find(uniqueId: UUID): CgsGamePlayer? = players[uniqueId]
    fun find(player: Player): CgsGamePlayer? = players[player.uniqueId]

    fun initialLoad()
    {
        handle = MongoStorageLayer(
            UriMongoConnection(Lemon.instance.mongoConfig.uri),
            "Scala", "cgs_global_players",
            CgsGamePlayer::class.java
        )

        Events.subscribe(
            AsyncPlayerPreLoginEvent::class.java,
            EventPriority.LOWEST
        ).handler { event ->
            handle.fetchEntryByKey(
                event.uniqueId.toString()
            ).thenAccept {
                players[event.uniqueId] = it ?: CgsGamePlayer(event.uniqueId)
            }
        }

        Events.subscribe(
            PlayerQuitEvent::class.java,
            EventPriority.LOWEST
        ).handler {
            players.remove(it.player.uniqueId)?.save()
        }
    }
}
