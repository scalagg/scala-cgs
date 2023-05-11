package gg.scala.cgs.lobby.rejoin

import gg.scala.cgs.common.instance.handler.CgsInstanceService
import gg.scala.cgs.common.player.GameSave
import gg.scala.cgs.common.states.CgsGameState
import gg.scala.cgs.lobby.CgsLobbyPlugin
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.md_5.bungee.api.chat.ClickEvent
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 5/2/2023
 */
@Service
object GameRejoinService
{
    @Inject
    lateinit var plugin: CgsLobbyPlugin

    val gameSaves = mutableMapOf<UUID, GameSave>()

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerQuitEvent::class.java)
            .handler {
                gameSaves.remove(it.player.uniqueId)
            }
            .bindWith(plugin)

        Events
            .subscribe(PlayerJoinEvent::class.java)
            .handler {
                CompletableFuture
                    .runAsync {
                        val key = "game-saves:${it.player.uniqueId}"
                        val gameSave = ScalaCommonsSpigot.instance
                            .kvConnection.sync().get(key)
                            ?.let { snapshot ->
                                Serializers.gson.fromJson(snapshot, GameSave::class.java)
                            }
                            ?: return@runAsync run {
                                println("1")
                            }

                        val server = CgsInstanceService
                            .servers[gameSave.lastPlayedGameId]
                            ?: return@runAsync run {
                                println("2")
                            }

                        val gameServer = server.gameServerInfo
                            ?: return@runAsync run {
                                println("3")
                            }

                        if (gameServer.state == CgsGameState.STARTED)
                        {
                            val expireTime = ScalaCommonsSpigot.instance
                                .kvConnection.sync()
                                .pexpiretime(key)
                                ?: -1L

                            gameSaves[it.player.uniqueId] = gameSave

                            Tasks.delayed(2L) {
                                FancyMessage()
                                    .withMessage(
                                        "${CC.GREEN}Looks like you were last in ${server.internalServerId}. Use ${CC.BOLD}/rejoin${CC.GREEN} to join back."
                                    )
                                    .andHoverOf(
                                        "${CC.GREEN}Rejoin your game!",
                                        "${CC.WHITE}Server: ${CC.GREEN}${server.internalServerId}"
                                    )
                                    .andCommandOf(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/rejoin"
                                    )
                                    .sendToPlayer(it.player)

                                if (expireTime != -1L)
                                {
                                    it.player.sendMessage(
                                        "${CC.GREEN}You have ${
                                            DurationFormatUtils.formatDurationWords(expireTime - System.currentTimeMillis(), true, true)
                                        } until your rejoin ticket expires."
                                    )
                                }
                            }
                        }
                    }
                    .exceptionally {
                        it.printStackTrace()
                        return@exceptionally null
                    }
            }
            .bindWith(plugin)
    }
}
