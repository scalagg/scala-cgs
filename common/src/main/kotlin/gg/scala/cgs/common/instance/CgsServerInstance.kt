package gg.scala.cgs.common.instance

import gg.scala.cgs.common.instance.game.CgsGameServerInfo
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsServerInstance(
    val internalServerId: String,
    val type: CgsServerType,
    var online: Int = 0,
    override val identifier: UUID
) : IDataStoreObject
{
    var gameServerInfo: CgsGameServerInfo? = null
}
