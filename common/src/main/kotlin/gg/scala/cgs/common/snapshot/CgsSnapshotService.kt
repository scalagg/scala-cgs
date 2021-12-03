package gg.scala.cgs.common.snapshot

import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 12/2/2021
 */
object CgsSnapshotService
{
    var service by Delegates.notNull<MongoStorageLayer<CgsSnapshot>>()
}
