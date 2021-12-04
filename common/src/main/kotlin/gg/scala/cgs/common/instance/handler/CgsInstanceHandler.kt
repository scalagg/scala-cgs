package gg.scala.cgs.common.instance.handler

import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.instance.CgsServerInstance
import gg.scala.cgs.common.instance.CgsServerType
import gg.scala.cgs.common.instance.game.CgsGameServerInfo
import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.bukkit.Tasks
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
object CgsInstanceHandler
{
    var current by Delegates.notNull<CgsServerInstance>()
    var service by Delegates.notNull<RedisStorageLayer<CgsServerInstance>>()

    fun initialLoad(type: CgsServerType)
    {
        current = CgsServerInstance(
            Lemon.instance.settings.id, type
        )

        if (type == CgsServerType.GAME_SERVER)
        {
            current.gameServerInfo = CgsGameServerInfo(
                CgsGameEngine.INSTANCE.uniqueId,
                CgsGameEngine.INSTANCE.gameArena.getId(),
                CgsGameEngine.INSTANCE.gameMode.getId()
            )
        }

        service = RedisStorageLayer(
            Lemon.instance.redisConnection,
            "cgs:servers", CgsServerInstance::class.java
        )

        Tasks.asyncTimer(0L, 55L) {
            service.saveEntry(current.internalServerId, current)
        }
    }
}
