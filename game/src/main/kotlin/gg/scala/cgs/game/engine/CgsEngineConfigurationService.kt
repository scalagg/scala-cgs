package gg.scala.cgs.game.engine

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.handler.CgsInstanceService
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler.buildMessage
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 1/22/2022
 */
@Service
object CgsEngineConfigurationService
{
    @Configure
    fun configure()
    {
        CgsInstanceService.configure(CgsServerType.GAME_SERVER)

        CgsGameEngine.INSTANCE.initialResourceLoad()

        buildMessage(
            "add-server",
            "id" to Lemon.instance
                .settings.id,
            "address" to "127.0.0.1",
            "port" to Bukkit.getPort()
                .toString()
        ).publish()

        Tasks.asyncTimer({
            ServerSync.getLocalGameServer()
                .setMetadata(
                    "game", "game-state",
                    CgsGameEngine.INSTANCE.gameState.name
                )

            ServerSync.getLocalGameServer()
                .setMetadata(
                    "game", "remaining",
                    Bukkit.getOnlinePlayers()
                        .count { !it.hasMetadata("spectator") }
                )
        }, 0L, 20L)
    }

    @Close
    fun close()
    {
        buildMessage(
            "remove-server",
            "id" to Lemon.instance
                .settings.id
        ).publish()

        Lemon.instance.aware.publishConnection.apply {
            sync().hdel("cgs:servers", Lemon.instance.settings.id)
        }
    }
}
