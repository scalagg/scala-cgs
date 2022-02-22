package gg.scala.cgs.common.snapshot

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.snapshot.wrapped.CgsWrappedGameSnapshot
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.serializers.Serializers

/**
 * @author GrowlyX
 * @since 2/20/2022
 */
@Service
object CgsGameSnapshotEngine
{
    private lateinit var controller:
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
}
