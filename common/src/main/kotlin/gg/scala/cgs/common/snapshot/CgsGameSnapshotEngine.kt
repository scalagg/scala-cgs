package gg.scala.cgs.common.snapshot

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.snapshot.wrapped.CgsWrappedGameSnapshot
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.RedisDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.serializers.Serializers
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 2/20/2022
 */
@Service
object CgsGameSnapshotEngine
{
    lateinit var controller:
            DataStoreObjectController<CgsWrappedGameSnapshot>

    @Configure
    fun configure()
    {
        controller = DataStoreObjectControllerCache.create()
        controller.provideCustomSerializer(Serializers.gson)
    }

    fun submitWrappedSnapshot()
    {
        controller.save(
            CgsWrappedGameSnapshot(CgsGameEngine.INSTANCE.getGameSnapshot()),
            DataStoreStorageType.REDIS
        ).join()
    }

    fun findRecentGamesOf(
        player: UUID,
        lambda: (List<CgsWrappedGameSnapshot>) -> Unit
    ): CompletableFuture<Void>
    {
        return controller.useLayerWithReturn<RedisDataStoreStorageLayer<CgsWrappedGameSnapshot>, CompletableFuture<Void>>(
            DataStoreStorageType.REDIS
        ) {
            return@useLayerWithReturn this.loadAllWithFilter {
                it.players.contains(player)
            }.thenAccept {
                lambda.invoke(it.values.toList())
            }
        }
    }
}
