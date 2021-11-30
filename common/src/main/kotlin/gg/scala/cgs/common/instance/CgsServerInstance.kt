package gg.scala.cgs.common.instance

import gg.scala.cgs.common.instance.game.CgsGameServerInfo

/**
 * @author GrowlyX
 * @since 11/30/2021
 */
class CgsServerInstance(
    val internalServerId: String,
    val type: CgsServerType
)
{
    var gameServerInfo: CgsGameServerInfo? = null
}
