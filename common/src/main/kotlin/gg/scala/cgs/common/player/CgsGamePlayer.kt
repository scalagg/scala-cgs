package gg.scala.cgs.common.player

import com.google.gson.JsonElement
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.common.Savable
import gg.scala.lemon.util.CubedCacheUtil
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsGamePlayer(
    val uniqueId: UUID
) : Savable
{
    var lastPlayedGameId: UUID? = null
    var lastPlayedGameDisconnectionTimestamp: Long? = null

    val gameSpecificStatistics = mutableMapOf<String, GameSpecificStatistics>()

    val username: String
        get()
        {
            // This will always be retrieved from the local cache
            // as the player's latest uniqueId is updated on AsyncPreLoginEvent.
            return CubedCacheUtil.fetchName(uniqueId)!!
        }

    override fun save(): CompletableFuture<Void>
    {
        return CgsPlayerHandler.handle.saveEntry(uniqueId.toString(), this)
    }
}
