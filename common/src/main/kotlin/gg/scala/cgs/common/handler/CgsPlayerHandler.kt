package gg.scala.cgs.common.handler

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.CgsGameState
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.player.CgsGamePlayer
import gg.scala.lemon.Lemon
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsPlayerHandler
{
    @JvmStatic
    val RE_LOG_DELTA = TimeUnit.MINUTES.toMillis(2L)

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

        handle.supplyWithCustomGson(Serializers.gson)

        Events.subscribe(
            AsyncPlayerPreLoginEvent::class.java,
            EventPriority.LOWEST
        ).handler { event ->
            handle.fetchEntryByKey(
                event.uniqueId.toString()
            ).whenComplete { it, throwable ->
                throwable?.printStackTrace()
                players[event.uniqueId] = it ?: CgsGamePlayer(event.uniqueId)
            }
        }

        if (isGameServer())
        {
            Events.subscribe(PlayerJoinEvent::class.java).handler {
                val cgsGamePlayer = find(it.player)

                // Extremely important as in CGS, game profiles are almost always
                // marked as not-null and will throw an exception if it is null.
                if (cgsGamePlayer == null)
                {
                    it.player.kickPlayer("${CC.RED}Sorry, we were unable to load your CGS data.")
                    return@handler
                }

                var calledReconnectEvent = false

                // Checking if the last played game is this current game instance.
                // If this is true, we will call the reconnect event for bukkit to handle.
                if (cgsGamePlayer.lastPlayedGameId == CgsGameEngine.INSTANCE.uniqueId)
                {
                    // Only allowing players to go through the reconnection
                    // logic if the game is still in progress.
                    if (CgsGameEngine.INSTANCE.gameState != CgsGameState.STARTED)
                        return@handler

                    val logoutTimestamp = cgsGamePlayer
                        .lastPlayedGameDisconnectionTimestamp!!

                    // Checking if it has been less than two minutes since the logout
                    val withinTimeframe = System.currentTimeMillis() < logoutTimestamp + RE_LOG_DELTA

                    val cgsParticipantReconnect = CgsGameEngine
                        .CgsGameParticipantReconnectEvent(it.player, withinTimeframe)

                    cgsParticipantReconnect.callNow(); calledReconnectEvent = true
                }

                // Calling the participant connection event which
                // will handle everything which is not seen here.
                val cgsParticipantConnect = CgsGameEngine
                    .CgsGameParticipantConnectEvent(it.player, calledReconnectEvent)

                cgsParticipantConnect.callNow()
            }

            // Making sure this event handler is invoked BEFORE the
            // profile save handle which is seen below at a priority of LOWEST.
            Events.subscribe(
                PlayerQuitEvent::class.java,
                EventPriority.HIGH
            ).handler {
                val cgsParticipantDisconnect = CgsGameEngine
                    .CgsGameParticipantDisconnectEvent(it.player)

                cgsParticipantDisconnect.callNow()

                it.player.removeMetadata("spectator", CgsGameEngine.INSTANCE.plugin)
            }
        }

        Events.subscribe(
            PlayerQuitEvent::class.java,
            EventPriority.LOWEST
        ).handler {
            players.remove(it.player.uniqueId)?.save()
        }
    }

    private fun isGameServer() = CgsInstanceHandler.current.type == CgsServerType.GAME_SERVER
}
