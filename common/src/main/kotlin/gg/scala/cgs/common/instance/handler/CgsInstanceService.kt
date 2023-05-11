package gg.scala.cgs.common.instance.handler

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.game.CgsGameServerInfo
import gg.scala.cgs.common.uniqueIdGlobal
import gg.scala.lemon.Lemon
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.RedisDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsInstanceService
{
    var current by Delegates.notNull<CgsServerInstance>()
    var servers = mapOf<UUID, CgsServerInstance>()

    fun configure(type: CgsServerType)
    {
        current = CgsServerInstance(
            Lemon.instance.settings.id,
            type,
            identifier = uniqueIdGlobal
        )

        if (type == CgsServerType.GAME_SERVER)
        {
            current.gameServerInfo = CgsGameServerInfo(
                CgsGameEngine.INSTANCE.uniqueId,
                CgsGameEngine.INSTANCE.gameArena?.getId() ?: "voting-in-progress",
                CgsGameEngine.INSTANCE.gameMode.getId(),
                CgsGameEngine.INSTANCE.gameInfo.fancyNameRender,
            )
        }

        Tasks.asyncTimer(0L, 20L) {
            if (current.gameServerInfo != null)
            {
                current.gameServerInfo!!.refresh()
            }

            current.online = Bukkit.getOnlinePlayers().size

            ScalaCommonsSpigot.instance.kvConnection
                .sync().psetex(
                    "minigames:servers:${CgsGameEngine.INSTANCE.uniqueId}",
                    TimeUnit.SECONDS.toMillis(5L),
                    Serializers.gson.toJson(current)
                )

            servers = ScalaCommonsSpigot
                .instance.kvConnection.sync()
                .keys("minigames:servers:*")
                .map {
                    ScalaCommonsSpigot
                        .instance.kvConnection
                        .sync()
                        .get(it)
                }
                .map {
                    Serializers.gson.fromJson(
                        it, CgsServerInstance::class.java
                    )
                }
                .associateBy(
                    CgsServerInstance::identifier
                )
        }
    }
}
