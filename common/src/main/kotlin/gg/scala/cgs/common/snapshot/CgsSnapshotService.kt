package gg.scala.cgs.common.snapshot

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import gg.scala.cgs.common.snapshot.database.FinalCgsSnapshot
import gg.scala.lemon.Lemon
import net.evilblock.cubed.serializers.Serializers
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsSnapshotService
{
    var service by Delegates.notNull<MongoStorageLayer<FinalCgsSnapshot>>()

    fun initialLoad()
    {
        service = MongoStorageLayer(
            UriMongoConnection(Lemon.instance.mongoConfig.uri),
            "Scala", "cgs_game_snapshots",
            FinalCgsSnapshot::class.java
        )

        service.supplyWithCustomGson(Serializers.gson)
    }
}
