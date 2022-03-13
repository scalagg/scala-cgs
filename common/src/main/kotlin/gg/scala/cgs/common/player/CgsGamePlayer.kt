package gg.scala.cgs.common.player

import gg.scala.cgs.common.player.handler.CgsPlayerHandler
import gg.scala.cgs.common.player.statistic.GameSpecificStatistics
import gg.scala.common.Savable
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsGamePlayer(
    val uniqueId: UUID
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    var lastPlayedGameId: UUID? = null
    var lastPlayedGameDisconnectionTimestamp: Long? = null

    val gameSpecificStatistics =
        mutableMapOf<String, GameSpecificStatistics>()

    override fun save(): CompletableFuture<Void>
    {
        return CgsPlayerHandler.handle
            .save(this, DataStoreStorageType.MONGO)
    }
}
